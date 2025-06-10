package dinhnguyen.filza.file.manager.ui.manager;

import android.os.Handler;
import android.os.Looper;

import dinhnguyen.filza.file.manager.constants.AudioConstants;

public class ProgressUpdater {
    
    private final Handler handler;
    private final Runnable updateRunnable;
    private final ProgressUpdateListener listener;
    
    private boolean isUpdating = false;
    
    public interface ProgressUpdateListener {
        void onProgressUpdate();
    }
    
    public ProgressUpdater(ProgressUpdateListener listener) {
        this.listener = listener;
        this.handler = new Handler(Looper.getMainLooper());
        
        this.updateRunnable = new Runnable() {
            @Override
            public void run() {
                if (isUpdating && listener != null) {
                    listener.onProgressUpdate();
                }
                if (isUpdating) {
                    handler.postDelayed(this, AudioConstants.DEFAULT_UPDATE_INTERVAL);
                }
            }
        };
    }
    
    public void startUpdates() {
        isUpdating = true;
        handler.post(updateRunnable);
    }
    
    public void stopUpdates() {
        isUpdating = false;
        handler.removeCallbacks(updateRunnable);
    }
    
    public boolean isUpdating() {
        return isUpdating;
    }
} 