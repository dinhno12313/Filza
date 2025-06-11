package dinhnguyen.filza.file.manager.viewmodel;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import dinhnguyen.filza.file.manager.media.AudioPlayerManager;
import dinhnguyen.filza.file.manager.ui.viewmodel.AudioPlayerViewModel;

public class AudioPlayerViewModelFactory implements ViewModelProvider.Factory {
    
    private final AudioPlayerManager audioPlayerManager;
    
    public AudioPlayerViewModelFactory(AudioPlayerManager audioPlayerManager) {
        this.audioPlayerManager = audioPlayerManager;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AudioPlayerViewModel.class)) {
            return (T) new AudioPlayerViewModel(audioPlayerManager);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
} 