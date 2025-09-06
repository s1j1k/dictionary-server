package com.example.dictionary.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

public class DatabaseConnector {
  private String jdbcUrl = "jdbc:sqlite:dictionary.db";
  private Connection conn;

  // FIXME turned to public to allow testing
  public Connection getConnection() throws SQLException {
    return DriverManager.getConnection(jdbcUrl);
  }

  // TODO This needs to run when thread is over
  public void releaseConnection() throws SQLException {
    this.conn.close();
  }

  // Constructor
  // FIXME db initialisation should happen before threads are handled
  public DatabaseConnector(String intialDictionaryFile) throws IOException, SQLException {

    // Initialize connection
    // FIXME should this be an instance variable or a new connection each time?
    this.conn = DriverManager.getConnection(jdbcUrl);

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
    try (Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {
      return rs.getInt(1) == 0;
    }
  }

  private void loadFromFile(String fileName) throws IOException, SQLException {
    try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
      // this.conn.setAutoCommit(false); // Start transaction
      String line;
      int WordId = 1;
      // Insert words into dictionary one at a time
      while ((line = reader.readLine()) != null) {
        // Process one line
        String[] data = line.split("\t");

        // Prepare SQL statements using the content of the line
        String insertWord = String.format("INSERT INTO Words (WordId, Word) VALUES (%d, %s)", WordId, data[0]);
        String insertMeaning = String.format(
            "INSERT INTO Meanings (WordId, PartOfSpeech, Meaning, Sentence) VALUES (%d, %s, %s, %s)", WordId, data[1],
            data[2], data[3]);

        // FIXME use transaction for this
        // FIXME use batch for this
        executeUpdate(insertWord);
        executeUpdate(insertMeaning);

        WordId += 1;
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

  public int executeUpdate(String sqlStatement) {
    // TODO update function if it needs pararms or not
    // TODO try with resources or use finally
    // TODO reduce concurrerncy ? or will SQL handle it automatically
    try (Connection conn = getConnection()) {
      PreparedStatement ps = conn.prepareStatement(sqlStatement);
      int rs = ps.executeUpdate();
      return rs;
    } catch (SQLException e) {
      System.out.println("ERROR: updating database failed!");
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

}
