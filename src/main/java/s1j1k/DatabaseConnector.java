package s1j1k;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseConnector {
  public DatabaseConnector() {

  }

  public ResultSet executeQuery(String sqlStatement) {
    String connectionUrl = "jdbc:mysql://localhost:3306/dictionary";
    try {
      // todo hide user & password
      Connection conn = DriverManager.getConnection(
              connectionUrl,
        "root",
        "rnt123"
      );
      PreparedStatement ps = conn.prepareStatement(sqlStatement);
      ResultSet rs = ps.executeQuery();
      return rs;
    } catch (SQLException e) {
      // todo handle the exception
    }
    return null;
  }

  public int executeUpdate(String sqlStatement) {
    String connectionUrl = "jdbc:mysql://localhost:3306/dictionary";
    // todo hide the user/password
    try {
      Connection conn = DriverManager.getConnection(
              connectionUrl,
              "root",
              "rnt123"
      );
      PreparedStatement ps = conn.prepareStatement(sqlStatement);
      int rs = ps.executeUpdate();
      return rs;
    } catch (SQLException e) {
      // todo handle the exception
    }
    return -1;
  }

  public String getListOfWords() throws SQLException {
    ResultSet rs = executeQuery("SELECT Words.Word, Meanings.PartOfSpeech, Meanings.Meaning, Meanings.Sentence\n" +
            "FROM Words\n" +
            "INNER JOIN Meanings on Words.WordId = Meanings.WordId;");

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

