package com.example.hw;

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

public class GoogleCloudTranslationApiCallable implements Callable<String> {
    private String ingredientName;


    public GoogleCloudTranslationApiCallable(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    @Override
    public String call() throws Exception{
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(getBaseURI());
        try {
            Response response = target
                    .path("language")
                    .path("translate")
                    .path("v2")
                    .queryParam("target", "pl")
                    .queryParam("q", this.ingredientName)
                    .queryParam("key", "")
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .get();


            if (response.getStatus() != 200) {
                throw new RuntimeException("Failed translateIngredientEnglishToPolish: HTTP error code : " + response.getStatus());
            }


            String jsonString = response.readEntity(String.class);

            JsonObject obj = JsonParser.parseString(jsonString).getAsJsonObject();
            JsonObject data = obj.getAsJsonObject("data");
            JsonArray translations = data.getAsJsonArray("translations");
            JsonObject firstTranslation = translations.get(0).getAsJsonObject();

            String translation = firstTranslation.get("translatedText").getAsString();

            return translation;
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private static URI getBaseURI() {
        return UriBuilder.fromUri("https://translation.googleapis.com").build();
    }


}
