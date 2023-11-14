package org.example;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

// main GUI frame
class DictGUI extends JFrame {//inheriting JFrame
    protected JButton buttonAdd = new JButton("Add New Word");
    protected JButton buttonSearch = new JButton("Search Word or Phrase");
    protected JButton buttonEdit = new JButton("Edit Word Meaning");

    protected JList<Word> listWords = new JList<>();
    protected CustomListModel<Word> listModel;
    protected java.util.List<Word> words = new ArrayList<>();

    public DictGUI() {
        initComponents();
        setSize(600, 480);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // todo way to add a word
        // todo remove a word
        // todo update meaning of a word
    }

    public void setWords(ArrayList<Word> words) {
        this.words = words;
        // display the list of words
        listModel = new CustomListModel<Word>(words);
        listWords.setModel(listModel);
        //listModel.addElement(new Person("John Doe"));
        add(listWords);
    }
    // other methods

    protected void initComponents() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JPanel panelButton = new JPanel();
        panelButton.setLayout(new FlowLayout(FlowLayout.CENTER));

        buttonAdd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                addWord();
            }
        });

        buttonEdit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                editMeaning();
            }
        });

        buttonSearch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                searchMeaning();
            }
        });

        panelButton.add(buttonAdd);
        panelButton.add(buttonSearch);
        panelButton.add(buttonEdit);

        add(panelButton);

        //https://www.codejava.net/java-se/swing/java-swing-example-for-searching-and-sorting-a-collection-of-objects-using-jlist

        // this is where the dictionary part will be displayed
        listWords.setPreferredSize(new Dimension(400, 360));

        setVisible(true); // required?

    }


    private void editMeaning() {

    }

    private void addWord() {

    }

    private void searchMeaning() {
        String word = JOptionPane.showInputDialog(this, "Enter word or phrase to search for:");

        if (word == null) {
            return;
        }

        // todo this part
//        Collections.sort(words);
//
//        int foundIndex = Collections.binarySearch(words, new Word(word));
//
//        if (foundIndex >= 0) {
//            listWords.setSelectedIndex(foundIndex);
//        } else {
//            JOptionPane.showMessageDialog(this, "Could not find the word/phrase " + word);
//        }
    }
}