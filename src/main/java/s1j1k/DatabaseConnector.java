package s1j1k;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseConnector {

  private static Logger LOGGER = LoggerFactory.getLogger(DatabaseConnector.class);

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
      // handle the exception
      LOGGER.error(String.valueOf(e));
    }
    return null;
  }

  public int executeUpdate(String sqlStatement) {
    String connectionUrl = "jdbc:mysql://localhost:3306/dictionary?";
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
      // handle the exception
      LOGGER.error(String.valueOf(e));
    }
    return -1;
  }
}