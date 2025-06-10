package dinhnguyen.filza.file.manager.ui.manager;

import android.widget.TextView;

import com.google.android.material.slider.Slider;

import dinhnguyen.filza.file.manager.utils.TimeFormatter;

public class SeekBarManager {
    
    private final Slider slider;
    private final TextView currentTimeText;
    private final TextView totalTimeText;
    
    private boolean isUserSeeking = false;
    private int totalDuration = 0;
    private SeekBarListener listener;
    
    public interface SeekBarListener {
        void onSeekTo(int position);
        void onSeekStart();
        void onSeekStop();
    }
    
    public SeekBarManager(Slider slider, TextView currentTimeText, TextView totalTimeText) {
        this.slider = slider;
        this.currentTimeText = currentTimeText;
        this.totalTimeText = totalTimeText;
        
        setupSlider();
    }
    
    public void setListener(SeekBarListener listener) {
        this.listener = listener;
    }
    
    private void setupSlider() {
        slider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(Slider slider, float value, boolean fromUser) {
                if (fromUser && totalDuration > 0) {
                    // Convert percentage to actual time position
                    int position = (int) ((value / 100.0) * totalDuration);
                    updateCurrentTimeDisplay(position);
                }
            }
        });
        
        slider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(Slider slider) {
                isUserSeeking = true;
                if (listener != null) {
                    listener.onSeekStart();
                }
            }

            @Override
            public void onStopTrackingTouch(Slider slider) {
                isUserSeeking = false;
                if (listener != null && totalDuration > 0) {
                    // Convert percentage to actual time position and seek
                    int position = (int) ((slider.getValue() / 100.0) * totalDuration);
                    listener.onSeekTo(position);
                }
                if (listener != null) {
                    listener.onSeekStop();
                }
            }
        });
    }
    
    public void setDuration(int duration) {
        this.totalDuration = duration;
        slider.setValueTo(100);
        totalTimeText.setText(TimeFormatter.formatTime(duration));
    }
    
    public void updateProgress(int currentPosition, int totalDuration) {
        if (!isUserSeeking && totalDuration > 0) {
            // Calculate percentage based on current position
            int progress = (int) ((double) currentPosition / totalDuration * 100);
            slider.setValue(progress);
            currentTimeText.setText(TimeFormatter.formatTime(currentPosition));
        }
    }
    
    public void updateCurrentTime(int currentPosition) {
        if (!isUserSeeking) {
            currentTimeText.setText(TimeFormatter.formatTime(currentPosition));
        }
    }
    
    private void updateCurrentTimeDisplay(int position) {
        currentTimeText.setText(TimeFormatter.formatTime(position));
    }
    
    public void reset() {
        slider.setValue(0);
        currentTimeText.setText(TimeFormatter.formatTime(0));
        totalDuration = 0;
        isUserSeeking = false;
    }
    
    public boolean isUserSeeking() {
        return isUserSeeking;
    }
    
    public boolean isSliderBeingTouched() {
        return isUserSeeking;
    }
    
    public int getCurrentSeekPosition() {
        if (totalDuration > 0) {
            return (int) ((slider.getValue() / 100.0) * totalDuration);
        }
        return 0;
    }
} 