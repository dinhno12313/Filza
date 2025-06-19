package dinhnguyen.filza.file.manager.media;

import android.content.Context;
import android.media.MediaPlayer;
import android.widget.Toast;

import java.io.File;

import dinhnguyen.filza.file.manager.constants.AudioConstants;
import dinhnguyen.filza.file.manager.utils.AudioFileValidator;

public class AudioPlayerManager {
    
    private final Context context;
    private MediaPlayer mediaPlayer;
    private AudioPlayerListener listener;
    
    public interface AudioPlayerListener {
        void onPlaybackStarted();
        void onPlaybackPaused();
        void onPlaybackCompleted();
        void onPlaybackError(String error);
        void onDurationChanged(int duration);
    }
    
    public AudioPlayerManager(Context context) {
        this.context = context;
    }
    
    public void setListener(AudioPlayerListener listener) {
        this.listener = listener;
    }
    
    public boolean loadAudioFile(String filePath) {
        if (filePath == null) {
            notifyError(AudioConstants.ERROR_NO_FILE_PATH);
            return false;
        }

        File audioFile = new File(filePath);
        if (!audioFile.exists()) {
            notifyError(AudioConstants.ERROR_FILE_NOT_EXISTS);
            return false;
        }

        if (!AudioFileValidator.isValidAudioFile(audioFile)) {
            notifyError(AudioConstants.ERROR_INVALID_AUDIO_FILE);
            return false;
        }

        return setupMediaPlayer(filePath);
    }
    
    private boolean setupMediaPlayer(String filePath) {
        try {
            releaseMediaPlayer();
            
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            
            setupMediaPlayerListeners();
            
            if (listener != null) {
                listener.onDurationChanged(mediaPlayer.getDuration());
            }
            
            return true;
            
        } catch (Exception e) {
            notifyError(AudioConstants.ERROR_LOAD_FAILED + ": " + e.getMessage());
            return false;
        }
    }
    
    private void setupMediaPlayerListeners() {
        if (mediaPlayer == null) return;
        
        mediaPlayer.setOnCompletionListener(mp -> {
            if (listener != null) {
                listener.onPlaybackCompleted();
            }
        });
        
        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            String error = getErrorMessage(what);
            notifyError(error);
            return true;
        });
    }
    
    public boolean play() {
        if (mediaPlayer == null) {
            notifyError(AudioConstants.ERROR_PLAYER_NOT_READY);
            return false;
        }

        if (!mediaPlayer.isPlaying()) {
            try {
                mediaPlayer.start();
                if (listener != null) {
                    listener.onPlaybackStarted();
                }
                return true;
            } catch (Exception e) {
                notifyError(AudioConstants.ERROR_PLAY_FAILED + ": " + e.getMessage());
                return false;
            }
        }
        return false;
    }
    
    public boolean pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            try {
                mediaPlayer.pause();
                if (listener != null) {
                    listener.onPlaybackPaused();
                }
                return true;
            } catch (Exception e) {
                notifyError(AudioConstants.ERROR_PAUSE_FAILED + ": " + e.getMessage());
                return false;
            }
        }
        return false;
    }
    
    public boolean seekTo(int position) {
        if (mediaPlayer != null) {
            try {
                // Ensure position is within valid bounds
                int duration = mediaPlayer.getDuration();
                if (position < 0) position = 0;
                if (duration > 0 && position > duration) position = duration;
                
                mediaPlayer.seekTo(position);
                return true;
            } catch (Exception e) {
                notifyError(AudioConstants.ERROR_SEEK_FAILED + ": " + e.getMessage());
                return false;
            }
        }
        return false;
    }
    
    public int getCurrentPosition() {
        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
    }
    
    public int getDuration() {
        return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
    }
    
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }
    
    public boolean isReady() {
        return mediaPlayer != null;
    }
    
    public void release() {
        releaseMediaPlayer();
    }
    
    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
    
    private String getErrorMessage(int what) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                return AudioConstants.ERROR_UNKNOWN;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                return AudioConstants.ERROR_SERVER_DIED;
            default:
                return AudioConstants.ERROR_UNKNOWN + " (" + what + ")";
        }
    }
    
    private void notifyError(String error) {
        if (listener != null) {
            listener.onPlaybackError(error);
        }
    }
} 