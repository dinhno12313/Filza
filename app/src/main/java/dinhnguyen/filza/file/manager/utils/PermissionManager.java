package dinhnguyen.filza.file.manager.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for managing file access permissions across different Android versions
 */
public class PermissionManager {
    
    /**
     * Get required permissions for file access based on Android version
     */
    public static String[] getRequiredPermissions() {
        List<String> permissions = new ArrayList<>();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ - Use granular media permissions
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO);
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // Android 14+ - Add visual user selected permission
                permissions.add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0+ - Use READ_EXTERNAL_STORAGE
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        
        return permissions.toArray(new String[0]);
    }
    
    /**
     * Check if all required permissions are granted
     */
    public static boolean hasRequiredPermissions(Context context) {
        String[] permissions = getRequiredPermissions();
        
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
    public static String[] getDeniedPermissions(Context context) {
        List<String> deniedPermissions = new ArrayList<>();
        String[] permissions = getRequiredPermissions();
        
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) 
                != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permission);
            }
        }
        
        return deniedPermissions.toArray(new String[0]);
    }
    
    /**
     * Check if permission should show rationale
     */
    public static boolean shouldShowPermissionRationale(Context context, String permission) {
        if (context instanceof android.app.Activity) {
            return ((android.app.Activity) context).shouldShowRequestPermissionRationale(permission);
        }
        return false;
    }
    
    /**
     * Get permission description for user
     */
    public static String getPermissionDescription(String permission) {
        switch (permission) {
            case Manifest.permission.READ_MEDIA_IMAGES:
                return "Truy cập hình ảnh để xem và chỉnh sửa";
            case Manifest.permission.READ_MEDIA_VIDEO:
                return "Truy cập video để phát và quản lý";
            case Manifest.permission.READ_MEDIA_AUDIO:
                return "Truy cập âm thanh để phát nhạc";
            case Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED:
                return "Truy cập tệp đa phương tiện được chọn";
            case Manifest.permission.READ_EXTERNAL_STORAGE:
                return "Truy cập bộ nhớ để quản lý tệp";
            default:
                return "Quyền truy cập cần thiết";
        }
    }
} 