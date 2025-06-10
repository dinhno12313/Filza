package dinhnguyen.filza.file.manager.ui;

import android.app.PictureInPictureParams;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Rational;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import java.io.File;

import dinhnguyen.filza.file.manager.R;
import com.google.android.material.button.MaterialButton;

public class VideoPlayerActivity extends AppCompatActivity {

    private static final String EXTRA_FILE_PATH = "filePath";
    private static final String PREFS_NAME = "VideoPlayerPrefs";
    private static final String PREF_POSITION_PREFIX = "position_";
    private static final String PREF_FULLSCREEN = "fullscreen_mode";
    
    private Toolbar toolbar;
    private FrameLayout videoContainer;
    private VideoView videoView;
    private MaterialButton btnFullscreen;
    private MaterialButton btnPiP;
    
    private String videoFilePath;
    private boolean isFullscreen = false;
    private boolean isPiPMode = false;
    private int savedPosition = 0;
    private boolean isVideoPrepared = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        
        initializeViews();
        setupToolbar();
        setupVideoView();
        handleVideoFile();
        loadSavedPreferences();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        videoContainer = findViewById(R.id.videoContainer);
        videoView = findViewById(R.id.videoView);
        btnFullscreen = findViewById(R.id.btnFullscreen);
        btnPiP = findViewById(R.id.btnPiP);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Video Player");
        }
    }
    
    private void setupVideoView() {
        // Set up MediaController
        android.widget.MediaController mediaController = new android.widget.MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        
        // Set up video completion listener
        videoView.setOnCompletionListener(mp -> {
            // Reset position when video completes
            saveVideoPosition(0);
            showToast("Video đã phát xong");
        });
        
        // Set up video error listener
        videoView.setOnErrorListener((mp, what, extra) -> {
            showToast("Lỗi phát video: " + getErrorMessage(what));
            return true;
        });
        
        // Set up video prepared listener
        videoView.setOnPreparedListener(mp -> {
            isVideoPrepared = true;
            // Restore saved position
            if (savedPosition > 0) {
                videoView.seekTo(savedPosition);
            }
            showToast("Video đã sẵn sàng phát");
        });
        
        // Set up fullscreen button
        btnFullscreen.setOnClickListener(v -> toggleFullscreen());
        
        // Set up PiP button (only for Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            btnPiP.setVisibility(View.VISIBLE);
            btnPiP.setOnClickListener(v -> enterPictureInPictureMode());
        } else {
            btnPiP.setVisibility(View.GONE);
        }
    }

    private void handleVideoFile() {
        String filePath = getIntent().getStringExtra(EXTRA_FILE_PATH);
        
        if (filePath == null) {
            showToast("Không nhận được đường dẫn file video");
            finish();
            return;
        }

        videoFilePath = filePath;
        File videoFile = new File(filePath);
        
        if (!videoFile.exists()) {
            showToast("File video không tồn tại");
            return;
        }

        if (!isVideoFile(videoFile.getName())) {
            showToast("File không phải là file video");
            return;
        }

        loadVideoFile(videoFile);
    }

    private boolean isVideoFile(String fileName) {
        if (fileName == null) return false;
        
        String lowerFileName = fileName.toLowerCase();
        return lowerFileName.endsWith(".mp4") || 
               lowerFileName.endsWith(".mkv") || 
               lowerFileName.endsWith(".3gp") || 
               lowerFileName.endsWith(".avi") || 
               lowerFileName.endsWith(".mov") || 
               lowerFileName.endsWith(".wmv") || 
               lowerFileName.endsWith(".flv") || 
               lowerFileName.endsWith(".webm");
    }

    private void loadVideoFile(File videoFile) {
        try {
            // Update toolbar title
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(videoFile.getName());
            }
            
            // Load video from file path
            Uri videoUri = Uri.fromFile(videoFile);
            videoView.setVideoURI(videoUri);
            
            // Start preparing the video
            videoView.requestFocus();
            
        } catch (Exception e) {
            showToast("Không thể tải video: " + e.getMessage());
        }
    }

    private void toggleFullscreen() {
        if (isFullscreen) {
            exitFullscreen();
        } else {
            enterFullscreen();
        }
    }

    private void enterFullscreen() {
        isFullscreen = true;
        
        // Hide toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        // Hide system UI
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN |
                       View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                       View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
        
        // Update button icon
        btnFullscreen.setIcon(getDrawable(R.drawable.custom_icon_fullscreen));
        
        saveFullscreenPreference(true);
    }

    private void exitFullscreen() {
        isFullscreen = false;
        
        // Show toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().show();
        }
        
        // Show system UI
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        
        // Update button icon
        btnFullscreen.setIcon(getDrawable(R.drawable.custom_icon_fullscreen));
        
        saveFullscreenPreference(false);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void enterPictureInPictureMode() {
        if (!isVideoPrepared) {
            showToast("Video chưa sẵn sàng cho PiP");
            return;
        }
        
        try {
            // Create PiP parameters
            Rational aspectRatio = new Rational(16, 9); // Default aspect ratio
            PictureInPictureParams params = new PictureInPictureParams.Builder()
                    .setAspectRatio(aspectRatio)
                    .build();
            
            // Enter PiP mode
            enterPictureInPictureMode(params);
            isPiPMode = true;
            
        } catch (Exception e) {
            showToast("Không thể vào chế độ PiP: " + e.getMessage());
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
        
        isPiPMode = isInPictureInPictureMode;
        
        if (isInPictureInPictureMode) {
            // Hide controls in PiP mode
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }
            btnFullscreen.setVisibility(View.GONE);
            btnPiP.setVisibility(View.GONE);
        } else {
            // Show controls when exiting PiP mode
            if (getSupportActionBar() != null) {
                getSupportActionBar().show();
            }
            btnFullscreen.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                btnPiP.setVisibility(View.VISIBLE);
            }
        }
    }

    private void saveVideoPosition(int position) {
        if (videoFilePath != null) {
            String key = PREF_POSITION_PREFIX + videoFilePath.hashCode();
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit().putInt(key, position).apply();
        }
    }

    private void loadSavedPreferences() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // Load saved position
        if (videoFilePath != null) {
            String key = PREF_POSITION_PREFIX + videoFilePath.hashCode();
            savedPosition = prefs.getInt(key, 0);
        }
        
        // Load fullscreen preference
        isFullscreen = prefs.getBoolean(PREF_FULLSCREEN, false);
        if (isFullscreen) {
            // Apply fullscreen on next frame
            videoContainer.post(this::enterFullscreen);
        }
    }

    private void saveFullscreenPreference(boolean fullscreen) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(PREF_FULLSCREEN, fullscreen).apply();
    }

    private String getErrorMessage(int what) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                return "Lỗi không xác định";
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                return "Server media đã dừng";
            default:
                return "Lỗi " + what;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.video_player_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_fullscreen) {
            toggleFullscreen();
            return true;
        } else if (id == R.id.action_pip && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            enterPictureInPictureMode();
            return true;
        } else if (id == R.id.action_reset_position) {
            resetVideoPosition();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void resetVideoPosition() {
        if (isVideoPrepared) {
            videoView.seekTo(0);
            saveVideoPosition(0);
            showToast("Đã đặt lại vị trí video");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        
        // Save current position
        if (isVideoPrepared && videoView.isPlaying()) {
            int currentPosition = videoView.getCurrentPosition();
            saveVideoPosition(currentPosition);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Restore fullscreen if needed
        if (isFullscreen && !isPiPMode) {
            enterFullscreen();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Save final position
        if (isVideoPrepared) {
            int currentPosition = videoView.getCurrentPosition();
            saveVideoPosition(currentPosition);
        }
        
        // Clean up video view
        if (videoView != null) {
            videoView.stopPlayback();
        }
    }

    @Override
    public void onBackPressed() {
        if (isFullscreen) {
            exitFullscreen();
        } else {
            super.onBackPressed();
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
} 