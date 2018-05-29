package gr.georkouk.recipes.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
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
        holder.tvRecipeServings.setText(String.valueOf(recipe.getServings()));

        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .centerCrop()
                .dontTransform()
                .placeholder(R.drawable.ic_recipe)
                .error(R.drawable.ic_recipe);

        Glide.with(this.context).load(recipe.getImage()).apply(options)
                .into(holder.imageView);

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

        @BindView(R.id.tvRecipeName)
        TextView tvRecipeName;

        @BindView(R.id.tvRecipeServings)
        TextView tvRecipeServings;

        @BindView(R.id.ivRecipeItem)
        ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);

            this.view = itemView;

            ButterKnife.bind(this, view);
        }

    }

    public interface OnItemclickListener {

        void onRecipeClick(Recipe recipe);

    }

}
