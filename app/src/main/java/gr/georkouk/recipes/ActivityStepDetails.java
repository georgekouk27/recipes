package gr.georkouk.recipes;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;
import gr.georkouk.recipes.entity.Recipe;


public class ActivityStepDetails extends AppCompatActivity {

    private Recipe recipe;
    private int position;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_details);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();

        if (intent != null){
            this.recipe = (Recipe) intent.getSerializableExtra("recipe");

            if(savedInstanceState == null) {
                this.position = intent.getIntExtra("position", 0);
            }
        }
        else {
            Toast.makeText(this, "Problem occurred", Toast.LENGTH_SHORT).show();

            this.finish();
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

    public int getPosition(){
        return this.position;
    }

    public void setPosition(int pos){
        this.position = pos;
    }

}
