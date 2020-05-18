/**@author Ben Wabschall
 * @author Tristan Redding
 * @author Mathew Floyd
 * @author Yun Chi Leong
 * @author Bryce Ostrem
 * 
 * This Class create an object with the purpose of displaying a section of a larger maze object.
 * 
 */
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import javax.swing.JComponent;

public class TileObject extends JComponent {//implements MouseListener { // mixed case must be used for method names

    private static final long serialVersionUID = 3;    
    
    private int _height, _width;
  
    private ArrayList<Line2D> lines = new ArrayList<Line2D>();

    private Graphics2D graphic = null;

    public boolean _active = false;

    public Rectangle2D shape;

    public int id, position, _rotation, _iniPosition, _iniRotation = 0;

    public TileObject(int id, Point pos, int wSize, int hSize) {
        // compute ideal height and width based off game window dimensions
        _height = hSize;
        _width = wSize;
        shape = new Rectangle2D.Double(pos.getX(), pos.getY(), _width, _height);
        this.id = id;
    }

    public void setLines(ArrayList<Line2D> lines)
    {
        // separate function instead of parameter on constructor
        // to reduce conflicts with other files
        this.lines = lines;

    }

    public ArrayList<Line2D> getLines(){
        return this.lines;
    }    

    public int rotate() {
        // with no arguments will just rotate 90 degrees
        // use with right click
        incrementRotation(90);
        this.repaint();
        return (_rotation / 90);
    }

    public int rotate(int rotate) {
        // with one argument, rotate to specific value, 0, 1, 2, 3
        // resets initial, use at start game
        int rotateAmount = findRotation(rotate);
        if(rotateAmount != 0)
        {
            incrementRotation(rotateAmount);
        }
        this.repaint();
        return (_rotation / 90);
    }
    
    public void resetInitRotate() {
      _iniRotation = _rotation;
    }

    public int resetRotation() {
        _rotation = _iniRotation;
        this.repaint();
        return (_rotation / 90);
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        if(graphic == null)
            graphic = g2d;
        g2d.rotate(Math.toRadians(_rotation), 50, 50);
        Font numbers = new Font("SansSerif", Font.PLAIN, _height / 5);
        
        g2d.setFont(numbers);
        g2d.setPaint(Color.LIGHT_GRAY);
        g2d.fill(shape);
        
        if(id < 0) {
            g2d.setPaint(Color.RED);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRect((int) shape.getMinX(), (int) shape.getMinY(), _width, _height);
        }
        
        g2d.setPaint(Color.BLACK);
        
        if(this.lines.size() > 0)
        {
            // array of coordinates for lines to draw
            g2d.setStroke(new BasicStroke(3));
            for(int i = 0; i < this.lines.size(); i++)
                g2d.draw(this.lines.get(i));
        }
    }

    public void showActive() {
        graphic = (Graphics2D)this.getGraphics();
        graphic.setPaint(Color.GREEN);
        graphic.setStroke(new BasicStroke(5));
        graphic.drawRect((int) shape.getMinX(), (int) shape.getMinY(), _width, _height);
    }

    // flashes red border to indicate invalid move
    public void showInvalid() {        
        graphic = (Graphics2D)this.getGraphics();
        graphic.setPaint(Color.RED);
        graphic.setStroke(new BasicStroke(5));
        graphic.drawRect((int) shape.getMinX(), (int) shape.getMinY(), _width, _height);
        pause(250);
        graphic.setPaint(null);
        graphic.drawRect((int) shape.getMinX(), (int) shape.getMinY(), _width, _height);
    }

    private void pause (int time) {
        try {
            Thread.sleep(time);
        }catch(InterruptedException e){
            //Do Nothing
        }

    }                   
    private void incrementRotation(int amount) {
        if((_rotation + amount) > 270)
        {
            _rotation = _rotation + amount - 360;
        }
        else
            _rotation += amount;
    }

    private int findRotation(int rotate) {
        // calculate distance from starting rotation
        // to ending rotation assuming rotate is 0, 1, 2, 3
        int rotateAmount = ((rotate * 90) - _rotation);
        if(rotateAmount < 0)
            rotateAmount = 360 + rotateAmount;
        return rotateAmount;
    }
}
