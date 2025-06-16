package dinhnguyen.filza.file.manager.listener;


import java.io.File;

public interface FileActionListener {
    void onRename(File file);
    void onMove(File file);
    void onCopy(File file);
    void onZip(File file);
    void onUnzip(File file);
    void onDuplicate(File file);
    void onDelete(File file);
} 