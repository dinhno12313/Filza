package dinhnguyen.filza.file.manager.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

import dinhnguyen.filza.file.manager.R;
import android.widget.ImageButton;
import dinhnguyen.filza.file.manager.constants.AudioConstants;
import dinhnguyen.filza.file.manager.media.AudioPlayerManager;
import dinhnguyen.filza.file.manager.ui.manager.ProgressUpdater;
import dinhnguyen.filza.file.manager.ui.manager.SeekBarManager;
import dinhnguyen.filza.file.manager.ui.viewmodel.AudioPlayerViewModel;
import dinhnguyen.filza.file.manager.viewmodel.AudioPlayerViewModelFactory;

public class MusicPlayerActivity extends AppCompatActivity implements ProgressUpdater.ProgressUpdateListener {
    
    private TextView textSongTitle;
    private TextView textCurrentTime;
    private TextView textTotalTime;
    private Slider seekBar;
    private ImageButton btnPlayPause;
    
    private AudioPlayerViewModel viewModel;
    private SeekBarManager seekBarManager;
    private ProgressUpdater progressUpdater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        
        initializeManagers();
        initializeViews();
        setupObservers();
        setupClickListeners();
        
        loadAudioFile();
    }

    private void initializeManagers() {
        AudioPlayerManager audioPlayerManager = new AudioPlayerManager(this);
        AudioPlayerViewModelFactory factory = new AudioPlayerViewModelFactory(audioPlayerManager);
        viewModel = new ViewModelProvider(this, factory).get(AudioPlayerViewModel.class);
        
        progressUpdater = new ProgressUpdater(this);
    }

    private void initializeViews() {
        // Set up toolbar with back navigation
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });
        
        textSongTitle = findViewById(R.id.textSongTitle);
        textCurrentTime = findViewById(R.id.textCurrentTime);
        textTotalTime = findViewById(R.id.textTotalTime);
        seekBar = findViewById(R.id.seekBar);
        btnPlayPause = findViewById(R.id.btnPlayPause);

        seekBarManager = new SeekBarManager(seekBar, textCurrentTime, textTotalTime);
        setupSeekBarListener();
    }
    
    private void setupSeekBarListener() {
        seekBarManager.setListener(new SeekBarManager.SeekBarListener() {
            @Override
            public void onSeekTo(int position) {
                // Only seek when user stops touching the seek bar
                // This prevents continuous seeking during drag
            }
            
            @Override
            public void onSeekStart() {
                // Stop progress updates while user is seeking
                progressUpdater.stopUpdates();
            }
            
            @Override
            public void onSeekStop() {
                // Get the final position and seek to it
                int finalPosition = seekBarManager.getCurrentSeekPosition();
                if (finalPosition > 0) {
                    viewModel.seekTo(finalPosition);
                }
                
                // Resume progress updates if playing
                if (viewModel.getIsPlaying().getValue() != null && viewModel.getIsPlaying().getValue()) {
                    progressUpdater.startUpdates();
                }
            }
        });
    }

    private void setupObservers() {
        viewModel.getSongTitle().observe(this, title -> {
            textSongTitle.setText(title);
            textSongTitle.setSelected(true); // Enable marquee
        });
        
        viewModel.getCurrentTime().observe(this, time -> {
            textCurrentTime.setText(time);
        });
        
        viewModel.getTotalTime().observe(this, time -> {
            textTotalTime.setText(time);
        });
        
        viewModel.getDuration().observe(this, duration -> {
            if (duration != null) {
                seekBarManager.setDuration(duration);
            }
        });
        
        viewModel.getIsPlaying().observe(this, isPlaying -> {
            updatePlayPauseButton(isPlaying);
            if (isPlaying) {
                progressUpdater.startUpdates();
            } else {
                progressUpdater.stopUpdates();
            }
        });
        
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                showToast(error);
                viewModel.clearError();
            }
        });
    }

    private void setupClickListeners() {
        btnPlayPause.setOnClickListener(v -> viewModel.togglePlayPause());
    }

    private void loadAudioFile() {
        String filePath = getIntent().getStringExtra(AudioConstants.EXTRA_FILE_PATH);
        viewModel.loadAudioFile(filePath);
    }

    private void updatePlayPauseButton(Boolean isPlaying) {
        if (isPlaying != null) {
            if (isPlaying) {
                btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
            } else {
                btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
            }
        }
    }

    @Override
    public void onProgressUpdate() {
        viewModel.updateProgress();
        
        // Update seekbar progress only if not user seeking
        Integer currentPosition = viewModel.getCurrentPosition().getValue();
        Integer duration = viewModel.getDuration().getValue();
        
        if (currentPosition != null && duration != null && duration > 0 && !seekBarManager.isUserSeeking()) {
            seekBarManager.updateProgress(currentPosition, duration);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        progressUpdater.stopUpdates();
        
        // Pause playback when activity is paused
        if (viewModel.getIsPlaying().getValue() != null && viewModel.getIsPlaying().getValue()) {
            viewModel.togglePlayPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progressUpdater.stopUpdates();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onBackPressed() {
        // Stop playback and finish activity
        if (viewModel.getIsPlaying().getValue() != null && viewModel.getIsPlaying().getValue()) {
            viewModel.togglePlayPause();
        }
        finish();
    }
} 