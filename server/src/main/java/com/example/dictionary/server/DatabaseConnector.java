package com.example.dictionary.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.example.dictionary.common.Response;

public class DatabaseConnector {
  private String jdbcUrl = "jdbc:sqlite:dictionary.db";

  private static Logger logger = LogManager.getLogger(DatabaseConnector.class);

  // FIXME turned to public to allow testing
  public Connection getConnection() throws SQLException {
    return DriverManager.getConnection(jdbcUrl);
  }

  // Constructor
  // FIXME db initialisation should happen before threads are handled
  public DatabaseConnector(String intialDictionaryFile) throws IOException, SQLException {

    // Initialize connection to test SQLite working
    try (Connection conn = getConnection()) {
      logger.info("Connection to dictionary.db established!");
    }

    // Create tables if they don't exist
    createTables();

    // Optionally load initial data if Words table is empty
    if (isEmpty("Words")) {
      loadFromFile(intialDictionaryFile);
    }

    // Create dictionary.db with SQLite if not already existing

    // TODO check if database is populated already, if not initialize the database

  }

  private void createTables() {
    // TODO prefill database if it's not already filled
    String createWordsTable = "CREATE TABLE IF NOT EXISTS Words (" +
        "WordId INTEGER PRIMARY KEY AUTOINCREMENT," +
        "Word TEXT NOT NULL UNIQUE);";

    // Referenced foreign key from Words, will delete meanings when word is deleted
    String createMeaningsTable = "CREATE TABLE IF NOT EXISTS Meanings (" +
        "MeaningId INTEGER PRIMARY KEY AUTOINCREMENT," +
        "WordId INTEGER NOT NULL," +
        "PartOfSpeech TEXT CHECK(PartOfSpeech IN ('noun','pronoun','verb','adjective','adverb','preposition','conjunction','interjection')),"
        +
        "Meaning TEXT NOT NULL," +
        "Sentence TEXT," +
        "FOREIGN KEY (WordId) REFERENCES Words(WordId) ON DELETE CASCADE);";

    // Execute using Statement or PreparedStatement
    executeUpdate(createWordsTable);
    executeUpdate(createMeaningsTable);
  }

  private boolean isEmpty(String tableName) throws SQLException {
    String sql = "SELECT COUNT(*) FROM " + tableName;
    try (Connection conn = getConnection()) {
      try (Statement stmt = conn.createStatement();
          ResultSet rs = stmt.executeQuery(sql)) {
        return rs.getInt(1) == 0;
      }
    }

  }

  private void loadFromFile(String fileName) throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {

      // Skip the header line
      String header = reader.readLine();

      // this.conn.setAutoCommit(false); // Start transaction
      String line;
      int WordId = 1;
      // Insert words into dictionary one at a time
      while ((line = reader.readLine()) != null) {
        // Process one line
        String[] data = line.split(",");

        // FIXME verify the line contains required data
        String word;
        String partOfSpeech;
        String meaning;
        String sentence = ""; // Not every word has an example sentence

        if (data.length < 3) {
          throw new IOException("CSV data is incomplete, failed to process line: " + line);
        }

        word = data[0];
        partOfSpeech = data[1];
        meaning = data[2];

        if (data.length == 4) {
          sentence = data[3];
        }

        // Check if Word is already in Words DB

        // Insert into Words and get the generated WordId
        try (Connection conn = getConnection()) {
          // Check if the word already exists
          String selectWord = "SELECT WordId FROM Words WHERE Word = ?";
          try (PreparedStatement checkStmt = conn.prepareStatement(selectWord)) {
            checkStmt.setString(1, word);

            try (ResultSet rs = checkStmt.executeQuery()) {
              int wordId;
              if (rs.next()) {
                // Word already exists → reuse its WordId
                wordId = rs.getInt("WordId");
              } else {
                // Word does not exist → insert it
                String insertWord = "INSERT INTO Words (Word) VALUES (?)";
                try (
                    PreparedStatement insertStmt = conn.prepareStatement(insertWord, Statement.RETURN_GENERATED_KEYS)) {
                  insertStmt.setString(1, word);
                  insertStmt.executeUpdate();

                  try (ResultSet genKeys = insertStmt.getGeneratedKeys()) {
                    if (genKeys.next()) {
                      wordId = genKeys.getInt(1);
                    } else {
                      throw new SQLException("Inserting word failed, no ID returned.");
                    }
                  }
                }
              }

              // Insert meaning for this word
              String insertMeaning = "INSERT INTO Meanings (WordId, PartOfSpeech, Meaning, Sentence) VALUES (?, ?, ?, ?)";
              try (PreparedStatement insertStmt2 = conn.prepareStatement(insertMeaning)) {
                insertStmt2.setInt(1, wordId);
                insertStmt2.setString(2, partOfSpeech);
                insertStmt2.setString(3, meaning);
                insertStmt2.setString(4, sentence);
                insertStmt2.executeUpdate();
              }
            }
          } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }

        } catch (SQLException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }

        // FIXME don't initialize the server if this fails
      }

    }
  }

  // FIXME woeks for things without params to insert, simple statements
  public int executeUpdate(String sqlStatement) {
    try (Connection conn = getConnection()) {
      try (PreparedStatement ps = conn.prepareStatement(sqlStatement)) {
        int rs = ps.executeUpdate();
        return rs;
      }
    } catch (SQLException e) {
      logger.error("Updating database failed. Statement: " + sqlStatement);
      e.printStackTrace();
    }
    return -1;
  }

  /**
   * Search for the meaning of a word in the DB
   * 
   * @param wordToSearch
   * @return
   * @throws Exception in case of unexpected errors
   */
  public Response searchWord(String wordToSearch) throws Exception {
    // Search for a word in the database and return the Meaning(s)

    // Check if word exists
    try {
      int wordId = checkWordExists(wordToSearch);
      if (wordId == -1) {
        return new Response("fail", "Word not found in dictionary");
      }
    } catch (Throwable e) {
      logger.error("An error occured while checking word exists", e);
      throw new Exception("An error occured while checking word exists");
    }

    List<String> meaningList = new ArrayList<>();

    // Get the list of meanings from the database
    try (Connection conn = getConnection()) {

      String sql = "SELECT m.MeaningId, m.PartOfSpeech, m.Meaning, m.Sentence " +
          "FROM Words w JOIN Meanings m ON w.WordId = m.WordId " +
          "WHERE w.Word = ?";

      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, wordToSearch);
        try (ResultSet rs = stmt.executeQuery()) {
          while (rs.next()) {
            int meaningId = rs.getInt("MeaningId");
            String partOfSpeech = rs.getString("PartOfSpeech");
            String meaning = rs.getString("Meaning");
            String sentence = rs.getString("Sentence");

            int meaningNumber = meaningList.size() + 1;
            String meaningString;

            // PartOfSpeech is optional, include before meaning it if it's there
            if (partOfSpeech != null && partOfSpeech.length() > 1) {
              meaningString = String.format("%d. %s: %s", meaningNumber, partOfSpeech, meaning);
            } else {
              meaningString = String.format("%d. %s", meaningNumber, meaning);
            }

            // Sentence is optional, include it at the end if it's there
            if (sentence != null && sentence.length() > 2) {
              meaningString += " (" + sentence + ")";
            }
            meaningList.add(meaningString);
          }
        }

        // Return the content of the result or null if there's nothing
        // FIXME add a wrapper function to get Result class?
        String result = String.join(System.lineSeparator(), meaningList);
        Response response = new Response("success", result);
        return response;
      }
    } catch (SQLException e) {
      logger.error("A database error occured while searching for word meanings", e);
      return new Response("fail", "A database error occured while searching for word meanings");
    } catch (Throwable e) {
      logger.error("An unexpected error occured while searching for word meanings", e);
      throw new Exception("An unexpected error occured while searching for word meanings");
    }
  }

  /**
   * Check if word exists in the DB
   * 
   * @return word ID if it exists in DB, or -1 if it does not
   * @throws SQLException
   */
  public int checkWordExists(String wordToCheck) throws SQLException {
    try (Connection conn = getConnection()) {
      try (PreparedStatement findWordId = conn.prepareStatement("SELECT WordId FROM Words WHERE Word = ?")) {
        findWordId.setString(1, wordToCheck);

        try (ResultSet rs = findWordId.executeQuery()) {
          if (rs.next()) {
            // Word exists, get its ID
            return rs.getInt("WordId");
          } else {
            return -1;
          }
        }
      }
    }
  }

  /**
   * Add a word and its meanings to dictionary
   * 
   * @param wordToAdd
   * @param meanings
   * @return
   * @throws Exception
   */
  public Response addWord(String wordToAdd, String meanings) throws Exception {
    // Check word exists, return an error response if it does exist
    try {
      int wordId = checkWordExists(wordToAdd);
      if (wordId != -1) {
        return new Response("fail", "Error: Word already exists in dictionary.");
      }
    } catch (SQLException e) {
      logger.error("An error occured while connection to database.", e);
      throw new Exception("An error occured while connection to database.");
    }

    // Word doesn't exist already, add it to the dictionary
    int wordId;
    try (Connection conn = getConnection()) {

      // Add word to database and get the wordId
      try (PreparedStatement addWord = conn.prepareStatement("INSERT INTO Words (Word) VALUES (?)")) {
        addWord.setString(1, wordToAdd);

        addWord.executeUpdate();

        try (ResultSet genKeys = addWord.getGeneratedKeys()) {
          if (genKeys.next()) {
            wordId = genKeys.getInt(1);
          } else {
            throw new SQLException("Inserting word failed, no ID returned.");
          }
        }
      }

      // Separate meanings into an array
      // NOTE we assume meanings are separated by new line
      // TODO indicate somewhere on the GUI this assumption
      String[] meaningsArray = meanings.split(System.lineSeparator());

      // Insert meanings for this word
      // NOTE we are skipping PartOfSpeech, Sentence, can be added back as an extra
      // feature later
      for (String meaning : meaningsArray) {
        String insertMeaning = "INSERT INTO Meanings (WordId, Meaning) VALUES (?, ?)";
        try (PreparedStatement addMeaning = conn.prepareStatement(insertMeaning)) {
          addMeaning.setInt(1, wordId);
          addMeaning.setString(2, meaning);
          addMeaning.executeUpdate();
        }
      }

    }

    return new Response("success", "New word added successfully.");
  }

  /**
   * Get the list of words in the dictionary, used for debugging / logging
   * purposes
   * 
   * @return
   * @throws SQLException
   */
  public String getListOfWords() throws SQLException {
    ResultSet rs = null;
    Statement stmt = null;
    Connection conn = null;

    // Use StringBuilder for efficient string concatenation
    StringBuilder wordList = new StringBuilder();

    try {
      conn = getConnection(); // Get the connection
      stmt = conn.createStatement(); // Create a statement
      rs = stmt.executeQuery(
          "SELECT Words.Word, Meanings.PartOfSpeech, Meanings.Meaning, Meanings.Sentence FROM Words INNER JOIN Meanings on Words.WordId = Meanings.WordId;"); // Execute
                                                                                                                                                              // the
                                                                                                                                                              // query

      // Process the ResultSet
      // FIXME do this more efficiently and use a class for Words
      while (rs.next()) {
        String word = rs.getString("Word");
        String partOfSpeech = rs.getString("PartOfSpeech");
        String meaning = rs.getString("Meaning");
        String sentence = rs.getString("Sentence");

        // Append formatted data to the StringBuilder
        wordList.append(String.format("%s,%s,%s,%s\n", word, partOfSpeech, meaning, sentence));
      }
    } catch (SQLException e) {
      logger.error("An error occured while processing the ResultSet", e);
    } finally {
      // Close resources in the reverse order of opening them
      try {
        if (rs != null)
          rs.close(); // Close ResultSet
      } catch (SQLException e) {
        logger.error("An error occured while closing ResultSet", e);
      }
      try {
        if (stmt != null)
          stmt.close(); // Close Statement
      } catch (SQLException e) {
        logger.error("An error occured while closing Statement.", e);
      }
      try {
        if (conn != null)
          conn.close(); // Close Connection
      } catch (SQLException e) {
        logger.error("An error occured while closing Connection.", e);
      }
    }

    return wordList.toString();
  }

  /**
   * Delete a word and its meanings from the database
   * 
   * @param wordToRemove
   * @return
   * @throws Exception - in case of failure to connect to DB
   */
  public Response removeWord(String wordToRemove) throws Exception {

    // Check word exists, return an error response if it does not exist
    try {
      int wordId = checkWordExists(wordToRemove);
      if (wordId == -1) {
        return new Response("fail", "Error: Word does not exist in dictionary.");
      }
    } catch (SQLException e) {
      logger.error("An error occured while connecting to database.", e);
      throw new Exception("An error occured while connecting to database.");
    }

    // Delete word from database
    try (Connection conn = getConnection()) {
      try (PreparedStatement deleteWord = conn.prepareStatement("DELETE FROM Words WHERE Word = (?)")) {
        deleteWord.setString(1, wordToRemove);
        deleteWord.executeUpdate();
      } catch (Throwable e) {
        logger.error("An error occured while deleting word", e);
        throw new Exception("An error occurred while deleting word.");
      }
    } catch (Throwable e) {
      logger.error("An error occured while connecting to database.", e);
      throw new Exception("An error occured while connecting to database.");
    }

    return new Response("success", "Word successfully removed.");

  }

  public Response addMeaning(String wordToUpdate, String meaningToAdd) throws Exception {

    // Check word exists, return an error response if it does not exist
    try {
      int wordId = checkWordExists(wordToUpdate);
      if (wordId == -1) {
        return new Response("fail", "Error: Word does not exist in dictionary.");
      }
    } catch (SQLException e) {
      logger.error("An error occured while connecting to database.", e);
      throw new Exception("An error occured while connecting to database.");
    }

    // Add one more meaning to list
    // Check if this meaning is already included in database, return an error if it
    // is
    // TODO add prepared statements execution to a function which can throw the
    // required specific errors
    boolean exists = checkMeaningExists(wordToUpdate, meaningToAdd);
    if (exists) {
      return new Response("fail", "Error: Meaning exists already.");
    }

    try (Connection conn = getConnection()) {

      String sql2 = """
              INSERT INTO Meanings (WordId, Meaning)
              VALUES (
                  (SELECT WordId FROM Words WHERE Word = ?),
                  ?
              )
          """;

      try (PreparedStatement stmt = conn.prepareStatement(sql2)) {
        stmt.setString(1, wordToUpdate);
        stmt.setString(2, meaningToAdd);

        int rows = stmt.executeUpdate();
        if (rows > 0) {
          logger.info("Meaning added successfully.");
          return new Response("success", "Meaning added successfully.");
        } else {
          logger.info("Word not found, no meaning added.");
          return new Response("fail", "Word not found, no meaning added.");
        }
      } catch (Throwable e) {
        logger.error("An error occured while adding meaning to database.", e);
        throw new Exception("An error occured while adding meaning to database.");
      }

    } catch (Throwable e) {
      logger.error("An error occurred while connecting to database.", e);
      throw new Exception("An error occurred while connecting to database.");
    }

  }

  public Response updateMeaning(String wordToUpdate, String oldMeaning, String newMeaning) throws Exception {
    // Check word exists
    // FIXME can probably just combine with checking if meaning exists
    // Check word exists, return an error response if it does not exist
    try {
      int wordId = checkWordExists(wordToUpdate);
      if (wordId == -1) {
        return new Response("fail", "Error: Word does not exist in dictionary.");
      }
    } catch (SQLException e) {
      logger.error("An error occured while connecting to database.", e);
      throw new Exception("An error occured while connecting to database.");
    }

    // Check meaning exists
    boolean exists = checkMeaningExists(wordToUpdate, oldMeaning);
    if (exists == false) {
      return new Response("fail", "Meaning does not exist in database.");
    }

    // Meaning exists, update the meaning
    // FIXME should I be adding locks to the transactions or something ? 
    
    try (Connection conn = getConnection()) {
      String sql = """
              UPDATE Meanings
              SET Meaning = ?
              WHERE WordId = (SELECT WordId FROM Words WHERE Word = ?)
                AND Meaning = ?
          """;

      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, newMeaning);
        stmt.setString(2, wordToUpdate);
        stmt.setString(3, oldMeaning);

        int rows = stmt.executeUpdate();
        if (rows > 0) {
          return new Response("success", "Meaning updated successfully.");
        } else {
          return new Response("fail", "Error: No matching meaning found to update.");
        }
      }
    }

  }

  /**
   * Utility function to check if a meaning already exists in database
   * 
   * @param word
   * @param meaning
   * @return
   * @throws Exception
   */
  public boolean checkMeaningExists(String word, String meaning) throws Exception {
    try (Connection conn = getConnection()) {
      // FIXME use multi line strings in other places too
      // FIXME define constants (SQL queries) in another places
      String sql = """
              SELECT EXISTS (
                  SELECT 1
                  FROM Words w
                  JOIN Meanings m ON w.WordId = m.WordId
                  WHERE w.Word = ?
                    AND m.Meaning = ?
              )
          """;

      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, word);
        stmt.setString(2, meaning);

        try (ResultSet rs = stmt.executeQuery()) {
          if (rs.next()) {
            boolean exists = rs.getInt(1) == 1;
            if (exists) {
              return true;
            } else {
              return false;
            }
          } else {
            throw new Exception("No result when checking if meaning exists in database.");
          }
        }
      } catch (Throwable e) {
        logger.error("An error occurred while checking if meaning exists in database.", e);
        throw new Exception("An error occurred while checking if meaning exists in database.");
      }
    } catch (Throwable e) {
      logger.error("An error occurred while connecting to the database", e);
      throw new Exception("An error occurred while connecting to the database");
    }
  }
}
