package DissertationFiles;

import core.game.StateObservation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.PrintWriter;

//! The goal of this class is to gather the interesting data of a game
public class DataCollection
{
    //
    private String outputLocation = "./gameLogs/JSONData/gameData.txt";
    public JSONObject AllData = new JSONObject();
    public JSONArray PlayerPositions = new JSONArray();

    public void InitalizeDataCollection()
    {

    }


    public void AddGameStateToCollection(StateObservation SO)
    {
        JSONObject SOObject = new JSONObject();
        JSONArray pos = new JSONArray();
        try
        {
            SOObject.put("Pos", SO.getAvatarPosition());
            PlayerPositions.put(SOObject);
            AllData.put("PlayerPositions", PlayerPositions);
        } catch (JSONException e)
        {
            System.out.println("Error adding to player positions");
        }
        System.out.println(AllData.toString());
        SaveDataToFile(AllData);
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




}
