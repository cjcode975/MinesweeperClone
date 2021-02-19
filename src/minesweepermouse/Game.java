
package minesweepermouse;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Class to run a single game of minesweeper in a window
 * 
 * TO DO: Identify why the mouselistener doesn't like double clicks starting 
 * with a left click
 * 
 * @author cjcode975
 */
class Game {
    
    //Number of rows, columns and mines for the game
    private final int nrows, ncols, nmines; 
    //Store data on mine location, and number of mines adjacent to a point
    private final int[][] mines, adj_mines; 
    //Keep track of number of flags placed and number of cleaned slots
    private int nflagged = 0;
    private int num_cleared = 0;
    
    //Regions of no-mines are known as 'islands'. Keep a list of all spaces 
    //making up each island
    private ArrayList<Set<Integer>> islands;    
    //For keeping trackof which spots have been visited when identifying the islands
    private boolean visited [][];
    
    //GUI details
    private final JFrame window = new JFrame("Minesweeper");
    private final JPanel display = new JPanel();
    private final JTextField text1 = new JTextField("");  
    private final JTextField text2 = new JTextField("# Mines Remaining:");  
    private final Font game_font = new Font("Arial", Font.PLAIN, 12);
    //Buttons will form most of the game. Stored as an array here
    private final JButton buttons [][]; 
    private final int button_size = 25;
    
    /**
     * Create a new game of minesweeper, including a suitably sized window, 
     * minefield and new game buttons
     * 
     * @param N_Rows number of rows in minefield
     * @param N_Cols number of columns in minefield
     * @param N_Mines number of mines in minefield
     */
    public Game(int N_Rows, int N_Cols, int N_Mines, Point pos){
           
        //Initialise the minefield
        nrows = N_Rows;
        ncols = N_Cols;
        nmines = N_Mines;
        
        mines = new int[nrows][ncols];
        adj_mines = new int[nrows][ncols];  
                
        Fill_Mines();
        Adj_Mines();
        Islands();
         
        //Create GUI components
        //Window to hold game
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   
        window.setLocation(pos);
        window.setLayout(null);
        window.setResizable(false); 
        window.setSize(button_size*(ncols+2), button_size*(nrows+7)); 
        
        //JFrame
        display.setLayout(null);
        display.setBounds(0, 0, button_size*(ncols+2), button_size*(nrows+7));
        window.add(display); 
        
        //Field to keep track of mines left to flag
        text1.setText(Integer.toString(nmines));
        text1.setBounds(button_size*(ncols-2), button_size*(nrows+2), 3*button_size, button_size);     
        text1.setEditable(false);
        text1.setHorizontalAlignment(JTextField.CENTER);
        text1.setFont(game_font);
        display.add(text1);
        
        text2.setBounds(button_size, button_size*(nrows+2), 5*button_size, button_size);     
        text2.setEditable(false);
        text2.setHorizontalAlignment(JTextField.CENTER);
        text2.setFont(game_font);
        display.add(text2);
        
        //Buttons making up minefield
        buttons = new JButton[nrows][ncols];
        
        for(int i=0; i<nrows; i++){
            for(int j=0; j<ncols; j++){
                buttons[i][j] = new JButton();
                buttons[i][j].setMargin(new Insets(5,5,5,5));
                buttons[i][j].setFont(game_font);
                buttons[i][j].setBounds(button_size*(j+1), button_size*(i+1), button_size, button_size);
                //Mouse listener tracks whether to flag/clear a space based on right/left click
                buttons[i][j].addMouseListener(new ClearenceListener(i,j));
                display.add(buttons[i][j]);
            }
        }
        
        //Button to start a new game
        JButton end_game = new JButton("New Game");
        end_game.setMargin(new Insets(5,5,5,5));
        end_game.setFont(game_font);
        end_game.setBounds(button_size, button_size*(nrows+4), ncols*button_size, button_size);
        end_game.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae) {
                New_Game ng = new New_Game(window.getLocationOnScreen());
                window.dispose();
            }
            
        });
        display.add(end_game);
        
        //Start game
        window.setVisible(true);
    }
    
    /**
     * Generate the minefield for the game, randomly locating the mines
     */
    private void Fill_Mines(){
        int placed = 0;
        double to_check = nrows*ncols;
        
        Random rand = new Random(System.currentTimeMillis());
        
        /**Generate the mines by looping through spaces, each space is a mine 
         * with probability based on number of spaces and unplaced mines left         * 
         */
        for(int i=0; i<nrows; i++){
            for(int j=0; j<ncols; j++){
                
                if(rand.nextDouble() <= (nmines-placed)/to_check){
                    mines[i][j] = 1;
                    placed++;  
                    
                    if(placed==nmines){ //If all mines placed, can finish
                        return;                        
                    }
                }
                to_check--;
            }
        }            
    }
    
    /**
     * For each location, identify the number of adjacent mines
     */
    private void Adj_Mines(){
        for(int i=0; i<nrows; i++){
            for(int j=0; j<ncols; j++){
                if(mines[i][j]==1){
                    adj_mines[i][j] = -1;
                }
                else{
                    for(int i1=-1; i1<=1; i1++){
                        for(int j1=-1; j1<=1; j1++){
                            if(0<=i+i1 && i+i1<nrows && 0<=j+j1 && j+j1<ncols){ //Make sure not to leave array bounds
                                adj_mines[i][j] += mines[i+i1][j+j1];
                            }
                        }
                    }
                }
            }
        }
            
    }
    
    /**
     * Identify the 'islands' of no mines - the area which should be cleared in
     * one go when one the central locations is cleared.
     */
    private void Islands(){
        
        islands = new ArrayList<Set<Integer>>();
        visited = new boolean[nrows][ncols];        
      
        for(int i=0; i<nrows; i++){
            for(int j=0; j<ncols; j++){
                if(!visited[i][j] && adj_mines[i][j]==0){ 
                    //If is part of new island, look for island
                    Set<Integer> island = new HashSet<Integer>();
                    island.add(i*nrows+j);
                    islands.add(island);
                    visited[i][j] = true;
                    //Identify full island
                    Spread_Island(i,j);
                    
                }
                visited[i][j] = true;
            }
        }
                
    }
    
    /**
     * Given a position on an island, check if neighbouring positions are also 0
     * adjacent mine positions. If they are, spread the island out to include them.
     * @param i row position
     * @param j column position
     */
    private void Spread_Island(int i, int j){
        visited[i][j] = true;
        for(int m=-1; m<=1; m++){
            for(int n=-1; n<=1; n++){
                if(i+m>=0 && i+m<nrows && j+n>=0 && j+n<ncols){
                    //Add all neighbours to the island
                    islands.get(islands.size()-1).add(nrows*(i+m)+(j+n));
                    if(((!(m==0)) || (!(n==0))) && adj_mines[i+m][j+n] == 0 && !visited[i+m][j+n]){
                        /**
                         * If neighbour is within the island body, add recursively
                         *add its neighbours to the island, if this hasn't already 
                         *been done
                         */
                        Spread_Island(i+m,j+n);
                    }
                }
                
            }
        }
    }
    
    /**
     * Given a position on an island, identify which other spaces can be cleared
     * as part of the island
     * @param i row position
     * @param j column position
     * @return list position in islands containing the positions which can be cleared
     */
    private int Identify_Island(int i, int j){
        int pos = nrows*i+j;
        for(int k=0; k<islands.size(); k++){
            if(islands.get(k).contains(pos)){ return k; }
        }
        
        throw new IllegalArgumentException("Location not part of an island");
    }
    
    /**
     * Deal with the game ending by creating a dialog box to offer to start a 
     * new game 
     * @param win true if game won, false if game lost
     */
    private void Game_End(boolean win){
        
        //New window
        final JFrame eg_window = new JFrame("Minesweeper");
        eg_window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);       
        eg_window.setLocation(window.getLocationOnScreen());
        eg_window.setLayout(null);
        eg_window.setResizable(false); 
        eg_window.setSize(9*button_size, 8*button_size);
        
        JPanel eg_disp = new JPanel();
        eg_disp.setBounds(0,0,9*button_size, 6*button_size);
        eg_disp.setLayout(null);
        eg_window.add(eg_disp);
        
        //Ouptut win or lose state
        String eg_text_input = win ? "Won!" : "Lost.";
        JTextField eg_text = new JTextField("You "+eg_text_input);
        eg_text.setEditable(false);
        eg_text.setHorizontalAlignment(JTextField.CENTER);
        eg_text.setFont(new Font("Arial", Font.PLAIN, 12));
        eg_text.setBounds(button_size, button_size, 7*button_size, button_size);
        eg_disp.add(eg_text);
        
        //Start a new game with same difficulty
        JButton new_game = new JButton("New Game (Same Difficulty)");
        new_game.setFont(game_font);
        new_game.setMargin(new Insets(5,5,5,5));
        new_game.setBounds(button_size,3*button_size, 7*button_size, button_size);
        new_game.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae) {
                Game ng = new Game(nrows,ncols,nmines,eg_window.getLocationOnScreen());
                eg_window.dispose();
                window.dispose();                
            }
            
        });        
        eg_disp.add(new_game);
        
        //Start a game with different diffuculty
        JButton new_game2 = new JButton("New Game (New Difficulty)");
        new_game2.setFont(game_font);
        new_game2.setMargin(new Insets(5,5,5,5));
        new_game2.setBounds(button_size,5*button_size, 7*button_size, button_size);
        new_game2.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae) {
                New_Game ng = new New_Game(eg_window.getLocationOnScreen());
                eg_window.dispose();
                window.dispose();                
            }
            
        });        
        eg_disp.add(new_game2);
        
        eg_window.setVisible(true);
        window.setEnabled(false);
        
    }
    
    /**
     * Class to define the action of clearing/flagging a space based on how it
     * is clicked
     */
    private class ClearenceListener implements MouseListener{
        
        //Keep track of which mouse button is clicked
        private boolean leftclicked = false;
        private boolean rightclicked = false;
        
        //Keep track of which button the listener applies to
        private int row;
        private int col;
        
        /**
         * Initialise the listener with information as to which button in an 
         * array it belongs to
         * @param i row in array 
         * @param j column in array
         */
        public ClearenceListener(int i, int j){
            row = i;
            col = j;
        }

        /**
         * Action on mouse click ignored - events handled by mouse press + release
         * @param me mouse click event
         */
        @Override
        public void mouseClicked(MouseEvent me) { 
        }

        /**
         * If a mouse button is pressed, store which is pressed
         * @param me mouse event
         */
        @Override
        public void mousePressed(MouseEvent me) {
            if(SwingUtilities.isLeftMouseButton(me)){
                leftclicked = true;
            }            
            else if(SwingUtilities.isRightMouseButton(me)){
                rightclicked = true;
            }
            
        }

        /**
         * Take clearing/flagging action on the space according to type of mouse 
         * click. Left click clears, right flags, double click clears all 
         * adjacent non-flagged spaces if number of adjacent flags is equal to 
         * number of adjacent mines         * 
         * @param me mouse event
         */
        @Override
        public void mouseReleased(MouseEvent me) {
            /**
             * Double click - if number of adjacent flags = number adjacent mines
             * clear all adjacent non-flagged spaces
             */ 
            if(leftclicked && rightclicked){
                
                //Unclick mouse
                leftclicked = false;
                rightclicked = false;
                
                //Only works if space is cleared and needs non-zero number of adjacent mines                 
                if(!buttons[row][col].isEnabled() && !"".equals(buttons[row][col].getText())){
                    
                    //Check number of adjacent flags == number adjacent mines
                    int found_adj_mines = Integer.parseInt(buttons[row][col].getText());
                    for(int i=-1; i<=1; i++){
                        for(int j=-1; j<=1; j++){
                            if(row+i>=0 && row+i<nrows && col+j>=0 && col+j<ncols && "F".equals(buttons[row+i][col+j].getText())){
                                found_adj_mines--;
                            }
                        }
                    }
                    
                    if(found_adj_mines == 0){
                        
                        for(int i=-1; i<=1; i++){
                            for(int j=-1; j<=1; j++){
                                
                                //Clear any adjacent non-cleared, non-flagged spaces
                                if(row+i>=0 && row+i<nrows && col+j>=0 && col+j<ncols && buttons[row+i][col+j].isEnabled() && (!"F".equals(buttons[row+i][col+j].getText()))){
                                    
                                    //If mine is cleared, lose game
                                    if(mines[row+i][col+j]==1){
                                        buttons[row+i][col+j].setText("M");
                                        Game_End(false);
                                    }
                                    
                                    //Clear space
                                    else{
                                        
                                        //Clear a single space
                                        if(adj_mines[row+i][col+j]!=0){
                                            num_cleared++;
                                            buttons[row+i][col+j].setEnabled(false);
                                            buttons[row+i][col+j].setText(Integer.toString(adj_mines[row+i][col+j]));
                                        }
                                        //Clear an island
                                        else{
                                            int list_pos = Identify_Island(row+i,col+j);
                                            for(int pos : islands.get(list_pos)){
                                                int r = pos/nrows;
                                                int c = pos%nrows;
                            
                                                if(buttons[r][c].isEnabled()){
                                                    num_cleared++;
                                                    buttons[r][c].setEnabled(false);
                                                    buttons[r][c].setText(adj_mines[r][c]==0 ? "" : Integer.toString(adj_mines[r][c]));
                                                }                                                    
                                            }
                                        }  
                                        
                                        //Check if all spaces have been cleared
                                        //if so, win
                                        if(num_cleared==nrows*ncols-nmines){
                                            Game_End(true);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            //Left click == clear the space if not flagged
            else if(leftclicked){ 
                
                //Unclick mouse
                leftclicked = false;
                
                if(!buttons[row][col].isEnabled()){ return; }
                
                //Do nothing if space is flagged
                if("F".equals(buttons[row][col].getText())){                 
                    return;
                }
                
                //Atempt to clear a mine - lose game
                if(mines[row][col]==1){
                    buttons[row][col].setText("M");
                    Game_End(false);
                }
                
                //Succesffully clear location
                else{
                    
                    //set the text1 as number of adjacent mines
                    if(adj_mines[row][col]!=0){
                        num_cleared++;
                        buttons[row][col].setEnabled(false);
                        buttons[row][col].setText(Integer.toString(adj_mines[row][col]));
                    }
                    //If no adjacent mines, display the 'island' in the minefield
                    else{
                        int list_pos = Identify_Island(row,col);
                        for(int pos : islands.get(list_pos)){
                            int r = pos/nrows;
                            int c = pos%nrows;
                            
                            if(buttons[r][c].isEnabled()){
                                num_cleared++;
                                buttons[r][c].setEnabled(false);
                                buttons[r][c].setText(adj_mines[r][c]==0 ? "" : Integer.toString(adj_mines[r][c]));
                            }                                                      
                            
                        }
                    }                    
                    
                    //Check if all mines cleared - if so, win 
                    if(num_cleared==nrows*ncols-nmines){
                        Game_End(true);
                    }
                    
                }
            }
            
            //Flag or unflag a location
            else if(rightclicked){
                //Unclick mouse
                rightclicked = false;
                
                //Do nothing is space has been cleared
                if(!buttons[row][col].isEnabled()){ return; }
                
                //Flag if unflagged
                if(!"F".equals(buttons[row][col].getText())){
                    buttons[row][col].setText("F");
                    nflagged++;                    
                }
                //Unflag if flagged
                else{
                    buttons[row][col].setText("");
                    nflagged--;
                }  
                
                text1.setText(Integer.toString(nmines-nflagged));
            }
        }

        /**
         * Do nothing if mouse enters area over button
         * @param me 
         */
        @Override
        public void mouseEntered(MouseEvent me) {
            
        }

        /**
         * If mouse leaves, unclick mouse buttons
         * @param me 
         */
        @Override
        public void mouseExited(MouseEvent me) {
            leftclicked = false;
            rightclicked = false;
        }
        
    }
}
