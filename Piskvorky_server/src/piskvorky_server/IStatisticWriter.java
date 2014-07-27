package piskvorky_server;

import java.io.PrintStream;

/**
 *
 * @author Jakub
 */

/** interface, that describes manipulation with statistics file, when you want to add there some new results or send it to someone*/
public interface IStatisticWriter {
    
    /**loads statistics from a file into memory */
    void Load();
    
    /**saves the statistics from memory into file */
    void Save();
    
    /** saves the result (win)
    * @param player name of player who won
    * @param opponent name of player who lost
    */
    void AddWinTo(String player, String opponent);
    
    /** saves the result (draw)
    * @param player1 first player name
    * @param player2 second player name
    */
    void AddDrawTo(String player1, String player2);
    
    /** sends the statistics to specified user
    * @param out printstream, that will send the statistics
    */
    void SendTo(PrintStream out);
}
