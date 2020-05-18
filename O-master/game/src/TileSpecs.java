/**@author Ben Wabschall
 * @author Tristan Redding
 * @author Mathew Floyd
 * @author Yun Chi Leong
 * @author Bryce Ostrem
 * 
 *    This class is passed a raw byte file and converts values  
 *     based on index to integer and float, where floats are line coordinates to be drawn 
 *     in paintComponent
 *     
 *     pass in desired file from GameWindow.addTileTrays() by adding new instance of TileSpecs class
 */
import java.util.ArrayList;
import java.io.*;

import javax.swing.JOptionPane;

import java.awt.geom.Line2D;

public class TileSpecs {
    private String _fileName;    
    // saved game "played" (1st four bytes: 0xca, 0xfe, 0xde, 0xed)
    // new game "un-played" (1st four bytes: 0xca, 0xfe, 0xbe, 0xef)
    private String _hexValue; 
    private String _gameState; //client view of hexValue  

    private int _numTiles;

    private long _playTime;

    private int[] _tilePositions = new int[16];
    private int[] _tileRotations = new int[16];
    private int[] _numLines;

    private ArrayList<Line2D> _endPoints;
    private float[] _xyCoords;

    //holds the final set of coordinates for the lines 
    private ArrayList<ArrayList<Line2D>>_finalCoords 
    = new ArrayList<ArrayList<Line2D>>();

    public TileSpecs() {
        //default constructor to give access to write all bytes function.
    }

    public TileSpecs (String inputFile) throws IOException, InvalidHexException {
        File file = new File(inputFile);       

        if(file.exists()) {
            setFileName(inputFile);
            readAllBytes(); 
            if(_gameState == "UNDEFINED") {
                JOptionPane.showMessageDialog(null, "Save file has been corrupted.", 
                        "File I/O Error", JOptionPane.ERROR_MESSAGE);
                throw new InvalidHexException();
            }
        }
        else {          
            //pop up window with error message then prompt for new file
            JOptionPane.showMessageDialog(null, ""+file+" was not found.", 
                    "File I/O Error", JOptionPane.ERROR_MESSAGE);
            throw new IOException();
        }
    }

    public TileSpecs (TileObject[] tileCollection, boolean hasBeenPlayed) {
        //set gameState
        if (hasBeenPlayed) {
            _gameState = "played";
        }
        else {
            _gameState = "unplayed";
        }

        _numTiles = 16;
        _numLines = new int[16];

        //populate TileSpecs data
        for (int i = 0; i < 16; ++i) {
            _tilePositions[i] = tileCollection[i].position;
            _tileRotations[i] = tileCollection[i]._rotation / 90;
            _finalCoords.add(tileCollection[i].getLines());
            _numLines[i] = _finalCoords.get(i).size();
        }
    }


    public void readAllBytes() throws InvalidHexException {
        try (InputStream inputStream = new FileInputStream(_fileName);) {

            //we are working with 4 bytes at a time
            byte[] fourbytes = new byte[4];
            
            //we need 8 bytes for the timer value
            byte[] eightbytes = new byte[8];

            inputStream.read(fourbytes);

            //first value dictates a whether
            //this is a new or saved game
            _hexValue = Converter.convertToHex(fourbytes);
            hexComparator(_hexValue);    
            if(_gameState == "UNDEFINED") return;

            inputStream.read(fourbytes);
            //total number of tiles at play
            _numTiles = Converter.convertToInt(fourbytes);    

            //if a played game save the playTime
            //otherwise set playTime to 0 
            inputStream.read(eightbytes);
            if(_gameState == "played") {                      
                _playTime = Converter.convertToLong(eightbytes);
            }
            else {
                _playTime = 0;
            }

            _numLines = new int[_numTiles];             
            for(int i = 0; i < _numTiles; i++) {                

                //the tiles current position
                if(_gameState == "played") {
                    inputStream.read(fourbytes);
                    _tilePositions[i] = Converter.convertToInt(fourbytes);                                    

                    //the tiles current rotation
                    inputStream.read(fourbytes);
                    _tileRotations[i] = Converter.convertToInt(fourbytes);
                }
                else { 
                    inputStream.read(eightbytes);
                }

                //number of lines per tile
                inputStream.read(fourbytes);
                _numLines[i] = Converter.convertToInt(fourbytes);

                //set up 2D array for set of float end points
                _endPoints = new ArrayList<Line2D>(_numLines[i]);

                for(int j = 0; j < _numLines[i]; j++) {
                    //clear out array for next set of bytes
                    _xyCoords = new float[4];

                    for(int k = 0; k < 4; k++) {
                        inputStream.read(fourbytes);
                        _xyCoords[k] = Converter.convertToFloat(fourbytes);
                    }
                    float x0 = _xyCoords[0];
                    float y0 = _xyCoords[1];
                    float x1 = _xyCoords[2];
                    float y1 = _xyCoords[3];

                    _endPoints.add(new Line2D.Float(x0, y0, x1, y1));
                }
                _finalCoords.add(_endPoints);
            }
            inputStream.close();

        }   
        catch(IOException exception) {
            exception.printStackTrace();
        }
    }

    public void writeAllBytes(File saveFile) {
        float[] fourbytesCoords = new float[4];
        boolean hasBeenPlayed = _gameState.equals("played");

        try (OutputStream outputStream = new FileOutputStream(saveFile);){
            byte[] fourbytes = new byte[4];
            byte[] eightbytes = new byte[8];

            if (hasBeenPlayed) {
                //mark as "played game"            
                fourbytes = Converter.decodeHexString("cafedeed");
                outputStream.write(fourbytes);
            }
            else {
                fourbytes = Converter.decodeHexString("cafebeef");
                outputStream.write(fourbytes);
            }            

            //save the number tiles
            fourbytes = Converter.intToByte(_numTiles);
            outputStream.write(fourbytes);

            //save accrued play time
            eightbytes = Converter.longToByte(_playTime);
            outputStream.write(eightbytes);
            
            for(int i = 0; i < _numTiles; i++) {
                //save the tiles Position
                fourbytes = Converter.intToByte(_tilePositions[i]);
                outputStream.write(fourbytes);

                //save the tiles rotation
                fourbytes = Converter.intToByte(_tileRotations[i]);
                outputStream.write(fourbytes);

                //save the number lines for each tile ID
                fourbytes = Converter.intToByte(_numLines[i]);
                outputStream.write(fourbytes);                

                //write out the line coords and
                //save the endpoints for ea tile

                for(Line2D line : _finalCoords.get(i)) {
                    float x0 = (float) line.getX1();
                    float y0 = (float) line.getY1();
                    float x1 = (float) line.getX2();
                    float y1 = (float) line.getY2();

                    fourbytesCoords[0] = x0;
                    fourbytesCoords[1] = y0;
                    fourbytesCoords[2] = x1;
                    fourbytesCoords[3] = y1;

                    for(int j = 0; j < 4; j++) {
                        outputStream.write(Converter.floatToByte(fourbytesCoords[j]));
                    }
                }
            }
            outputStream.close();                       
        }

        catch(IOException exception) {
            exception.printStackTrace();
        }
    }


    private void hexComparator(String hex) {
        if (hex.equals("cafedeed")) {
            _gameState = "played";
        }
        else if (hex.equals("cafebeef")) {
            _gameState = "unplayed";
        }         
        else {
            _gameState = "UNDEFINED";           
        }
    }

    public void setFileName(String inputFile){
        _fileName = inputFile;
    }

    // pass in new gameState from TileObject
    // should be used with saving operation
    public void setHexValue(String gameState){
        if(gameState == "played") {
            this._hexValue = "0xcafedeed"; 
        }
        else if(gameState == "unplayed") {
            this._hexValue = "0xcafebeef"; 
        }
    }   

    public void setPlayTime(long time) {
        _playTime = time;
    }

    //client view of hexValue
    public String getGameState() {        
        return _gameState;
    }

    protected String getHexValue() {        
        return _hexValue;
    }

    //convert to milliseconds
    public long getPlayTime() {
        return _playTime;
    }

    public int getNumTiles() {
        return _numTiles;
    }

    public int[] getTilePositions() {
        return _tilePositions;
    }

    public int[] getTileRotations() {
        return _tileRotations;
    }

    public int[] getNumLines() {
        return _numLines;
    }   

    public ArrayList<ArrayList<Line2D>> getFinalCoords() {
        return _finalCoords;
    }
};