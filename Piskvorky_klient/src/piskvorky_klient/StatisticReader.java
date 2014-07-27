package piskvorky_klient;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Jakub
 */

/**class, that implements IStatisticReader, which means, it allows us to use the implemented methods */
public class StatisticReader implements IStatisticReader{

    private HashMap<String, HashMap<String, Result>> hashTable = new HashMap<>();
        
    @Override
    public int NumberOfWins(String player) {
        if (!hashTable.containsKey(player)) return 0;
        
        int count = 0;
        
        for (Result result : hashTable.get(player).values())
        {
            count += result.wins;
        }
        
        return count;
    }

    @Override
    public int NumberOfWins(String player, String opponent) {
        if (!hashTable.containsKey(player)) return 0;
        if (!hashTable.get(player).containsKey(opponent)) return 0;
        return hashTable.get(player).get(opponent).wins;
    }

    @Override
    public int NumberOfGames(String player) {
        if (!hashTable.containsKey(player)) return 0;
        
        int count = 0;
        
        for (Result result : hashTable.get(player).values())
        {
            count += result.wins;
            count += result.losts;
            count += result.draws;
        }
        
        return count;  }

    @Override
    public int NumberOfGames(String player, String opponent) {
        if (!hashTable.containsKey(player)) return 0;
        if (!hashTable.get(player).containsKey(opponent)) return 0;
        return (hashTable.get(player).get(opponent).wins + hashTable.get(player).get(opponent).losts + hashTable.get(player).get(opponent).draws);
    }

    @Override
    public int NumberOfDraws(String player) {
        if (!hashTable.containsKey(player)) return 0;
        
        int count = 0;
        
        for (Result result : hashTable.get(player).values())
        {
            count += result.draws;
        }
        
        return count;  
    }

    @Override
    public int NumberOfDraws(String player, String opponent) {
        if (!hashTable.containsKey(player)) return 0;
        if (!hashTable.get(player).containsKey(opponent)) return 0;
        return hashTable.get(player).get(opponent).draws;
    }

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
    public ArrayList<String> AllPlayers() {
       ArrayList<String> list = new ArrayList<>();
       
       for (String name : hashTable.keySet())
       {
           list.add(name);
       }
       
       return list;
    }

    @Override
    public ArrayList<String> PlayersPlayedWith(String player) {
        if (!hashTable.containsKey(player)) return null;
        
        ArrayList<String> list = new ArrayList<>();
        
        for (String name : hashTable.get(player).keySet())
        {
            list.add(name);
        }
        
        return list;
    }
    
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
