package gr.georkouk.recipes.interfaces;

import java.util.List;
import gr.georkouk.recipes.entity.Recipe;
import retrofit2.Call;
import retrofit2.http.GET;


public interface RestApi {

    @GET("2017/May/59121517_baking/baking.json")
    Call<List<Recipe>> getRecipesFromWeb();

}
