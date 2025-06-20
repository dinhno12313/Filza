package dinhnguyen.filza.file.manager.model;

import com.google.api.services.drive.model.File;

public class GoogleDriveFile {
    private String id;
    private String name;
    private String mimeType;
    private long size;
    private String webViewLink;
    private String downloadUrl;
    private boolean isFolder;
    private String parentId;
    private long modifiedTime;

    public GoogleDriveFile() {
    }

    public GoogleDriveFile(File driveFile) {
        this.id = driveFile.getId();
        this.name = driveFile.getName();
        this.mimeType = driveFile.getMimeType();
        this.size = driveFile.getSize() != null ? driveFile.getSize() : 0;
        this.webViewLink = driveFile.getWebViewLink();
        this.downloadUrl = driveFile.getWebContentLink();
        this.isFolder = "application/vnd.google-apps.folder".equals(driveFile.getMimeType());
        this.parentId = driveFile.getParents() != null && !driveFile.getParents().isEmpty() 
            ? driveFile.getParents().get(0) : null;
        this.modifiedTime = driveFile.getModifiedTime() != null 
            ? driveFile.getModifiedTime().getValue() : 0;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getWebViewLink() {
        return webViewLink;
    }

    public void setWebViewLink(String webViewLink) {
        this.webViewLink = webViewLink;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void setFolder(boolean folder) {
        isFolder = folder;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public long getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(long modifiedTime) {
        this.modifiedTime = modifiedTime;
    }
} 