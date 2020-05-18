/**
 * @author Kim Buckner
 * Date: Jan 13, 2019
 *
 *
 * A starting point for the COSC 3011 programming assignment
 * Probably need to fix a bunch of stuff, but this compiles and runs.
 *
 * This COULD be part of a package but I choose to make the starting point NOT a
 * package. However all other added elements should be sub-packages.
 *
 * Main should NEVER do much more than this in any program that is
 * user-interface intensive, such as this one. If I find that you have chosen
 * NOT to use Object-Oriented design methods, I will take huge deductions. 
 * 
 * 
 */
/**@author Ben Wabschall
 * @author Tristan Redding
 * @author Mathew Floyd
 * @author Yun Chi Leong
 * @author Bryce Ostrem
 */

import java.io.IOException;

import javax.swing.*;


public class Main 
{

    public static void main(String[] args) throws IOException//, InvalidHexException
    {   
       
        // that GameWindow would inherit from  
        int windowWidth = 1000;
        int windowHeight = 1000;                                    // game window sized to 1000 x 1000
        int gridWidth = ( (int) Math.floor( windowWidth / 2.5) );  
        int gridHeight = ( (int) Math.floor( windowHeight / 2.5) ); // game grid sized to 400 x 400


        try {
            GameWindow game = new GameWindow("Group Oscar aMaze", windowWidth, windowHeight,
                    gridWidth, gridHeight);   
            // The 4 that are installed on Linux here
            // May have to test on Windows boxes to see what is there.
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            // This is the "Java" or CrossPlatform version and the default
            //UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            // Linux only
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
            // really old style Motif 
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
        } 
        catch (UnsupportedLookAndFeelException e) {
            // handle possible exception
        }
        catch (ClassNotFoundException e) {
            // handle possible exception
        }
        catch (InstantiationException e) {
            // handle possible exception
        }
        catch (IllegalAccessException e) {
            // handle possible exception
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

};
