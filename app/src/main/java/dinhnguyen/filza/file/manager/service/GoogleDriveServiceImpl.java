package dinhnguyen.filza.file.manager.service;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;
import com.google.api.client.http.javanet.NetHttpTransport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import dinhnguyen.filza.file.manager.model.GoogleDriveFile;

public class GoogleDriveServiceImpl implements GoogleDriveService {
    
    private static final String TAG = "GoogleDriveServiceImpl";
    private static final String ROOT_FOLDER_ID = "root";
    
    private Context context;
    private Drive driveService;
    private Stack<String> folderStack;
    private String currentFolderId;
    
    public GoogleDriveServiceImpl(Context context) {
        this.context = context;
        this.folderStack = new Stack<>();
        this.currentFolderId = ROOT_FOLDER_ID;
    }
    
    @Override
    public void initialize(GoogleSignInAccount account) {
        try {
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                context, Arrays.asList(DriveScopes.DRIVE_FILE, DriveScopes.DRIVE_READONLY));
            credential.setSelectedAccount(account.getAccount());
            
            driveService = new Drive.Builder(
                new NetHttpTransport(),
                new GsonFactory(),
                credential)
                .setApplicationName("Filza File Manager")
                .build();
                
            Log.d(TAG, "Google Drive service initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Google Drive service", e);
        }
    }
    
    @Override
    public boolean isInitialized() {
        return driveService != null;
    }
    
    @Override
    public void getFiles(String folderId, GoogleDriveCallback<List<GoogleDriveFile>> callback) {
        if (!isInitialized()) {
            callback.onError("Google Drive service not initialized");
            return;
        }
        
        new Thread(() -> {
            try {
                String query = folderId.equals(ROOT_FOLDER_ID) 
                    ? "'root' in parents and trashed=false"
                    : "'" + folderId + "' in parents and trashed=false";
                
                FileList result = driveService.files().list()
                    .setQ(query)
                    .setSpaces("drive")
                    .setFields("files(id,name,mimeType,size,webViewLink,webContentLink,parents,modifiedTime)")
                    .execute();
                
                List<GoogleDriveFile> files = new ArrayList<>();
                if (result.getFiles() != null) {
                    for (com.google.api.services.drive.model.File file : result.getFiles()) {
                        files.add(new GoogleDriveFile(file));
                    }
                }
                
                // Update current folder
                if (!folderId.equals(currentFolderId)) {
                    folderStack.push(currentFolderId);
                    currentFolderId = folderId;
                }
                
                callback.onSuccess(files);
                
            } catch (Exception e) {
                Log.e(TAG, "Error getting files from Google Drive", e);
                callback.onError("Failed to load files: " + e.getMessage());
            }
        }).start();
    }
    
    @Override
    public void getFileById(String fileId, GoogleDriveCallback<GoogleDriveFile> callback) {
        if (!isInitialized()) {
            callback.onError("Google Drive service not initialized");
            return;
        }
        
        new Thread(() -> {
            try {
                com.google.api.services.drive.model.File file = driveService.files().get(fileId)
                    .setFields("id,name,mimeType,size,webViewLink,webContentLink,parents,modifiedTime")
                    .execute();
                
                callback.onSuccess(new GoogleDriveFile(file));
                
            } catch (Exception e) {
                Log.e(TAG, "Error getting file by ID", e);
                callback.onError("Failed to get file: " + e.getMessage());
            }
        }).start();
    }
    
    @Override
    public void downloadFile(GoogleDriveFile driveFile, File localFile, GoogleDriveCallback<File> callback) {
        if (!isInitialized()) {
            callback.onError("Google Drive service not initialized");
            return;
        }
        
        if (driveFile.getDownloadUrl() == null) {
            callback.onError("File cannot be downloaded");
            return;
        }
        
        new Thread(() -> {
            try {
                // Create parent directories if they don't exist
                File parentDir = localFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                
                // Download the file
                HttpResponse response = driveService.getRequestFactory()
                    .buildGetRequest(new GenericUrl(driveFile.getDownloadUrl()))
                    .execute();
                
                try (InputStream inputStream = response.getContent();
                     FileOutputStream outputStream = new FileOutputStream(localFile)) {
                    
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
                
                callback.onSuccess(localFile);
                
            } catch (Exception e) {
                Log.e(TAG, "Error downloading file", e);
                callback.onError("Failed to download file: " + e.getMessage());
            }
        }).start();
    }
    
    @Override
    public void uploadFile(File localFile, String parentFolderId, GoogleDriveCallback<GoogleDriveFile> callback) {
        if (!isInitialized()) {
            callback.onError("Google Drive service not initialized");
            return;
        }
        
        if (!localFile.exists()) {
            callback.onError("Local file does not exist");
            return;
        }
        
        new Thread(() -> {
            try {
                // Create file metadata
                com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
                fileMetadata.setName(localFile.getName());
                fileMetadata.setParents(Collections.singletonList(parentFolderId));
                
                // Get MIME type
                String mimeType = getMimeType(localFile.getName());
                fileMetadata.setMimeType(mimeType);
                
                // Create file content
                FileContent mediaContent = new FileContent(mimeType, localFile);
                
                // Upload the file
                com.google.api.services.drive.model.File uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id,name,mimeType,size,webViewLink,webContentLink,parents,modifiedTime")
                    .execute();
                
                callback.onSuccess(new GoogleDriveFile(uploadedFile));
                
            } catch (Exception e) {
                Log.e(TAG, "Error uploading file", e);
                callback.onError("Failed to upload file: " + e.getMessage());
            }
        }).start();
    }
    
    @Override
    public void uploadFile(File localFile, String parentFolderId, UploadProgressCallback callback) {
        if (!isInitialized()) {
            callback.onError("Google Drive service not initialized");
            return;
        }
        
        if (!localFile.exists()) {
            callback.onError("Local file does not exist");
            return;
        }
        
        new Thread(() -> {
            try {
                // Create file metadata
                com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
                fileMetadata.setName(localFile.getName());
                fileMetadata.setParents(Collections.singletonList(parentFolderId));
                
                // Get MIME type
                String mimeType = getMimeType(localFile.getName());
                fileMetadata.setMimeType(mimeType);
                
                // Create file content
                FileContent mediaContent = new FileContent(mimeType, localFile);
                
                // Simulate progress updates
                callback.onProgress(10);
                
                // Upload the file
                com.google.api.services.drive.model.File uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id,name,mimeType,size,webViewLink,webContentLink,parents,modifiedTime")
                    .execute();
                
                callback.onProgress(100);
                callback.onSuccess(new GoogleDriveFile(uploadedFile));
                
            } catch (Exception e) {
                Log.e(TAG, "Error uploading file", e);
                callback.onError("Failed to upload file: " + e.getMessage());
            }
        }).start();
    }
    
    @Override
    public void createFolder(String folderName, String parentFolderId, GoogleDriveCallback<GoogleDriveFile> callback) {
        if (!isInitialized()) {
            callback.onError("Google Drive service not initialized");
            return;
        }
        
        new Thread(() -> {
            try {
                // Create folder metadata
                com.google.api.services.drive.model.File folderMetadata = new com.google.api.services.drive.model.File();
                folderMetadata.setName(folderName);
                folderMetadata.setMimeType("application/vnd.google-apps.folder");
                folderMetadata.setParents(Collections.singletonList(parentFolderId));
                
                // Create the folder
                com.google.api.services.drive.model.File createdFolder = driveService.files().create(folderMetadata)
                    .setFields("id,name,mimeType,size,webViewLink,webContentLink,parents,modifiedTime")
                    .execute();
                
                callback.onSuccess(new GoogleDriveFile(createdFolder));
                
            } catch (Exception e) {
                Log.e(TAG, "Error creating folder", e);
                callback.onError("Failed to create folder: " + e.getMessage());
            }
        }).start();
    }
    
    private String getMimeType(String fileName) {
        String extension = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1).toLowerCase();
        }
        
        switch (extension) {
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt":
                return "application/vnd.ms-powerpoint";
            case "pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "txt":
                return "text/plain";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "mp4":
                return "video/mp4";
            case "avi":
                return "video/x-msvideo";
            case "mp3":
                return "audio/mpeg";
            case "wav":
                return "audio/wav";
            case "zip":
                return "application/zip";
            case "rar":
                return "application/x-rar-compressed";
            default:
                return "application/octet-stream";
        }
    }
    
    @Override
    public String getCurrentFolderId() {
        return currentFolderId;
    }
    
    @Override
    public boolean canNavigateToParent() {
        return !folderStack.isEmpty() || !ROOT_FOLDER_ID.equals(currentFolderId);
    }
    
    @Override
    public void navigateToParent(GoogleDriveCallback<String> callback) {
        if (folderStack.isEmpty()) {
            // Navigate to root
            currentFolderId = ROOT_FOLDER_ID;
            callback.onSuccess(ROOT_FOLDER_ID);
        } else {
            // Navigate to parent folder
            currentFolderId = folderStack.pop();
            callback.onSuccess(currentFolderId);
        }
    }
    
    @Override
    public void signOut() {
        driveService = null;
        folderStack.clear();
        currentFolderId = ROOT_FOLDER_ID;
        Log.d(TAG, "Google Drive service signed out");
    }
} 