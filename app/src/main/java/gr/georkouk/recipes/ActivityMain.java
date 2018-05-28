package gr.georkouk.recipes;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import gr.georkouk.recipes.adapter.RecyclerAdapterRecipes;
import gr.georkouk.recipes.entity.Recipe;
import gr.georkouk.recipes.interfaces.RestApi;
import gr.georkouk.recipes.network.RestClient;
import gr.georkouk.recipes.utils.RecipesIdlingResource;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static gr.georkouk.recipes.utils.IdlingResources.getIdlingResource;


public class ActivityMain extends AppCompatActivity {

    private static final int RECYCLERVIEW_COLUMNS_FOR_TABLET = 3;
    private static final int RECYCLERVIEW_COLUMNS_FOR_MOBILE_PORTRAIT = 1;
    private static final int RECYCLERVIEW_COLUMNS_FOR_MOBILE_LANDSCAPE = 2;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    public static RecipesIdlingResource idlingResource;
    private RestApi restApi;
    private RecyclerAdapterRecipes adapterRecipes;
    private int columnsNum;
    private Parcelable recyclerPos;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

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

        idlingResource = getIdlingResource(idlingResource);
        if (idlingResource != null) {
            idlingResource.setIdleState(false);
        }

        this.recyclerPos = null;
        if(savedInstanceState != null) {
            recyclerPos =  savedInstanceState.getParcelable("recyclerPos");
        }

        this.restApi = RestClient.getClient().create(RestApi.class);

        fillRecipes();

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable(
                "recyclerPos",
                recyclerView.getLayoutManager().onSaveInstanceState()
        );

        super.onSaveInstanceState(outState);
    }

    private void initializeView(){
        recyclerView.setLayoutManager(new GridLayoutManager(this, columnsNum));

        this.adapterRecipes = new RecyclerAdapterRecipes();

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

                if (idlingResource != null) {
                    idlingResource.setIdleState(true);
                }

                if(recyclerPos != null){
                    recyclerView.getLayoutManager().onRestoreInstanceState(recyclerPos);

                    recyclerPos = null;
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Recipe>> call, @NonNull Throwable t) {
                Log.e("my==>", t.toString());
            }

        });
    }

}
