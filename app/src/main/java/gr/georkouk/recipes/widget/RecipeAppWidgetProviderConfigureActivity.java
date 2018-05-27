package gr.georkouk.recipes.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gr.georkouk.recipes.R;
import gr.georkouk.recipes.entity.Recipe;

import static gr.georkouk.recipes.utils.Json.readJsonFile;

/**
 * The configuration screen for the {@link RecipeAppWidgetProvider RecipeAppWidgetProvider} AppWidget.
 */
public class RecipeAppWidgetProviderConfigureActivity extends Activity {

    private static final String PREFS_NAME = "gr.georkouk.recipes.widget.RecipeAppWidgetProvider";
    private static final String PREF_PREFIX_KEY_ING = "appwidgetIngredients_";
    private static final String PREF_PREFIX_KEY_RECIPE_NAME = "appwidgetRecipe_";
    private static final String PREF_PREFIX_KEY_SERVINGS_NUM = "appwidgetNum_";
    private static final String PREF_PREFIX_KEY_RECIPE_OBJ = "appwidgetRecipeObj_";
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;


    public RecipeAppWidgetProviderConfigureActivity() {
        super();
    }

    private static void saveSelectRecipePref(Context context, int appWidgetId, Recipe recipe) {

        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();

        prefs.putString(PREF_PREFIX_KEY_RECIPE_OBJ + appWidgetId, new Gson().toJson(recipe));

        prefs.apply();
    }

    public static Recipe loadRecipeObj(Context context, int appWidgetId){

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);

        String json = prefs.getString(PREF_PREFIX_KEY_RECIPE_OBJ + appWidgetId, null);

        return new Gson().fromJson(json, Recipe.class);
    }

    static void deletePref(Context context, int appWidgetId) {

        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();

        prefs.remove(PREF_PREFIX_KEY_RECIPE_NAME + appWidgetId);
        prefs.remove(PREF_PREFIX_KEY_SERVINGS_NUM + appWidgetId);
        prefs.remove(PREF_PREFIX_KEY_ING + appWidgetId);

        prefs.apply();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.recipe_app_widget_provider_configure);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        String jsonContent = readJsonFile(this);

        Gson gson = new GsonBuilder().create();

        final Recipe[] recipesObjects = gson.fromJson(jsonContent, Recipe[].class);
        final String[] recipes = new String[recipesObjects.length];

        for(int i = 0; i < recipesObjects.length; i++){
            recipes[i] = recipesObjects[i].getName().trim();
        }

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, recipes);

        ListView listView = findViewById(R.id.lvWidgetRecipes);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                final Context context = RecipeAppWidgetProviderConfigureActivity.this;

                // When the button is clicked, store the string locally
                saveSelectRecipePref(context,
                        mAppWidgetId,
                        recipesObjects[i]
                );

                // It is the responsibility of the configuration activity to update the app widget
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                RecipeAppWidgetProvider.updateAppWidget(context, appWidgetManager, mAppWidgetId);

                // Make sure we pass back the original appWidgetId
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);

                finish();

            }

        });

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            );
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

    }

}

