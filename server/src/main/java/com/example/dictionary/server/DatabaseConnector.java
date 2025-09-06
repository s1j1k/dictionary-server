package com.example.dictionary.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

public class DatabaseConnector {
  private String jdbcUrl = "jdbc:sqlite:dictionary.db";

  // FIXME turned to public to allow testing
  public Connection getConnection() throws SQLException {
    return DriverManager.getConnection(jdbcUrl);
  }

  // Constructor
  // FIXME db initialisation should happen before threads are handled
  public DatabaseConnector(String intialDictionaryFile) throws IOException, SQLException {

    // Initialize connection to test SQLite working
    try (Connection conn = getConnection()) {
      System.out.println("Connection to dictionary.db established!");
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

    String createMeaningsTable = "CREATE TABLE IF NOT EXISTS Meanings (" +
        "MeaningId INTEGER PRIMARY KEY AUTOINCREMENT," +
        "WordId INTEGER," +
        "PartOfSpeech TEXT CHECK(PartOfSpeech IN ('noun','pronoun','verb','adjective','adverb','preposition','conjunction','interjection')),"
        +
        "Meaning TEXT NOT NULL," +
        "Sentence TEXT," +
        "FOREIGN KEY (WordId) REFERENCES Words(WordId));";

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

    // TODO test that table content matches
    // TODO function to add a single word

    // // FIXME test this, also ignore one line
    //
    //

    // try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

    //

    // // Set parameters for the PreparedStatement
    // pstmt.setString(1, data[0]);
    // // pstmt.setString(2, data[1]);
    // // pstmt.setInt(3, Integer.parseInt(data[2])); // Example: if column3 is an
    // integer

    // pstmt.addBatch(); // Add to batch for efficient insertion
    // }
    // pstmt.executeBatch(); // Execute all batched inserts
    // }
    // }
    // conn.commit(); // Commit the transaction

    // System.out.println("Data imported successfully!");
  }

  // FIXME woeks for things without params to insert, simple statements
  public int executeUpdate(String sqlStatement) {
    try (Connection conn = getConnection()) {
      try (PreparedStatement ps = conn.prepareStatement(sqlStatement)) {
        int rs = ps.executeUpdate();
        return rs;
      }
    } catch (SQLException e) {
      System.out.println("ERROR: updating database failed! Statement: " + sqlStatement);
      e.printStackTrace();
    }
    return -1;
  }

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
      System.out.println("Error while processing the ResultSet: " + e.getMessage());
    } finally {
      // Close resources in the reverse order of opening them
      try {
        if (rs != null)
          rs.close(); // Close ResultSet
      } catch (SQLException e) {
        System.out.println("Error while closing ResultSet: " + e.getMessage());
      }
      try {
        if (stmt != null)
          stmt.close(); // Close Statement
      } catch (SQLException e) {
        System.out.println("Error while closing Statement: " + e.getMessage());
      }
      try {
        if (conn != null)
          conn.close(); // Close Connection
      } catch (SQLException e) {
        System.out.println("Error while closing Connection: " + e.getMessage());
      }
    }

    return wordList.toString();
  }

  public String searchWord(String wordToSearch) {
    try (Connection conn = getConnection()) {
      // Search for a word and return the meaning/s

      // Get the WordId
      int wordId = -1;
      try (PreparedStatement findWordId = conn.prepareStatement(
          "SELECT WordId FROM Words WHERE Word = ?")) {

        findWordId.setString(1, wordToSearch);

        try (ResultSet rs = findWordId.executeQuery()) {
          if (rs.next()) {
            // Word exists → get its ID
            wordId = rs.getInt("WordId");
          }
        }
      }

      // Search for the meanings of the word
      String insertMeaning = "SELECT PartOfSpeech Meanings.Meaning Meanings.Sentence FROM FROM Words INNER JOIN Meanings on Words.WordId = Meanings.WordId;";
      try (PreparedStatement insertStmt2 = conn.prepareStatement(insertMeaning)) {
        insertStmt2.setInt(1, wordId);
        insertStmt2.executeUpdate();
      }

    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
