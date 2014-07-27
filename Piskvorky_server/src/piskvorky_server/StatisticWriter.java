package piskvorky_server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;

/**
 *
 * @author Jakub
 */

/**class, that implements IStatisticWriter*/
public class StatisticWriter implements IStatisticWriter {
    
    private HashMap<String, HashMap<String, Result>> hashTable = new HashMap<>();
    
    @Override
    public void Load() {
        try (BufferedReader reader = new BufferedReader(new FileReader("gamelist")))
        {
            String line;
            String[] row;
            
            while ((line = reader.readLine()) != null)
            {
                row = line.split(":");
                
                if (!hashTable.containsKey(row[0])) //if there is not a hashtable for the name, we add it
                {
                    hashTable.put(row[0], new HashMap<String, Result>());
                }
                
                HashMap<String, Result> pomTable = hashTable.get(row[0]); //now it exists and we put there a new result with new enemy
                
                pomTable.put(row[1], new Result(Integer.parseInt(row[2]), Integer.parseInt(row[3]), Integer.parseInt(row[4]))); //the enemy can not be there yet
            }
        }
        catch (IOException e)
        {
            System.out.println("While loading result list, there has been some error occured: " + e.getMessage());
        }  
        catch (ArrayIndexOutOfBoundsException a)
        {
            System.out.println("There is an error in the resut list (gamelist) format! It will not probably work properly!");
        }        
    }

    @Override
    public void Save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("gamelist")))
        {
            String out;
            for (String sr : hashTable.keySet()) 
            {
                for (String s : hashTable.get(sr).keySet())
                {
                    out = sr + ":" + s + ":" + hashTable.get(sr).get(s).toString();
                    writer.write(out + "\n");
                }
            }
            
        }
        catch (IOException e)
        {
            System.out.println("An error occured while trying to save the results into the file");
        }
    }

    @Override
    public void AddWinTo(String player, String opponent) {
        AddWin(player, opponent);
        AddLoss(opponent, player);
    }
    
    /** private method used to save win
    * @param player winner name
    * @param opponent loser name
    */
    private void AddWin(String player, String opponent)
    {
        if (!hashTable.containsKey(player))
        {
            hashTable.put(player, new HashMap<String, Result>());
        }
        
        HashMap<String, Result> pomTable = hashTable.get(player);
        
        if (!pomTable.containsKey(opponent))
        {
            pomTable.put(opponent, new Result(0, 0, 0));
        }
        
        pomTable.get(opponent).wins++;
    }
    
    /** private method used to save loss
    * @param player loser name
    * @param opponent winner name
    */
    private void AddLoss(String player, String opponent)
    {
        if (!hashTable.containsKey(player))
        {
            hashTable.put(player, new HashMap<String, Result>());
        }
        
        HashMap<String, Result> pomTable = hashTable.get(player);
        
        if (!pomTable.containsKey(opponent))
        {
            pomTable.put(opponent, new Result(0, 0, 0));
        }
        
        pomTable.get(opponent).losts++;
    }
    
     /** private method used to save draw
    * @param player first player name
    * @param opponent second player name
    */
    private void AddDraw(String player, String opponent)
    {
        if (!hashTable.containsKey(player))
        {
            hashTable.put(player, new HashMap<String, Result>());
        }
        
        HashMap<String, Result> pomTable = hashTable.get(player);
        
        if (!pomTable.containsKey(opponent))
        {
            pomTable.put(opponent, new Result(0, 0, 0));
        }
        
        pomTable.get(opponent).draws++;
    }

    @Override
    public void AddDrawTo(String player1, String player2) {
        AddDraw(player1, player2);
        AddDraw(player2, player1);
    }

    @Override
    public void SendTo(PrintStream writer) 
    {
        String out;
        for (String sr : hashTable.keySet()) 
            {
                for (String s : hashTable.get(sr).keySet())
                {
                    out = sr + ":" + s + ":" + hashTable.get(sr).get(s).toString();
                    writer.println(out);
                }
            }
    }
    
    /**private class, that represents the result of all games between two players*/
    private class Result
    {
        public int wins;
        public int draws;
        public int losts;
        
        public Result(int wins, int losts, int draws)
        {
            this.wins = wins;
            this.losts = losts;
            this.draws = draws;
        }
        
        @Override
        public String toString()
        {
            return (wins + ":" + losts + ":" + draws);
        }
    }
}
