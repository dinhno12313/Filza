package dinhnguyen.filza.file.manager.listener;

public interface PageClickListener {
    void onPageClicked(int position);
    void onPageLongClicked(int position);
    void onPageExportRequested(int position);
} 