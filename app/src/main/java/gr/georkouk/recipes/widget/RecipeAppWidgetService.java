package gr.georkouk.recipes.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;


public class RecipeAppWidgetService extends RemoteViewsService {

    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new RecipeAppWidgetListProvider(
                this.getApplicationContext(),
                intent
        );
    }

}
