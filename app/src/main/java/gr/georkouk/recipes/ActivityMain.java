package gr.georkouk.recipes;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import java.util.List;
import gr.georkouk.recipes.adapter.RecyclerAdapterRecipes;
import gr.georkouk.recipes.entity.Recipe;
import gr.georkouk.recipes.interfaces.RestApi;
import gr.georkouk.recipes.network.RestClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ActivityMain extends AppCompatActivity {

    private static final int RECYCLERVIEW_COLUMNS_FOR_TABLET = 3;
    private static final int RECYCLERVIEW_COLUMNS_FOR_MOBILE_PORTRAIT = 1;
    private static final int RECYCLERVIEW_COLUMNS_FOR_MOBILE_LANDSCAPE = 2;

    private RestApi restApi;
    private RecyclerAdapterRecipes adapterRecipes;
    private int columnsNum;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(getResources().getBoolean(R.bool.isTablet)) {
            columnsNum = RECYCLERVIEW_COLUMNS_FOR_TABLET;
        }
        else {
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
                columnsNum = RECYCLERVIEW_COLUMNS_FOR_MOBILE_PORTRAIT;
            }
            else {
                columnsNum = RECYCLERVIEW_COLUMNS_FOR_MOBILE_LANDSCAPE;
            }
        }

        initializeView();

        this.restApi = RestClient.getClient().create(RestApi.class);

        fillRecipes();
    }

    private void initializeView(){

        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new GridLayoutManager(this, columnsNum));

        this.adapterRecipes = new RecyclerAdapterRecipes(this);

        this.adapterRecipes.setOnItemClickListener(new RecyclerAdapterRecipes.OnItemclickListener() {

            @Override
            public void onRecipeClick(Recipe recipe) {
                Intent intent = new Intent(ActivityMain.this, ActivityDetails.class);

                intent.putExtra("recipe", recipe);

                startActivity(intent);
            }

        });

        recyclerView.setAdapter(this.adapterRecipes);
    }

    private void fillRecipes(){
        Call<List<Recipe>> call = this.restApi.getRecipesFromWeb();

        call.enqueue(new Callback<List<Recipe>>() {

            @Override
            public void onResponse(@NonNull Call<List<Recipe>> call, @NonNull Response<List<Recipe>> response) {
                adapterRecipes.swapData(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<List<Recipe>> call, @NonNull Throwable t) {
                Log.e("my==>", t.toString());
            }

        });
    }

}
