package dinhnguyen.filza.file.manager.constants;

public class PdfConstants {
    
    // Intent extras
    public static final String EXTRA_FILE_PATH = "filePath";
    
    // Error messages
    public static final String ERROR_NO_FILE_PATH = "Không nhận được đường dẫn file PDF";
    public static final String ERROR_FILE_NOT_EXISTS = "File PDF không tồn tại";
    public static final String ERROR_INVALID_PDF_FILE = "File không phải là PDF";
    public static final String ERROR_LOAD_FAILED = "Không thể mở file PDF";
    public static final String ERROR_PAGE_INVALID = "Trang không hợp lệ";
    public static final String ERROR_BITMAP_CREATION = "Không thể tạo ảnh từ trang này";
    public static final String ERROR_EXPORT_FAILED = "Lỗi khi xuất ảnh";
    public static final String ERROR_RENDERER_NOT_READY = "PDF renderer chưa sẵn sàng";
    
    // Success messages
    public static final String SUCCESS_PAGE_EXPORTED = "Đã xuất trang %d thành: %s";
    public static final String SUCCESS_PDF_LOADED = "PDF đã được tải thành công";
    
    // UI messages
    public static final String UI_PAGE_CLICKED = "Trang %d";
    public static final String UI_EXPORT_ALL_FEATURE = "Tính năng xuất tất cả trang sẽ được thêm sau";
    public static final String UI_LOADING_PDF = "Đang tải PDF...";
    
    // File operations
    public static final String EXPORT_DIR_NAME = "pdf_exports";
    public static final String EXPORT_FILE_PREFIX = "page_";
    public static final String EXPORT_FILE_SUFFIX = ".png";
    public static final int EXPORT_QUALITY = 100;
    
    // Default values
    public static final int DEFAULT_ZOOM_LEVEL = 1;
    public static final float ZOOM_STEP = 0.25f;
    public static final float MIN_ZOOM = 0.5f;
    public static final float MAX_ZOOM = 3.0f;
} 