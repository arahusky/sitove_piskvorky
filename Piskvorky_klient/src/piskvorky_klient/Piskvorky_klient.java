/*
  The main package for client. If you want to connect somewhere else than to a localhost, set a variable inetAdressToJoin to the i.p. adress, where the server runs.
 */
package piskvorky_klient;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import javax.swing.*;

/**
 *
 * @author Jakub
 */

/** basic class, that maintains (shows and handles) login and later also main frame, where you can challange your oppoenents or show the statistics*/ 
public class Piskvorky_klient {

    public static void main(String[] args) {
        // TODO code application logic here
        piskKlient = new Piskvorky_klient();
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
               CreateGUILogin();
            }
        });
    }
     
    /** the IP address of the server (needs to be set before running (implicitly null = localhost)*/
    static String inetAdressToJoin = null; 
    static Socket socket; 
    static PrintStream writer;
    static BufferedReader reader;
    static JFrame loginFrame = null;    
    static boolean reportedError = false;
    static Piskvorky_klient piskKlient;
    static Statistics statistic;
    
    /** Creates login frame*/
    public static void CreateGUILogin()
    {        
        loginFrame = new JFrame("Klient - login");        
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setVisible(true);
        Container pane = loginFrame.getContentPane();
        
        pane.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();                
        
        c.insets = new Insets(10,10,0,10);  // padding
        c.weightx = 1.0;
        JLabel label = new JLabel("Username:");
        pane.add(label,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        final JTextField usnameField = new JTextField();
        usnameField.setColumns(13);
        pane.add(usnameField,c);
                
        label = new JLabel("Password:");
        c.gridwidth = 1;
        pane.add(label,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        final JPasswordField passfield = new JPasswordField();
        passfield.setColumns(13);
        pane.add(passfield,c);
        passfield.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String pom = usnameField.getText();
                if (TryLogin(pom, passfield.getPassword()))
                {
                    loginFrame.setVisible(false);
                    name = pom;
                    new Listener(socket);
                }
                else
                {
                    if (!reportedError)
                    JOptionPane.showMessageDialog(new JFrame(), "The password or username incorrect or you're already logged somewhere else.");
                }
                reportedError = false;
            }
        });

        JButton button = new JButton("Submit");
        pane.add(button,c);  
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {                
                String pom = usnameField.getText();
                if (TryLogin(pom, passfield.getPassword()))
                {
                    loginFrame.setVisible(false);
                    name = pom;
                    Listener listener = new Listener(socket);
                }
                else
                {
                    if (!reportedError)
                    JOptionPane.showMessageDialog(new JFrame(), "The password or username incorrect or you're already logged somewhere else.");
                }
                reportedError = false;
            }
        });
                    
        loginFrame.pack();
    }      
       
    /** tries to login to the server with specified username and password
     * @param username username to valid
     * @param password password
     * @return if the connection is succesfully established returns true, otherwise false
     */
    private static boolean TryLogin(String username, char[] password)
    {
        try {
            InetAddress addr = InetAddress.getByName(inetAdressToJoin);
            socket = new Socket(addr, 7777);
            writer = new PrintStream(socket.getOutputStream());
            reader = new BufferedReader((new InputStreamReader(socket.getInputStream())));
            String sr = reader.readLine(); //wait until server is ready
            
            if (!"OK".equals(sr)) return false; //if server has some problems, return false
                
            writer.println(username); //sent him our username
            String passwd = new String(password);
            writer.println(passwd); //and password
            String in = reader.readLine(); //and wait for the answer
            
            if ("yes".equals(in)) {
                return true;
            } else {
                CloseConnection(socket, reader, writer);
                return false;
            }
        } catch (UnknownHostException e) {
            reportedError = true;
            JOptionPane.showMessageDialog(new JFrame(), "Server not found.");
        } catch (IOException ex) {            
            reportedError = true;
            JOptionPane.showMessageDialog(new JFrame(), "Server not running.");
        } 
        return false;
    }
    
    /** closes the connection between us and the server*/
    private static void CloseConnection(Socket socket, BufferedReader reader, PrintStream writer) throws IOException
    {
        if (socket != null) socket.close();
        if (reader != null) reader.close();
        if (writer != null) writer.close();
        if (listener != null) listener.end = true;
    }
    
    static ArrayList<String> usernamesList = null;
    static Listener listener = null;
    static JPanel panel = new JPanel(new GridBagLayout());
    static JFrame mainRoomFrame = null;    
    static Piskvorky_game piskvorky = null;
    static String name;
    
    /** method to create frame, where we can challange someone, or see statistics */
    public static void CreateGUIMainRoom()
    {
        mainRoomFrame = new JFrame("Main room");
        mainRoomFrame.setVisible(true);
        mainRoomFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainRoomFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                writer.println("leaving");
                try {
                    CloseConnection(socket, reader, writer);
                } catch (IOException e) {
                }
                System.exit(0);
            }
        });
        
        Container pane = mainRoomFrame.getContentPane();
        pane.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();        
        
        c.insets = new Insets(10,10,0,10);  // padding
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1.0;
        c.weighty = 1.0;
        JLabel label = new JLabel("Main Room");
        pane.add(label, c);
        
        JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
        sep.setPreferredSize(new Dimension(5,5));
        c.fill = GridBagConstraints.BOTH;
        pane.add(sep,c);
                
        label = new JLabel("Number of users: " + usernamesList.size());
        pane.add(label, c);
        
        panel = new JPanel(new GridBagLayout());
        pane.add(panel);

        //we write down all connected users 
        for (String sr : usernamesList) {
            label = new JLabel(sr);
            c.gridwidth = GridBagConstraints.RELATIVE;
            panel.add(label, c);
            JButton button = new JButton("Challenge");
            c.gridwidth = GridBagConstraints.REMAINDER;
            panel.add(button, c);
            button.addActionListener(new ButtonClick(sr));
        }
                
        c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.RELATIVE;
        JButton button = new JButton("Refresh list");
        panel.add(button,c);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainRoomFrame.setVisible(false);
                writer.println("refresh");
            }
        });   
        
        statistic = new Statistics(name, mainRoomFrame);
        button = new JButton("Statistics");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainRoomFrame.setVisible(false);
                statistic.ShowLoading();
                writer.println("statistics");
            }
        }); 
        panel.add(button,c);
        
        mainRoomFrame.pack();     
    }
          
    /** after clicking on challange button, this listener is called and challenges the chosen oponnent */
    private static class ButtonClick implements ActionListener
    {
        String username;
        
        public ButtonClick(String username)
        {
            this.username = username;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            writer.println("challenge " + username);
        }        
    }
    
    /** creates new game */
    private static void CreateNewGame(String usernameEnemy, boolean start)
    {
        new Piskvorky_game(socket, usernameEnemy, piskKlient, start);
    }
        
    /** class, that listens on specified socket and reacts on the accepted data */
    private static class Listener extends Thread {

        public Socket socket;
        public boolean end = false;

        public Listener(Socket socket) {
            this.socket = socket;
            start();
        }

    @Override
    public void run() {
        try {
            writer.println("refresh"); //we want to get the names of all users on the server
            while (end == false) {
                String sr = reader.readLine();
                DecideWhatToDo(sr);
            }
        } catch (SocketException se) {
            try
            {
                socket.close();                
            }
            catch (IOException e)
            {
                
            }
            JOptionPane.showMessageDialog(new JFrame(), "The server was shut-down. The client will be exited. Sorry fot that.");
            System.exit(0);
        } catch (IOException e) {
            System.out.println(e);
        }
    }
    
    /** method, which decides, what to do with received data */
    private void DecideWhatToDo(String sr) throws IOException
    {
        String[] array = sr.split(" ");
        switch (array[0]) {
            case "refresh":
                //server sends us the names of connected users so that we can show them to user
                usernamesList = new ArrayList<>();
                String line = reader.readLine();

                while (!"endOfUsers".equals(line)) {
                    usernamesList.add(line);
                    line = reader.readLine();
                }
                CreateGUIMainRoom();
                break;
            case "statistics":
                //server sends us new statistics
                ArrayList<String> pom = new ArrayList<>();
                line = reader.readLine();
                while (!"endOfStatistics".equals(line))
                {
                    pom.add(line);
                    line = reader.readLine();
                }
                try (BufferedWriter writer = new BufferedWriter(new FileWriter("gamelist")))
                {
                    for (String s : pom)
                    {
                        writer.write(s + "\n");
                    }
                    pom = null;                   
                }
                catch (IOException e)
                {
                    System.out.println("An error occured while trying to make new file (due to new statistics)");
                }    
                statistic.ShowStatistics();
                break;
            case "leaving": 
                //server has to be quit, the only thing we can do is quit too
                CloseConnection(socket, reader, writer);
                JOptionPane.showMessageDialog(new JFrame(), "Server was shut-down. Your game will be exited. Sorry for that.");
                System.exit(0);
                break;
            case "challenge":
                //someone challanges us to play a game with him                
                if (JOptionPane.showConfirmDialog(new JFrame(), "Do you accept a challenge from " + array[1] + "?", "WARNING",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    writer.println("yes " + array[1]);  
                    mainRoomFrame.setVisible(false); //disable our main room window
                    CreateNewGame(array[1], true); 
                } else {
                    writer.println("no " + array[1]);
                }
                break;
            case "no":
                switch (array[1])
                {
                    case "exists":
                        //we challenged some user, but he is already left
                        JOptionPane.showMessageDialog(new JFrame(), "Such a user is not here anymore or he is currently playing game");
                        mainRoomFrame.setVisible(false);
                        writer.println("refresh");
                        break;      
                    case "declined":
                        //user, who we challenged to play with us, declined our game offer
                        JOptionPane.showMessageDialog(new JFrame(), "User " + array[2] + " declined your game offer." );
                        break;
                }
                break;
            case "yes":          
                //user, who we challenged, agreed to play a game with us
                JOptionPane.showMessageDialog(new JFrame(), "Playing game");
                mainRoomFrame.setVisible(false);  
                CreateNewGame(array[1], false);
                break;
            case "movement":
                //movement in our game
                Piskvorky_game.myPanel.OpponentMovement(Integer.parseInt(array[2]), Integer.parseInt(array[3]));
                break;
            case "lost":
                //we have lost our game
                Piskvorky_game.myPanel.LostInfo();
                break;
            case "win":
                //our opponent left (reigned)
                Piskvorky_game.myPanel.EnemyLeaveInfo();
                break;
            case "draw":
                //our opponent left (reigned)
                Piskvorky_game.myPanel.DrawInfo();
                break;
        }
    }
    }      
}
