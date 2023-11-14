# Distributed Dictionary System

- Uses MySQL database to store words and a single simple definition (no examples)

## How to run the app locally
1. Set up MySQL server
    Start the local MySQL server in a terminal:
    ```sh
    sudo mysql -u root -p
    ```
    The following only needs to be run once to set up the `dictionary` database:
    ```sh
    source dictionary.sql
    ```
    By default the SQL server will run on localhost port 3306.

2. Start the dictionary server by entering the below command in a terminal:
    ```sh
    java –jar DictionaryServer.jar <port> <dictionary-file>
    ```

3. Start one or more client server/s by entering the below command in a terminal:
    ```sh
    java –jar DictionaryClient.jar <server-address> <server-port>
    ```

*Note that user-level processes/services generally use port number value >= 1024.*

