package com.example.dictionary.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.example.dictionary.common.Response;

/**
 * ~ Database Connector Class ~
 * 
 * Handles interaction with SQLite database.
 * 
 * @author Sally Arnold
 *         Student ID: 992316
 */
public class DatabaseConnector {

  private static final Logger logger = LogManager.getLogger(DatabaseConnector.class);

  // JDBC connection URL for SQLite database
  private final String jdbcUrl = "jdbc:sqlite:dictionary.db";

  // Lock to provide thread safety and prevent concurrent writes & writes/read
  // (concurrent reads allowed)
  private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

  // Delay in ms to allow verifying thread safety
  private long delayMillis = 0;

  /**
   * Constructor
   * Initializes database connection, creates tables if not present,
   * and loads initial data if DB is empty.
   *
   * @param initialDictionaryFile CSV file for initial data
   * @throws IOException  if reading file fails
   * @throws SQLException if DB setup fails
   */
  public DatabaseConnector(String initialDictionaryFile) throws IOException, SQLException {
    // Test connection
    try (Connection conn = getConnection()) {
      logger.info("Connection to dictionary.db established!");
    }

    // Create required tables
    createTables();

    // Optionally load initial data
    if (isEmpty("Words")) {
      loadFromFile(initialDictionaryFile);
    }
  }

  // Constructor to accept delay
  public DatabaseConnector(String initialDictionaryFile, long delayMillis) throws IOException, SQLException {
    this(initialDictionaryFile);
    this.delayMillis = delayMillis;
  }

  private void simulateDelay() {
    if (delayMillis > 0) {
      try {
        Thread.sleep(delayMillis);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  public Connection getConnection() throws SQLException {
    return DriverManager.getConnection(jdbcUrl);
  }

  private void createTables() {
    String createWordsTable = "CREATE TABLE IF NOT EXISTS Words (" +
        "WordId INTEGER PRIMARY KEY AUTOINCREMENT," +
        "Word TEXT NOT NULL UNIQUE);";

    String createMeaningsTable = "CREATE TABLE IF NOT EXISTS Meanings (" +
        "MeaningId INTEGER PRIMARY KEY AUTOINCREMENT," +
        "WordId INTEGER NOT NULL," +
        "PartOfSpeech TEXT CHECK(PartOfSpeech IN ('noun','pronoun','verb','adjective','adverb','preposition','conjunction','interjection')),"
        +
        "Meaning TEXT NOT NULL," +
        "Sentence TEXT," +
        "FOREIGN KEY (WordId) REFERENCES Words(WordId) ON DELETE CASCADE);";

    executeUpdate(createWordsTable);
    executeUpdate(createMeaningsTable);
  }

  private boolean isEmpty(String tableName) throws SQLException {
    String sql = "SELECT COUNT(*) FROM " + tableName;
    try (Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {
      return rs.getInt(1) == 0;
    }
  }

  /**
   * Check if word exists in the DB
   * 
   * @param wordToCheck the word to look up
   * @return word ID if exists, -1 if not
   */
  public int checkWordExists(String wordToCheck) throws SQLException {
    rwLock.readLock().lock();
    try {
      simulateDelay();
      try (Connection conn = getConnection();
          PreparedStatement findWordId = conn.prepareStatement("SELECT WordId FROM Words WHERE Word = ?")) {
        findWordId.setString(1, wordToCheck);
        try (ResultSet rs = findWordId.executeQuery()) {
          if (rs.next()) {
            return rs.getInt("WordId");
          } else {
            return -1;
          }
        }
      }
    } finally {
      rwLock.readLock().unlock();
    }

  }

  /**
   
   */
  /**
   * Load data from a CSV file into the database.
   * CSV format: Word,PartOfSpeech,Meaning[,Sentence]
   * 
   * @param fileName
   * @throws IOException
   */
  private void loadFromFile(String fileName) throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
      reader.readLine(); // skip header
      String line;

      while ((line = reader.readLine()) != null) {
        String[] data = line.split(",");

        if (data.length < 3) {
          throw new IOException("CSV data is incomplete: " + line);
        }

        String word = data[0];
        String partOfSpeech = data[1];
        String meaning = data[2];
        String sentence = (data.length == 4) ? data[3] : "";

        try {
          // NOTE: This can be extended to include partOfSpeech and sentence
          addWord(word, meaning);
        } catch (Exception e) {
          logger.error("Error adding word from initial file.", e);
          throw new IOException(e);
        }
      }
    }
  }

  // Execute a simple update SQL command (no params)
  public int executeUpdate(String sqlStatement) {
    try (Connection conn = getConnection();
        PreparedStatement ps = conn.prepareStatement(sqlStatement)) {
      return ps.executeUpdate();
    } catch (SQLException e) {
      logger.error("Updating database failed. Statement: " + sqlStatement, e);
    }
    return -1;
  }

  // Public CRUD API

  /**
   * Search for meanings of a word
   * 
   * @param wordToSearch
   * @return
   * @throws Exception
   */
  public Response searchWord(String wordToSearch) throws Exception {

    rwLock.readLock().lock();
    try {
      simulateDelay();
      // Word must exist
      try {
        if (checkWordExists(wordToSearch) == -1) {
          return new Response("fail", "Word not found in dictionary");
        }
      } catch (Throwable e) {
        logger.error("Error checking word existence", e);
        throw new Exception("An error occured while checking word exists");
      }

      List<String> meaningList = new ArrayList<>();

      String sql = """
              SELECT m.MeaningId, m.PartOfSpeech, m.Meaning, m.Sentence
              FROM Words w
              JOIN Meanings m ON w.WordId = m.WordId
              WHERE w.Word = ?
          """;

      try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, wordToSearch);

        try (ResultSet rs = stmt.executeQuery()) {
          while (rs.next()) {
            String partOfSpeech = rs.getString("PartOfSpeech");
            String meaning = rs.getString("Meaning");
            String sentence = rs.getString("Sentence");

            int meaningNumber = meaningList.size() + 1;
            String meaningString = (partOfSpeech != null && partOfSpeech.length() > 1)
                ? String.format("%d. %s: %s", meaningNumber, partOfSpeech, meaning)
                : String.format("%d. %s", meaningNumber, meaning);

            if (sentence != null && sentence.length() > 2) {
              meaningString += " (" + sentence + ")";
            }
            meaningList.add(meaningString);
          }
        }

        String result = String.join(System.lineSeparator(), meaningList);
        return new Response("success", result);

      } catch (SQLException e) {
        logger.error("Database error during search", e);
        return new Response("fail", "A database error occured while searching for word meanings");
      }
    } finally {
      rwLock.readLock().unlock();
    }

  }

  /**
   * Add a new word with meanings
   * 
   * @param wordToAdd
   * @param meanings
   * @return
   * @throws Exception
   */
  public Response addWord(String wordToAdd, String meanings) throws Exception {
    rwLock.writeLock().lock();
    try {
      simulateDelay();
      // Must not already exist
      if (checkWordExists(wordToAdd) != -1) {
        return new Response("fail", "Error: Word already exists in dictionary.");
      }

      try (Connection conn = getConnection()) {
        int wordId;

        try (PreparedStatement addWord = conn.prepareStatement("INSERT INTO Words (Word) VALUES (?)",
            Statement.RETURN_GENERATED_KEYS)) {
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

        String[] meaningsArray = meanings.split(System.lineSeparator());

        for (String meaning : meaningsArray) {
          try (PreparedStatement addMeaning = conn.prepareStatement(
              "INSERT INTO Meanings (WordId, Meaning) VALUES (?, ?)")) {
            addMeaning.setInt(1, wordId);
            addMeaning.setString(2, meaning);
            addMeaning.executeUpdate();
          }
        }
      }

      return new Response("success", "New word added successfully.");
    } finally {
      rwLock.writeLock().unlock();
    }

  }

  /**
   * Remove a word and its meanings
   * 
   * @param wordToRemove
   * @return
   * @throws Exception
   */
  public Response removeWord(String wordToRemove) throws Exception {
    rwLock.writeLock().lock();
    try {
      simulateDelay();
      if (checkWordExists(wordToRemove) == -1) {
        return new Response("fail", "Error: Word does not exist in dictionary.");
      }

      try (Connection conn = getConnection();
          PreparedStatement deleteWord = conn.prepareStatement("DELETE FROM Words WHERE Word = (?)")) {
        deleteWord.setString(1, wordToRemove);
        deleteWord.executeUpdate();
      }

      return new Response("success", "Word successfully removed.");

    } finally {
      rwLock.writeLock().unlock();
    }
  }

  /**
   * Add a meaning to an existing word
   * 
   * @param wordToUpdate
   * @param meaningToAdd
   * @return
   * @throws Exception
   */
  public synchronized Response addMeaning(String wordToUpdate, String meaningToAdd) throws Exception {
    rwLock.writeLock().lock();
    try {
      simulateDelay();

      if (checkWordExists(wordToUpdate) == -1) {
        return new Response("fail", "Error: Word does not exist in dictionary.");
      }

      if (checkMeaningExists(wordToUpdate, meaningToAdd)) {
        return new Response("fail", "Error: Meaning exists already.");
      }

      String sql = """
              INSERT INTO Meanings (WordId, Meaning)
              VALUES (
                  (SELECT WordId FROM Words WHERE Word = ?),
                  ?
              )
          """;

      try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, wordToUpdate);
        stmt.setString(2, meaningToAdd);

        int rows = stmt.executeUpdate();
        if (rows > 0) {
          return new Response("success", "Meaning added successfully.");
        } else {
          return new Response("fail", "Word not found, no meaning added.");
        }
      }
    } finally {
      rwLock.writeLock().unlock();
    }

  }

  /**
   * Update an existing meaning
   * 
   * @param wordToUpdate
   * @param oldMeaning
   * @param newMeaning
   * @return
   * @throws Exception
   */
  public Response updateMeaning(String wordToUpdate, String oldMeaning, String newMeaning)
      throws Exception {

    rwLock.writeLock().lock();
    try {
      simulateDelay();

      if (checkWordExists(wordToUpdate) == -1) {
        return new Response("fail", "Error: Word does not exist in dictionary.");
      }

      if (!checkMeaningExists(wordToUpdate, oldMeaning)) {
        return new Response("fail", "Meaning does not exist in database.");
      }

      String sql = """
              UPDATE Meanings
              SET Meaning = ?
              WHERE WordId = (SELECT WordId FROM Words WHERE Word = ?)
                AND Meaning = ?
          """;

      try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(sql)) {
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
    } finally {
      rwLock.writeLock().unlock();
    }

  }

  /**
   * Check if a meaning exists for a word
   * 
   * @param word
   * @param meaning
   * @return
   * @throws Exception
   */
  public boolean checkMeaningExists(String word, String meaning) throws Exception {
    rwLock.readLock().lock();
    try {
      simulateDelay();
      String sql = """
              SELECT EXISTS (
                  SELECT 1
                  FROM Words w
                  JOIN Meanings m ON w.WordId = m.WordId
                  WHERE w.Word = ?
                    AND m.Meaning = ?
              )
          """;

      try (Connection conn = getConnection();
          PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, word);
        stmt.setString(2, meaning);

        try (ResultSet rs = stmt.executeQuery()) {
          return rs.next() && rs.getInt(1) == 1;
        }
      }
    } finally {
      rwLock.readLock().unlock();
    }

  }

  /**
   * Get a list of all words + meanings (debug/logging only)
   * 
   * @return
   * @throws SQLException
   */
  public String getListOfWords() throws SQLException {
    StringBuilder wordList = new StringBuilder();

    try (Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT Words.Word, Meanings.PartOfSpeech, Meanings.Meaning, Meanings.Sentence " +
                "FROM Words INNER JOIN Meanings ON Words.WordId = Meanings.WordId;")) {

      while (rs.next()) {
        wordList.append(String.format(
            "%s,%s,%s,%s\n",
            rs.getString("Word"),
            rs.getString("PartOfSpeech"),
            rs.getString("Meaning"),
            rs.getString("Sentence")));
      }
    }

    return wordList.toString();
  }
}
