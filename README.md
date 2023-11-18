# Distributed Dictionary System
Server-client multi threaded dictionary system. 

- When the dictionary server is launched, the dictionary data is loaded from a file containing an initial list of words and their meanings. 
- The server uses a MySQL database to store words along with a meaning for efficient word search. 
- When words are added or removed, the database is updated to reflect the changes.
- 



## How to run the app locally
1. Set up MySQL server
    Start the local MySQL server in a terminal:
    ```sh
    sudo mysql -u root -p
    ```
   
    Ensure local update is enabled by running the below in MySQL. 
    This allows the initial dictionary data to be read from initial-dictionary.txt.
    ```sql
    SET GLOBAL local_infile=1;
    ```
    Quit MySQL and start a new session with the MySql client (e.g. your IDE) setting: `allowLoadLocalInfile=true`.

    The following only needs to be run once to set up the `dictionary` database:
    ```sql
    SOURCE dictionary.sql
    ```
   Then you can `quit` MySQL. By default the SQL server will run on localhost port 3306.


2. Start the dictionary server by entering the below command in a terminal:
    ```sh
    java –jar DictionaryServer.jar <port>
    ```

3. Start one or more client server/s by entering the below command in a terminal:
    ```sh
    java –jar DictionaryClient.jar <server-address> <server-port>
    ```

*Note that user-level processes/services generally use port number value >= 1024.*

