package dinhnguyen.filza.file.manager.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages file access permissions for different Android versions
 * Implements modern permission handling for Android 13+ (API 33+)
 */
public class PermissionManager {
    
    private static final String TAG = "PermissionManager";
    
    // Permissions for Android 13+ (API 33+)
    private static final String[] MEDIA_PERMISSIONS = {
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO
    };
    
    // Legacy permissions for Android 12 and below
    private static final String[] LEGACY_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    
    /**
     * Check if all required file access permissions are granted
     */
    public static boolean hasFileAccessPermissions(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses granular media permissions
            return hasAllPermissions(context, MEDIA_PERMISSIONS);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ uses scoped storage, only need READ_EXTERNAL_STORAGE
            return ContextCompat.checkSelfPermission(context, 
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            // Android 10 and below need both read and write permissions
            return hasAllPermissions(context, LEGACY_PERMISSIONS);
        }
    }
    
    /**
     * Get the list of permissions that need to be requested
     */
    public static String[] getRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return MEDIA_PERMISSIONS;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        } else {
            return LEGACY_PERMISSIONS;
        }
    }
    
    /**
     * Check if external storage is available and accessible
     */
    public static boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || 
               Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }
    
    /**
     * Check if the app has access to a specific directory
     */
    public static boolean canAccessDirectory(@NonNull Context context, @NonNull String directoryPath) {
        if (!hasFileAccessPermissions(context)) {
            return false;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11+, check if directory is in app's scoped storage
            return isInAppScopedStorage(context, directoryPath);
        }
        
        return true;
    }
    
    /**
     * Check if a path is within the app's scoped storage area
     */
    private static boolean isInAppScopedStorage(@NonNull Context context, @NonNull String path) {
        // App's external files directory
        String appExternalDir = context.getExternalFilesDir(null).getAbsolutePath();
        // App's external cache directory
        String appCacheDir = context.getExternalCacheDir().getAbsolutePath();
        
        return path.startsWith(appExternalDir) || path.startsWith(appCacheDir);
    }
    
    /**
     * Check if all permissions in the array are granted
     */
    private static boolean hasAllPermissions(@NonNull Context context, @NonNull String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) 
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Get permissions that are not granted
     */
    public static List<String> getDeniedPermissions(@NonNull Context context) {
        List<String> deniedPermissions = new ArrayList<>();
        String[] requiredPermissions = getRequiredPermissions();
        
        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(context, permission) 
                    != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permission);
            }
        }
        
        return deniedPermissions;
    }
    
    /**
     * Check if any permission should show rationale
     */
    public static boolean shouldShowPermissionRationale(@NonNull Context context) {
        if (context instanceof android.app.Activity) {
            android.app.Activity activity = (android.app.Activity) context;
            String[] permissions = getRequiredPermissions();
            
            for (String permission : permissions) {
                if (androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Callback interface for permission results
     */
    public interface PermissionCallback {
        void onPermissionsGranted();
        void onPermissionsDenied();
    }
} 