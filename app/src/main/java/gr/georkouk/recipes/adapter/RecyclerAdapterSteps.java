package gr.georkouk.recipes.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import gr.georkouk.recipes.R;
import gr.georkouk.recipes.entity.Step;


public class RecyclerAdapterSteps extends RecyclerView.Adapter<RecyclerAdapterSteps.ViewHolder> {

    private List<Step> steps;
    private OnItemclickListener onItemclickListener;


    public RecyclerAdapterSteps(){
        this.steps = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerAdapterSteps.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recipe_step_item, parent, false);

        return new RecyclerAdapterSteps.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapterSteps.ViewHolder holder, final int position) {
        Step step = steps.get(position);

        holder.tvStepNumber.setText(String.valueOf(position + 1));
        holder.tvStepName.setText(step.getShortDescription());

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemclickListener.onStepClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != this.steps ? this.steps.size() : 0);
    }

    public void swapData(List<Step> steps){
        this.steps = steps;

        notifyDataSetChanged();
    }

    public void setOnItemClickListener(RecyclerAdapterSteps.OnItemclickListener onItemClickListener) {
        this.onItemclickListener = onItemClickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private View view;
        private TextView tvStepNumber;
        private TextView tvStepName;

        ViewHolder(View itemView) {
            super(itemView);

            this.view = itemView;
            this.tvStepNumber = view.findViewById(R.id.tvStepNumber);
            this.tvStepName = view.findViewById(R.id.tvStepName);
        }

    }

    public interface OnItemclickListener {

        void onStepClick(int position);

    }

}
