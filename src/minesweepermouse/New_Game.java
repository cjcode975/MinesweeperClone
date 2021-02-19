package minesweepermouse;

import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * GUI to create a new game of minesweeper
 * 
 * @author cjcode975
 */
public class New_Game {
    private int button_size = 25;
        
    private final JFrame window = new JFrame("Minesweeper");
    
    public New_Game(Point pos){
                
        //Setup GUI
        window.setSize(17*button_size, 12*button_size);
        window.setLocation(pos);
        window.setLayout(null);
        window.setResizable(false); 
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JPanel display = new JPanel();
        display.setBounds(0, 0, 17*button_size, 12*button_size);
        display.setLayout(null);
        window.add(display);
        
        //Information on each difficulty
        String text_label[][] = {{"Rows", "Columns", "Mines"}, {"10", "10", "10"}, {"16","16", "40"}, {"16","30","99"}, {"0", "0", "0"}};
        JTextField text[][] = new JTextField[5][3];
        
        //Display information
        for(int i=0; i<5; i++){
            for(int j=0; j<3; j++){
                text[i][j] = new JTextField(text_label[i][j]);
                text[i][j].setHorizontalAlignment(JTextField.CENTER);
                text[i][j].setFont(new Font("Arial", Font.PLAIN, 12));
                if(i<4){ text[i][j].setEditable(false); }
                text[i][j].setBounds(j*button_size*4+5*button_size, i*button_size*2+button_size, 3*button_size, button_size);
                display.add(text[i][j]);
            }
        }
        
        //For editable boxes to create custom game, add listeners to remove any 
        //inputted non-int values
        for(int i=0; i<3; i++){
            text[4][i].addActionListener(new Only_Ints(text[4][i]));
        }
        
        //Button to start new game on easy mode
        JButton Easy = new JButton("Easy");
        Easy.setMargin(new Insets(5,5,5,5));
        Easy.setFont(new Font("Arial", Font.PLAIN, 12));
        Easy.setBounds(button_size, 3*button_size, 3*button_size, button_size);
        Easy.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae) {
                Game new_game = new Game(10,10,10,window.getLocationOnScreen());
                window.dispose();
            }
            
        });
        display.add(Easy);
        
        //Button to start new game on medium mode
        JButton Med = new JButton("Medium");
        Med.setMargin(new Insets(5,5,5,5));
        Med.setFont(new Font("Arial", Font.PLAIN, 12));
        Med.setBounds(button_size, 5*button_size, 3*button_size, button_size);
        Med.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae) {
                Game new_game = new Game(16,16,40,window.getLocationOnScreen());
                window.dispose();
            }
            
        });
        display.add(Med);
        
        //Button to start new game on hard mode
        JButton Hard = new JButton("Hard");
        Hard.setMargin(new Insets(5,5,5,5));
        Hard.setFont(new Font("Arial", Font.PLAIN, 12));
        Hard.setBounds(button_size, 7*button_size, 3*button_size, button_size);
        Hard.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae) {
                Game new_game = new Game(16,30,99,window.getLocationOnScreen());
                window.dispose();
            }
            
        });
        display.add(Hard);
        
        //Button to start new game on custom mode
        JButton Cust = new JButton("Custom");
        Cust.setMargin(new Insets(5,5,5,5));
        Cust.setFont(new Font("Arial", Font.PLAIN, 12));
        Cust.setBounds(button_size, 9*button_size, 3*button_size, button_size);
        Cust.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae) {
                Game new_game = new Game(Integer.parseInt(text[4][0].getText()),Integer.parseInt(text[4][1].getText()),Integer.parseInt(text[4][2].getText()),window.getLocationOnScreen());
                window.dispose();
            }
            
        });
        display.add(Cust);
        
        window.setVisible(true);
        
    }
    
    /**
     * Listener to check if an integer is inputted, otherwise undo the input
     */
    private class Only_Ints implements ActionListener{
        private JTextField holder;
        private int val = 0;
        
        /**
         * Creator for listener to enforce only ints inputted to a TextField
         * @param Holder Store which TextField the listener is checking
         */
        public Only_Ints(JTextField Holder){
            holder = Holder;
        }

        /**
         * Undo any attempt to input a non int
         * @param ae text entry action
         */
        @Override
        public void actionPerformed(ActionEvent ae) { 
            try{val = Integer.parseInt(holder.getText());}
            catch(NumberFormatException e){holder.setText(Integer.toString(val));}
        }
        
    }
    
}
