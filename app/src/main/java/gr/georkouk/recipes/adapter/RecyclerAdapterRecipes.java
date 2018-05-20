package gr.georkouk.recipes.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import gr.georkouk.recipes.R;
import gr.georkouk.recipes.entity.Recipe;


public class RecyclerAdapterRecipes extends RecyclerView.Adapter<RecyclerAdapterRecipes.ViewHolder>{

    private Context context;
    private List<Recipe> recipes;
    private OnItemclickListener onItemclickListener;

    public RecyclerAdapterRecipes(Context context){
        this.context = context;
        this.recipes = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recipe_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Recipe recipe = this.recipes.get(position);

        holder.tvRecipeName.setText(recipe.getName());

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemclickListener.onRecipeClick(recipe);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != this.recipes ? this.recipes.size() : 0);
    }

    public void swapData(List<Recipe> recipes){
        this.recipes = recipes;

        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemclickListener onItemClickListener) {
        this.onItemclickListener = onItemClickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private View view;
        private TextView tvRecipeName;

        ViewHolder(View itemView) {
            super(itemView);

            this.view = itemView;
            this.tvRecipeName = view.findViewById(R.id.tvRecipeName);
        }

    }

    public interface OnItemclickListener {

        void onRecipeClick(Recipe recipe);

    }

}
