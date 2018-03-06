package DissertationFiles;
import core.game.Observation;
import core.game.StateObservation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import tools.Pair;
import tools.Vector2d;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


//! The goal of this class is to gather the interesting data of a game
public class DataCollection
{
    // Singleton
    private static DataCollection dataCollection = new DataCollection();

    public static DataCollection getInstance()
    {
        return dataCollection;
    }

    // Output location
    private String outputLocation = "../R/Data/gameData.txt";
    // Json object containing all the data
    public JSONObject AllData = new JSONObject();
    // The game data to be inserted into AllData
    public JSONObject GameData = new JSONObject();
    // The Player Positions to be inserted into GameData
    public JSONArray PlayerPositions = new JSONArray();
    // A vector of positions that the agent has been at
    public ArrayList<Vector2d> listOfAgentLccations = new ArrayList<Vector2d>(); //Max game time is 2000 ticks
    // iteration is incremented when the game tick is 0, and used to record the level that is being run
    private int levelIteration = 0;
    // An Int to store the number of cells that have been explored
    private int cellsExplored = 0;

    private HashMap<JSONObject, Integer> PositionHistory = new HashMap<JSONObject, Integer>();


    // Run this function every frame to get the players position and other data
    public void AddGameStateToCollection(StateObservation SO)
    {
        //! Add player position to all data
        if (SO.getAvatarPosition() != null)
            AddPlayerPosition(SO);

        // Add to list of positions if it doesnt exist
        if (!dataCollection.listOfAgentLccations.contains(SO.getAvatarPosition()))
        {
            cellsExplored++;
            dataCollection.listOfAgentLccations.add(SO.getAvatarPosition());
            JSONObject newJson = new JSONObject(ConvertPositionToJSON(SO.getAvatarPosition()));
            PositionHistory.put(newJson, 0);
        }
        else
        {
            if(PositionHistory.get(ConvertPositionToJSON(SO.getAvatarPosition())) != null)
            {
                int test = PositionHistory.get(ConvertPositionToJSON(SO.getAvatarPosition()));
            }

        }


        Vector2d pos = new Vector2d(SO.getAvatarPosition());
    }


    //! Run this function at the end of the game to record the end game stats
    // GameScore, Death location, Win location
    public void AddGameEndStats(StateObservation SO)
    {
        if (SO.isAvatarAlive())
            GameData.put("LastLocation", ConvertPositionToJSON(SO.getAvatarPosition()));
        else
            GameData.put("DeathLocation", ConvertPositionToJSON(SO.getAvatarPosition()));
        GameData.put("GameScore", SO.getGameScore());
        GameData.put("GameSpaceSearched", calculatePercentageOfExploredLevel(SO));                                      // Calculate search space
        GameData.put("GameTick", SO.getGameTick());
        GameData.put("AvatarType", SO.getAvatarType());

        // Add the values to allData json object
        dataCollection.AllData.put("GameData", GameData);

        // Save game points visted to file
        CaptureScreen(SO);

        //Write the data
        System.out.println(AllData.toString());
        //SaveDataToFile(AllData);
    }


    // Add player positions to the state observation
    public void AddPlayerPosition(StateObservation SO)
    {
        try
        {
            // Add player positions to the pos object
            dataCollection.PlayerPositions.put(ConvertPositionToJSON(SO.getAvatarPosition()));

            //int timesVisited = PositionHistory.get(ConvertPositionToJSON(SO.getAvatarPosition()));
            PositionHistory.put(ConvertPositionToJSON(SO.getAvatarPosition()), 1);
            // Add that to the list of positions
            dataCollection.AllData.put("PlayerPositions" + dataCollection.levelIteration, dataCollection.PlayerPositions);
            //dataCollection.AllData.put("PlayerPositions", dataCollection.PlayerPositions);

            // IF the game tick is 0 then increment the game counter
            if (SO.getGameTick() == 0)
            {
                dataCollection.levelIteration++;
            }
        } catch (JSONException e)
        {
            System.out.println("Error adding to player positions");
        }
    }


    //! This code writes the JSON data to a text file in the gameLogs Directory
    public void SaveDataToFile(JSONObject gameData)
    {
        try
        {
            PrintWriter writer = new PrintWriter(outputLocation, "UTF-8");
            writer.println(AllData.toString());
            writer.close();
        } catch (Exception e)
        {
            System.out.println("Error writing json file: " + e);
        }
    }

    //! Retrun a json object containing the x and y from a vector2D position
    private JSONObject ConvertPositionToJSON(Vector2d position)
    {
        JSONObject ret = new JSONObject();
        JSONObject XPos = new JSONObject();
        JSONObject YPos = new JSONObject();
        JSONArray pos = new JSONArray();

        // Create and array of x and y positions
        XPos.put("X", position.x);
        YPos.put("Y", position.y);
        pos.put(XPos);
        pos.put(YPos);
        ret.put("Pos", pos);
        return ret;
    }


    //! This function calculates the area in which the controller explored
    //TODO: Remove the imovable cells from the equation
    private double calculatePercentageOfExploredLevel(StateObservation SO)
    {
        ArrayList<Observation> grid[][] = SO.getObservationGrid();
        ArrayList<Observation> obs[] = SO.getFromAvatarSpritesPositions();


        int cellsUnexplored = 0;

        double percent = 0;

        int mapSize = grid.length * grid[0].length;
        percent = (double) cellsExplored / (double) mapSize;
        System.out.println("Percentage of level explored: " + percent * 100.0);
        return percent;
    }

    private void CaptureScreen(StateObservation SO)
    {
        try
        {
            Rectangle screenRect = new Rectangle(SO.getWorldDimension());
            BufferedImage capture = new Robot().createScreenCapture(screenRect);
            ImageIO.write(capture, "bmp", new File("TestIamge.jpg"));
        }
        catch (Exception e)
        {
            System.out.println("Error in saving image to file: " + e);
        }
    }
}
