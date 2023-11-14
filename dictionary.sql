CREATE DATABASE dictionary;
USE dictionary;
CREATE TABLE dictionary(
    word VARCHAR(20) NOT NULL,
    meaning VARCHAR(100) NOT NULL,
    PRIMARY KEY(word));
