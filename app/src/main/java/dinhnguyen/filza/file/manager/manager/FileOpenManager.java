package dinhnguyen.filza.file.manager.manager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import java.io.File;

import dinhnguyen.filza.file.manager.constants.FileConstants;
import dinhnguyen.filza.file.manager.ui.ImageViewerActivity;
import dinhnguyen.filza.file.manager.ui.MusicPlayerActivity;
import dinhnguyen.filza.file.manager.ui.PdfViewerActivity;
import dinhnguyen.filza.file.manager.ui.TxtViewerActivity;
import dinhnguyen.filza.file.manager.ui.VideoPlayerActivity;
import dinhnguyen.filza.file.manager.utils.FileTypeDetector;

public class FileOpenManager {
    
    private final Context context;
    private final Runnable refreshCallback;
    
    public FileOpenManager(Context context) {
        this(context, null);
    }
    
    public FileOpenManager(Context context, Runnable refreshCallback) {
        this.context = context;
        this.refreshCallback = refreshCallback;
    }
    
    public void openFile(File file) {
        if (!file.exists()) {
            showToast(FileConstants.ERROR_FILE_NOT_EXISTS);
            return;
        }
        
        String fileName = file.getName().toLowerCase();
        
        if (FileTypeDetector.isImageFile(fileName)) {
            openImageFile(file);
        } else if (FileTypeDetector.isAudioFile(fileName)) {
            openAudioFile(file);
        } else if (FileTypeDetector.isPdfFile(fileName)) {
            openPdfFile(file);
        } else if (FileTypeDetector.isTextFile(fileName)) {
            openTextFile(file);
        } else if (FileTypeDetector.isVideoFile(fileName)) {
            openVideoFile(file);
        } else if (FileTypeDetector.isZipFile(fileName)) {
            openZipFile(file);
        } else {
            openWithSystemApp(file);
        }
    }
    
    private void openImageFile(File file) {
        Intent intent = new Intent(context, ImageViewerActivity.class);
        intent.putExtra(FileConstants.EXTRA_FILE_PATH, file.getAbsolutePath());
        context.startActivity(intent);
    }
    
    private void openAudioFile(File file) {
        Intent intent = new Intent(context, MusicPlayerActivity.class);
        intent.putExtra(FileConstants.EXTRA_FILE_PATH, file.getAbsolutePath());
        context.startActivity(intent);
    }
    
    private void openPdfFile(File file) {
        Intent intent = new Intent(context, PdfViewerActivity.class);
        intent.putExtra(FileConstants.EXTRA_FILE_PATH, file.getAbsolutePath());
        context.startActivity(intent);
    }
    
    private void openTextFile(File file) {
        Intent intent = new Intent(context, TxtViewerActivity.class);
        intent.putExtra(FileConstants.EXTRA_FILE_PATH, file.getAbsolutePath());
        context.startActivity(intent);
    }
    
    private void openVideoFile(File file) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.putExtra(FileConstants.EXTRA_FILE_PATH, file.getAbsolutePath());
        context.startActivity(intent);
    }
    
    private void openZipFile(File file) {
        // For zip files, we'll unzip them when clicked
        try {
            // Create destination directory with the same name as the zip file (without extension)
            String fileName = file.getName();
            String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
            File destDir = new File(file.getParent(), baseName);
            
            // If destination already exists, add a number suffix
            int counter = 1;
            while (destDir.exists()) {
                destDir = new File(file.getParent(), baseName + "(" + counter + ")");
                counter++;
            }
            
            destDir.mkdir();
            unzipFile(file, destDir);
            
            showToast(FileConstants.SUCCESS_FILE_UNZIPPED);
            
            // Refresh the file list
            if (refreshCallback != null) {
                refreshCallback.run();
            }
        } catch (Exception e) {
            showToast(FileConstants.ERROR_UNZIP_FAILED + ": " + e.getMessage());
        }
    }
    
    private void unzipFile(File zipFile, File destDir) throws java.io.IOException {
        try (java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(new java.io.FileInputStream(zipFile))) {
            java.util.zip.ZipEntry entry;
            byte[] buffer = new byte[1024];
            
            while ((entry = zis.getNextEntry()) != null) {
                File file = new File(destDir, entry.getName());
                
                // Create parent directories if they don't exist
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    // Create parent directories for the file
                    File parent = file.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }
                    
                    // Write the file
                    try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                        int length;
                        while ((length = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, length);
                        }
                    }
                }
                
                zis.closeEntry();
            }
        }
    }
    
    private void openWithSystemApp(File file) {
        try {
            Uri uri = Uri.fromFile(file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String mimeType = FileTypeDetector.getMimeType(file.getName());
            intent.setDataAndType(uri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {
                showToast(FileConstants.ERROR_NO_APP_FOUND);
            }
        } catch (Exception e) {
            showToast(FileConstants.ERROR_CANNOT_OPEN_FILE + ": " + e.getMessage());
        }
    }
    
    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
} 