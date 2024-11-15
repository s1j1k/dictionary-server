package s1j1k;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class DatabaseConnector {
  private String url;
  private String username;
  private String password;
  private String intialDictionaryFile;


  // FIXME turned to public to allow testing
  public Connection getConnection() throws SQLException {
    try {
      //Registering the Driver
//      DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
      return DriverManager.getConnection(this.url, this.username, this.password);
    } catch (SQLException e) {
      // TODO use a logger instead
      System.err.println("ERROR: Failed to establish database connection");

      throw e;
    }
  }


  public DatabaseConnector(String intialDictionaryFile) throws IOException, SQLException {
    // TODO check if database is populated already, if not initialize the database

    Properties props = new Properties();

    // Load the properties file from the resources folder
    try (InputStream in = getClass().getClassLoader().getResourceAsStream("s1j1k/db.properties")) {
      if (in == null) {
        throw new FileNotFoundException("db.properties file not found in classpath");
      }
      props.load(in);
    }

    // Assign properties to instance variables (using 'this' for clarity)
    this.url = props.getProperty("jdbc.url");
    this.username = props.getProperty("jdbc.username");
    this.password = props.getProperty("jdbc.password");

    // Assign the local variable to the instance variable (without 'this' since they don't conflict)
    this.intialDictionaryFile = intialDictionaryFile;
    // TODO prefill database if it's not already filled

  }

  public int executeUpdate(String sqlStatement) {
    // TODO update function if it needs pararms or not
    // TODO try with resources or use finally
    // TODO reduce concurrerncy ? or will SQL handle it automatically
    try {
      Connection con = getConnection();
      PreparedStatement ps = con.prepareStatement(sqlStatement);
      int rs = ps.executeUpdate();
      con.close();
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
      conn = getConnection();  // Get the connection
      stmt = conn.createStatement();  // Create a statement
      rs = stmt.executeQuery("SELECT Words.Word, Meanings.PartOfSpeech, Meanings.Meaning, Meanings.Sentence FROM Words INNER JOIN Meanings on Words.WordId = Meanings.WordId;");  // Execute the query

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
        if (rs != null) rs.close();  // Close ResultSet
      } catch (SQLException e) {
        System.out.println("Error while closing ResultSet: " + e.getMessage());
      }
      try {
        if (stmt != null) stmt.close();  // Close Statement
      } catch (SQLException e) {
        System.out.println("Error while closing Statement: " + e.getMessage());
      }
      try {
        if (conn != null) conn.close();  // Close Connection
      } catch (SQLException e) {
        System.out.println("Error while closing Connection: " + e.getMessage());
      }
    }

    return wordList.toString();
  }


}

