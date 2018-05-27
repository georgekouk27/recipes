package gr.georkouk.recipes.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import gr.georkouk.recipes.R;
import gr.georkouk.recipes.entity.Recipe;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link RecipeAppWidgetProviderConfigureActivity RecipeAppWidgetProviderConfigureActivity}
 */
public class RecipeAppWidgetProvider extends AppWidgetProvider {

    private static final String WidgetClickTag = "WidgetClickTag";


    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.recipe_app_widget_provider);

        Recipe recipe = RecipeAppWidgetProviderConfigureActivity.loadRecipeObj(context, appWidgetId);

        if(recipe == null){
            return;
        }

        String recipeName = recipe.getName().trim();
        String servingsNumber = String.valueOf(recipe.getServings());

        views.setTextViewText(R.id.appwidget_recipe_name,
                (recipeName.equals("")) ? context.getString(R.string.unknownRecipe) : recipeName);

        views.setTextViewText(R.id.appwidget_recipe_servings,
                (servingsNumber.equals("")) ? "-" : servingsNumber);

        //RemoteViews Service needed to provide adapter for ListView
        Intent svcIntent = new Intent(context, RecipeAppWidgetService.class);
        svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        //passing app widget id to that RemoteViews Service
        //setting adapter to listview of the widget
        views.setRemoteAdapter(R.id.widget_ingredients_list, svcIntent);

        Intent intent = new Intent(context, RecipeAppWidgetProvider.class);
        intent.setAction(WidgetClickTag);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.widget_header, pIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RecipeAppWidgetProviderConfigureActivity.deletePref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {

    }

    @Override
    public void onDisabled(Context context) {

    }

}

