package s1j1k;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class DatabaseConnector {

  private final String connectionUrl =
    "jdbc:mysql://localhost:3306/test?serverTimezone=UTC";

  public DatabaseConnector() {

  }

  public ResultSet executeStatement(String sqlStatement) {
    try (
      Connection conn = DriverManager.getConnection(
        connectionUrl,
        "root",
        "rnt123"
      );
      PreparedStatement ps = conn.prepareStatement(sqlStatement);
      ResultSet rs = ps.executeQuery();
    ) {
      return rs;
    } catch (SQLException e) {
      // handle the exception
    }
    return null;
  }

  public void loadDataFromFile(String dataFile) {
    String sqlLoadDataFromFile = String.format(
      "LOAD DATA LOCAL INFILE '%s' INTO TABLE dictionary",
      dataFile
    );
    executeStatement(sqlLoadDataFromFile);
  }
}
// handling the result set
// while (rs.next()) {
//             long id = rs.getLong("ID");
//             String name = rs.getString("FIRST_NAME");
//             String lastName = rs.getString("LAST_NAME");
