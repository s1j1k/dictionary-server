DROP SCHEMA dictionary;
CREATE DATABASE dictionary;
USE dictionary;

CREATE TABLE Words
(
    WordId INT NOT NULL AUTO_INCREMENT,
    Word   VARCHAR(20) NOT NULL UNIQUE,
    PRIMARY KEY (WordId)
);

# test importing into Words - this will be done in Java
LOAD DATA LOCAL INFILE 'initial-dictionary.txt'
INTO TABLE Words
IGNORE 1 LINES
    (Word);

CREATE TABLE Meanings
(
    WordId INT,
    TempWord   VARCHAR(20),
    MeaningId INT NOT NULL AUTO_INCREMENT,
    PartOfSpeech ENUM('noun','pronoun','verb','adjective','adverb','preposition','conjunction','interjection'),
    Meaning   VARCHAR(100) NOT NULL,
    Sentence   VARCHAR(100),
    PRIMARY KEY (MeaningId),
    FOREIGN KEY (WordId) REFERENCES Words (WordId)
);

# test importing into Meanings
LOAD DATA LOCAL INFILE 'initial-dictionary.txt'
    INTO TABLE Meanings
    IGNORE 1 LINES
    (TempWord, PartOfSpeech, Meaning, Sentence);

# Get the Word ID
UPDATE Meanings, Words
SET Meanings.WordId = Words.WordId
WHERE Meanings.TempWord = Words.Word;

ALTER TABLE Meanings
DROP COLUMN TempWord;