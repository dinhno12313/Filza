package dinhnguyen.filza.file.manager.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.File;

import dinhnguyen.filza.file.manager.constants.AudioConstants;
import dinhnguyen.filza.file.manager.media.AudioPlayerManager;
import dinhnguyen.filza.file.manager.utils.AudioFileValidator;
import dinhnguyen.filza.file.manager.utils.TimeFormatter;

public class AudioPlayerViewModel extends ViewModel {
    
    private final AudioPlayerManager audioPlayerManager;
    
    private final MutableLiveData<String> songTitle = new MutableLiveData<>();
    private final MutableLiveData<String> currentTime = new MutableLiveData<>();
    private final MutableLiveData<String> totalTime = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentPosition = new MutableLiveData<>();
    private final MutableLiveData<Integer> duration = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    
    private String currentFilePath;
    
    public AudioPlayerViewModel(AudioPlayerManager audioPlayerManager) {
        this.audioPlayerManager = audioPlayerManager;
        setupAudioPlayerListener();
    }
    
    private void setupAudioPlayerListener() {
        audioPlayerManager.setListener(new AudioPlayerManager.AudioPlayerListener() {
            @Override
            public void onPlaybackStarted() {
                isPlaying.setValue(true);
            }
            
            @Override
            public void onPlaybackPaused() {
                isPlaying.setValue(false);
            }
            
            @Override
            public void onPlaybackCompleted() {
                isPlaying.setValue(false);
                currentPosition.setValue(0);
                currentTime.setValue(TimeFormatter.formatTime(0));
            }
            
            @Override
            public void onPlaybackError(String error) {
                errorMessage.setValue(error);
            }
            
            @Override
            public void onDurationChanged(int newDuration) {
                duration.setValue(newDuration);
                totalTime.setValue(TimeFormatter.formatTime(newDuration));
            }
        });
    }
    
    public LiveData<String> getSongTitle() {
        return songTitle;
    }
    
    public LiveData<String> getCurrentTime() {
        return currentTime;
    }
    
    public LiveData<String> getTotalTime() {
        return totalTime;
    }
    
    public LiveData<Integer> getCurrentPosition() {
        return currentPosition;
    }
    
    public LiveData<Integer> getDuration() {
        return duration;
    }
    
    public LiveData<Boolean> getIsPlaying() {
        return isPlaying;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    public boolean loadAudioFile(String filePath) {
        currentFilePath = filePath;
        
        if (filePath == null) {
            errorMessage.setValue(AudioConstants.ERROR_NO_FILE_PATH);
            return false;
        }
        
        File audioFile = new File(filePath);
        if (!audioFile.exists()) {
            errorMessage.setValue(AudioConstants.ERROR_FILE_NOT_EXISTS);
            return false;
        }
        
        if (!AudioFileValidator.isValidAudioFile(audioFile)) {
            errorMessage.setValue(AudioConstants.ERROR_INVALID_AUDIO_FILE);
            return false;
        }
        
        boolean success = audioPlayerManager.loadAudioFile(filePath);
        if (success) {
            songTitle.setValue(AudioFileValidator.getFileNameWithoutExtension(audioFile.getName()));
        }
        
        return success;
    }
    
    public void togglePlayPause() {
        if (audioPlayerManager.isReady()) {
            if (audioPlayerManager.isPlaying()) {
                audioPlayerManager.pause();
            } else {
                audioPlayerManager.play();
            }
        } else {
            errorMessage.setValue(AudioConstants.ERROR_PLAYER_NOT_READY);
        }
    }
    
    public void seekTo(int position) {
        if (audioPlayerManager.isReady()) {
            // Ensure position is within valid bounds
            int duration = audioPlayerManager.getDuration();
            if (position < 0) position = 0;
            if (position > duration) position = duration;
            
            audioPlayerManager.seekTo(position);
            currentPosition.setValue(position);
            currentTime.setValue(TimeFormatter.formatTime(position));
        }
    }
    
    public void updateProgress() {
        if (audioPlayerManager.isReady() && audioPlayerManager.isPlaying()) {
            int position = audioPlayerManager.getCurrentPosition();
            currentPosition.setValue(position);
            currentTime.setValue(TimeFormatter.formatTime(position));
        }
    }
    
    public void reset() {
        currentPosition.setValue(0);
        currentTime.setValue(TimeFormatter.formatTime(0));
        isPlaying.setValue(false);
    }
    
    public void clearError() {
        errorMessage.setValue(null);
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        audioPlayerManager.release();
    }
} 