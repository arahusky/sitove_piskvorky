package piskvorky_klient;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Jakub
 */

/**class, that represents statistics window, it shows us statistics */
public class Statistics {
    String name;
    static JFrame loading;
    static JFrame mainWindow;
    static JLabel smallHeader;
    static JLabel wins;
    static JLabel losses;
    static JLabel draws;
    static ArrayList<String> playersPlayedWith;
    static PieChart pieChart;
    static JPanel centerPart;
    static JPanel piePanel;
    StatisticReader statisticReader = new StatisticReader();
    
    JFrame mainRoomFrame;
    
    /** statistics constructor
    * @param name the name of user
    * @param mainRoomFrame  the instance of JFrame, which represents main window (the one, where we challange oponnents)
    */
    public Statistics(String name, JFrame mainRoomFrame)
    {       
        this.name = name;
        this.mainRoomFrame = mainRoomFrame;
    }
    
    /** method, that shows dialog saying, that the data are downloading */
    public void ShowLoading()
    {
        loading = new JFrame();
        loading.add(new JLabel("Please wait untill we download and load the statistics"));
        loading.setVisible(true);
        loading.pack();
    }
    
    /** method to show the statistics to user*/
    public void ShowStatistics()
    {
        statisticReader.Load();
        loading.setVisible(false); 
        mainWindow = new JFrame();
        mainWindow.setLayout(new BorderLayout());
        mainWindow.setResizable(false);
        mainWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                mainWindow.setVisible(false);
                mainRoomFrame.setVisible(true);               
            }});
        
        //header
        JLabel header = new JLabel("Statistics of " + name, JLabel.CENTER);      
        header.setFont(new Font("SansSerif", Font.BOLD, 18));
        header.setBorder(BorderFactory.createLineBorder(Color.black));
        mainWindow.add(header, BorderLayout.NORTH);
        
        //left side (small header + jlist of opponents)
        JPanel pan = new JPanel();      
        pan.setLayout(new BorderLayout());

        playersPlayedWith = statisticReader.PlayersPlayedWith(name);
                
        if (playersPlayedWith != null)
        {        
            pan.add(new JLabel("Opponents"), BorderLayout.NORTH);
            JList jlist = new JList(playersPlayedWith.toArray());
            jlist.addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent e) {
                     JList list = (JList) e.getSource();
                     int index = list.getSelectedIndex();                    
                     String opponent = playersPlayedWith.get(index);
                     
                     smallHeader.setText("Games against " + opponent);
                     double percentageWin = 100 * statisticReader.NumberOfWins(name, opponent) / statisticReader.NumberOfGames(name, opponent);
                     wins.setText(statisticReader.NumberOfWins(name, opponent) + " (" + Math.round(percentageWin) + "%)");
                     
                     double percentageDraw = 100 * statisticReader.NumberOfDraws(name, opponent) / statisticReader.NumberOfGames(name, opponent);
                     draws.setText(statisticReader.NumberOfDraws(name, opponent) + " (" + Math.round(percentageDraw) + "%)");
                     
                     int pomLosses = statisticReader.NumberOfGames(name, opponent) - statisticReader.NumberOfDraws(name, opponent) - statisticReader.NumberOfWins(name, opponent);
                     double percentageLoss = 100 - percentageDraw - percentageWin;
                     losses.setText(pomLosses + " (" + Math.round(percentageLoss) + "%)");                     
                     
                     piePanel.remove(pieChart);
                     
                     pieChart = ReturnPieChart(percentageWin, percentageDraw, percentageLoss);
                     
                     piePanel.add(pieChart);
                }
            });
            pan.add(jlist);
        }
        mainWindow.add(pan, BorderLayout.WEST);        
        
        //right side (=TOP 3)
        pan = new JPanel();        
        pan.setLayout(new GridLayout(0,1));
        pan.add(new JLabel(" TOP 3 (wins): "));
        for (String name : ThreePointBest())
        {
            int points = statisticReader.NumberOfDraws(name)*1/2 + statisticReader.NumberOfWins(name);
            pan.add(new JLabel("  - " + name + " (" + points + ") "));
        }
        pan.add(new JLabel());
        
        pan.add(new JLabel(" TOP 3 (%):"));
        for (String name : ThreePercentageBest())
        {
            int percent = Math.round(100*(statisticReader.NumberOfDraws(name)*1/2 + statisticReader.NumberOfWins(name))/statisticReader.NumberOfGames(name));
            pan.add(new JLabel("   - " + name + " (" + percent + "%) "));
        }        
        mainWindow.add(pan, BorderLayout.EAST);
        
        //center part        
        centerPart = new JPanel();
        
        centerPart.setBorder(BorderFactory.createLineBorder(Color.black));
        centerPart.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(10, 10, 10, 10);
        smallHeader = new JLabel("Your statistics (all games)");
        centerPart.add(smallHeader, c);
        
        if (statisticReader.NumberOfGames(name) == 0)
        {
           mainWindow.add(new JLabel("You did not play a game yet")); 
        }
        else
        {
        c.insets = new Insets(0, 10, 0, 10);
        c.gridwidth = GridBagConstraints.RELATIVE;
        centerPart.add(new JLabel("Wins: "),c);
        
        c.gridwidth = GridBagConstraints.REMAINDER;
        double winPercentage = 0;
        if (statisticReader.NumberOfGames(name) != 0)
        {
            winPercentage = 100 * statisticReader.NumberOfWins(name) / statisticReader.NumberOfGames(name);
        }
        wins = new JLabel(statisticReader.NumberOfWins(name) + " (" + winPercentage + "%) ");
        centerPart.add(wins,c);
        
        c.gridwidth = GridBagConstraints.RELATIVE;
        centerPart.add(new JLabel("Draws: "),c);
        
        c.gridwidth = GridBagConstraints.REMAINDER;
        double drawPercentage = 0;
        if (statisticReader.NumberOfGames(name) != 0)
        {
            drawPercentage = 100 * statisticReader.NumberOfDraws(name) / statisticReader.NumberOfGames(name);
        }
        draws = new JLabel(statisticReader.NumberOfDraws(name) + " (" + drawPercentage + "%) ");
        centerPart.add(draws,c);
        
        c.gridwidth = GridBagConstraints.RELATIVE;
        centerPart.add(new JLabel("Losses: "),c);
        
        c.gridwidth = GridBagConstraints.REMAINDER;
        double lossPercentage = 100 - drawPercentage - winPercentage;       
        int count = statisticReader.NumberOfGames(name) - statisticReader.NumberOfDraws(name) - statisticReader.NumberOfWins(name);
        losses = new JLabel(count  + " (" + lossPercentage + "%) ");
        centerPart.add(losses,c);
        

        c.gridheight = GridBagConstraints.REMAINDER;
        c.insets = new Insets(10, 10, 0, 10);
        
        piePanel = new JPanel();
        piePanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        pieChart = ReturnPieChart(winPercentage, drawPercentage, lossPercentage);
        piePanel.add(pieChart);
        centerPart.add(piePanel,c);
        mainWindow.add(centerPart, BorderLayout.CENTER);
        }
    
        
        mainWindow.setVisible(true);
        mainWindow.pack();
    }
    
    /** method to create a new piechart
    * @param wins the percentage of won games
    * @param draws the percentage of draws
    * @param losses the percentage of lost games
    * @return instance of PieChart
    */
    private PieChart ReturnPieChart(double wins, double draws, double losses)
    {
        
        ArrayList<Double> values = new ArrayList<>();
        values.add(wins);
        values.add(draws);
        values.add(losses);

        ArrayList<Color> colors = new ArrayList<>();
        colors.add(Color.blue);
        colors.add(Color.orange);
        colors.add(Color.red);
        
        return new PieChart(values, colors);
    }
    
    /**method, that returns three players, who have the most points (wins + draws) */
    private String[] ThreePointBest()
    {        
        ArrayList<String> users = statisticReader.AllPlayers();
        
        float[] points = new float[3];
        String[] bestPlayers = new String[3];
        
        for (String user : users)
        {
            float count = 0;
            count += (statisticReader.NumberOfDraws(user) * 1/2);
            count += statisticReader.NumberOfWins(user);
            
            if (count > points[0]) //he is currently best
            {
                points[2] = points[1];
                bestPlayers[2] = bestPlayers[1];
                points[1] = points[0];
                bestPlayers[1] = bestPlayers[0];
                points[0] = count;
                bestPlayers[0] = user;
            }
            else if (count > points[1])
            {
                points[2] = points[1];
                bestPlayers[2] = bestPlayers[1];
                points[1] = count;
                bestPlayers[1] = user;
            }
            else if (count > points[2])
            {
                points[2] = count;
                bestPlayers[2] = user;
            }            
        }    
        
        return bestPlayers;        
    }
    
    /**method, that return three players, who have best percentage of wins (draws) */
    private String[] ThreePercentageBest()
    {        
        ArrayList<String> users = statisticReader.AllPlayers();
        
        float[] points = new float[3];
        String[] bestPlayers = new String[3];
        
        for (String user : users)
        {
            if (statisticReader.NumberOfGames(user) == 0) continue;
            
            float count = 0;
            count += (statisticReader.NumberOfDraws(user) * 1/2);
            count += statisticReader.NumberOfWins(user);
            
            count = count / (statisticReader.NumberOfGames(user));
            
            if (count > points[0]) //he is currently best
            {
                points[2] = points[1];
                bestPlayers[2] = bestPlayers[1];
                points[1] = points[0];
                bestPlayers[1] = bestPlayers[0];
                points[0] = count;
                bestPlayers[0] = user;
            }
            else if (count > points[1])
            {
                points[2] = points[1];
                bestPlayers[2] = bestPlayers[1];
                points[1] = count;
                bestPlayers[1] = user;
            }
            else if (count > points[2])
            {
                points[2] = count;
                bestPlayers[2] = user;
            }            
        }    
        
        return bestPlayers;        
    }
}
