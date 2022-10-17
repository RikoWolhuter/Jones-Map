package com.example.jonesmap;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JsonParser {
    private HashMap<String, String> parseJsonObject(JSONObject object) {
        HashMap<String, String> datalist = new HashMap<>();
        try {
            //get name from object
            String name = object.getString("name");

            //Get Latitude from object
            String latitude = object.getJSONObject("geometry")
                    .getJSONObject("location").getString("lat");
            //get longitude from object
            String longitude = object.getJSONObject("geometry")
                    .getJSONObject("location").getString("lng");
            datalist.put("name", name);
            datalist.put("lng", longitude);
            datalist.put("lat", latitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return datalist;
    }
    private  List<HashMap<String, String>>parseJsonArray(JSONArray jsonArray){
        //initialize hash map list
        List<HashMap<String, String>> datalist= new ArrayList<>();
        for(int i = 0; i< jsonArray.length(); i++){
            try{
                HashMap<String, String> data = parseJsonObject((JSONObject) jsonArray.get(i));
                //add data in hash map list
                datalist.add(data);
            }catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
        return datalist;
    }
    List<HashMap<String, String>> parseResult(JSONObject object){
        //initialize json array
        JSONArray jsonArray =null;

            try{
                jsonArray =object.getJSONArray("results");
            }catch (JSONException e)
            {
                e.printStackTrace();
            }

        return parseJsonArray(jsonArray);
    }

}

