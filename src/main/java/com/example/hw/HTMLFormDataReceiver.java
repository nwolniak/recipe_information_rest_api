package com.example.hw;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

@Path("/submitted")
public class HTMLFormDataReceiver {
    private final String imgURL = "https://spoonacular.com/cdn/ingredients_100x100/";
    private Recipe recipe;
    private Nutrients recipeNutrients;
    private final Map<String, String> translatedIngredients = new HashMap<>();
    private final Map<String, Nutrients> ingredientsNutrients = new HashMap<>();
    private final Map<String, Double> ingredientsAmountInGrams = new HashMap<>();


    @POST
    public Response getFormData(@FormParam("dish_name") String dish_name) throws IOException, ExecutionException, InterruptedException {
        long start = System.currentTimeMillis();
        Map<String, Future<Nutrients>> nutrientsFutures = new HashMap<>();
        Map<String, Future<Double>> ingredientsGramsFutures = new HashMap<>();
        Map<String, Future<String>> ingredientsNamesTranslatedFutures = new HashMap<>();


        SpoonacularApiClient spoonacularApiClient = new SpoonacularApiClient();

        // Get recipeID by dish name request
        int recipeId = spoonacularApiClient.getRecipeIdByDishName(dish_name);

        // Get recipe by recipeID reguest
        this.recipe = spoonacularApiClient.getRecipeById(recipeId);


        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(8);
        for (Ingredient ingredient : this.recipe.getExtentedIngredients()) {
            String ingredientName = ingredient.getName();

            Future<Nutrients> nutrientsFuture = executor.submit(new EdamamFoodApiNutrientsCallable(ingredientName));
            Future<Double> ingredientGramsFuture = executor.submit(new SpoonacularApiConvertionToGramsCallable(ingredientName, ingredient.getAmount(), ingredient.getUnit()));
            Future<String> ingredientNameTranslatedFuture = executor.submit(new GoogleCloudTranslationApiCallable(ingredientName));

            nutrientsFutures.put(ingredientName, nutrientsFuture);
            ingredientsGramsFutures.put(ingredientName, ingredientGramsFuture);
            ingredientsNamesTranslatedFutures.put(ingredientName, ingredientNameTranslatedFuture);
        }

        for (Entry<String, Future<Nutrients>> e : nutrientsFutures.entrySet()) {
            this.ingredientsNutrients.put(e.getKey(), e.getValue().get());
        }
        for (Entry<String, Future<Double>> e : ingredientsGramsFutures.entrySet()) {
            this.ingredientsAmountInGrams.put(e.getKey(), e.getValue().get());
        }
        for (Entry<String, Future<String>> e : ingredientsNamesTranslatedFutures.entrySet()) {
            this.translatedIngredients.put(e.getKey(), e.getValue().get());
        }

        // shutdown ThreadPoolExecutor
        executor.shutdown();


        long end = System.currentTimeMillis();
        System.out.println("Elapsed Time in milli seconds: " + (end - start));


        // Calculate total nutrients per recipe
        this.recipeNutrients = new Nutrients();
        for (Ingredient ingredient : this.recipe.getExtentedIngredients()) {
            String ingredientName = ingredient.getName();
            Nutrients ingredientNutrients = this.ingredientsNutrients.get(ingredientName);
            double ingredientAmountInGrams = this.ingredientsAmountInGrams.get(ingredientName);

            double energy = ingredientNutrients.getENERC_KCAL();
            double proteins = ingredientNutrients.getPROCNT();
            double fat = ingredientNutrients.getFAT();
            double carbohydrates = ingredientNutrients.getCHOCDF();
            double fiber = ingredientNutrients.getFIBTG();

            this.recipeNutrients.setENERC_KCAL(this.recipeNutrients.getENERC_KCAL() + ((ingredientAmountInGrams / 100) * energy));
            this.recipeNutrients.setPROCNT(this.recipeNutrients.getPROCNT() + ((ingredientAmountInGrams / 100) * proteins));
            this.recipeNutrients.setFAT(this.recipeNutrients.getFAT() + ((ingredientAmountInGrams / 100) * fat));
            this.recipeNutrients.setCHOCDF(this.recipeNutrients.getCHOCDF() + ((ingredientAmountInGrams / 100) * carbohydrates));
            this.recipeNutrients.setFIBTG(this.recipeNutrients.getFIBTG() + ((ingredientAmountInGrams / 100) * fiber));
        }

        Document doc = this.createHtmlResponse();


        return Response.status(200).entity(doc.toString()).build();
    }

    public Document createHtmlResponse() throws IOException {
        Ingredient[] ingredients = this.recipe.getExtentedIngredients();


        Document doc = Jsoup.parse("<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>MyResponse</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "</body>\n" +
                "</html>");

        doc.body().appendElement("h1")
                .append("Recipe : " + this.recipe.getTitle());

        doc.body().appendElement("h3")
                .append("Ready in minutes: " + this.recipe.getReadyInMinutes());

        doc.body().appendElement("h3")
                .append("Servings: " + this.recipe.getServings());

        doc.body().appendElement("img")
                .attr("src", this.recipe.getImage())
                .attr("alt", "recipeIMG");


        doc.body().appendElement("h3")
                .append("Ingredients:");


        Element list = doc.body().appendElement("ul");

        for (Ingredient ingredient : ingredients) {
            String ingredientName = ingredient.getName();
            Nutrients nutrients = this.ingredientsNutrients.get(ingredientName);
            list.appendElement("li").append(ingredientName + " [" + this.translatedIngredients.get(ingredientName) + "]");
            Element ingredientInfoList = list.appendElement("ol");
            ingredientInfoList.appendElement("img")
                    .attr("src", this.imgURL + ingredient.getImage())
                    .attr("alt", ingredient.getName())
                    .attr("width", "100")
                    .attr("height", "100");
            ingredientInfoList.appendElement("li").append("Amount: " + ingredient.getAmount());
            ingredientInfoList.appendElement("li").append("Unit: " + ingredient.getUnit());
            ingredientInfoList.appendElement("li").append("Nutrients [per 100g]:");
            Element ingredientNutrientsList = ingredientInfoList.appendElement("ul");
            ingredientNutrientsList.appendElement("li").append("Energy: " + nutrients.getENERC_KCAL() + "kcal");
            ingredientNutrientsList.appendElement("li").append("Proteins: " + nutrients.getPROCNT() + "g");
            ingredientNutrientsList.appendElement("li").append("Fat: " + nutrients.getFAT() + "g");
            ingredientNutrientsList.appendElement("li").append("Carbohydrates: " + nutrients.getCHOCDF() + "g");
            ingredientNutrientsList.appendElement("li").append("Fiber: " + nutrients.getFIBTG() + "g");
            ingredientInfoList.appendElement("li").append("Weight: " + this.ingredientsAmountInGrams.get(ingredientName) + "g");
        }

        doc.body().appendElement("h3")
                .append("Total nutrients for recipe:");

        Element totalNutrientsList = doc.body().appendElement("ul");
        totalNutrientsList.appendElement("li").append("Energy: " + String.format("%.2f", this.recipeNutrients.getENERC_KCAL()) + "kcal");
        totalNutrientsList.appendElement("li").append("Proteins: " + String.format("%.2f", this.recipeNutrients.getPROCNT()) + "g");
        totalNutrientsList.appendElement("li").append("Fat: " + String.format("%.2f", this.recipeNutrients.getFAT()) + "g");
        totalNutrientsList.appendElement("li").append("Carbohydrates: " + String.format("%.2f", this.recipeNutrients.getCHOCDF()) + "g");
        totalNutrientsList.appendElement("li").append("Fiber: " + String.format("%.2f", this.recipeNutrients.getFIBTG()) + "g");


        return doc;
    }

}
