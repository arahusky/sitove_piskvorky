/**
 * The main package for server. Run by pressing start. Stop by pressing stop.
 * Users are save in passwd file (format: username:password)
 */
package piskvorky_server;

import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.swing.*;
/**
 *
 * @author Jakub
 */

/**class, that creates simple server GUI and then creates new instance of server (class Server) */
public class Piskvorky_server {

    /** whether is server running or not*/
    static JLabel status; 
    static Server server;
    /** number of active users*/
    static JLabel numUsers; 
    
    /** just calls private method CreateGUI, that creates server GUI */
    public static void main(String[] args) {
        // TODO code application logic here
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
               CreateGUI();
            }
        });
    }
    
    /** creates simple gui for server*/
    private static void CreateGUI()
    {
        JFrame frame = new JFrame("Server");        
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                if (server != null)
                {    
                    server.exit();
                }
                System.exit(0);
            }
        });
        Container pane = frame.getContentPane();
        
        pane.setLayout(new GridLayout(4,2));
        
        JLabel label;
        
        label = new JLabel("IP");
        pane.add(label);
        try {
            InetAddress a = InetAddress.getLocalHost();
            label = new JLabel(a.getHostAddress());
        } catch (UnknownHostException ex) {
            System.out.println("Unknown host exception (could not make localhost)");
            label = new JLabel("Unknown");
        }
        pane.add(label);
        label = new JLabel("Number of users:  ");
        pane.add(label);
        numUsers = new JLabel("0");
        pane.add(numUsers);
        label = new JLabel("Currently:");
        pane.add(label);
        status = new JLabel("Not running");        
        status.setForeground(Color.red);
        pane.add(status);
        
        JButton button = new JButton("Start");
        pane.add(button);
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                server = new Server(numUsers);
                status.setText("Running");
                status.setForeground(Color.GREEN);
                }
        });
        
        button = new JButton("Stop");
        pane.add(button);
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {               
                status.setText("Not running");
                status.setForeground(Color.red);
                if (server != null)
                {    
                    server.exit();
                    server = null;
                }            
            }
        });
        
        frame.pack();
    }
}
