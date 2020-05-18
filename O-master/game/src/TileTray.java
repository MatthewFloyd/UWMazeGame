/**@author Ben Wabschall
 * @author Tristan Redding
 * @author Mathew Floyd
 * @author Yun Chi Leong
 * @author Bryce Ostrem
 * 
 * This class creates two 8x1 tile trays on the left and right of the play window,
 * 
 * takes window size as parameters from main.
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.*;

public class TileTray extends JPanel
{
    public static final long serialVersionUID=4;

    public TileTray(int gridWidth, int gridLength, TileObject[] tileObjects)
    {

        setLayout(new GridLayout (8,1,0,10));
        setPreferredSize(new Dimension (100,100)); //sets dimensions of each grid
        setBackground(Color.DARK_GRAY);

        //create tile tray
        for (int i =0; i<8; i++){
            add(tileObjects[i]);
        }
        setVisible(true);
        //tile tray created
    }

}