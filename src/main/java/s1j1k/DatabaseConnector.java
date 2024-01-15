package s1j1k;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnector {
  private String url;
  private String username;
  private String password;

  private Connection getConnection() throws SQLException {
    return DriverManager.getConnection(url, username, password);
  }

  public DatabaseConnector() throws IOException, SQLException {
    Properties props = new Properties();
    FileInputStream in = new FileInputStream("src/main/java/s1j1k/db.properties");
    props.load(in);
    in.close();
//    String driver = props.getProperty("jdbc.driver");
//    if (driver != null) {
//      Class.forName(driver) ;
//    }
    String url = props.getProperty("jdbc.url");
    String username = props.getProperty("jdbc.username");
    String password = props.getProperty("jdbc.password");
  }

  public ResultSet executeQuery(String sqlStatement) {
    try {
      Connection con = getConnection();
      PreparedStatement ps = con.prepareStatement(sqlStatement);
      ResultSet rs = ps.executeQuery();
      con.close();
      return rs;
    } catch (SQLException e) {
      System.out.println("ERROR: database query failed!");
    }
    return null;
  }

  public int executeUpdate(String sqlStatement) {
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
    ResultSet rs = executeQuery("SELECT Words.Word, Meanings.PartOfSpeech, Meanings.Meaning, Meanings.Sentence FROM Words INNER JOIN Meanings on Words.WordId = Meanings.WordId;");

    String wordList = "";

    while(rs.next()) {
      String word = rs.getString("Word");
      String partOfSpeech = rs.getString("PartOfSpeech");
      String meaning = rs.getString("Meaning");
      String sentence = rs.getString("Sentence");
      wordList += String.format("%s,%s,%s,%s\n", word, partOfSpeech, meaning, sentence);
    }

    return wordList;
  }

}

