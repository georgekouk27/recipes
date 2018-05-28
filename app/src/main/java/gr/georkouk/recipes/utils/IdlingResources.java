package gr.georkouk.recipes.utils;

public class IdlingResources {

    public static RecipesIdlingResource getIdlingResource(RecipesIdlingResource idlingResource) {
        if (idlingResource == null) {
            idlingResource = new RecipesIdlingResource();
        }

        return idlingResource;
    }

}
