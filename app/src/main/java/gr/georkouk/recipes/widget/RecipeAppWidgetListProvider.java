package gr.georkouk.recipes.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import java.util.ArrayList;
import gr.georkouk.recipes.R;
import gr.georkouk.recipes.entity.Ingredient;
import gr.georkouk.recipes.entity.Recipe;


public class RecipeAppWidgetListProvider implements RemoteViewsService.RemoteViewsFactory{

    private ArrayList<String[]> dataList = new ArrayList<>();
    private Context context;


    public RecipeAppWidgetListProvider(Context context, Intent intent) {
        this.context = context;

        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        Recipe recipe = RecipeAppWidgetProviderConfigureActivity.loadRecipeObj(context, appWidgetId);

        if(recipe == null) {
            return;
        }

        for (Ingredient ing : recipe.getIngredients()) {
            dataList.add(
                    new String[] {ing.getQuantity() + " " + ing.getMeasure(),
                            ing.getIngredient().toLowerCase()});
        }

    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public RemoteViews getViewAt(int position) {

        final RemoteViews remoteView = new RemoteViews(
                context.getPackageName(), R.layout.recipe_app_widget_list_item);

        remoteView.setTextViewText(R.id.recipeIngredients, dataList.get(position)[1]);
        remoteView.setTextViewText(R.id.recipeAmountIngredients, dataList.get(position)[0]);

        return remoteView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

}
