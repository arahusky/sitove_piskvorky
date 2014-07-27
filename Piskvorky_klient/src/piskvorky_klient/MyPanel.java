package piskvorky_klient;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author Jakub
 */

/** class representing gamefield */
class MyPanel extends JPanel {

    int dimension; //number of columns and rows, we allow just square gamefields
    int[][] gameField;
    boolean myTurn = true;
    PrintStream out;
    String enemyName;
    int sizeOneCell = 25; //the width and height of a cell
    JLabel whoseTurn;
    
    /** 
    * @param gameField the two-dimensional field, on which the game will be played
    * @param socket to communicate with server
    * @param enemyName the username of enemy
    * @param whoStarts true if it is me, who start, otherwise false
    * @param whoseTurn JLabel to show who is on move
    */
    public MyPanel(int[][] gameField, Socket socket, final String enemyName, boolean whoStarts, final JLabel whoseTurn) {
        this.gameField = gameField;
        this.dimension = gameField.length;
        try
        {
        out = new PrintStream(socket.getOutputStream());
        }
        catch (IOException e)
        {
            System.out.println("IO Error " + e.getMessage());
        }
        this.myTurn = whoStarts;
        this.enemyName = enemyName;
        this.whoseTurn = whoseTurn;
        
        setBorder(BorderFactory.createLineBorder(Color.black));
        addMouseListener(new MouseAdapter() {
            
            @Override
            public void mouseClicked(MouseEvent e) {
                if (myTurn && IsFieldEmpty(new Point(e.getX() / sizeOneCell, e.getY() / sizeOneCell))) {
                    myTurn = false;
                    Point point = CountCoordinatesAndSaveThem(e.getX(), e.getY());
                    whoseTurn.setText("Oponent's turn");
                    repaint();
                    if (DidIWin())
                    {
                        Piskvorky_game.endOfGame = true;
                        out.println("win " + enemyName + " " + point.x + " " + point.y);
                        JOptionPane.showMessageDialog(new JFrame(), "You win, congratulations");
                    }
                    else if (IsDraw())
                    {                        
                        Piskvorky_game.endOfGame = true;
                        out.println("draw " + enemyName + " " + point.x + " " + point.y);
                        JOptionPane.showMessageDialog(new JFrame(), "It's draw");
                    }
                    else
                    {
                        out.println("movement " + enemyName + " " + point.x + " " + point.y);                        
                    }
                }
            }
        });
    }

    /**
     * @param point the point on gamefield
     * @return whether there is nothing on the specified point
     */
    private boolean IsFieldEmpty(Point point)
    {
        if (point.y >= dimension || point.x >= dimension || point.x < 0 || point.y<0) return false; //if it is somewhere outside the gamefield
        return ((gameField[point.y][point.x]==0));        
    }
    
    /** just auxiliary method to return new Point
     *@param x x-coordinate of the point
     * @param y y-coordinate of the point
     * @return Point counted from given coordinates
     * */
    private Point CountCoordinatesAndSaveThem(int x, int y)
    {
        int myX = x / sizeOneCell;
        int myY = y / sizeOneCell;
        gameField[myY][myX] = 1;
        return new Point(myX, myY);
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(500, 500);
    }

    /** everytime, when anyone makes a move, we call this method, which simply repaints the gamefield*/
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (Piskvorky_game.endOfGame)
        {
            DrawFinalCombination(g);
        }
        else
        {
            g.setColor(Color.black);

            //vertical lines
            for (int i = 1; i < dimension; i++) {
                g.drawLine(0 + sizeOneCell * i, 0, 0 + sizeOneCell * i, 10 + sizeOneCell * dimension);
            }

            //horizontal lines
            for (int i = 1; i < dimension; i++) {
                g.drawLine(0, 0 + sizeOneCell * i, sizeOneCell * dimension, 0 + sizeOneCell * i);
            }

            Font font = new Font("serif", Font.BOLD, 14);
            g.setFont(font);

            for (int y = 0; y < dimension; y++) {
                for (int x = 0; x < dimension; x++) {
                    if (gameField[y][x] == 1) {
                        g.setColor(Color.red);
                        DrawSign(g, "X", x * sizeOneCell + 10, y * sizeOneCell + 20);
                    } else if (gameField[y][x] == 2) {
                        g.setColor(Color.blue);
                        DrawSign(g, "O", x * sizeOneCell + 10, y * sizeOneCell + 20);
                    }
                }
            }
        }
        
    }
    
    /** method to be drawn on the end */
    private void DrawFinalCombination(Graphics g)
    {
         g.setColor(Color.black);
         
         Point[] winnerField = findWinner();

            //vertical lines
            for (int i = 1; i < dimension; i++) {
                g.drawLine(0 + sizeOneCell * i, 0, 0 + sizeOneCell * i, 10 + sizeOneCell * dimension);
            }

            //horizontal lines
            for (int i = 1; i < dimension; i++) {
                g.drawLine(0, 0 + sizeOneCell * i, sizeOneCell * dimension, 0 + sizeOneCell * i);
            }

            Font font = new Font("serif", Font.BOLD, 14);
            g.setFont(font);

            for (int y = 0; y < dimension; y++) {
                for (int x = 0; x < dimension; x++) {
                    if (gameField[y][x] == 1) {
                        g.setColor(Color.red);
                        DrawSign(g, "X", x * sizeOneCell + 10, y * sizeOneCell + 20);
                    } else if (gameField[y][x] == 2) {
                        g.setColor(Color.blue);
                        DrawSign(g, "O", x * sizeOneCell + 10, y * sizeOneCell + 20);
                    }
                }
            }
            
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            g.drawLine(winnerField[0].x * sizeOneCell + 13, winnerField[0].y* sizeOneCell + 13, winnerField[4].x* sizeOneCell + 13, winnerField[4].y* sizeOneCell + 13);
    }
    
    /** method to find five in a row 
     *@retrun Point array, where there are coordinates of five winning signs
     */
    private Point[] findWinner()
    {
        Point[] pomField = new Point[5];
        
        for (int a = 1; a < 3; a++) {
            for (int x = 0; x < dimension; x++) {
                for (int y = 0; y < dimension; y++) {
                    if (gameField[y][x] == a) {
                        //to the right
                        if (x + 4 < dimension) {
                            if ((gameField[y][x + 1] == a) && (gameField[y][x + 2] == a) && (gameField[y][x + 3] == a) && (gameField[y][x + 4] == a)) {
                                pomField[0] = new Point(x, y);
                                pomField[1] = new Point(x + 1, y);
                                pomField[2] = new Point(x + 2, y);
                                pomField[3] = new Point(x + 3, y);
                                pomField[4] = new Point(x + 4, y);
                            }
                        }

                        //right down
                        if ((x + 4 < dimension) && (y + 4 < dimension)) {
                            if ((gameField[y + 1][x + 1] == a) && (gameField[y + 2][x + 2] == a) && (gameField[y + 3][x + 3] == a) && (gameField[y + 4][x + 4] == a)) {
                                pomField[0] = new Point(x, y);
                                pomField[1] = new Point(x + 1, y + 1);
                                pomField[2] = new Point(x + 2, y + 2);
                                pomField[3] = new Point(x + 3, y + 3);
                                pomField[4] = new Point(x + 4, y + 4);
                            }
                        }

                        //down
                        if (y + 4 < dimension) {
                            if ((gameField[y + 1][x] == a) && (gameField[y + 2][x] == a) && (gameField[y + 3][x] == a) && (gameField[y + 4][x] == a)) {
                                pomField[0] = new Point(x, y);
                                pomField[1] = new Point(x, y + 1);
                                pomField[2] = new Point(x, y + 2);
                                pomField[3] = new Point(x, y + 3);
                                pomField[4] = new Point(x, y + 4);
                            }
                        }

                        //left down
                        if ((y + 4 < dimension) && (x - 4 >= 0)) {
                            if ((gameField[y + 1][x - 1] == a) && (gameField[y + 2][x - 2] == a) && (gameField[y + 3][x - 3] == a) && (gameField[y + 4][x - 4] == a)) {
                                pomField[0] = new Point(x, y);
                                pomField[1] = new Point(x - 1, y + 1);
                                pomField[2] = new Point(x - 2, y + 2);
                                pomField[3] = new Point(x - 3, y + 3);
                                pomField[4] = new Point(x - 4, y + 4);
                            }
                        }
                    }
                }
            }
        }
        
        return pomField;
    }
    
    /** method to check, whether it is a draw (gamefield full without any winning combination)
     *@return true if draw
     */
    private boolean IsDraw()
    {
        for (int x = 0; x<20; x++)
        {
            for (int y = 0; y<20; y++)
            {
                if (gameField[y][x] == 0) return false;
            }
        }
        
        return true;
    }
    
    /** method to draw a sign
     * @param g Graphics which to use
     * @param sign the sign to draw ('X' or 'O')
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public void DrawSign(Graphics g, String sign, int x, int y)
    {       
       g.drawString(sign, x, y);
    }
    
    /** shows dialog saying to you, that you lost */
    public void LostInfo()
    {
        myTurn = false;
        Piskvorky_game.endOfGame = true;
        JOptionPane.showMessageDialog(new JFrame(), "You lost");
    }
    
     /** shows dialog saying to you, that it's a draw */
    public void DrawInfo()
    {
        myTurn = false;
        Piskvorky_game.endOfGame = true;
        JOptionPane.showMessageDialog(new JFrame(), "It's a draw");
    }
    
    /** shows dialog saying to you, that you win, because your enemy left*/
    public void EnemyLeaveInfo()
    {
        myTurn = false;
        Piskvorky_game.endOfGame = true;
        JOptionPane.showMessageDialog(new JFrame(), "Your opponent left the game. You win!");
    }
    
    /** method that shows an opponent movement and allows us to play (is public so that the listener can call it, whenever the opponent moves)*/
    public void OpponentMovement(int x, int y)
    {
        gameField[y][x] = 2; 
        repaint();
        myTurn = true;
        whoseTurn.setText("It is your turn");        
    }
    
    /**method, that returns true in case, that i already win (that I have five crosses on the gamefield)*/
    private boolean DidIWin()
    {
        for (int x = 0; x<dimension; x++)
        {
         for (int y = 0; y<dimension;y++)
         {
             if (gameField[y][x]==1)
             {
                 //to the right
                 if (x+4 < dimension)
                 {
                     if ((gameField[y][x+1] == 1) && (gameField[y][x+2] == 1) && (gameField[y][x+3] == 1) && (gameField[y][x+4] == 1))
                         return true;
                 }
                 
                 //right down
                 if ((x+4 < dimension) && (y+4 < dimension))
                 {
                     if ((gameField[y+1][x+1] == 1) && (gameField[y+2][x+2] == 1) && (gameField[y+3][x+3] == 1) && (gameField[y+4][x+4] == 1))
                         return true;
                 }
                 
                 //down
                 if (y+4 < dimension)
                 {
                     if ((gameField[y+1][x] == 1) && (gameField[y+2][x] == 1) && (gameField[y+3][x] == 1) && (gameField[y+4][x] == 1))
                         return true;
                 }
                 
                 //left down
                 if ((y+4 < dimension) && (x-4>=0))
                 {
                      if ((gameField[y+1][x-1] == 1) && (gameField[y+2][x-2] == 1) && (gameField[y+3][x-3] == 1) && (gameField[y+4][x-4] == 1))
                         return true;
                 }
             }
         }
        }
        
        return false;
    }
}


