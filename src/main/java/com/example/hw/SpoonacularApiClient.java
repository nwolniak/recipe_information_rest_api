package com.example.hw;

import com.google.gson.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;


public class SpoonacularApiClient {
    private Client client;
    private WebTarget target;

    public SpoonacularApiClient() {
        this.client = ClientBuilder.newClient();
        this.target = client.target(getBaseURI());
    }

    public int getRecipeIdByDishName(String dishName) {
        try {
            Response response = this.target.path("recipes/complexSearch")
                    .queryParam("query", dishName)
                    .queryParam("number", 1)
                    .queryParam("apiKey", "")
                    .request()
                    .header("Content-type", MediaType.APPLICATION_JSON)
                    .get();


            if (response.getStatus() != 200) {
                throw new RuntimeException("Failed getRecipeIdByDishName: HTTP error code : " + response.getStatus());
            }


            String jsonString = response.readEntity(String.class);
            JsonObject obj = JsonParser.parseString(jsonString).getAsJsonObject();
            JsonArray results = obj.getAsJsonArray("results");
            JsonObject firstResult = results.get(0).getAsJsonObject();

            int recipeId = firstResult.get("id").getAsInt();

            return recipeId;
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            return -1;
        }
    }

    public Recipe getRecipeById(int recipeId) {
        try {
            Response response = this.target.path("recipes/" + recipeId + "/information")
                    .queryParam("includeNutrition", "false")
                    .queryParam("apiKey", "")
                    .request()
                    .header("Content-type", MediaType.APPLICATION_JSON)
                    .get();


            if (response.getStatus() != 200) {
                throw new RuntimeException("Failed getRecipeById: HTTP error code : " + response.getStatus());
            }


            String jsonString = response.readEntity(String.class);
            Gson g = new Gson();
            Recipe recipe = g.fromJson(jsonString, Recipe.class);

            return recipe;
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }



    private static URI getBaseURI() {
        return UriBuilder.fromUri("https://api.spoonacular.com").build();
    }


}
