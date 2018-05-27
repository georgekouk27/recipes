package gr.georkouk.recipes;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import gr.georkouk.recipes.entity.Recipe;
import gr.georkouk.recipes.fragment.FragmentStepDetails;


public class ActivityDetails extends AppCompatActivity {

    private Recipe recipe;
    private boolean twoPane;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();

        if (intent != null){
            this.recipe = (Recipe) intent.getSerializableExtra("recipe");
        }
        else {
            Toast.makeText(this, "Problem occurred", Toast.LENGTH_SHORT).show();

            this.finish();
        }

        setTitle(this.recipe.getName());

        twoPane = findViewById(R.id.fragmentStepDetails) != null;
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
