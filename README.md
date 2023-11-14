# Distributed Dictionary System

- Uses MySQL database to store words and a simple definition (no examples)

## How to use the app
Start the MySQL server 
```sh
sudo mysql -u root -p < dictionary.sql/code>
```

Start the dictionary server by entering the below command in terminal:
```sh
java –jar DictionaryServer.jar <port> <dictionary-file>
```
Start one or more client server/s by entering the below command in terminal:
```sh
java –jar DictionaryClient.jar <server-address> <server-port>
```

Note that user-level processes/services generally use port number value >= 1024.

# Server-Client communication rules
## Client requests format
- `INIT` Request a list of initial words to display, should be synchronised
- `PUT <word> <meaning>` Add a word to the dictionary
- `EDIT <word> <new meaning>` Edit the meaning of a word

## Server requests 
- Nothing
