/**
 * @author Kim Buckner
 * Date: Jan 13, 2019
 *
 * This is the actual "game". Will have to make some major changes.
 * This is just a "hollow" shell.
 *
 * When you get done, I should see the buttons at the top in the "play" area
 * (NOT a pull-down menu). The only one that should do anything is Quit.
 *
 * Should also see something that shows where the 4x4 board and the "spare"
 * tiles will be when we get them stuffed in.
 *
 * This COULD be part of a package but I choose to make the starting point NOT a
 * package. However all other added elements should certainly sub-packages.
 */
/**
 * @author Ben Wabschall
 * @author Tristan Redding
 * @author Mathew Floyd
 * @author Yun Chi Leong
 * @author Bryce Ostrem
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Line2D;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public class GameWindow extends JFrame implements ActionListener
{
    private TileSpecs _tileSpecs;

    public static final long serialVersionUID=1;

    protected TileObject selected; //place holder to help in performing tile movements

    protected TileObject[] left; //Tile objects for the left side tray

    protected TileObject[] right; //Tile objects for the right side tray

    protected TileObject[] board; //Tile objects for the game grid

    protected TileTray westTray;

    protected TileTray eastTray;

    protected GameGrid gameGrid;

    protected Timer timer; 

    protected long elapsedTime; 

    protected boolean hasBeenPlayed = false; 

    protected int gridWidth;

    protected int gridHeight;

    protected JPanel northAnchor = new JPanel(); // sub-container for timer and buttons

    public GameWindow(String frameName, int frameWidth,int frameHeight,
            int gridWidth, int gridHeight) throws IOException, InvalidHexException  
    {
        super(frameName);

        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;

        northAnchor.setLayout(new GridLayout(2, 1));

        setSize(frameWidth, frameHeight);
        setVisible(true);  

        getContentPane().setBackground(Color.GRAY);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.getRootPane().setBorder(BorderFactory.createMatteBorder(0, 25, 25, 25, Color.DARK_GRAY));

        addGameTimer();

        addButtons(frameWidth, frameHeight);                             

        this.addTileTrays();

        this.applyRotation();

        this.addGameGrid();

        this.add(northAnchor, BorderLayout.NORTH);
        revalidate();                                       
    }

    public void addButtons(int frameWidth, int frameHeight){

        JButton newButton = new JButton("File");
        newButton.setSize(frameWidth / 6, frameHeight / 3);
        newButton.setActionCommand("file");       

        newButton.addActionListener(this);

        JButton resetButton = new JButton("Reset");
        resetButton.setSize(frameWidth / 6, frameHeight / 3);
        resetButton.setActionCommand("reset");    

        resetButton.addActionListener(this);

        // Button to Exit from the program
        JButton exitButton = new JButton("Quit");
        exitButton.setSize(frameWidth / 6, frameHeight / 3);
        exitButton.setActionCommand("exit");      

        // Set this as the buttons ActionListener
        exitButton.addActionListener(this);

        GameButtons gameButtons = new GameButtons(frameWidth, 35, newButton, resetButton, exitButton);               
        northAnchor.add(gameButtons);
        return;
    }

    JLabel time;
    public void addGameTimer() { 
        time = new JLabel();
        time.setSize(10, 40);
        time.setFont(new Font("Times New Roman",Font.PLAIN,20));
        time.setHorizontalAlignment(SwingConstants.CENTER);
        time.setOpaque(true);
        time.setBackground(Color.DARK_GRAY);
        time.setForeground(Color.RED);
        northAnchor.add(time);
    }

    public void calculateTime(long lastPlayedTime) {
        if(lastPlayedTime != 0) elapsedTime = lastPlayedTime; //add last saved time to new elapsedTime        
        timer = new Timer(100, new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                elapsedTime += 100; 
                long seconds = elapsedTime/1000%60;
                long minutes = elapsedTime/60000%60;
                long hours = elapsedTime/3600000;
                time.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
            }
        });        
        if(!timer.isRunning() && lastPlayedTime == 0) timer.start();  
        
        revalidate();
    }

    public String getTime() {
        String labelText = time.getText();  //return text in jlabel (hh:mm:ss)
        return labelText;
    }

    public void resetTime() {
        timer.stop();
        elapsedTime = 0; 
        time.setText(String.format("%02d:%02d:%02d", 0, 0, 0));
    }

    public void resume() { timer.start(); }

    public void pause() { timer.stop(); }

    public void addTileTrays() throws IOException, InvalidHexException
    {
        left = new TileObject[8]; // 17. The term initialize can be used where an object or a concept is established.
        right = new TileObject[8]; // 17. The term initialize can be used where an object or a concept is established.

        try {
            _tileSpecs = new TileSpecs("./input/default.mze");  
        }
        //file doesn't exist
        catch(FileNotFoundException exception) {
            loadFile();
        } 
        catch(Exception exception) {
            exception.printStackTrace();
        }


        ArrayList<ArrayList<Line2D>> lines = _tileSpecs.getFinalCoords();
        for(int i = 0;i<8; i++)
        {
            Point pos = new Point(0,0);
            TileObject templ = new TileObject(i, pos, 100, 100);
            TileObject tempr = new TileObject(i + 8, pos, 100, 100);
            templ.addMouseListener(new MovementHandler());
            tempr.addMouseListener(new MovementHandler());
            templ.setLines(lines.get(i));
            tempr.setLines(lines.get(i + 8));
            left[i] = templ;
            right[i] = tempr;
        }

        this.applyRandomPositioning();

        westTray = new TileTray(200, 900, left);
        eastTray = new TileTray(200, 900, right);

        add(westTray, BorderLayout.WEST);
        add(eastTray, BorderLayout.EAST);
    }

    public void addGameGrid()
    {
        board = new TileObject[16]; // 17. The term initialize can be used where an object or a concept is established.

        for(int i = 0; i < 16; i++)
        {
            Point pos = new Point(0,0);
            TileObject temp = new TileObject(-1, pos, 100, 100); 
            temp.addMouseListener(new MovementHandler());
            temp._iniPosition = i + 16;
            temp.position = i + 16;
            board[i] = temp;
        }

        gameGrid = new GameGrid(400, 400, board);     
        JPanel gridAnchor = new JPanel(); // Container to use GameGrid's defined dimensions

        gridAnchor.setLayout(new GridBagLayout()); // Center GameGrid by GB Layout default    
        gridAnchor.setBackground(Color.DARK_GRAY);

        gridAnchor.add(gameGrid);
        add(gridAnchor, BorderLayout.CENTER);
    }

    /**
     * For the buttons
     * @param e is the ActionEvent
     * 
     * The odd syntax for non-java people is that "exit" for instance is
     * converted to a String object, then that object's equals() method is
     * called.
     */
    public void actionPerformed(ActionEvent e) {
        if("exit".equals(e.getActionCommand()))
        {       	
            if(hasBeenPlayed) {
                pause();
                if(JOptionPane.showConfirmDialog(null, "Would you like to save your game?",
                        "Quit", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    this.saveFile();    
                } 
            }
            System.exit(0);
        }
        if("reset".equals(e.getActionCommand()))
            reset();

        if("file".equals(e.getActionCommand()))
            try{
                file();
            }
        catch(Exception exception) {
            exception.printStackTrace();
        }
    }

    /*
     * function to reset the board using ids as locations
     *  0-7 left tray 8-15 right tray -1s as empty tiles on the board
     * */
    public void reset() {
        resetTime();
        hasBeenPlayed = false;
        //temp array to hold the TileObjects
        TileObject[] temp = new TileObject[32];

        //populate temp with all the tiles
        int i = 0;
        for(;i < 8; ++i) {
            temp[i] = left[i];
        }

        for(int k = 0; k < 8; ++k) {
            temp[i] = right[k];
            ++i;
        }

        for(int j = 0; j < 16; ++j) {
            temp[i] = board[j];
            ++i;
        }

        //sort tiles into proper array
        i = 0;
        for(int b = 0; i < 32; ++i) {
            int pos = temp[i]._iniPosition, id = temp[i].id;
            temp[i].resetRotation();
            if (pos > 15) {
                board[pos - 16] = temp[i];
                board[pos - 16].position = pos;
                ++b;
            } else if (pos > 7){
                right[pos - 8] = temp[i];
                right[pos - 8].position = pos;
            } else {
                left[pos] = temp[i];
                left[pos].position = pos;
            }
        }

        //remake the the board
        westTray = new TileTray(200, 900, left);
        eastTray = new TileTray(200, 900, right);
        gameGrid = new GameGrid(400, 400, board);
        JPanel gridAnchor = new JPanel(); // Container to use GameGrid's defined dimensions

        gridAnchor.setLayout(new GridBagLayout()); // Center GameGrid by GB Layout default    
        gridAnchor.setBackground(Color.DARK_GRAY);

        gridAnchor.add(gameGrid); 

        add(westTray, BorderLayout.WEST);
        add(eastTray, BorderLayout.EAST);
        add(gridAnchor, BorderLayout.CENTER);

        revalidate();
    }

    public void file() throws IOException {
        if(hasBeenPlayed) pause(); 
        int result = JOptionPane.showInternalOptionDialog(null, "Would you like to load or save a file?", "File",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, new Object [] {"Load", "Save", "Cancel"}, null);

        if (result == JOptionPane.YES_OPTION) {
            this.loadFile();              
        }
        else if (result == JOptionPane.NO_OPTION) {
            this.saveFile();
        }
        else {
            if(hasBeenPlayed) resume();                        
        }
    }

    public void applyRotation() {
        Random rand = new Random();
        int zeroRotation = 0, oneRotation = 0, twoRotations = 0, threeRotations = 0;
        for (int i = 0; i < 8; ++i) {
            left[i].repaint();
            right[i].repaint();
            if (zeroRotation < 3) {
                left[i].rotate(rand.nextInt(4));
                right[i].rotate(rand.nextInt(4));
            }
            else {
                left[i].rotate(rand.nextInt(3) + 1);
                right[i].rotate(rand.nextInt(3) + 1);
            }

            if (left[i]._iniRotation == 0) {
                ++zeroRotation;
            } else if (left[i]._iniRotation == 90) {
                ++oneRotation;
            } else if (left[i]._iniRotation == 180) {
                ++twoRotations;
            } else {
                ++threeRotations;
            }

            if (right[i]._iniRotation == 0) {
                ++zeroRotation;
            } else if (right[i]._iniRotation == 90) {
                ++oneRotation;
            } else if (right[i]._iniRotation == 180) {
                ++twoRotations;
            } else {
                ++threeRotations;
            }
        }

        if (oneRotation < 1) {
            for (int i = 0; i < 8 && oneRotation < 1; ++i) {
                if (left[i]._iniRotation == 0) {
                    left[i].rotate(1);
                    ++oneRotation;
                } else if (right[i]._iniRotation == 0) {
                    right[i].rotate(1);
                    ++oneRotation;
                }
            }
        }

        if (twoRotations < 1) {
            for (int i = 0; i < 8 && twoRotations < 1; ++i) {
                if (left[i]._iniRotation == 0) {
                    left[i].rotate(2);
                    ++twoRotations;
                } else if (right[i]._iniRotation == 0) {
                    right[i].rotate(2);
                    ++twoRotations;
                }
            }
        }

        if (threeRotations < 1) {
            for (int i = 0; i < 8 && threeRotations < 1; ++i) {
                if (left[i]._iniRotation == 0) {
                    left[i].rotate(3);
                    ++threeRotations;
                } else if (right[i]._iniRotation == 0) {
                    right[i].rotate(3);
                    ++threeRotations;
                }
            }
        }
    }

    public void applyRandomPositioning() {
        ArrayList<TileObject> temp = new ArrayList<TileObject>();

        for(int i = 0; i < 8; ++i) {
            temp.add(left[i]);
            temp.add(right[i]);
        }

        Collections.shuffle(temp);

        for(int i = 0; i < 8; ++i) {
            left[i] = temp.get(i);
            left[i]._iniPosition = i;
            left[i].position = i;
            right[i] = temp.get(i + 8);
            right[i]._iniPosition = i+8;
            right[i].position = i+8;
        }
    }

    public void loadFile() throws IOException {
        // load part 1
        if(hasBeenPlayed) {
            if(JOptionPane.showConfirmDialog(null, "Would you like to save your current game first?", 
                    "Quit", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                this.saveFile();
            }
        }

        FileDialog fd = new FileDialog(this, "Choose a file to load", FileDialog.LOAD);
        fd.setFile("*.mze");
        fd.setVisible(true);
        String dir = fd.getDirectory();
        String fileName = fd.getFile();
        if(fileName != null) {
            this.load(dir + fileName);
        }else {
            if(hasBeenPlayed) resume();   //no file selected, resume timer.
        }
    }

    public void load(String fileName) throws IOException {
        try{
            _tileSpecs = new TileSpecs(fileName); 

            if (_tileSpecs.getGameState().equals("unplayed")) {
                this.setupUnplayed(_tileSpecs);
                this.applyRotation();
                revalidate();
            }
            else {
                this.setupPlayed(_tileSpecs);                 
            }
            if(hasBeenPlayed) resetTime(); 
        }
        catch(FileNotFoundException exception) {
            loadFile();
        }  
        catch(InvalidHexException exception) {
            //render empty gameWindow

            for(int i = 0;i<8; i++)
            {
                left[i]._iniRotation = 0;
                left[i].id = -1;
                left[i].setLines(new ArrayList<Line2D>());

                right[i].id = -1;
                right[i]._iniRotation = 0;
                right[i].setLines(new ArrayList<Line2D>());

                board[i]._iniPosition = i+16;
                board[i].position = i+16;
                board[i].id = -1;
                board[i]._iniRotation = 0;
                board[i]._rotation = 0;
                board[i].setLines(new ArrayList<Line2D>());

                board[i+8]._iniPosition = i+24;
                board[i+8].position = i+24;
                board[i+8].id = -1;
                board[i+8]._iniRotation = 0;
                board[i+8]._rotation = 0;
                board[i].setLines(new ArrayList<Line2D>());
            }

            westTray = new TileTray(200, 900, left);
            eastTray = new TileTray(200, 900, right);
            gameGrid = new GameGrid(400, 400, board);
            JPanel gridAnchor = new JPanel(); // Container to use GameGrid's defined dimensions

            gridAnchor.setLayout(new GridBagLayout()); // Center GameGrid by GB Layout default    
            gridAnchor.setBackground(Color.DARK_GRAY);

            gridAnchor.add(gameGrid); 

            add(westTray, BorderLayout.WEST);
            add(eastTray, BorderLayout.EAST);
            add(gridAnchor, BorderLayout.CENTER);

            time.setText(String.format("%02d:%02d:%02d", 0, 0, 0));
            revalidate();
        }

    }

    public void setupUnplayed(TileSpecs specs) {
        ArrayList<ArrayList<Line2D>> lines = specs.getFinalCoords();
        resetTime();
        for(int i = 0;i<8; i++)
        {
            //setup board
            //populate left tile tray
            left[i].id = i;
            left[i].setLines(lines.get(i));
            //populate right tile tray
            right[i].id = i+8;
            right[i].setLines(lines.get(i+8));
            //populate lower half of the board
            board[i]._iniPosition = i+16;
            board[i].position = i+16;
            board[i].id = -1;
            board[i].setLines(new ArrayList<Line2D>());
            //populate top half of the board
            board[i+8]._iniPosition = i+24;
            board[i+8].position = i+24;
            board[i+8].id = -1;
            board[i+8].setLines(new ArrayList<Line2D>());
        }

        this.applyRandomPositioning();

        //remake the the board
        westTray = new TileTray(200, 900, left);
        eastTray = new TileTray(200, 900, right);
        gameGrid = new GameGrid(400, 400, board);
        JPanel gridAnchor = new JPanel(); // Container to use GameGrid's defined dimensions

        gridAnchor.setLayout(new GridBagLayout()); // Center GameGrid by GB Layout default    
        gridAnchor.setBackground(Color.DARK_GRAY);

        gridAnchor.add(gameGrid); 

        add(westTray, BorderLayout.WEST);
        add(eastTray, BorderLayout.EAST);
        add(gridAnchor, BorderLayout.CENTER);

        hasBeenPlayed = false;
        revalidate();
    }

    public void setupPlayed(TileSpecs specs) {
                                   
        elapsedTime = specs.getPlayTime();    
        long seconds = elapsedTime/1000%60;
        long minutes = elapsedTime/60000%60;
        long hours = elapsedTime/3600000;
        time.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));//pass saved playTime here   

        specs.setPlayTime(specs.getPlayTime());

        //populate left, right, and board with blank tiles
        for (int i = 0; i < 8; ++i) {
            //populate left tile tray
            left[i]._iniPosition = i;
            left[i].position = i;
            left[i].id = -1;

            //populate right tile tray
            right[i]._iniPosition = i+8;
            right[i].position = i+8;
            right[i].id = -1;
            //populate lower half of the board
            board[i]._iniPosition = i+16;
            board[i].position = i+16;
            board[i].id = -1;
            //populate top half of the board
            board[i+8]._iniPosition = i+24;
            board[i+8].position = i+24;
            board[i+8].id = -1;
        }
        //extract data from tile specs
        int[] positions = specs.getTilePositions(), rotations = specs.getTileRotations();
        ArrayList<ArrayList<Line2D>> lines = specs.getFinalCoords();


        //sort new tiles into their positions
        for(int i = 0; i < 16; ++i) {
            if(positions[i] > 15) { //sort into the board
                board[positions[i] - 16].id = i;
                board[positions[i] - 16]._iniPosition = positions[i];
                board[positions[i] - 16].position = positions[i];
                board[positions[i] - 16].setLines(lines.get(i));
                board[positions[i] - 16].rotate(rotations[i]);
            } 
            else if(positions[i] > 7) { //sort into the right tray
                right[positions[i] - 8].id = i;
                right[positions[i] - 8]._iniPosition = positions[i];
                right[positions[i] - 8].position = positions[i];
                right[positions[i] - 8].setLines(lines.get(i));
                right[positions[i] - 8].rotate(rotations[i]);
            }
            else { //sort into the left tray
                left[positions[i]].id = i;
                left[positions[i]]._iniPosition = positions[i];
                left[positions[i]].position = positions[i];
                left[positions[i]].setLines(lines.get(i));
                left[positions[i]].rotate(rotations[i]);
            }
        }

        //remake the the board
        westTray = new TileTray(200, 900, left);
        eastTray = new TileTray(200, 900, right);
        gameGrid = new GameGrid(400, 400, board);
        JPanel gridAnchor = new JPanel(); // Container to use GameGrid's defined dimensions

        gridAnchor.setLayout(new GridBagLayout()); // Center GameGrid by GB Layout default    
        gridAnchor.setBackground(Color.DARK_GRAY);

        gridAnchor.add(gameGrid); 

        add(westTray, BorderLayout.WEST);
        add(eastTray, BorderLayout.EAST);
        add(gridAnchor, BorderLayout.CENTER);

        hasBeenPlayed = false;
        revalidate();
    }

    public void saveFile() {
        FileDialog fd = new FileDialog(this, "Choose a file to save", FileDialog.SAVE);
        fd.setFile("*.mze");
        fd.setVisible(true);
        String dir = fd.getDirectory();
        String fileName = fd.getFile();
        if(!(fileName == null)) {            
            try {
                File file = new File(dir + fileName);
                if(file.createNewFile()) {
                    this.save(file);
                }
                else {
                    this.save(file);
                }
                hasBeenPlayed = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
          if(hasBeenPlayed) resume(); // No file selected, resume timer.
        }
        // write content
        // call to other function
        // in file created and/or overwrite
    }

    public void save(File file) {
        TileObject[] temp = new TileObject[16];
        int count = 0;
        //grab all the populated tiles
        for (int i = 0; i < 8; ++i) {
            //grab from left tile tray
            if(left[i].id != -1) {
                temp[count] = left[i];
                ++count;
            }
            //grab from right tile tray
            if(right[i].id != -1) {
                temp[count] = right[i];
                ++count;
            }
            //grab from lower half of the board
            if(board[i].id != -1) {
                temp[count] = board[i];
                ++count;
            }
            if(board[i + 8].id != -1) {
                temp[count] = board[i + 8];
                ++count;
            }
        }

        Arrays.sort(temp, new TileSorter());

        TileSpecs mazeSaver = new TileSpecs(temp, hasBeenPlayed);
        mazeSaver.setPlayTime(elapsedTime);
        mazeSaver.writeAllBytes(file);
    }
    
    public int checkWinGame() {

      for(int i = 0; i < board.length; ++i) {
        if((board[i].getLines().size() <= 0) ||
            (board[i]._iniRotation != board[i]._rotation || 
            (board[i].id != (board[i].position - 16)))) {
          return 0;
        }
      }
      // all actual tiles not a placeholder
      // the tiles have correct rotation
      return 1;
    }

    protected class TileSorter implements Comparator<TileObject>{
        //used to sort in ascending order by id
        public int compare(TileObject tileA, TileObject tileB) {
            return tileA.id - tileB.id;
        }
    }

    protected class MovementHandler implements MouseListener
    {
        @Override
        public void mouseClicked(MouseEvent e) {
            TileObject current = (TileObject) e.getSource();

            if (SwingUtilities.isLeftMouseButton(e)) 
            {   
                // if there is a tile selected and the tile 
                //click is empty move selected to the tile position.
                if (selected != null && current.id == -1) { 
                    if(!hasBeenPlayed) {
                        calculateTime(0);
                    }
                    hasBeenPlayed = true;
                    int tmpId = current.id, tmpPosition = current._iniPosition;

                    current.id = selected.id;
                    current._rotation = selected._rotation;
                    current._iniRotation = selected._iniRotation;
                    current._iniPosition = selected._iniPosition;
                    current.setLines(selected.getLines());
                    current.repaint();

                    selected.id = tmpId;
                    selected._iniPosition = tmpPosition;
                    selected._active = false;
                    selected.setLines(new ArrayList<Line2D>());
                    selected.repaint();
                    selected = null;
                }   //else if a tile has been clicked make it selected
                else if (current.id != -1 && selected == null) { 
                    selected = current;
                    selected._active = true;
                    selected.showActive();
                }                
                // if there is a tile selected and the tile click is NOT empty
                // flash red border on tile, return to previous valid selection                   
                else if(current.id != -1 && selected != null) {                    
                    TileObject tmp = selected;
                    selected = current;
                    selected._active = false;
                    selected.showInvalid();                     
                    current.repaint();                     
                    selected = null;
                    tmp.repaint();
                }
            }
            else if (SwingUtilities.isRightMouseButton(e)) {
                current.rotate();
            }
            // On button press after it has been processed
            if(checkWinGame() == 1) {
              String[] options = {"Start new game", "Load game", "Quit"};
              int x = JOptionPane.showOptionDialog(null, String.format("Final time: %d:%d:%d", elapsedTime/3600000, elapsedTime/60000%60, elapsedTime/1000%60), "Congratulations, you won!", 
                  JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[2]);
              switch (x) {
              case 0: // start new game
                try {
                    load("./input/default.mze");
                } catch (IOException e1) {
                    if(JOptionPane.showInternalOptionDialog(null, "An unforseen error occured.", "Error",
                            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                            null, new Object [] { "Quit" }, null) == JOptionPane.YES_OPTION)
                    {
                        System.exit(1);
                    }
                }
                break;
              case 1: // load game
                try {
                    loadFile();
                } catch (IOException e1) {
                    if(JOptionPane.showInternalOptionDialog(null, "An unforseen error occured.", "Error",
                            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                            null, new Object [] { "Quit" }, null) == JOptionPane.YES_OPTION)
                    {
                        System.exit(1);
                    }
                }
                break;
              case 2: // Quit
              default: //Quit
                System.exit(0);
                break;
              }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            // Not needed currently   
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            // Not needed currently
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        } 
    }
};
