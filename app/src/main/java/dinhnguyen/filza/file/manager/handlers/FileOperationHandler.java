package dinhnguyen.filza.file.manager.handlers;
import dinhnguyen.filza.file.manager.listener.FileActionListener;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import java.io.File;
import dinhnguyen.filza.file.manager.R;

public class FileOperationHandler implements FileActionListener {

    private final Context context;
    private final Refreshable reloader;

    public interface Refreshable {
        void refresh();
    }

    public FileOperationHandler(Context context, Refreshable reloader) {
        this.context = context;
        this.reloader = reloader;
    }

    @Override
    public void onRename(File file) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_rename, null);
        EditText editTextNewName = dialogView.findViewById(R.id.editTextNewName);
        editTextNewName.setText(file.getName());

        new AlertDialog.Builder(context)
                .setTitle("Rename")
                .setView(dialogView)
                .setPositiveButton("Rename", (dialog, which) -> {
                    String newName = editTextNewName.getText().toString().trim();
                    if (newName.isEmpty()) {
                        Toast.makeText(context, "File name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    File newFile = new File(file.getParentFile(), newName);

                    if (newFile.exists()) {
                         Toast.makeText(context, "A file with this name already exists", Toast.LENGTH_SHORT).show();
                         return;
                    }

                    if (file.renameTo(newFile)) {
                        Toast.makeText(context, "Renamed to " + newName, Toast.LENGTH_SHORT).show();
                        reloader.refresh();
                    } else {
                        Toast.makeText(context, "Failed to rename file", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onMove(File file) {
        // TODO: Show a folder picker dialog to let the user select the destination directory.
        // For now, move to Downloads as a placeholder.
        File downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS);
        File destFile = new File(downloadsDir, file.getName());

        if (file.renameTo(destFile)) {
            Toast.makeText(context, "Moved to Downloads", Toast.LENGTH_SHORT).show();
            reloader.refresh();
        } else {
            Toast.makeText(context, "Failed to move file", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCopy(File file) {
        // TODO: Show a folder picker dialog to let the user select the destination directory.
        // For now, copy to Downloads as a placeholder.
        File downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS);
        File destFile = new File(downloadsDir, file.getName());

        try {
            if (file.isDirectory()) {
                copyDirectory(file, destFile);
            } else {
                copyFile(file, destFile);
            }
            Toast.makeText(context, "Copied to Downloads", Toast.LENGTH_SHORT).show();
            reloader.refresh();
        } catch (Exception e) {
            Toast.makeText(context, "Failed to copy: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onZip(File file) {
        // TODO: Optionally, let user pick zip destination and name.
        try {
            String zipName = file.getName() + ".zip";
            File zipFile = new File(file.getParent(), zipName);

            try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(new java.io.FileOutputStream(zipFile))) {
                if (file.isDirectory()) {
                    zipDirectory(file, file.getName(), zos);
                } else {
                    zipFile(file, file.getName(), zos);
                }
            }

            Toast.makeText(context, dinhnguyen.filza.file.manager.constants.FileConstants.SUCCESS_FILE_ZIPPED, Toast.LENGTH_SHORT).show();
            reloader.refresh();
        } catch (Exception e) {
            Toast.makeText(context, dinhnguyen.filza.file.manager.constants.FileConstants.ERROR_ZIP_FAILED + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUnzip(File file) {
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
            
            Toast.makeText(context, dinhnguyen.filza.file.manager.constants.FileConstants.SUCCESS_FILE_UNZIPPED, Toast.LENGTH_SHORT).show();
            reloader.refresh();
        } catch (Exception e) {
            Toast.makeText(context, dinhnguyen.filza.file.manager.constants.FileConstants.ERROR_UNZIP_FAILED + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDuplicate(File file) {
        String baseName = file.getName();
        String namePart;
        String extPart = "";

        int dotIndex = baseName.lastIndexOf('.');
        if (dotIndex != -1 && file.isFile()) {
            namePart = baseName.substring(0, dotIndex);
            extPart = baseName.substring(dotIndex); // includes dot
        } else {
            namePart = baseName;
        }

        // Find a new name that doesn't exist: name(1), name(2), ...
        int counter = 1;
        File destFile;
        do {
            String newName = namePart + "(" + counter + ")" + extPart;
            destFile = new File(file.getParent(), newName);
            counter++;
        } while (destFile.exists());

        try {
            if (file.isDirectory()) {
                copyDirectory(file, destFile);
            } else {
                copyFile(file, destFile);
            }
            Toast.makeText(context, "Duplicated", Toast.LENGTH_SHORT).show();
            reloader.refresh();
        } catch (Exception e) {
            Toast.makeText(context, "Failed to duplicate: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

// --- Utility methods ---

    private void copyFile(File source, File dest) throws java.io.IOException {
        try (java.io.FileInputStream fis = new java.io.FileInputStream(source);
             java.io.FileOutputStream fos = new java.io.FileOutputStream(dest)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            fos.getFD().sync();
        }
    }

    private void copyDirectory(File source, File dest) throws java.io.IOException {
        if (!dest.exists()) {
            dest.mkdir();
        }
        File[] files = source.listFiles();
        if (files != null) {
            for (File file : files) {
                File destFile = new File(dest, file.getName());
                if (file.isDirectory()) {
                    copyDirectory(file, destFile);
                } else {
                    copyFile(file, destFile);
                }
            }
        }
    }

    private void zipDirectory(File directory, String basePath, java.util.zip.ZipOutputStream zos) throws java.io.IOException {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                String path = basePath + File.separator + file.getName();
                if (file.isDirectory()) {
                    zipDirectory(file, path, zos);
                } else {
                    zipFile(file, path, zos);
                }
            }
        }
    }

    private void zipFile(File file, String entryName, java.util.zip.ZipOutputStream zos) throws java.io.IOException {
        java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry(entryName);
        zos.putNextEntry(zipEntry);

        try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
        }

        zos.closeEntry();
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

    @Override
    public void onDelete(File file) {
        new AlertDialog.Builder(context)
                .setTitle("Delete File")
                .setMessage("Are you sure you want to delete " + file.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (deleteRecursively(file)) {
                        Toast.makeText(context, "Deleted: " + file.getName(), Toast.LENGTH_SHORT).show();
                        reloader.refresh();
                    } else {
                        Toast.makeText(context, "Failed to delete: " + file.getName(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private boolean deleteRecursively(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] children = fileOrDirectory.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        return fileOrDirectory.delete();
    }
} 