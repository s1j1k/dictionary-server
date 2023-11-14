package s1j1k;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.text.html.StyleSheet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTMLEditorKit;

class DictGUI extends JFrame {
    protected JButton buttonAdd = new JButton("Add New Word");
    protected JButton buttonSearch = new JButton("Search Word or Phrase");
    protected JButton buttonEdit = new JButton("Edit Word Meaning");
    StyleSheet styleSheet = new StyleSheet();

    public DictGUI() {
        super("Erudite");
        initComponents();
        setSize(700, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    protected void initComponents() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        // title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER,100,5));
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


        // this is where the dictionary part will be displayed
        //listWords.setPreferredSize(new Dimension(400, 360));

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

    public static void main(String args[]) {
        DictGUI gui = new DictGUI();
    }
}