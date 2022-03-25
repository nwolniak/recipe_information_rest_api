package com.example.hw;


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

public class SpoonacularApiConvertionToGramsCallable implements Callable<Double> {
    private final String ingredientName;
    private final double sourceAmount;
    private final String sourceUnit;


    public SpoonacularApiConvertionToGramsCallable(String ingredientName, double sourceAmount, String sourceUnit) {
        this.ingredientName = ingredientName;
        this.sourceAmount = sourceAmount;
        this.sourceUnit = sourceUnit;
    }


    @Override
    public Double call() throws Exception {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(getBaseURI());

        try {
            Response response = target
                    .path("recipes")
                    .path("convert")
                    .queryParam("ingredientName", this.ingredientName)
                    .queryParam("sourceAmount", this.sourceAmount)
                    .queryParam("sourceUnit", this.sourceUnit)
                    .queryParam("targetUnit", "grams")
                    .queryParam("apiKey", "")
                    .request()
                    .header("Content-type", MediaType.APPLICATION_JSON)
                    .get();


            if (response.getStatus() != 200) {
                throw new RuntimeException("Failed convertToGrams: HTTP error code : " + response.getStatus());
            }


            String jsonString = response.readEntity(String.class);

            JsonObject obj = JsonParser.parseString(jsonString).getAsJsonObject();
            double targetAmount = obj.get("targetAmount").getAsDouble();

            return targetAmount;
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            return -1.0;
        }
    }


    private static URI getBaseURI() {
        return UriBuilder.fromUri("https://api.spoonacular.com").build();
    }
}
