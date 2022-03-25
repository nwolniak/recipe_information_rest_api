package com.example.hw;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.concurrent.Callable;

public class EdamamFoodApiNutrientsCallable implements Callable<Nutrients> {
    private final String ingredientName;

    public EdamamFoodApiNutrientsCallable(String ingredientName) {
        this.ingredientName = ingredientName;
    }


    @Override
    public Nutrients call() throws Exception {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(getBaseURI());

        try {
            Response response = target
                    .path("api")
                    .path("food-database")
                    .path("v2")
                    .path("parser")
                    .queryParam("app_id", "097dcf73")
                    .queryParam("app_key", "")
                    .queryParam("ingr", ingredientName)
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .get();


            if (response.getStatus() != 200) {
                throw new RuntimeException("Failed getNutrients: HTTP error code : " + response.getStatus());
            }


            String jsonString = response.readEntity(String.class);

            JsonObject obj = JsonParser.parseString(jsonString).getAsJsonObject();
            JsonArray parsedArray = obj.getAsJsonArray("parsed");
            JsonObject firstElement = parsedArray.get(0).getAsJsonObject();
            JsonObject foodObj = firstElement.getAsJsonObject("food");
            JsonObject nutrientsObj = foodObj.getAsJsonObject("nutrients");


            Gson g = new Gson();
            Nutrients nutrients = g.fromJson(nutrientsObj.toString(), Nutrients.class);

            return nutrients;
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }


    private static URI getBaseURI() {
        return UriBuilder.fromUri("https://api.edamam.com").build();
    }
}
