package dinhnguyen.filza.file.manager.constants;

public class AudioConstants {
    
    // Intent extras
    public static final String EXTRA_FILE_PATH = "filePath";
    
    // Error messages
    public static final String ERROR_NO_FILE_PATH = "Không nhận được đường dẫn file âm thanh";
    public static final String ERROR_FILE_NOT_EXISTS = "File không tồn tại";
    public static final String ERROR_INVALID_AUDIO_FILE = "File không phải là file âm thanh";
    public static final String ERROR_LOAD_FAILED = "Không thể tải file âm thanh";
    public static final String ERROR_PLAYER_NOT_READY = "MediaPlayer chưa sẵn sàng";
    public static final String ERROR_PLAY_FAILED = "Không thể phát nhạc";
    public static final String ERROR_PAUSE_FAILED = "Không thể tạm dừng";
    public static final String ERROR_SEEK_FAILED = "Không thể tìm kiếm";
    public static final String ERROR_UNKNOWN = "Lỗi không xác định";
    public static final String ERROR_SERVER_DIED = "Server media đã dừng";
    
    // Success messages
    public static final String SUCCESS_AUDIO_LOADED = "File âm thanh đã được tải";
    public static final String SUCCESS_PLAYBACK_STARTED = "Bắt đầu phát nhạc";
    public static final String SUCCESS_PLAYBACK_PAUSED = "Đã tạm dừng";
    
    // UI messages
    public static final String UI_PLAYBACK_COMPLETED = "Phát nhạc hoàn thành";
    public static final String UI_LOADING_AUDIO = "Đang tải file âm thanh...";
    
    // Default values
    public static final int DEFAULT_UPDATE_INTERVAL = 1000; // 1 second
    public static final int DEFAULT_SEEKBAR_MAX = 100;
} 