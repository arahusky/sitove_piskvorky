package piskvorky_server;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.JLabel;

/**
 * @author Jakub
 */

/** class, that represents server (serversocket), for each connected user there is an instance of private class ClientThread, 
 *which handles the communication between server and the client
 */
public class Server extends Thread{

    /**arraylist to store all logged users */
    public static ArrayList<ClientThread> listOfUsers = new ArrayList<>();
    
    /**number of connected users */
    public JLabel numUsers; 
    /**hashmap, where we store all users and theirs password */
    private HashMap<String, String> userBase = null;
    private ServerSocket serverSocket;
    /** statistics (that handles adding new results and when requested sending results to users) */
    private StatisticWriter statistic; 
    
    /**
     *
     * @param numUsers label, that says number of server users, that will be maintained
     */
    public Server(JLabel numUsers)
    {
        this.numUsers = numUsers;
        statistic = new StatisticWriter();
        statistic.Load();
        start();
    }

    @Override
    public void run() {
        try {
            LoadAllUsers();
            serverSocket = new ServerSocket(7777);
            
            System.out.println("Server ready to serve");
            while (true) {
                Socket socket = serverSocket.accept();   
                new ClientThread(socket);
            }
        }
        catch (IOException e)
        {
            System.out.println("Unable to accept new user (there may be such an user already logged or the server is shutting down)");
        }
        finally
        {
            try {
                serverSocket.close();
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }
    }
    
    /** loads from a file all possible users (user and his password), so that we can easily decide, whether to accept such a user, or not*/
    private void LoadAllUsers()
    {
        userBase = new HashMap<>();
        
        try(BufferedReader reader = new BufferedReader(new FileReader(new File("passwd"))))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                String[] user = line.split(":");
                userBase.put(user[0], user[1]);
            }
            
        } catch (FileNotFoundException ex) {
            System.out.println(ex);
        } catch (IOException ex) {
            System.out.println(ex);
        }        
    }
    
    /** Decreases number of users (changes label.Text - when user leaves) */
    private synchronized void DecreaseNumberOfUsers()
    {
        if ((Integer.parseInt(numUsers.getText()) - 1) >= 0)
        {
           numUsers.setText((Integer.parseInt(numUsers.getText()) - 1) + ""); //deducts 1 to number of users on server
        }
        else
        {
            numUsers.setText("0");
        }
    }
    
    /** Increases number of users (changes label.Text - when user comes) */
    private synchronized void IncreaseNumberOfUsers()
    {
        numUsers.setText((Integer.parseInt(numUsers.getText()) + 1) + ""); //adds 1 to number of users on server
    }
        
    /** Function to indicate, whether such user with the password exists
    * @param username username
     * @param passwd password
     * @return true (when such an user with the password exists, if not false) 
    */
    public synchronized boolean ExistsSuchUser(String username, String passwd)
    {
        return (userBase.get(username) != null && userBase.get(username).equals(passwd));            
    }
    
    /** Function to indicate, whether such user is already logged in
    * @param username username
     * @return true (when such an user with the password is already logged in) 
    */
    private synchronized boolean IsSuchUserAlreadyLoggedIn(String username)
    {
        for (ClientThread t : listOfUsers)
        {
            if (t.username.equals(username)) return true;
        }
        
        return false;
    }
    
    /** closes server */
    public void exit()
    {
        System.out.println("exiting server");
        
        ClientThread[] array = new ClientThread[listOfUsers.size()];
        
        for (int i = 0; i<listOfUsers.size(); i++)
        {
            array[i] = listOfUsers.get(i);
        }
        
        for (int i = 0; i<array.length; i++)
        {
            array[i].out.println("leaving");
            try {
                array[i].closeWithoutListRemoving();
            } 
            catch (IOException e)                
            {                
            }
            catch (NullPointerException ex)
            {                
                System.out.println(ex);
            }
        }
        listOfUsers = new ArrayList<>();
        try {
            serverSocket.close();
        } catch (IOException ex) {
            System.out.println(ex);
        }
        
        statistic.Save();
    }
    
    /** saves the result of a game
    * @param firstPlayer first player username
     * @param secondPlayer second player username
     * @param result  result of a game (may be: 1=first player wins, 2=draw)
     */
    private void SaveResult(String firstPlayer, String secondPlayer, int result)
    {
        if (result == 1)
        {
            statistic.AddWinTo(firstPlayer, secondPlayer);
        }
        else if (result == 2)
        {
            statistic.AddDrawTo(firstPlayer, secondPlayer);
        }
    }

    /** for every connected user, there will be a special thread maintaining a communication with him (listening to his socket) */
    private class ClientThread extends Thread {

        public Socket socket;
        public BufferedReader in;
        public PrintStream out;
        public String username;
        public boolean playingGame = false;
        public String enemyName;

        /** constructor of a new clientthread
        * @param socket socket, that will this thread handle
        * @throws IOException when not being able to get input/output stream from the socket
        */
        public ClientThread(Socket socket) throws IOException {
            this.socket = socket;
            
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintStream(socket.getOutputStream());
            start();
        }

        @Override
        public void run() {
            try
            {
                out.println("OK"); //send a message to client indicating, that i am ready to receive messages
                
                String usname = in.readLine();
                String passwd = in.readLine();
                if (!ExistsSuchUser(usname, passwd) || IsSuchUserAlreadyLoggedIn(usname))
                {
                    out.println("no");
                    socket.close();
                    in.close();
                    out.close();
                    return;
                }
                                
                out.println("yes"); // tell him, he can join a main room
                this.username = usname;
                IncreaseNumberOfUsers();
                listOfUsers.add(this);  // add him to list of users
                
                String sr = in.readLine(); // wait until user is ready
                SendUsernames();
                
                while (true) {
                    sr = in.readLine();
                    if (!"leaving".equals(sr)) {                        
                        DecideWhatToDo(sr);
                    } else {
                        close();
                        return;
                    }
                }                
            }           
            catch (SocketException s) //when user leaves
            {
                if (this.playingGame) //when he leaves and he was playing game, we must inform his opponent of that
                {
                    int i = ExistsSuchPlayes(enemyName);
                    if (i != -1)
                    {
                       listOfUsers.get(i).out.println("win " + this.username);
                       listOfUsers.get(i).playingGame = false;
                       SaveResult(enemyName, username, 1);
                    }   
                }
                try {
                    close();
                } catch (IOException ex) {
                    System.out.println("There has been some error when trying to close connection with: " + this.username);
                }
            }
            catch (IOException e)
            {
                try {
                    close();
                } catch (IOException ex) {
                    System.out.println("There has been some error when trying to close connection with: " + this.username);
                }
            }
        }
        
        /** method, that decides what to do with incoming message from user
        * @param sr the string that we received
        */        
        private void DecideWhatToDo(String sr)
        {
            if (sr == null) return;
            String[] pom = sr.split(" ");
            switch (pom[0]) {
                case "refresh":
                    //user wants the names of user connected to the server
                    SendUsernames(); 
                    break;
                case "statistics":
                    out.println("statistics");
                    statistic.SendTo(out);
                    out.println("endOfStatistics");
                    break;
                case "challenge": 
                    //user challenges someone (in pom[1])
                    int i = ExistsSuchPlayes(pom[1]);
                    if (i != -1)
                    {
                        if (listOfUsers.get(i).playingGame) {
                            out.println("no exists");
                        } else {
                            listOfUsers.get(i).out.println("challenge " + this.username);
                        }
                    }
                    else
                    {
                        this.out.println("no exists");
                    }
                    break;
                case "yes": 
                    //user accepts an offer from a player
                    i = ExistsSuchPlayes(pom[1]);
                    if (i != -1)
                    {
                        listOfUsers.get(i).out.println("yes " + this.username);
                        listOfUsers.get(i).playingGame = true;
                        listOfUsers.get(i).enemyName = this.username;
                        enemyName = listOfUsers.get(i).username;
                        playingGame = true;
                    }
                    else
                    {
                        this.out.println("no exists");
                    }
                    break;
                case "no":
                    //user declines the offer
                    i = ExistsSuchPlayes(pom[1]);
                    if (i != -1)
                    {
                        listOfUsers.get(i).out.println("no declined " + this.username);
                    }
                    break;
                case "movement":
                    //user makes a movement
                     i = ExistsSuchPlayes(pom[1]);
                    if (i != -1)
                    {
                        listOfUsers.get(i).out.println("movement " + this.username + " " + pom[2] + " " + pom[3]);
                    }
                    else
                    {
                        //enemy is not here anymore, we have to tell it to client
                        this.out.println("no exists");
                        
                        this.playingGame = false;
                    }
                    
                    break;   
                case "win":
                    //user wins (his opponent, to who we're sending it, wins)
                    playingGame = false;
                    i = ExistsSuchPlayes(pom[1]);
                    if (i != -1)
                    {
                        listOfUsers.get(i).out.println("movement " + this.username + " " + pom[2] + " " + pom[3]);
                        listOfUsers.get(i).out.println("lost " + this.username);
                        listOfUsers.get(i).playingGame = false;
                    }
                    SaveResult(this.username, pom[1], 1);
                    break;
                case "lost":
                    //in case, that we closed the game (reigned), we have to tell the enemy, that he wins
                    playingGame = false;
                    i = ExistsSuchPlayes(pom[1]);
                    if (i != -1)
                    {
                        listOfUsers.get(i).out.println("win " + this.username);
                        listOfUsers.get(i).playingGame = false;
                    }
                    SaveResult(pom[1], this.username, 1);
                    break;    
                case "draw":
                    //it's a draw
                    playingGame = false;
                    i = ExistsSuchPlayes(pom[1]);
                    if (i != -1)
                    {
                        listOfUsers.get(i).out.println("movement " + this.username + " " + pom[2] + " " + pom[3]);
                        listOfUsers.get(i).out.println("draw " + this.username);
                        listOfUsers.get(i).playingGame = false;
                    }
                    SaveResult(this.username, pom[1], 2);
                    break;
            }
        }
        
        /** method to find out, whether the player with given username exists (is in main room - listOfUser)
        * @param username
        * @return if exists, return his index in array, else returns -1
        */        
        private int ExistsSuchPlayes(String username)
        { 
                for (int i = 0; i<listOfUsers.size(); i++)
                {
                    if (listOfUsers.get(i).username.equals(username)) return i;
                }
                
                return -1;
        }
        
        /** closes the clientthread (connection between user and server)
         * @throws IOException when not able to close it
         */
        public void close() throws IOException
        {
            listOfUsers.remove(this);
            socket.close();
            in.close();
            out.close();           
            DecreaseNumberOfUsers();
        }
        
        /** closes the clienthread, but does not remove it from a list 
        * @throws IOException when not able to close it
        */
        public void closeWithoutListRemoving() throws IOException
        {
            socket.close();
            in.close();
            out.close();           
            DecreaseNumberOfUsers();
        }
        
        /**method, that sends the usernames of all connected users from server to client */
        private void SendUsernames()
        {
            out.println("refresh");
            for (ClientThread t : listOfUsers)
            {
                if (t != this)
                {
                    out.println(t.username);
                }
            }
            out.println("endOfUsers");        }
    }
}

