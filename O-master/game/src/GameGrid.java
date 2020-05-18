/**@author Ben Wabschall
 * @author Tristan Redding
 * @author Mathew Floyd
 * @author Yun Chi Leong
 * @author Bryce Ostrem
 *
 *    This class creates a 4x4 grid in the center of the play window
 *     
 *     pass in desired size from main()
 */
import javax.swing.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

public class GameGrid extends JPanel
{
    public static final long serialVersionUID=2;

    public GameGrid(int gridWidth, int gridLength, TileObject[] tileObjects)
    {

        setLayout(new GridLayout (4, 4, 0, 0));
        setPreferredSize(new Dimension (gridWidth, gridLength)); //sets dimensions of grid

        setBackground(Color.DARK_GRAY);
        setBorder(BorderFactory.createEmptyBorder(2,2,2,2));

        //create grid
        for (int i =0; i<(4*4); i++){
            add(tileObjects[i]);
        }
        setVisible(true);
        //grid created
    }
}