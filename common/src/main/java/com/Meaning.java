package com;

public class Meaning {
    String partOfSpeech; // FIXME make this an enum of options, make it mandatory
    String meaning; // mandatory
    String sentence; // optional

    public Meaning (String partOfSpeech, String meaning, String sentence) {
        this.partOfSpeech = partOfSpeech;
        this.meaning = meaning;
        this.sentence = sentence;
    }

    public Meaning (String partOfSpeech, String meaning) {
        this(partOfSpeech, meaning, null);
    }
}
