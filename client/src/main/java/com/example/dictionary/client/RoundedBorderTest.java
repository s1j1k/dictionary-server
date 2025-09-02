/*
* Credit: https://forums.oracle.com/ords/apexds/post/jpanel-border-with-rounded-corners-0946
*/
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

public class RoundedBorderTest {
    RoundedBorderTest() {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //JPanel p = new JPanel();
        //p.setBorder(new RoundedBorder(50));
        //p.add(new JLabel("This is a JLabel"));
        //f.add(p);
        f.add(new JButton("NORTH"));//, BorderLayout.NORTH);
        f.add(new JButton("SOUTH"), BorderLayout.SOUTH);
        f.add(new JButton("EAST"), BorderLayout.EAST);
        f.add(new JButton("WEST"), BorderLayout.WEST);
        f.setSize(400,300);
        f.setVisible(true);
    }
    public static void main(String[] args) {
        new RoundedBorderTest();
    }

    private static class RoundedBorder implements Border {

        private int radius;

        RoundedBorder(int radius) {
            this.radius = radius;
        }
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(this.radius+1, this.radius+1, this.radius+2, this.radius);
        }

        @Override
        public boolean isBorderOpaque() {
            return true;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.drawRoundRect(x,y,width-1,height-1,radius,radius);
        }
    }
}