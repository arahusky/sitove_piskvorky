package piskvorky_klient;

/**
 * @author Jakub
 */

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.PrintStream;
import java.net.*;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/** class, that represents one game against oponnent */
public class Piskvorky_game extends Thread{
   private static int dimension = 20; 
   static Socket socket;
   public static MyPanel myPanel;
   static String usernameEnemy;
   static boolean whoStarts;
   static JLabel WhoseTurn;
   public static boolean endOfGame;
   
    /** game constructor
    * @param socket socket, via that we communicate
    * @param usernameEnemy the username of our oponnent
    * @param piskKlient the instance of Piskvorky_klient class
    * @param whoStarts true if it is me, who starts, otherwise false
    */
   public Piskvorky_game(Socket socket, String usernameEnemy, Piskvorky_klient piskKlient, boolean whoStarts)
   {
       Piskvorky_game.socket = socket;
       Piskvorky_game.endOfGame = false;
       Piskvorky_game.usernameEnemy = usernameEnemy;
       Piskvorky_game.whoStarts = whoStarts;
       createAndShowGUI();
   }

   /** creates frame design*/
  private static void createAndShowGUI() {
    final JFrame f = new JFrame("Piskvorky game");
    f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    f.setLayout(new BorderLayout());
    f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                try {
                    if (!endOfGame) {
                        int result = JOptionPane.showConfirmDialog(new JFrame(), "Do you really want to quit and lose a game?", "WARNING",
                                JOptionPane.YES_NO_OPTION); //we show him a dialog, whether he really wants to quit a game
                        if (result == JOptionPane.NO_OPTION || result == JOptionPane.CLOSED_OPTION) {
                            return; //if he realized, he does not want to quit the game, we return back to the game                          
                        }
                        else //else we inform the enemy, that he wins, because we left the game
                        {
                            PrintStream out = new PrintStream(socket.getOutputStream());
                            out.println("lost " + usernameEnemy);                            
                        }
                    }
                    
                    Piskvorky_klient.CreateGUIMainRoom();
                    
                } catch (IOException e) {
                }
                f.dispose();
            }
        });
    
    JLabel header = new JLabel("Piskvorky against " + usernameEnemy, JLabel.CENTER);      
    header.setFont(new Font("SansSerif", Font.BOLD, 18));
    header.setBorder(BorderFactory.createLineBorder(Color.black));
    f.add(header, BorderLayout.NORTH);
    
    WhoseTurn = new JLabel("Oponent's turn");
    if (whoStarts)
    {
    WhoseTurn = new JLabel("It is your turn");
    }
    //panel.add(WhoseTurn);
    f.add(WhoseTurn, BorderLayout.SOUTH);   
       
    
    myPanel = new MyPanel(new int[dimension][dimension], socket, usernameEnemy, whoStarts, WhoseTurn);
    f.add(myPanel, BorderLayout.CENTER);
    
    f.pack();
    f.setVisible(true);
    f.setResizable(false);
  }
}

