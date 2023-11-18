CREATE DATABASE dictionary;
USE dictionary;
CREATE TABLE dictionary(
    ID INT IDENTITY(1,1),
    WORD VARCHAR(20) NOT NULL,
    MEANING VARCHAR(100) NOT NULL,
    PRIMARY KEY(ID));
