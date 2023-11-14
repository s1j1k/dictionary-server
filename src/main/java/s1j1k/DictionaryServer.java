package s1j1k;

import static java.util.concurrent.Executors.newFixedThreadPool;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.xml.crypto.Data;

class DictionaryMonitor {}

// multi threaded
class ClientHandler implements Runnable {

  private Socket clientSocket;
  private DictionaryServer dictionaryServer;

  public ClientHandler(Socket clientSocket, DictionaryServer dictionaryServer) {
    this.clientSocket = clientSocket;
    this.dictionaryServer = dictionaryServer;
  }

  public void run() throws RuntimeException {
    DataOutputStream dataOut = null;
    OutputStream streamOut = null;
    String request = null;
    String response = null;

    // Get a communication stream associated with the socket
    DataInputStream is = null;
    DataOutputStream os = null;
    try {
      is = new DataInputStream(clientSocket.getInputStream());
      os = new DataOutputStream(clientSocket.getOutputStream());

      request = new String(is.readUTF());

      System.out.println("Server received request: " + request);

      // handle the particular request
      if (request.equals("INIT")) {
        // set the required response
        System.out.println("Handling INIT Request");
        response = this.dictionaryServer.getDictionaryJSON();
        System.out.println("Server sending response: " + response);
        os.writeUTF(response);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      // When done, just close the connection and exit
      try {
        is.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      try {
        os.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    try {
      this.clientSocket.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}

public class DictionaryServer {

  DatabaseConnector databaseConnector = DatabaseConnector();

  // Maximum number of threads in the thread pool
  static final int MAX_TH = 10;

  public DictionaryServer(String intialDictionaryFile) {
    // load initial dictionary data from txt file using SQL statements
    
    String sqlLoadInitialDictionary = String.format(
      "LOAD DATA LOCAL INFILE '%s' INTO TABLE dictionary",
      intialDictionaryFile
    );
    

    
  }

  public static void main(String args[]) throws IOException {
    // read port number todo take from command line
    int port = Integer.parseInt(args[1]);

    // initialize dictionary
    String initialDictionaryFile = args[2];

    // initialize dictionary server
    DictionaryServer dictionaryServer = new DictionaryServer(
      initialDictionaryFile
    );

    // Register service on the given port
    ServerSocket serverSocket = new ServerSocket(port);

    // creating a thread pool with MAX_TH number of threads
    ExecutorService threadPool = newFixedThreadPool(MAX_TH);

    System.out.println("Waiting for client connections...");

    // accept multiple connections and use multiple threads
    while (true) {
      // create a thread and process any input clients requests?
      Socket clientSocket = serverSocket.accept(); // Wait and accept a connection
      System.out.println("Accepted a connection.");
      // create a thread to handle this client
      Runnable task = new ClientHandler(clientSocket, dictionaryServer);
      threadPool.execute(task);
    }
    // fixme somehow exited loop? this pl.shutdown belongs elsewhere?
    // this statement is not reachable ?!
    // pool is shutdown
    // threadPool.shutdown();

  }
}
