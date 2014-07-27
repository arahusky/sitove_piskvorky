package piskvorky_klient;

import java.util.ArrayList;

/**
 *
 * @author Jakub
 */

/**interface representing work with statistics, when you want to read from it */
public interface IStatisticReader {
    
    /** loads a statistic from a file into the memoty (must be called before working with class)*/
    void Load();
    
    /**
     * @param player name of the player
     * @return number of wins of specified player
     */
    int NumberOfWins(String player);
    
    /**
     * @param player the name of the player
     * @param opponent the name of his oponnent
     * @return the number of wins of the player against the opponent
     */
    int NumberOfWins(String player, String opponent);
    
    /**
     * @param player name of the player
     * @return number of games, that the specified player played
     */
    int NumberOfGames(String player);
    
    /**
     * @param player name of the player
     * @param opponent name of the oponnent
     * @return number of games, that the player played against the oponent
     */
    int NumberOfGames(String player, String opponent);
    
    /**
     * @param player name of the player
     * @return number of draws of specified player
     */
    int NumberOfDraws(String player);
    
    /**
     * @param player name of the player
     * @param opponent name of the oponnent
     * @return number of games, that the player drawed with the oponent
     */
    int NumberOfDraws(String player, String opponent);
    
    /**
     * @return the list of all players
     */
    ArrayList<String> AllPlayers();
    
    /**
     * @param player the username of player
     * @return  the list of players, that have played with the player
     */
    ArrayList<String> PlayersPlayedWith (String player);
}
