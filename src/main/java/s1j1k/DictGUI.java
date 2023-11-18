package s1j1k;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.text.html.StyleSheet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

class DictGUI extends JFrame {
    private int width;
    private int height;
    JButton buttonAdd = new JButton("Add New Word");
    JButton buttonSearch = new JButton("Search Word or Phrase");
    JButton buttonEdit = new JButton("Edit Word Meaning");
    StyleSheet styleSheet = new StyleSheet();

    public DictGUI(int width, int height) {
        super("Erudite");
        setLookAndFeel();
        this.width = width;
        this.height = height;
        initComponents();
        setSize(this.width, this.height);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true); // required?
    }

    private static void setLookAndFeel() {
        try {
            // Set System L&F
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception exc) {
            // ignore error
        }
    }

    protected void initComponents() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        // title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER,50,5));
        JTextPane title = new JTextPane();
        title.setEditable(false);
        title.setFocusable(false);
        title.setOpaque(false);

        // add a HTMLEditorKit to the editor pane
        HTMLEditorKit kit = new HTMLEditorKit();
        title.setEditorKit(kit);

        // add styles to the HTML
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("h1 {color: #2b2a33; font-family: Segoe UI, Arial, Helvetica, sans-serif;}");
        title.setText("<h1>Erudite Dictionary</h1>");
        titlePanel.add(title);
        add(titlePanel);

        // create search field
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,20,5));
        searchPanel.setBackground(java.awt.Color.white);
        searchPanel.setBorder(null);

        // add search icon
        // todo add search button action to the icon
        // todo add search action to the enter key
        try {
            BufferedImage searchImage = ImageIO.read(new File("./src/main/resources/search.png"));
            ImageIcon searchImageIcon = new ImageIcon(searchImage);
            searchImageIcon = new ImageIcon(searchImageIcon.getImage().getScaledInstance(25, 25,Image.SCALE_SMOOTH));
            JLabel searchIcon = new JLabel(searchImageIcon);
            searchPanel.add(searchIcon);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JTextField searchField = new JTextField("Search");
        searchField.setPreferredSize(new Dimension(this.width - 150, 50));
        searchPanel.add(searchField);
        searchField.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        searchField.setSize(200,50);
        searchField.setForeground(Color.decode("#c6c6c6"));
        // increase font size
        Font font1 = new Font("SansSerif", Font.PLAIN, 15);
        searchField.setFont(font1);
        add(searchPanel);

        JPanel panelButton = new JPanel();
        panelButton.setLayout(new FlowLayout(FlowLayout.CENTER));

//        buttonAdd.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                addWord();
//            }
//        });
//
//        buttonEdit.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                editMeaning();
//            }
//        });
//
//        buttonSearch.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent evt) {
//                searchMeaning();
//            }
//        });
//
//        panelButton.add(buttonAdd);
//        panelButton.add(buttonSearch);
//        panelButton.add(buttonEdit);
//
//        add(panelButton);


        // this is where the dictionary part will be displayed
        //listWords.setPreferredSize(new Dimension(400, 360));



    }


    private void editMeaning(String word, String meaning) {

    }

    private void addWord(String word, String meaning) {

    }

    private void searchMeaning() {

    }
//        String word = JOptionPane.showInputDialog(this, "Enter word or phrase to search for:");
//
//        if (word == null) {
//            return;
//        }

//        Collections.sort(words);
//
//        int foundIndex = Collections.binarySearch(words, new Word(word));
//
//        if (foundIndex >= 0) {
//            listWords.setSelectedIndex(foundIndex);
//        } else {
//            JOptionPane.showMessageDialog(this, "Could not find the word/phrase " + word);
//        }
//    }
    public static void main(String arguments[]) {
        DictGUI gui = new DictGUI(500,500);
    }
}