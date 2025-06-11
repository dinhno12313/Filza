package dinhnguyen.filza.file.manager.manager;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import dinhnguyen.filza.file.manager.constants.FileConstants;
import dinhnguyen.filza.file.manager.handlers.FileHandler;

public class FileImportManager {
    
    private final Context context;
    private final List<FileHandler> fileHandlers;
    
    public FileImportManager(Context context, List<FileHandler> fileHandlers) {
        this.context = context;
        this.fileHandlers = fileHandlers;
    }
    
    public void importFile(Uri uri, File targetDirectory, Runnable onSuccess) {
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            String originalName = getFileNameFromUri(uri);
            String fileName = (originalName != null ? originalName : System.currentTimeMillis() + "_imported");
            
            File targetFile = new File(targetDirectory, fileName);
            copyFile(in, targetFile);
            
            handleImportedFile(uri, fileName);
            onSuccess.run();
            
        } catch (IOException e) {
            showToast(FileConstants.ERROR_IMPORT_FAILED);
            e.printStackTrace();
        }
    }
    
    private void copyFile(InputStream in, File targetFile) throws IOException {
        try (FileOutputStream out = new FileOutputStream(targetFile)) {
            byte[] buffer = new byte[FileConstants.BUFFER_SIZE];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
        }
    }
    
    private void handleImportedFile(Uri uri, String fileName) {
        String mimeType = context.getContentResolver().getType(uri);
        if (mimeType == null) mimeType = "*/*";
        
        for (FileHandler handler : fileHandlers) {
            if (handler.canHandle(mimeType, fileName)) {
                handler.handle(context, uri);
                return;
            }
        }
        
        showToast(FileConstants.ERROR_NO_HANDLER);
    }
    
    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) result = cursor.getString(nameIndex);
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }
    
    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
} 