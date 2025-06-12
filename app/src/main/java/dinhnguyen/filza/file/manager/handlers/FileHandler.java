package dinhnguyen.filza.file.manager.handlers;


import android.content.Context;
import android.net.Uri;

/**
 * Interface for handling different types of files in the application.
 * Implementations should handle specific file types based on MIME type and file extension.
 */
public interface FileHandler {
    /**
     * Determines if this handler can handle the given file.
     *
     * @param mimeType The MIME type of the file
     * @param fileName The name of the file
     * @return true if this handler can handle the file, false otherwise
     */
    boolean canHandle(String mimeType, String fileName);

    /**
     * Handles the file with the given URI.
     *
     * @param context The context to use for handling the file
     * @param uri The URI of the file to handle
     */
    void handle(Context context, Uri uri);
} 