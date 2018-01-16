package DissertationFiles;

import core.game.StateObservation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

//! The goal of this class is to gather the interesting data of a game
public class DataCollection
{
    //
    public JSONArray AllData = new JSONArray();
    public JSONArray PlayerPositions = new JSONArray();

    public void InitalizeDataCollection()
    {

    }


    public void AddGameStateToCollection(StateObservation SO)
    {
        JSONObject SOObject = new JSONObject();
        try
        {
            SOObject.put("PlayerPositions", SO.getAvatarPosition());
            PlayerPositions.put(SOObject);
        } catch (JSONException e)
        {
            System.out.println("Error adding to player positions");
        }
    }

    public void SaveDataToFile(JSONObject gameData)
    {
        
    }


}
