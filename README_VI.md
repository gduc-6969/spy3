# Spy3 - Quản Lý Cuộc Gọi & SMS

Ứng dụng Flutter toàn diện để quản lý tin nhắn SMS, nhật ký cuộc gọi, danh bạ, ứng dụng đã cài đặt và chức năng chặn cuộc gọi/SMS trên thiết bị Android.

## 📱 Tổng Quan

Spy3 là ứng dụng di động dựa trên Flutter cung cấp:
- Giám sát và quản lý tin nhắn SMS
- Theo dõi và phân tích nhật ký cuộc gọi
- Quản lý danh bạ
- Liệt kê ứng dụng đã cài đặt
- Khả năng chặn cuộc gọi và SMS
- Sàng lọc cuộc gọi nâng cao (Android 10+)

## 📚 Cấu Trúc Tài Liệu

Tài liệu dự án này được chia thành hai hướng dẫn toàn diện:

### 📱 [Hướng Dẫn Triển Khai Flutter](README_FLUTTER_VI.md)
Bao phủ hoàn toàn việc triển khai frontend Flutter/Dart:
- Kiến trúc ứng dụng và quản lý trạng thái
- Các thành phần giao diện và triển khai Material Design 3
- Models, providers và services
- Giao tiếp platform channel
- Mẫu giao diện người dùng và thiết kế responsive
- Chiến lược kiểm thử và tối ưu hiệu suất

### 🤖 [Hướng Dẫn Triển Khai Android](README_ANDROID_VI.md)
Tài liệu chi tiết triển khai native Android:
- Trình xử lý platform channel Java
- Hệ thống quản lý quyền
- Tích hợp content provider
- Triển khai background service
- Cân nhắc bảo mật và thực hành tốt nhất
- Cấu hình build và phương pháp kiểm thử

## 🏗️ Cấu Trúc Dự Án

```
spy3/
├── lib/                    # Mã nguồn Flutter/Dart → Xem README_FLUTTER_VI.md
├── android/               # Triển khai native Android → Xem README_ANDROID_VI.md
├── ios/                   # Triển khai native iOS (cơ bản)
├── windows/               # Hỗ trợ desktop Windows
├── linux/                 # Hỗ trợ desktop Linux
├── macos/                 # Hỗ trợ desktop macOS
├── web/                   # Hỗ trợ nền tảng web
└── test/                  # Tệp kiểm thử
```

## 🚀 Bắt Đầu Nhanh

### Yêu Cầu Trước
- Flutter SDK (phiên bản ổn định mới nhất)
- Android Studio hoặc VS Code
- Android SDK và công cụ
- Java Development Kit (JDK) 8+

### Cài Đặt
1. Clone repository
2. Điều hướng đến thư mục dự án
3. Chạy `flutter pub get` để cài đặt dependencies
4. Kết nối thiết bị Android hoặc khởi động emulator
5. Chạy `flutter run` hoặc `flutter build apk` để cài đặt

### Khởi Chạy Lần Đầu
1. Cấp tất cả quyền được yêu cầu khi được nhắc
2. Ứng dụng sẽ tự động tải tin nhắn SMS, nhật ký cuộc gọi và danh bạ
3. Sử dụng nút bật/tắt service trong app bar để khởi động/dừng dịch vụ chặn

## ✨ Tính Năng Chính

### 📱 Chức Năng Cốt Lõi
- **Quản Lý SMS**: Đọc, hiển thị và chặn tin nhắn SMS
- **Quản Lý Cuộc Gọi**: Xem nhật ký cuộc gọi, chặn số, bật sàng lọc cuộc gọi
- **Truy Cập Danh Bạ**: Hiển thị danh bạ thiết bị với khả năng chặn
- **Liệt Kê Ứng Dụng**: Hiển thị tất cả ứng dụng đã cài đặt với thông tin chi tiết
- **Chặn Số**: Chặn/bỏ chặn số điện thoại với lưu trữ bền vững
- **Quản Lý Quyền**: Yêu cầu và quản lý tất cả quyền cần thiết
- **Dịch Vụ Nền**: Chạy dịch vụ chặn để lọc cuộc gọi/SMS theo thời gian thực

### 🎨 Giao Diện Người Dùng
- **Material Design 3**: Ngôn ngữ thiết kế hiện đại, nhất quán
- **Điều Hướng Tab**: Sáu phần chính (Dashboard, SMS, Cuộc gọi, Danh bạ, Ứng dụng, Đã chặn)
- **Phần Tử Tương Tác**: FloatingActionButtons để làm mới dữ liệu thủ công
- **Cập Nhật Thời Gian Thực**: Giao diện reactive với quản lý trạng thái Provider
- **Trạng Thái Trống**: Hướng dẫn hữu ích khi không có dữ liệu
- **Chỉ Báo Tải**: Phản hồi trực quan trong các hoạt động

### 🔒 Bảo Mật & Riêng Tư
- **Quyền Chi Tiết**: Chỉ yêu cầu quyền cần thiết
- **Xác Thực Quyền**: Kiểm tra quyền trước các hoạt động nhạy cảm
- **Lưu Trữ Bảo Mật**: Lưu trữ cục bộ được bảo vệ cho số đã chặn
- **Ranh Giới Lỗi**: Xử lý graceful khi từ chối quyền

## 📋 Tổng Quan Tab

### Tab Dashboard
- Thống kê tóm tắt (số lượng SMS, nhật ký cuộc gọi, danh bạ, số đã chặn)
- Giám sát trạng thái dịch vụ
- Giao diện quản lý quyền

### Tab SMS
- Lịch sử tin nhắn SMS hoàn chỉnh
- Thông tin người gửi và dấu thời gian
- Chặn số trực tiếp từ danh sách tin nhắn

### Tab Cuộc Gọi
- Lịch sử cuộc gọi với chỉ báo loại (đến/đi/nhỡ)
- Thời lượng cuộc gọi và dấu thời gian
- Chặn số từ lịch sử cuộc gọi

### Tab Danh Bạ
- Trình duyệt danh bạ thiết bị
- Chặn/bỏ chặn danh bạ dễ dàng

### Tab Ứng Dụng ⭐ *Tính Năng Mới*
- Danh sách hoàn chỉnh các ứng dụng đã cài đặt
- Phân biệt ứng dụng hệ thống vs người dùng
- Chi tiết ứng dụng (phiên bản, ngày cài đặt, tên package)
- Hộp thoại thông tin ứng dụng chi tiết

### Tab Đã Chặn
- Quản lý số đã chặn toàn diện
- Chức năng bỏ chặn dễ dàng

## 🔧 Điểm Nổi Bật Kỹ Thuật

### Kiến Trúc
- **Clean Architecture**: Tách biệt models, services, providers và UI
- **Quản Lý Trạng Thái**: Giao diện reactive với mẫu Provider
- **Platform Channels**: Giao tiếp Flutter-Android liền mạch
- **Xử Lý Lỗi**: Xử lý lỗi toàn diện trong toàn bộ stack

### Hiệu Suất
- **Render Hiệu Quả**: ListView.builder cho bộ dữ liệu lớn
- **Quản Lý Bộ Nhớ**: Dọn dẹp và disposal tài nguyên đúng cách
- **Xử Lý Nền**: Các hoạt động không chặn với phản hồi người dùng
- **Truy Vấn Tối Ưu**: Truy xuất dữ liệu hạn chế với phân trang

## 🛠️ Phát Triển

### Build
```bash
# Debug build
flutter run

# Release APK
flutter build apk --release

# Build theo kiến trúc cụ thể
flutter build apk --target-platform android-arm64
```

### Kiểm Thử
Ứng dụng bao gồm kiểm thử toàn diện cho:
- ✅ Xử lý và xác thực quyền
- ✅ Điều hướng UI và tương tác người dùng
- ✅ Giao tiếp platform channel
- ✅ Quản lý vòng đời service
- ✅ Hoạt động cơ sở dữ liệu và tính toàn vẹn dữ liệu
- ✅ Các tình huống lỗi và phục hồi

## 📖 Chi Tiết Triển Khai

Để biết thông tin triển khai chi tiết, vui lòng tham khảo tài liệu chuyên biệt:

- **[Hướng Dẫn Flutter](README_FLUTTER_VI.md)**: Triển khai Flutter hoàn chỉnh với giải thích từng dòng code
- **[Hướng Dẫn Android](README_ANDROID_VI.md)**: Chi tiết triển khai native Android toàn diện

## 🤝 Đóng Góp

Dự án này minh họa các thực hành phát triển Flutter hiện đại với tích hợp Android native toàn diện. Tài liệu phục vụ như cả triển khai tham khảo và tài nguyên giáo dục cho:

- Kiến trúc ứng dụng Flutter
- Tích hợp phát triển Android native
- Hệ thống quản lý quyền
- Triển khai background service
- Triển khai Material Design 3
- Mẫu quản lý trạng thái

## 📄 Giấy Phép

Dự án này được dành cho mục đích tham khảo giáo dục và phát triển.

---

*Để biết chi tiết triển khai hoàn chỉnh, giải thích code và tìm hiểu kỹ thuật sâu, vui lòng khám phá các hướng dẫn triển khai Flutter và Android chuyên dụng.*