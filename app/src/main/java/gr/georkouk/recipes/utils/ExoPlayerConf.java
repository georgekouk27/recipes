package gr.georkouk.recipes.utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;
import gr.georkouk.recipes.R;
import static android.media.AudioManager.AUDIOFOCUS_REQUEST_GRANTED;


public class ExoPlayerConf {

    private static final String TAG = ExoPlayerConf.class.getSimpleName();
    final private PlayerView exoPlayerView;
    final private Activity activity;
    private long startPos;
    private Uri mediaUri;
    private Uri thumbnailUri;
    private ProgressBar mediaLoadingProgressBar;
    private ImageView thumbnail;
    private LinearLayout connectivityErrorLayout;
    private SimpleExoPlayer exoPlayer;
    private static ComponentListener componentListener;
    private static MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;
    private AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener;
    private AudioManager audioManager;
    private BroadcastReceiver noisyReceiver;
    private BroadcastReceiver networkConnectivityReceiver;
    private boolean repairNeeded = false;
    private boolean stopWaitingForConnecion = false;


    public ExoPlayerConf(@NonNull final Activity activity,
                          final PlayerView exoPlayerView,
                          final long positionReset,
                          final ProgressBar mediaLoadingProgressBar,
                          final ImageView thumbnail,
                          final Uri thumbnailUri,
                          final LinearLayout connectivityErrorLayout,
                          final ImageView refreshMedia) {

        this.exoPlayerView = exoPlayerView;
        this.activity = activity;
        this.startPos = positionReset;
        this.mediaLoadingProgressBar = mediaLoadingProgressBar;
        this.thumbnail = thumbnail;
        this.thumbnailUri = thumbnailUri;
        this.connectivityErrorLayout = connectivityErrorLayout;

        if(ConfigurationInfo.onPhoneLandscape(activity)) {
            ConfigurationInfo.hideSystemUI(activity);

            ViewGroup.LayoutParams playerViewParams = exoPlayerView.getLayoutParams();
            int[] screenDimens = ConfigurationInfo.findScreenDimens(activity);
            playerViewParams.width = screenDimens[0];
            playerViewParams.height = screenDimens[1];
            exoPlayerView.setLayoutParams(playerViewParams);
        }

        audioManager = (AudioManager)activity.getSystemService(Context.AUDIO_SERVICE);
        componentListener = new ComponentListener();

        // Audio Focus Change Listener
        onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        if(exoPlayer != null) {
                            exoPlayer.setPlayWhenReady(true);
                        }

                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                        float percent = 0.7f;
                        int seventyVolume = (int) (currentVolume*percent);
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, seventyVolume, 0);

                        break;
                    case AudioManager.AUDIOFOCUS_LOSS:
                        if(exoPlayer != null) {
                            exoPlayer.setPlayWhenReady(false);
                        }

                        startPos = getContentPosition();
                        exoPlayerView.showController();

                        releasePlayer();

                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        if(exoPlayer != null) {
                            exoPlayer.setPlayWhenReady(false);
                        }

                        break;
                    default:
                        if(exoPlayer != null) {
                            exoPlayer.setPlayWhenReady(true);
                        }
                }
            }
        };


        // Handle Controller Visibility
        exoPlayerView.setControllerAutoShow(false);
        exoPlayerView.setControllerVisibilityListener(new PlayerControlView.VisibilityListener() {
            @Override
            public void onVisibilityChange(int visibility) {
                if(exoPlayer != null &&
                        (exoPlayer.getPlaybackState() == Player.STATE_READY
                                || exoPlayer.getPlaybackState() == Player.STATE_ENDED)) {

                    if(visibility == 0) {
                        exoPlayerView.showController();
                    }
                    else {
                        exoPlayerView.hideController();
                    }
                }
                else {
                    exoPlayerView.hideController();
                }

                if(exoPlayer == null || !exoPlayer.getPlayWhenReady()) {

                    if(exoPlayer == null && mediaUri != null && !mediaUri.toString().equals("")) {
                        initializeMediaSession();
                        initializePlayer(mediaUri);
                    }

                    if(visibility == 0) {
                        exoPlayerView.showController();
                    }
                    else {
                        exoPlayerView.hideController();
                    }

                }
            }
        });

        // When headphones are removed, pausing the media player
        noisyReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if( exoPlayer != null && exoPlayer.getPlayWhenReady()) {
                    exoPlayer.setPlayWhenReady(false);
                }
            }
        };

        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        activity.registerReceiver(noisyReceiver, filter);

        // Detect Connectivity Connection
        // This listener is actually used in order to resume playing
        // if network connectivity was out for a little bit but now is stable again
        // we check that the connectivity action was connecting back to network,
        // that the player faced a problem playing because of it
        // and finally that it hasn't passed the time limit waiting for connection to come back
        // if the limit exceeded then we set a refresh button and let user press it when ready
        networkConnectivityReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                if (NetworkConnectivity.isNetworkAvailable(activity)
                        && repairNeeded
                        && stopWaitingForConnecion) {

                    repairNeeded = false;

                    restorePlaying();
                }
            }
        };

        IntentFilter filter2 = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        activity.registerReceiver(networkConnectivityReceiver, filter2);

        refreshMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restorePlaying();
            }
        });

        if(thumbnailUri != null){
            try{
                RequestOptions options = new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .centerCrop()
                        .placeholder(R.mipmap.ic_launcher_round)
                        .error(R.mipmap.ic_launcher_round)
                        .dontTransform();

                Glide.with(activity).load(thumbnail).apply(options)
                        .into(thumbnail);
            }
            catch (Exception ignore){
            }

        }

    }

    private void restorePlaying() {
        stopWaitingForConnecion = false;

        if(mediaUri != null && !mediaUri.toString().equals("")) {
            repairNeeded = false;
        }

        connectivityErrorLayout.setVisibility(View.GONE);

        releasePlayer();

        mediaLoadingProgressBar.setVisibility(View.VISIBLE);

        if(thumbnailUri != null){
            try{
                RequestOptions options = new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .centerCrop()
                        .dontTransform()
                        .placeholder(R.mipmap.ic_launcher_round)
                        .error(R.mipmap.ic_launcher_round);

                Glide.with(activity).load(thumbnail).apply(options)
                        .into(thumbnail);
            }
            catch (Exception ignore){
            }
        }

        thumbnail.setVisibility(View.VISIBLE);
        initializePlayer(mediaUri);
    }

    /*
     * Initializes the Media Session to be enabled with media buttons, transport controls, callbacks
     * and media controller.
     */
    public void initializeMediaSession() {

        mediaSession = new MediaSessionCompat(activity, TAG);

        // Enable callbacks from MediaButtons and TransportControls.
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        );

        mediaSession.setMediaButtonReceiver(null);

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player.
        mStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE
                );

        mediaSession.setPlaybackState(mStateBuilder.build());
        mediaSession.setCallback(new StepMediaSessionCallback());
        mediaSession.setActive(true);
    }

    @SuppressWarnings("unchecked")
    public void initializePlayer(final Uri mediaUri) {
        this.mediaUri = mediaUri;

        if (exoPlayer == null) {
            int result = audioManager.requestAudioFocus(
                    onAudioFocusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
            );

            BandwidthMeter defaultBandWidthMeter = new DefaultBandwidthMeter();
            TrackSelection.Factory videoTrackSelectionFactory =
                    new AdaptiveTrackSelection.Factory(defaultBandWidthMeter);

            DefaultTrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
            exoPlayer = ExoPlayerFactory.newSimpleInstance(activity, trackSelector);
            exoPlayer.addListener(componentListener);

            exoPlayerView.setPlayer(exoPlayer);

            DataSource.Factory mediaDataSourceFactory = new DefaultDataSourceFactory(
                    activity,
                    Util.getUserAgent(activity, activity.getString(R.string.app_name)),
                    (TransferListener<? super DataSource>) defaultBandWidthMeter);

            Cache cache = new SimpleCache(activity.getCacheDir(),
                    new LeastRecentlyUsedCacheEvictor(50 * 1024 * 1024));
            mediaDataSourceFactory = new CacheDataSourceFactory(cache,
                    mediaDataSourceFactory);


            final ExtractorMediaSource mediaSource =
                    new ExtractorMediaSource.Factory(mediaDataSourceFactory)
                            .createMediaSource(mediaUri);

            // restoring position
            if (startPos != 0) {
                exoPlayer.seekTo(startPos);
            }

            exoPlayer.prepare(mediaSource, false, false);
            exoPlayer.setPlayWhenReady(result == AUDIOFOCUS_REQUEST_GRANTED);
        }
    }

    private void releasePlayer() {
        if (exoPlayer != null) {
            exoPlayer.stop();
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    public void abandon() {
        if(exoPlayer != null){
            try {
                activity.unregisterReceiver(noisyReceiver);
                activity.unregisterReceiver(networkConnectivityReceiver);
            }
            catch (Exception ignore) {
            }
        }

        releasePlayer();

        audioManager.abandonAudioFocus(onAudioFocusChangeListener);
    }

    public long getContentPosition() {
        return exoPlayer == null ? -1 : exoPlayer.getContentPosition();
    }

    // Handle different states
    private class ComponentListener extends Player.DefaultEventListener {
        @Override
        public void onPlayerError(ExoPlaybackException error) {
            super.onPlayerError(error);

            repairNeeded = true;

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(!repairNeeded) {
                        return;
                    }

                    stopWaitingForConnecion = true;
                    startPos = getContentPosition();

                    Toast.makeText(activity,
                            activity.getString(R.string.error_media_player),
                            Toast.LENGTH_LONG
                    ).show();

                    exoPlayerView.hideController();
                    thumbnail.setVisibility(View.GONE);
                    mediaLoadingProgressBar.setVisibility(View.GONE);
                    connectivityErrorLayout.setVisibility(View.VISIBLE);
                }
            }, 5000);
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if(stopWaitingForConnecion) {
                exoPlayerView.hideController();
                thumbnail.setVisibility(View.GONE);
                mediaLoadingProgressBar.setVisibility(View.GONE);

                return;
            }

            if (playWhenReady) {

                /*
                 * request audio focus when user press play
                 * this actually runs after starting playing
                 * but since there is no other way to handle this as mention at an issue on ExoPlayer's repo
                 * we do it now and if focus get denied we immediately pause the player
                 * and hopefully the user doesn't notice it
                 */
                int result = audioManager.requestAudioFocus(onAudioFocusChangeListener,
                        AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN
                );

                // focus got denied --> immediately pause the player
                if(result != AUDIOFOCUS_REQUEST_GRANTED) {
                    exoPlayer.setPlayWhenReady(false);

                    return;
                }

                switch (playbackState) {
                    case Player.STATE_IDLE:
                        thumbnail.setVisibility(View.VISIBLE);
                        mediaLoadingProgressBar.setVisibility(View.VISIBLE);
                        exoPlayerView.hideController();

                        break;
                    case Player.STATE_BUFFERING:
                        thumbnail.setVisibility(View.VISIBLE);
                        mediaLoadingProgressBar.setVisibility(View.VISIBLE);
                        exoPlayerView.hideController();

                        break;
                    case Player.STATE_READY:
                        thumbnail.setVisibility(View.GONE);
                        mediaLoadingProgressBar.setVisibility(View.GONE);

                        mStateBuilder.setState(
                                PlaybackStateCompat.STATE_PLAYING,
                                exoPlayer.getCurrentPosition(),
                                1f
                        );

                        break;
                    case Player.STATE_ENDED:
                        thumbnail.setVisibility(View.GONE);
                        mediaLoadingProgressBar.setVisibility(View.GONE);
                        exoPlayerView.showController();

                        break;
                    default:
                        break;
                }
            }
            else {
                thumbnail.setVisibility(View.GONE);
                mediaLoadingProgressBar.setVisibility(View.GONE);

                if (playbackState == Player.STATE_READY) {
                    mStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                            exoPlayer.getCurrentPosition(), 1f);
                }
            }

            mediaSession.setPlaybackState(mStateBuilder.build());
        }
    }

    /*
     * Media Session Callbacks, where all external clients control the player.
     */
    private class StepMediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            exoPlayer.setPlayWhenReady(true);
        }

        @Override
        public void onPause() {
            exoPlayer.setPlayWhenReady(false);
        }

        @Override
        public void onSkipToPrevious() {
            exoPlayer.seekTo(0);
        }
    }

    /*
     * Broadcast Receiver registered to receive the MEDIA_BUTTON intent coming from clients.
     */
    public static class MediaReceiver extends BroadcastReceiver {

        public MediaReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            MediaButtonReceiver.handleIntent(mediaSession, intent);
        }

    }

}
