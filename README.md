# 📁 FilzaFileManager - Ứng dụng Quản lý Tệp Android

## 📋 Tổng quan dự án

**FilzaFileManager** là một ứng dụng quản lý tệp Android hiện đại được phát triển bởi nhóm sinh viên CNTT. Ứng dụng cung cấp giao diện thân thiện để duyệt, xem và quản lý các loại tệp khác nhau trên thiết bị Android, tích hợp với Google Drive để đồng bộ hóa dữ liệu.

### 🎯 Mục tiêu dự án

- Xây dựng ứng dụng quản lý tệp hoàn chỉnh với giao diện Material Design 3
- Hỗ trợ xem và chỉnh sửa nhiều loại tệp (hình ảnh, video, âm thanh, PDF, văn bản)
- Tích hợp Google Drive để đồng bộ hóa và sao lưu dữ liệu
- Cung cấp trải nghiệm người dùng mượt mà và trực quan
- Áp dụng các kiến trúc và design pattern hiện đại trong Android development

## ✨ Tính năng chính

### 📂 Quản lý tệp cơ bản
- **Duyệt tệp**: Giao diện danh sách và lưới với sắp xếp theo tên, ngày, kích thước
- **Tạo thư mục**: Tạo thư mục mới trong bất kỳ vị trí nào
- **Nhập tệp**: Import tệp từ bộ nhớ thiết bị
- **Chế độ chọn nhiều**: Chọn và thao tác với nhiều tệp cùng lúc
- **Tìm kiếm**: Tìm kiếm tệp trong thư mục hiện tại

### 🖼️ Xem và chỉnh sửa tệp
- **Hình ảnh**: Xem ảnh với zoom, vẽ, highlight, lưu chỉnh sửa
  - Hỗ trợ: JPG, JPEG, PNG, GIF, BMP, WebP
- **Video**: Phát video với Picture-in-Picture, điều khiển đầy đủ
  - Hỗ trợ: MP4, MKV, 3GP, AVI, MOV, WMV, FLV, WebM
- **Âm thanh**: Phát nhạc với giao diện đẹp, hiển thị metadata
  - Hỗ trợ: MP3, WAV, OGG, M4A, FLAC, AAC
- **PDF**: Xem PDF với zoom, highlight, xuất trang
- **Văn bản**: Xem file text với tìm kiếm, đánh số dòng
  - Hỗ trợ: TXT, LOG, MD, JSON, XML, CSV

### ☁️ Tích hợp Google Drive
- **Đăng nhập**: Xác thực qua Google Sign-In
- **Duyệt tệp**: Xem và duyệt tệp trên Google Drive
- **Tải lên**: Upload tệp từ thiết bị lên Google Drive
- **Tải xuống**: Download tệp từ Google Drive về thiết bị
- **Đồng bộ**: Quản lý tệp giữa thiết bị và cloud

### 🎨 Giao diện người dùng
- **Material Design 3**: Giao diện hiện đại với Material You
- **Dark/Light theme**: Hỗ trợ chế độ tối và sáng
- **Responsive**: Tương thích với nhiều kích thước màn hình
- **Animations**: Hiệu ứng chuyển động mượt mà
- **Accessibility**: Hỗ trợ người dùng khuyết tật

## 🛠️ Công nghệ sử dụng

### Ngôn ngữ và Framework
- **Java 11**: Ngôn ngữ lập trình chính
- **Android SDK**: Framework phát triển Android
- **Gradle**: Hệ thống build và quản lý dependencies

### Kiến trúc và Design Patterns
- **MVVM (Model-View-ViewModel)**: Kiến trúc UI
- **Repository Pattern**: Quản lý dữ liệu
- **Factory Pattern**: Tạo đối tượng
- **Observer Pattern**: Reactive programming với LiveData
- **Strategy Pattern**: Xử lý các loại tệp khác nhau

### Thư viện chính
- **AndroidX**: Thư viện Android hiện đại
- **Material Components**: UI components theo Material Design
- **PhotoView**: Zoom và pan cho hình ảnh
- **Google Play Services**: Xác thực và Google Drive API
- **Google API Client**: Tương tác với Google APIs

### Google Drive Integration
- **Google Sign-In**: Xác thực người dùng
- **Google Drive API v3**: Quản lý tệp trên Drive
- **OAuth 2.0**: Bảo mật xác thực

## 📱 Yêu cầu hệ thống

- **Android**: API level 30 (Android 11) trở lên
- **RAM**: Tối thiểu 2GB
- **Storage**: 50MB cho ứng dụng
- **Internet**: Cần kết nối cho tính năng Google Drive

## 🚀 Cài đặt và Build

### Yêu cầu phát triển
- **Android Studio**: Arctic Fox trở lên
- **JDK**: Version 11
- **Gradle**: Version 8.9.0

### Các bước cài đặt

1. **Clone repository**
   ```bash
   git clone https://github.com/dinhno12313/FilzaFileManager
   cd FilzaFileManager
   ```

2. **Cấu hình Google Drive API**
   - Tạo project trên [Google Cloud Console](https://console.cloud.google.com/)
   - Bật Google Drive API
   - Tạo OAuth 2.0 credentials
   - Thêm file `google-services.json` vào thư mục `app/`

3. **Build project**
   ```bash
   ./gradlew build
   ```

4. **Chạy ứng dụng**
   ```bash
   ./gradlew installDebug
   ```

### Cấu hình Google Drive
1. Mở [Google Cloud Console](https://console.cloud.google.com/)
2. Tạo project mới hoặc chọn project có sẵn
3. Bật Google Drive API
4. Tạo OAuth 2.0 Client ID
5. Download file `google-services.json`
6. Đặt file vào thư mục `app/` của project

## 📖 Hướng dẫn sử dụng

### 🏠 Màn hình chính
1. **Khởi động ứng dụng**: Mở FilzaFileManager từ launcher
2. **Duyệt tệp**: Chạm vào thư mục để mở, chạm vào tệp để xem
3. **Tạo thư mục**: Chạm vào nút "+" ở góc phải dưới
4. **Nhập tệp**: Chạm vào nút upload ở góc trái dưới

### 🖼️ Xem hình ảnh
1. Chạm vào file ảnh để mở ImageViewer
2. **Zoom**: Chạm đúp hoặc pinch để zoom
3. **Vẽ**: Chạm vào nút bút để vẽ lên ảnh
4. **Highlight**: Chạm vào nút highlight để tô sáng
5. **Lưu**: Chạm vào nút save để lưu chỉnh sửa

### 🎵 Phát nhạc
1. Chạm vào file âm thanh để mở MusicPlayer
2. **Điều khiển**: Play/Pause, Next/Previous, Seek
3. **Metadata**: Hiển thị tên bài hát, nghệ sĩ
4. **Album art**: Hiển thị ảnh album nếu có

### 📄 Xem PDF
1. Chạm vào file PDF để mở PdfViewer
2. **Điều hướng**: Vuốt để chuyển trang
3. **Zoom**: Pinch để zoom in/out
4. **Highlight**: Tô sáng nội dung quan trọng
5. **Xuất**: Xuất trang hoặc toàn bộ PDF

### 📝 Xem văn bản
1. Chạm vào file text để mở TxtViewer
2. **Tìm kiếm**: Sử dụng thanh tìm kiếm
3. **Đánh số dòng**: Bật/tắt hiển thị số dòng
4. **Font size**: Tăng/giảm kích thước chữ

### ☁️ Google Drive
1. **Đăng nhập**: Chạm vào menu → Google Drive
2. **Duyệt**: Xem tệp trên Google Drive
3. **Upload**: Chạm vào nút upload để tải lên
4. **Download**: Chạm vào tệp để tải xuống

### 🎯 Chế độ chọn nhiều
1. Chạm vào menu → Select
2. Chọn các tệp cần thao tác
3. Chạm vào Actions để thực hiện thao tác
4. Chạm Cancel để thoát chế độ chọn

## 👥 Thành viên nhóm

<!-- Thêm thông tin thành viên nhóm vào đây -->
- **Trưởng nhóm**: [Nguyễn Quang Định] - [2251061741]
- **Thành viên 1**: [Phạm Đức Đô] - [2251061742]
- **Thành viên 2**: [Nguyễn Trung Kiên] - [2251061765]


## 📄 Bản quyền

**FilzaFileManager** là dự án đồ án nhóm được phát triển cho mục đích học tập và nghiên cứu. Dự án này được phân phối dưới giấy phép học thuật và **KHÔNG được sử dụng cho mục đích thương mại**.

### Giấy phép
- **Mục đích**: Học tập và nghiên cứu
- **Sử dụng thương mại**: Không được phép
- **Phân phối**: Chỉ được phép trong phạm vi học thuật
- **Bản quyền**: Thuộc về nhóm phát triển

### Thư viện bên thứ ba
- **PhotoView**: Copyright (c) 2011-2019 Chris Banes
- **Material Components**: Copyright (c) Google LLC
- **Google APIs**: Copyright (c) Google LLC

## 🤝 Đóng góp

Dự án này được phát triển cho mục đích học tập. Nếu bạn muốn đóng góp ý tưởng hoặc báo cáo lỗi, vui lòng liên hệ với nhóm phát triển.

## 📞 Liên hệ

- **Email**: [dinhno12313@gmail.com]
- **GitHub**: [github.com/dinhno12313]
- **Trường**: [Đại Học Thuỷ Lợi]
- **Khoa**: [Công Nghệ Thông Tin]

---

**Lưu ý**: Dự án này được phát triển bởi sinh viên CNTT cho mục đích học tập. Vui lòng tôn trọng bản quyền và không sử dụng cho mục đích thương mại. 