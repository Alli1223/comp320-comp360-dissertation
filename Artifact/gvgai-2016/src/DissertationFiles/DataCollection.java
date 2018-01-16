package DissertationFiles;

import core.game.StateObservation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import tools.Vector2d;

import java.io.PrintWriter;

//! The goal of this class is to gather the interesting data of a game
public class DataCollection
{
    //
    private String outputLocation = "./gameLogs/JSONData/gameData.txt";
    public JSONObject AllData = new JSONObject();
    public JSONObject GameData = new JSONObject();
    public JSONArray PlayerPositions = new JSONArray();


    public void AddGameStateToCollection(StateObservation SO)
    {
        //! Add player position to all data
            AddPlayerPosition(SO);



    }

    //! Run this function at the end of the game to record the end game stats
    // GameScore, Death location, Win location
    public void AddGameEndStats(StateObservation SO)
    {
        // IF the
        if(SO.isAvatarAlive())
            GameData.put("LastLocation", ConvertPositionToJSON(SO.getAvatarPosition()));
        else
            GameData.put("DeathLocation", ConvertPositionToJSON(SO.getAvatarPosition()));
        GameData.put("GameScore", SO.getGameScore());
        GameData.put("GameTick", SO.getGameTick());
        //GameData.put(SO.toString())
        AllData.put("GameData", GameData);

        //Write the data
        System.out.println(AllData.toString());
        SaveDataToFile(AllData);
    }


    public void AddPlayerPosition(StateObservation SO)
    {
        System.out.println(SO.toString());
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

}
