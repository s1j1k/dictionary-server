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

  public ResultSet executeStatement(String sqlStatement) {
    String connectionUrl = "jdbc:mysql://localhost:3306/dictionary?serverTimezone=UTC";
    try {
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

  public void loadDataFromFile(String dataFile) {
    String sqlLoadDataFromFile = String.format(
      "LOAD DATA LOCAL INFILE '%s' INTO TABLE dictionary",
      dataFile
    );
    ResultSet rs = executeStatement(sqlLoadDataFromFile);
  }
}