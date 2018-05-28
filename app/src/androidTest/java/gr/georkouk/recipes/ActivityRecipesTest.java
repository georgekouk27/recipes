package gr.georkouk.recipes;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.IdlingRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.runner.AndroidJUnit4;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static gr.georkouk.recipes.utils.Json.readJsonFile;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;


@RunWith(AndroidJUnit4.class)
public class ActivityRecipesTest {

    private ArrayList<Recipe> recipes;
    private Context appContext = InstrumentationRegistry.getTargetContext();
    private RecipesIdlingResource idlingResource;

    @Rule
    public IntentsTestRule<ActivityMain> activityTestRule
            = new IntentsTestRule<>(ActivityMain.class);

    @Before
    public void registerIdlingResource() {
        idlingResource = ActivityMain.idlingResource;
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

    @Before
    public void loadJson() {
        recipes = new ArrayList<>();

        String jsonContent = readJsonFile(appContext);

        Gson gson = new GsonBuilder().create();
        Recipe[] recipesArray = gson.fromJson(jsonContent, Recipe[].class);

        recipes.addAll(Arrays.asList(recipesArray));
    }

    @Test
    public void clickOnRecipe_opensUpRecipeDetails() {
        onView(ViewMatchers.withId(R.id.recyclerView))
                .perform(RecyclerViewActions.scrollToPosition(1));

        onView(withId(R.id.recyclerView))
                .perform(RecyclerViewActions
                        .actionOnItemAtPosition(1, click()));

        intended(hasComponent(
                new ComponentName(getTargetContext(), ActivityDetails.class)));

        onView(withId(R.id.ingredients_card))
                .check(matches(isDisplayed()));

        onView(withId(R.id.recyclerViewSteps))
                .check(matches(isDisplayed()));
    }

    @Test
    public void clickOnRecipe_opensUpRecipeDetailsWithInfo() {
        onView(withId(R.id.recyclerView))
                .perform(RecyclerViewActions
                        .actionOnItemAtPosition(1, click()));

        intended(hasExtraWithKey("recipe"));
    }

    @Test
    public void scrollToItem_checkItsText() {
        // First, scroll to the position that needs to be matched and click on it.
        onView(ViewMatchers.withId(R.id.recyclerView))
                .perform(RecyclerViewActions.scrollToPosition(3));

        // Match the text in an item below the fold and check that it's displayed.
        onView(allOf(isDisplayed(), withText(recipes.get(3).getName())))
                .check(matches(isDisplayed()));
    }

    @After
    public void unregisterIdlingResource() {
        if (idlingResource != null) {
            IdlingRegistry.getInstance().unregister(idlingResource);
        }
    }

}
