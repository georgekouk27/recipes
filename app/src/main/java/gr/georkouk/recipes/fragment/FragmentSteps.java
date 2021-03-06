package gr.georkouk.recipes.fragment;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import gr.georkouk.recipes.ActivityDetails;
import gr.georkouk.recipes.R;
import gr.georkouk.recipes.adapter.RecyclerAdapterSteps;
import gr.georkouk.recipes.entity.Ingredient;
import gr.georkouk.recipes.entity.Recipe;

public class FragmentSteps extends Fragment{

    @BindView(R.id.tvIngredients)
    TextView tvIngredients;

    @BindView(R.id.recyclerViewSteps)
    RecyclerView recyclerView;

    private Recipe recipe;
    private Parcelable recyclerPos;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_steps, container, false);

        ButterKnife.bind(this, view);

        this.recyclerPos = null;
        if(savedInstanceState != null) {
            recyclerPos =  savedInstanceState.getParcelable("recyclerPos");
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        this.recipe = ((ActivityDetails) getActivity()).getSelectedRecipe();

        initializeView();
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
        tvIngredients.setText(getIngredientsText());

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));

        RecyclerAdapterSteps recyclerAdapterSteps = new RecyclerAdapterSteps();

        recyclerAdapterSteps.setOnItemClickListener(new RecyclerAdapterSteps.OnItemclickListener() {

            @Override
            public void onStepClick(int position) {
                ((ActivityDetails) getActivity()).stepClick(position);
            }

        });

        recyclerView.setAdapter(recyclerAdapterSteps);

        recyclerAdapterSteps.swapData(this.recipe.getSteps());

        if(recyclerPos != null){
            recyclerView.getLayoutManager().onRestoreInstanceState(recyclerPos);

            recyclerPos = null;
        }
    }

    private String getIngredientsText(){
        StringBuilder ingredientsSb = new StringBuilder();

        for (Ingredient ingredient : this.recipe.getIngredients()) {

            ingredientsSb.append(String.format(
                    Locale.getDefault(),
                    "• %s (%d %s)",
                    ingredient.getIngredient(),
                    ingredient.getQuantity().intValue(),
                    ingredient.getMeasure()
            ));

            ingredientsSb.append("\n");
        }

        return ingredientsSb.toString();
    }

}
