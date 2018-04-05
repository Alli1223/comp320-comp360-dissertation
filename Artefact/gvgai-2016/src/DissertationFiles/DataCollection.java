package DissertationFiles;
import core.game.Game;
import core.game.Observation;
import core.game.StateObservation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import tools.Vector2d;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


//! The goal of this class is to gather the interesting data of a game
public class DataCollection
{
    // Singleton
    private static DataCollection dataCollection = new DataCollection();

    // Get the data collection instance
    public static DataCollection getInstance()
    {
        return dataCollection;
    }

    // Output location
    private String outputLocation = "../R/Data/";
    // Json object containing all the data
    public JSONObject AllData = new JSONObject();
    // The game data to be inserted into AllData
    public JSONObject GameData = new JSONObject();
    // The Player Positions to be inserted into GameData
    public JSONArray AvatarPositions = new JSONArray();
    // The Player Positions to be inserted into GameData
    public JSONArray AgentScore = new JSONArray();
    // A vector of positions that the agent has been at
    public ArrayList<Vector2d> listOfAgentLccations = new ArrayList<Vector2d>(); //Max game time is 2000 ticks
    // iteration is incremented when the game tick is 0, and used to record the level that is being run
    public int gameIteration = 0;
    public int levelIteration = 0;
    private int localGameIterator = -1;
    private boolean recordPositionsOverMultipleGames = true;
    public int totalCellsExplored = 0;
    // An Int to store the number of cells that have been explored
    private int cellsExplored = 0;
    // Game name for saving the data correctly
    public String ControllerName;
    //Set the block size for the game
    private int blockSize;

    //! Save the final image
    public Boolean renderGames = false;

    public int getBlockSize()
    {
        return blockSize;
    }

    // History of points and how many times they were visited
    public ConcurrentHashMap<String, Integer> cellsVisited = new ConcurrentHashMap<String, Integer>();


    // Run this function every frame to get the players position and other data
    public void AddGameStateToCollection(StateObservation SO)
    {
        dataCollection.blockSize = SO.getBlockSize();
        Vector2d playerPosition = SO.getAvatarPosition();
        //! Add player position to all data
        if (playerPosition != null)
            AddPlayerPosition(SO);

        String point;
        // Add to list of positions if it doesnt exist (new position)
        if (!dataCollection.listOfAgentLccations.contains(playerPosition))
        {
            dataCollection.cellsExplored++;
            dataCollection.listOfAgentLccations.add(playerPosition);
            point = playerPosition.toString();
            dataCollection.cellsVisited.put(point, 0);
        }
        dataCollection.totalCellsExplored++;

        // Update points visited
        point = playerPosition.toString();
        if (dataCollection.cellsVisited.containsKey(point) && point != null)
        {
            // Get and update the position history of that element
            int test = dataCollection.cellsVisited.get(point);
            test++;
            dataCollection.cellsVisited.replace(point, test);
        }

        // Add score to list
        //dataCollection.AgentScore.put(SO.getGameScore());
    }


    //! Run this function at the end of the game to record the end game stats
    // GameScore, Death location, Win location
    public void AddGameEndStats(StateObservation SO)
    {
        // if (SO.isAvatarAlive())
        //     GameData.put("LastLocation", ConvertPositionToJSON(SO.getAvatarPosition()));
        // else
        //     GameData.put("DeathLocation", ConvertPositionToJSON(SO.getAvatarPosition()));
        GameData.put("ControllerName", dataCollection.ControllerName);
        GameData.put("GameScore", SO.getGameScore());
        GameData.put("GameSpaceSearched", calculatePercentageOfExploredLevel(SO));                                      // Calculate search space
        GameData.put("TotalCellsVisisted", dataCollection.cellsExplored);
        GameData.put("EndGameTick", SO.getGameTick());
        GameData.put("levelSize", SO.getWorldDimension().width * SO.getWorldDimension().height);

        // Add the values to allData json object
        dataCollection.AllData.put("GameData_" + dataCollection.gameIteration + "_" + dataCollection.levelIteration, GameData);

        // Save game points visted to file
        CaptureScreen(SO);

        // If the level has changed
        if(dataCollection.localGameIterator != dataCollection.gameIteration && recordPositionsOverMultipleGames)
        {
            // Reset the necessary data
            dataCollection.localGameIterator = dataCollection.gameIteration;
            dataCollection.cellsVisited.clear();
            dataCollection.listOfAgentLccations.clear();
            dataCollection.totalCellsExplored = 0;
            dataCollection.levelIteration = 0;
            dataCollection.cellsExplored = 0;
        }
        else
        {
            dataCollection.levelIteration++;
        }



    }

    //! Covert the JSON to a CSV file
    // TODO:FIX THIS
    public void JSONARRAYToCSV(JSONArray jsonArray)
    {
        /*
        JSONObject scores = new JSONObject();
        scores.put("Scores", jsonArray);
        String jsonString = scores.toString();

        JSONObject output;
        try {
            output = new JSONObject(jsonArray.toString());


            JSONArray docs = output.getJSONArray("Scores");

            String csv = CDL.toString(docs);

            SaveDataToFile(csv, true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        */
    }


    // Add player positions to the state observation
    private void AddPlayerPosition(StateObservation SO)
    {
        try
        {
            // Add player positions to the pos object
            dataCollection.AvatarPositions.put(ConvertPositionToJSON(SO.getAvatarPosition()));


            // Add playerPosition objects
            //dataCollection.AllData.put("AvatarPositions" + dataCollection.gameIteration, dataCollection.AvatarPositions);
            //dataCollection.AllData.put("AvatarPositions", dataCollection.AvatarPositions);

        } catch (JSONException e)
        {
            System.out.println("Error adding to player positions");
        }
    }


    //! This code writes the JSON data to a text file in the gameLogs Directory
    public void SaveDataToFile(String data, boolean isCSVFile)
    {
        try
        {
            if(isCSVFile)
            {
                PrintWriter writer = new PrintWriter(outputLocation + "gameData_" + dataCollection.ControllerName + ".csv", "UTF-8");
                writer.println(data);
                writer.close();
            }
            else
            {
                PrintWriter writer = new PrintWriter(outputLocation + "gameData_" + dataCollection.ControllerName + ".txt", "UTF-8");
                writer.println(data);
                writer.close();
            }

        } catch (Exception e)
        {
            System.out.println("Error writing data file: " + e);
        }
    }

    //! Retrun a json object containing the x and y from a vector2D position
    private JSONObject ConvertPositionToJSON(Vector2d position)
    {
        // Create an json object and put two new json objects within that
        JSONObject ret = new JSONObject();
        JSONObject XPos = new JSONObject();
        JSONObject YPos = new JSONObject();
        JSONArray pos = new JSONArray();

        // Create and array of x and y positions
        XPos.put("X", position.x);
        YPos.put("Y", position.y);
        pos.put(XPos);
        pos.put(YPos);
        ret.put("Pos", position.toString()); // old: ret.put("Pos", pos);
        return ret;
    }


    //! This function calculates the area in which the controller explored
    private double calculatePercentageOfExploredLevel(StateObservation SO)
    {
        //Get the grid to calcualate map size
        ArrayList<Observation> grid[][] = SO.getObservationGrid();

        double percent = 0;

        // Get the map size negative the immovable positions
        int mapSize = grid.length * grid[0].length;
        percent = (double) dataCollection.cellsExplored / (double) mapSize;
        System.out.println("Percentage of level explored: " + percent * 100.0);
        return percent * 100;
    }

    //! Capture the screen
    private void CaptureScreen(StateObservation SO)
    {
        if(renderGames)
        {
            try
            {
                //Create a rect to capture
                Rectangle screenRect = new Rectangle(SO.getWorldDimension());
                // Set the position of the capture position to cut out the window headder bar
                screenRect.y += 30;
                screenRect.x += 10;

                //Save and write image
                BufferedImage capture = new Robot().createScreenCapture(screenRect);
                ImageIO.write(capture, "bmp", new File("../R/Data/ScreenCapture/LastFrame_" + dataCollection.ControllerName + "_" + dataCollection.gameIteration + "_" + dataCollection.levelIteration + ".jpg"));
            } catch (Exception e)
            {
                System.out.println("Error in saving image to file: " + e);
            }
        }
    }

    //! Seralise the string (NOT USED)
    private String serializeString(String str)
    {

        String returnString = "NULL";
        try
        {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream so = new ObjectOutputStream(bo);
            so.writeObject(str);
            //so.flush();
            return bo.toString();
        } catch (Exception e)
        {
            System.out.println("Error serializingString: " + e);
        }
        return returnString;
    }
}
