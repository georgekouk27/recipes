package gr.georkouk.recipes.fragment;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.Util;

import butterknife.BindView;
import butterknife.ButterKnife;
import gr.georkouk.recipes.ActivityDetails;
import gr.georkouk.recipes.ActivityStepDetails;
import gr.georkouk.recipes.R;
import gr.georkouk.recipes.entity.Recipe;
import gr.georkouk.recipes.entity.Step;
import gr.georkouk.recipes.utils.ConfigurationInfo;
import gr.georkouk.recipes.utils.ExoPlayerConf;


public class FragmentStepDetails extends Fragment implements View.OnClickListener{

    private final static String PLAYBACK_POS = "playbackPos";
    private final static String STEP_INDEX = "stepIndex";

    @BindView(R.id.play_media_step_details)
    PlayerView exoPlayerView;

    @BindView(R.id.video_thumbnail_details)
    ImageView thumbnail;

    @BindView(R.id.text_view_description_step_details)
    TextView descriptionTextView;

    @BindView(R.id.media_layout_step_details)
    FrameLayout mediaLayout;

    @BindView(R.id.progress_bar_load_media_step_details)
    ProgressBar mediaLoadingProgressBar;

    @BindView(R.id.media_connectivity_error)
    LinearLayout connectivityErrorLayout;

    @BindView(R.id.image_view_refresh_media_step_details)
    ImageView imageRefreshMedia;

    @BindView(R.id.previous_step_details_linear)
    LinearLayout btPrevious;

    @BindView(R.id.next_step_details_linear)
    LinearLayout btNext;

    private Recipe recipe;
    private int selectedStepNum;
    private long playerPosition = 0;
    private Step step;
    private ExoPlayerConf exoPlayerConf;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_step_details, container, false);

        ButterKnife.bind(this, view);

        this.selectedStepNum = 0;

        if(savedInstanceState != null) {
            if(savedInstanceState.containsKey(PLAYBACK_POS)) {
                playerPosition = (long) savedInstanceState.get(PLAYBACK_POS);
            }

            if(savedInstanceState.containsKey(STEP_INDEX)) {
                selectedStepNum = (int) savedInstanceState.get(STEP_INDEX);

                Activity activity = getActivity();

                if(activity instanceof ActivityStepDetails) {
                    ((ActivityStepDetails) getActivity()).setPosition(selectedStepNum);
                }

            }
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong(PLAYBACK_POS, playerPosition);
        outState.putInt(STEP_INDEX, selectedStepNum);

        if(exoPlayerConf != null) {
            exoPlayerConf.abandon();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        initializeView();

        Activity activity = getActivity();

        if(activity instanceof ActivityDetails){
            this.recipe = ((ActivityDetails) getActivity()).getSelectedRecipe();
        }
        else{
            this.recipe = ((ActivityStepDetails) getActivity()).getSelectedRecipe();
            this.selectedStepNum = ((ActivityStepDetails) getActivity()).getPosition();
        }

        this.step = this.recipe.getSteps().get(this.selectedStepNum);

        checkPlayerAndScreenModification();

        fillView(false);
    }

    @Override
    public void onPause() {
        super.onPause();

        if(exoPlayerConf != null) {
            long currentPos = exoPlayerConf.getContentPosition();

            if(currentPos != -1) {
                playerPosition = currentPos;
            }

            if(Util.SDK_INT <= 23) {
                exoPlayerConf.abandon();
                exoPlayerConf = null;
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if(exoPlayerConf != null && Util.SDK_INT > 23) {
            exoPlayerConf.abandon();
            exoPlayerConf = null;
        }
    }

    private void initializeView(){
        btPrevious.setOnClickListener(this);

        btNext.setOnClickListener(this);

        if(!ConfigurationInfo.onPhoneLandscape(getContext())
                || getResources().getBoolean(R.bool.isTablet)){

            int height;

            if(getResources().getBoolean(R.bool.isTablet)) {
                height = (int) ((getResources().getDisplayMetrics().widthPixels - 550) * (9f / 16));
            }
            else {
                height = (int) (getResources().getDisplayMetrics().widthPixels * (9f / 16));
            }

            ViewGroup.LayoutParams params = exoPlayerView.getLayoutParams();
            params.height = height;

            exoPlayerView.setLayoutParams(params);
            thumbnail.setLayoutParams(params);
        }
    }

    private void checkPlayerAndScreenModification() {
        if(this.step.getVideoURL() != null
                && !this.step.getVideoURL().trim().equals("")) {

            if(getActivity() != null && exoPlayerConf == null) {

                exoPlayerConf = new ExoPlayerConf(getActivity(),
                        exoPlayerView,
                        playerPosition,
                        mediaLoadingProgressBar,
                        thumbnail,
                        Uri.parse(this.step.getThumbnailURL()),
                        connectivityErrorLayout,
                        imageRefreshMedia
                );
            }

            if(getActivity() != null
                    && ConfigurationInfo.onPhoneLandscape(getContext())){

                ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
            }
        }

    }

    private void fillView(boolean fromClick){
        if(fromClick){
            this.step = this.recipe.getSteps().get(this.selectedStepNum);
        }

        if(getResources().getBoolean(R.bool.isTablet)){
            getActivity().setTitle(recipe.getName());
        }
        else {
            getActivity().setTitle(selectedStepNum + ") " + step.getShortDescription());
        }

        String mediaUrl = this.step.getVideoURL().trim();
        if(mediaUrl.trim().equals("")) {
            mediaLayout.setVisibility(View.GONE);
        }
        else {
            if(exoPlayerConf == null) {
                checkPlayerAndScreenModification();
            }

            mediaLayout.setVisibility(View.VISIBLE);
            exoPlayerConf.initializeMediaSession();
            exoPlayerConf.initializePlayer(Uri.parse(mediaUrl));
        }

        descriptionTextView.setText(step.getDescription());
    }

    public void showStepDetails(int position){
        this.selectedStepNum = position;

        playerPosition = 0;

        if(exoPlayerConf != null) {
            exoPlayerConf.abandon();
        }

        exoPlayerConf = null;

        fillView(true);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.previous_step_details_linear:
                if((selectedStepNum - 1) >= 0){
                    selectedStepNum--;

                    playerPosition = 0;

                    if(exoPlayerConf != null) {
                        exoPlayerConf.abandon();
                    }

                    exoPlayerConf = null;

                    fillView(true);
                }

                break;
            case R.id.next_step_details_linear:
                if((selectedStepNum + 1) < this.recipe.getSteps().size()){
                    selectedStepNum++;

                    playerPosition = 0;

                    if(exoPlayerConf != null) {
                        exoPlayerConf.abandon();
                    }

                    exoPlayerConf = null;

                    fillView(true);
                }

                break;
            default:
                break;
        }
    }

}
