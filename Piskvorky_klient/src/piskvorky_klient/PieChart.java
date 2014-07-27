package piskvorky_klient;

/**
 *
 * @author http://blue-walrus.com/2012/09/simple-pie-chart-in-java-swing/
 * with some upgrades (legend)
 */
 
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
 
import javax.swing.JPanel;
 
/** class, that represents piechart graph */
public class PieChart extends JPanel {
  
    private ArrayList values;
    private ArrayList colors;
    
     /** PieChart constructor
     * @param values the list of values to be shown
     * @param colors the list of colors, that will be shown
     */
    public PieChart(ArrayList values, ArrayList colors) {
  
        setPreferredSize(new Dimension(175,175));
        this.values = values;
        this.colors = colors;
    }
 
    @Override
    protected void paintComponent(Graphics g) {
 
        super.paintComponent(g);
        int width = getSize().width - 25;
 
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
                RenderingHints.VALUE_ANTIALIAS_ON);
 
                 int lastPoint = -270;
 
            for (int i = 0; i < values.size(); i++) {
                g2d.setColor((Color) colors.get(i));
 
                Double val = (Double) values.get(i);
                Double angle = (val / 100) * 360;
 
                g2d.fillArc(0, 0, width, width, lastPoint, -angle.intValue());
                 
                lastPoint = lastPoint + -angle.intValue();
            }
            
            //legend
            g2d.setColor(Color.black);
            g2d.drawRect(5, 157, 145, 16);
            
            g2d.drawOval(9, 160, 10, 10);
            g2d.setColor(Color.BLUE);
            g2d.fillOval(9, 160, 10, 10);
            
            g2d.setColor(Color.black);            
            g2d.drawString("win", 23, 170);
            
            g2d.drawOval(50, 160, 10, 10);
            g2d.setColor(Color.ORANGE);
            g2d.fillOval(50, 160, 10, 10);
            
            g2d.setColor(Color.black);            
            g2d.drawString("draw", 65, 170);
                        
            g2d.drawOval(100, 160, 10, 10);
            g2d.setColor(Color.RED);
            g2d.fillOval(100, 160, 10, 10);
            
            g2d.setColor(Color.black);            
            g2d.drawString("loss", 115, 170);
    }
}
