package com.example.hw;


public class Recipe {
    private int id;
    private String title;
    private String image;
    private int readyInMinutes;
    private int servings;
    private Ingredient[] extendedIngredients;


    public int getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public String getImage() {
        return this.image;
    }

    public int getReadyInMinutes() {
        return this.readyInMinutes;
    }

    public int getServings(){return this.servings;}

    public Ingredient[] getExtentedIngredients() {
        return this.extendedIngredients;
    }
}
