import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import s1j1k.DatabaseConnector;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseConnectorTest {
    private DatabaseConnector databaseConnector;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize the database connector
        // TODO move database name to a test config file
        databaseConnector = new DatabaseConnector("s1j1k/initial-dictionary.txt");
    }

    @Test
    void testDatabaseConnection() {
        // Test if a valid connection can be established
        try {
            assertNotNull(databaseConnector.getConnection(), "Database connection should be established.");
        } catch (SQLException e) {
            fail("Connection to the database should not fail: " + e.getMessage());
        }
    }

//    @Test
//    void testExecuteQuery() {
//        // Test that a query executes successfully
//        String sql = "SELECT * FROM Words";
//        try {
//            assertNotNull(databaseConnector.executeQuery(sql), "Query execution should return a result set.");
//        } catch (Exception e) {
//            fail("Query execution failed: " + e.getMessage());
//        }
//    }

    @Test
    void testGetListOfWords() {
        // Test the getListOfWords method for correct functionality
        try {
            String wordList = databaseConnector.getListOfWords();
            System.out.println("Word List: " + wordList);
            assertNotNull(wordList, "Word list should not be null.");
        } catch (SQLException e) {
            fail("Error retrieving list of words: " + e.getMessage());
        }
    }

    @Test
    void testExecuteUpdate() {
        // Test that an update operation works correctly
        // TODO use a test DB or something
        String insertSql = "INSERT INTO Words (Word) VALUES ('TestWord')";
        int rowsAffected = databaseConnector.executeUpdate(insertSql);
        assertEquals(1, rowsAffected, "Insert operation should affect one row.");
    }
}
