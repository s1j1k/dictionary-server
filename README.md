# Distributed Dictionary System
Server-client multi threaded dictionary system. 

- When the dictionary server is launched, the dictionary data is loaded from a file containing an initial list of words and their meanings. 
- The server uses a SQLite database to store words along with a meaning for efficient word search. 
- When words are added or removed, the database is updated to reflect the changes.
- 

## Run locally


1. Start the dictionary server by entering the below command in a terminal on the server:
    ```sh
    java –jar DictionaryServer.jar <port> initial-dictionary.txt delayMillis
    ```

2. Start one or more client server/s by entering the below command in a terminal on the client:
    ```sh
    java –jar DictionaryClient.jar <server-address> <server-port>
    ```

*Note that user-level processes/services generally use port number value >= 1024.*

