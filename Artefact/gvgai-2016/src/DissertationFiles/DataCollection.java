package DissertationFiles;

import core.game.Observation;
import core.game.StateObservation;
import levelGenerators.constructiveLevelGenerator.LevelData;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import tools.GameAnalyzer;
import tools.LevelMapping;
import tools.Vector2d;

import java.io.PrintWriter;
import java.util.ArrayList;

//! The goal of this class is to gather the interesting data of a game
public class DataCollection
{
    //
    private String outputLocation = "../R/Data/gameData.txt";
    public JSONObject AllData = new JSONObject();
    public JSONObject GameData = new JSONObject();
    public JSONArray PlayerPositions = new JSONArray();

    private int posIterator = 0;
    private ArrayList<Vector2d> _listOfPlayerLccations = new ArrayList<Vector2d>();


    // Run this function every frame to get the players position and other data
    public void AddGameStateToCollection(StateObservation SO)
    {
        //! Add player position to all data
            AddPlayerPosition(SO);

            // Add to list of positions
            for(int i = 0; i < _listOfPlayerLccations.size(); i++)
            {
                if(SO.getAvatarPosition() != _listOfPlayerLccations.get(i))
                {
                    _listOfPlayerLccations.set(i,  SO.getAvatarPosition());
                    posIterator++;
                }
            }
    }

    //! Run this function at the end of the game to record the end game stats
    // GameScore, Death location, Win location
    public void AddGameEndStats(StateObservation SO)
    {
        calculatePercentageOfExploredLevel(SO);

        if(SO.isAvatarAlive())
            GameData.put("LastLocation", ConvertPositionToJSON(SO.getAvatarPosition()));
        else
            GameData.put("DeathLocation", ConvertPositionToJSON(SO.getAvatarPosition()));
        GameData.put("GameScore", SO.getGameScore());
        GameData.put("GameTick", SO.getGameTick());
        GameData.put("AvatarType", SO.getAvatarType());
        AllData.put("GameData", GameData);

        //Write the data
        System.out.println(AllData.toString());
        SaveDataToFile(AllData);
    }


    public void AddPlayerPosition(StateObservation SO)
    {
        try
        {
            // Add player positions to the pos object
            PlayerPositions.put(ConvertPositionToJSON(SO.getAvatarPosition()));
            // Add that to the
            AllData.put("PlayerPositions", PlayerPositions);
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
        }
        catch (Exception e)
        {

        }
    }

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

    private int calculatePercentageOfExploredLevel(StateObservation SO)
    {
        ArrayList<Observation> grid[][] = SO.getObservationGrid();
        ArrayList<Observation> obs[] = SO.getFromAvatarSpritesPositions();


        int cellsExplored = 0;
        int cellsUnexplored = 0;
        int ret;

        // Loop through the grid and check each cell to see if the player has been there
        for(int x = 0; x < grid.length; x++)
        {
            for(int y = 0; y < grid[x].length; y++)
            {

                for(int i = 0; i < _listOfPlayerLccations.size(); i++)
                {
                    if(grid[x][y].get(i).category != 6)  // Catagory 6 is static (wall) -- See Types.java class
                    if(_listOfPlayerLccations.get(i).x == x && _listOfPlayerLccations.get(i).y == y)
                    {
                        cellsExplored++;
                    }

                }
            }
        }

        int mapSize = grid.length * grid[0].length;
        ret = mapSize - cellsExplored;
        return ret;
    }
}
