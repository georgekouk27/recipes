package gr.georkouk.recipes;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.IdlingRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.ViewMatchers;
import android.view.View;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import gr.georkouk.recipes.entity.Recipe;
import gr.georkouk.recipes.utils.RecipesIdlingResource;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey;
import static android.support.test.espresso.intent.matcher.IntentMatchers.isInternal;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static gr.georkouk.recipes.utils.Json.readJsonFile;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;


public class ActivityDetailsTest {

    private Context appContext = InstrumentationRegistry.getTargetContext();
    private RecipesIdlingResource idlingResource;


    @Rule
    public IntentsTestRule<ActivityDetails> detailsActivityActivityTestRule  =
            new  IntentsTestRule<ActivityDetails>(ActivityDetails.class) {
                @Override
                protected Intent getActivityIntent() {

                    ArrayList<Recipe> recipes = new ArrayList<>();

                    String jsonContent = readJsonFile(appContext);

                    Gson gson = new GsonBuilder().create();
                    Recipe[] recipesArray = gson.fromJson(jsonContent, Recipe[].class);

                    recipes.addAll(Arrays.asList(recipesArray));

                    InstrumentationRegistry.getTargetContext();
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.putExtra("recipe", recipes.get(0));

                    return intent;
                }
            };

    @Before
    public void registerIdlingResource() {
        idlingResource = ActivityDetails.idlingResource;

        IdlingRegistry.getInstance().register(idlingResource);
    }

    @Before
    public void stubAllExternalIntents() {
        // By default Espresso Intents does not stub any Intents.
        // Stubbing needs to be setup before
        // every test run. In this case all external Intents will be blocked.
        intending(not(isInternal()))
                .respondWith(new Instrumentation
                        .ActivityResult(Activity.RESULT_OK, null));
    }

    @Test
    public void openRecipeDetails_showCorrespondingInfo() {
        ActivityDetails detailsActivity = detailsActivityActivityTestRule.getActivity();

        View viewById = detailsActivity.findViewById(R.id.ingredients_label);

        assertThat(viewById, notNullValue());

        assertThat(viewById, instanceOf(TextView.class));

        TextView textView = (TextView) viewById;

        assertThat(textView.getText().toString(),is("Ingredients"));
    }

    @Test
    public void clickOnRecipeStep_opensUpStepDetailsActivity() {
        onView(ViewMatchers.withId(R.id.recyclerView))
                .perform(RecyclerViewActions.scrollToPosition(0));

        onView(withId(R.id.recyclerViewSteps))
                .perform(RecyclerViewActions
                        .actionOnItemAtPosition(0, click()));

        intended(hasComponent(
                new ComponentName(getTargetContext(), ActivityStepDetails.class)));

        onView(withId(R.id.bottom_border_buttons))
                .check(matches(isDisplayed()));
    }

    @Test
    public void clickOnRecipeStep_opensUpStepDetailsActivityWithCorrespondingInfo() {
        onView(ViewMatchers.withId(R.id.recyclerView))
                .perform(RecyclerViewActions.scrollToPosition(0));

        onView(withId(R.id.recyclerViewSteps))
                .perform(RecyclerViewActions
                        .actionOnItemAtPosition(0, click()));

        intended(hasExtraWithKey("recipe"));

        intended(hasExtraWithKey("position"));
    }

    @After
    public void unregisterIdlingResource() {
        if (idlingResource != null) {
            IdlingRegistry.getInstance().unregister(idlingResource);
        }
    }

}
