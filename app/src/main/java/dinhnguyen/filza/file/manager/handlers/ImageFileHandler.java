package dinhnguyen.filza.file.manager.handlers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import dinhnguyen.filza.file.manager.ui.ImageViewerActivity;

/**
 * Handler for common image file types (jpg, jpeg, png, gif).
 * Opens images using the custom ImageViewerActivity.
 */
public class ImageFileHandler implements FileHandler {
    private static final Set<String> SUPPORTED_EXTENSIONS = new HashSet<>(
            Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp")
    );

    private static final Set<String> SUPPORTED_MIME_TYPES = new HashSet<>(
            Arrays.asList(
                    "image/jpeg",
                    "image/jpg",
                    "image/png",
                    "image/gif",
                    "image/bmp",
                    "image/webp"
            )
    );

    @Override
    public boolean canHandle(String mimeType, String fileName) {
        if (mimeType != null && SUPPORTED_MIME_TYPES.contains(mimeType.toLowerCase())) {
            return true;
        }

        String extension = getFileExtension(fileName);
        return extension != null && SUPPORTED_EXTENSIONS.contains(extension.toLowerCase());
    }

    @Override
    public void handle(Context context, Uri uri) {
        // Convert URI to file path for ImageViewerActivity
        String filePath = uri.getPath();
        if (filePath != null) {
            Intent intent = new Intent(context, ImageViewerActivity.class);
            intent.putExtra("filePath", filePath);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    private String getMimeType(Uri uri) {
        String extension = getFileExtension(uri.toString());
        if (extension != null) {
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (mimeType != null) {
                return mimeType;
            }
        }
        return "image/*";
    }
} 