/**@author Ben Wabschall
 * @author Tristan Redding
 * @author Mathew Floyd
 * @author Yun Chi Leong
 * @author Bryce Ostrem
 *
 *
 *  This Class creates three buttons exit, reset, and new
 *  
 *  
 */
import javax.swing.*;
import java.awt.*;

public class GameButtons extends JPanel {

    public static final long serialVersionUID=5;

    public GameButtons(int width, int height, JButton newButton, JButton resetButton, JButton exitButton)
    {

        this.setLayout(new FlowLayout(FlowLayout.CENTER, width / 6, height / 3));
        this.setPreferredSize(new Dimension (width, height));

        this.setBackground(Color.DARK_GRAY);

        this.add(newButton);

        this.add(resetButton);

        this.add(exitButton);
    }
}
