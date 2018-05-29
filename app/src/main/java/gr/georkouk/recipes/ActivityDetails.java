package gr.georkouk.recipes;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import gr.georkouk.recipes.entity.Recipe;
import gr.georkouk.recipes.fragment.FragmentStepDetails;
import gr.georkouk.recipes.utils.RecipesIdlingResource;

import static gr.georkouk.recipes.utils.IdlingResources.getIdlingResource;


public class ActivityDetails extends AppCompatActivity {

    private Recipe recipe;
    private boolean twoPane;
    public static RecipesIdlingResource idlingResource;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        idlingResource = getIdlingResource(idlingResource);
        if (idlingResource != null) {
            idlingResource.setIdleState(false);
        }

        Intent intent = getIntent();

        if (intent != null){
            this.recipe = (Recipe) intent.getSerializableExtra("recipe");
        }
        else {
            Toast.makeText(this, "Problem occurred", Toast.LENGTH_SHORT).show();

            this.finish();
        }

        setTitle(this.recipe.getName());

        twoPane = getResources().getBoolean(R.bool.isTablet);

        idlingResource = getIdlingResource(idlingResource);
        if (idlingResource != null) {
            idlingResource.setIdleState(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    public Recipe getSelectedRecipe() {
        return this.recipe;
    }

    public void stepClick(int position){
        if(twoPane) {
            FragmentStepDetails fragmentStepDetails =
                    (FragmentStepDetails) getSupportFragmentManager().findFragmentById(R.id.fragmentStepDetails);

            fragmentStepDetails.showStepDetails(position);
        }
        else {
            Intent intent = new Intent(this, ActivityStepDetails.class);

            intent.putExtra("recipe", recipe);

            intent.putExtra("position", position);

            startActivity(intent);
        }
    }

}
