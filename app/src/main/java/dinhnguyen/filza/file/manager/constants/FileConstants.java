package dinhnguyen.filza.file.manager.constants;

public class FileConstants {
    
    // Intent extras
    public static final String EXTRA_FILE_PATH = "filePath";
    
    // File operations
    public static final int BUFFER_SIZE = 4096;
    
    // Default file names
    public static final String DEFAULT_IMPORTED_FILE_PREFIX = "imported_";
    
    // Error messages
    public static final String ERROR_FILE_NOT_EXISTS = "Tệp không tồn tại";
    public static final String ERROR_CANNOT_OPEN_FILE = "Không thể mở tệp";
    public static final String ERROR_NO_APP_FOUND = "Không tìm thấy ứng dụng để mở tệp này";
    public static final String ERROR_IMPORT_FAILED = "Lỗi nhập tệp";
    public static final String ERROR_CREATE_FOLDER = "Không thể tạo thư mục";
    public static final String ERROR_FOLDER_EXISTS = "Thư mục đã tồn tại";
    public static final String ERROR_EMPTY_FOLDER_NAME = "Tên thư mục không được để trống";
    public static final String ERROR_NO_HANDLER = "Tệp đã nhập nhưng không có trình xử lý phù hợp";
    public static final String ERROR_UNZIP_FAILED = "Lỗi khi giải nén tệp";
    public static final String ERROR_ZIP_FAILED = "Lỗi khi nén tệp";
    
    // Success messages
    public static final String SUCCESS_FOLDER_CREATED = "Thư mục đã được tạo";
    public static final String SUCCESS_FILE_IMPORTED = "Tệp đã được nhập thành công";
    public static final String SUCCESS_FILE_UNZIPPED = "Đã giải nén tệp thành công";
    public static final String SUCCESS_FILE_ZIPPED = "Đã nén tệp thành công";
    
    // Dialog titles
    public static final String DIALOG_CREATE_FOLDER_TITLE = "Tên thư mục mới";
    public static final String DIALOG_BUTTON_CREATE = "Tạo";
    public static final String DIALOG_BUTTON_CANCEL = "Hủy";
    
    // Placeholder messages
    public static final String PLACEHOLDER_LEFT_BUTTON = "Nút trái chưa dùng";
    
    // Multi-select messages
    public static final String MULTI_SELECT_ENTERED = "Chế độ chọn nhiều đã được kích hoạt";
    public static final String MULTI_SELECT_EXITED = "Đã thoát chế độ chọn nhiều";
    public static final String MULTI_SELECT_ITEMS_SELECTED = "Đã chọn %d tệp";
    
    // View mode messages
    public static final String VIEW_MODE_GRID = "Chế độ xem lưới";
    public static final String VIEW_MODE_LIST = "Chế độ xem danh sách";
    
    // Sort mode messages
    public static final String SORT_BY_NAME = "Sắp xếp theo tên";
    public static final String SORT_BY_DATE = "Sắp xếp theo ngày";
    public static final String SORT_BY_SIZE = "Sắp xếp theo kích thước";
} 