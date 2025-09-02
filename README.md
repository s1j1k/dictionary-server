# Distributed Dictionary System
Server-client multi threaded dictionary system. 

- When the dictionary server is launched, the dictionary data is loaded from a file containing an initial list of words and their meanings. 
- The server uses a MySQL database to store words along with a meaning for efficient word search. 
- When words are added or removed, the database is updated to reflect the changes.
- 



## How to run the app locally
Requires: MySQL Server installed, Java SDK installed
1. Install MySQL server and initialize the dictionary database.
    Start the local MySQL server in a terminal:
    ```sh
    sudo mysql -u root -p
    ```
    Ensure local update is enabled by running the below in MySQL.
    ```sql
    SET GLOBAL local_infile=1;
    ```
    This allows the initial dictionary data to be read from initial-dictionary.txt.
    Quit MySQL `quit` and start a new session with the MySql client (e.g. your IDE) setting: `allowLoadLocalInfile=true`.
    The following only needs to be run once to set up the `dictionary` database:
    ```sql
    SOURCE dictionary.sql
    ```
   Then you can `quit` MySQL. By default the SQL server will run on localhost port 3306.


2. Start the dictionary server by entering the below command in a terminal on the server:
    ```sh
    java –jar DictionaryServer.jar <port> initial-dictionary.txt
    ```

3. Start one or more client server/s by entering the below command in a terminal on the client:
    ```sh
    java –jar DictionaryClient.jar <server-address> <server-port>
    ```

*Note that user-level processes/services generally use port number value >= 1024.*

