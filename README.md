# Fashion Shop Desktop

Ứng dụng quản lý cửa hàng thời trang trên máy tính, xây dựng bằng Java 21 và JavaFX. Client giao tiếp với server qua REST API và WebSocket; dữ liệu lưu trong MySQL/MariaDB.

## Chức năng

- Khách hàng: xem, tìm kiếm và lọc sản phẩm theo danh mục; xem chi tiết; quản lý giỏ hàng, đặt hàng và xem đơn hàng.
- Quản trị viên: quản lý sản phẩm, đơn hàng, khách hàng và tồn kho.
- Đăng nhập, phân quyền ADMIN/CUSTOMER bằng JWT; mật khẩu được băm bằng BCrypt.
- Chat giữa khách hàng và quản trị viên qua WebSocket.

## Công nghệ và cấu trúc

| Thư mục | Nội dung |
| --- | --- |
| `server/` | Java server dùng Spark Java, JDBC, JWT và WebSocket |
| `client-customer/` | Giao diện JavaFX chính, gồm chức năng khách hàng và quản trị |
| `client-admin/` | Client quản trị riêng |
| `database/` | Script tạo bảng và dữ liệu mẫu |
| `server/uploads/products/` | Ảnh sản phẩm |

Yêu cầu: **JDK 21**, **Maven 3.9+**, **MySQL/MariaDB** (có thể dùng MySQL trong XAMPP), kết nối Internet cho lần đầu tải thư viện Maven. Đây là ứng dụng desktop Java, không cần chạy Apache/PHP.

## Cài đặt

### 1. Tải mã nguồn

```powershell
git clone https://github.com/NhatHao-2805/FashionShopDesktop.git
cd FashionShopDesktop
java -version
mvn -version
```

Đảm bảo Maven sử dụng JDK 21; kiểm tra `JAVA_HOME` và `PATH` nếu báo lỗi phiên bản Java.

### 2. Chuẩn bị cơ sở dữ liệu

Bật MySQL trong XAMPP hoặc dịch vụ MySQL/MariaDB của bạn. Trên **database mới**, dùng phpMyAdmin hoặc MySQL CLI để nhập lần lượt:

1. `database/fashion_shop_mariadb.sql` — tạo database `fashion_shop` và các bảng.
2. `database/seed.sql` — thêm dữ liệu mẫu.

Ví dụ mở MySQL CLI trên Windows:

```powershell
D:\xampp\mysql\bin\mysql.exe -u root -p
```

Sau đó chạy trong MySQL CLI (thay đường dẫn bằng vị trí clone thực tế):

```sql
SOURCE D:/xampp/htdocs/GitHub/FashionShopDesktop/database/fashion_shop_mariadb.sql;
SOURCE D:/xampp/htdocs/GitHub/FashionShopDesktop/database/seed.sql;
```

Không nhập lại các script vào database đang sử dụng: bảng hoặc dữ liệu có thể trùng. Hãy sao lưu trước khi thay đổi dữ liệu.

### 3. Cấu hình server

Tạo cấu hình cục bộ từ file mẫu (chạy tại thư mục gốc dự án):

```powershell
Copy-Item server/src/main/resources/application.example.properties server/src/main/resources/application.properties
```

Sửa `server/src/main/resources/application.properties` theo máy của bạn. Thay `jwt.secret` bằng chuỗi ngẫu nhiên riêng có ít nhất 32 ký tự; file cấu hình cục bộ này được Git bỏ qua.

```properties
db.url=jdbc:mysql://127.0.0.1:3306/fashion_shop?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh&useSSL=false&allowPublicKeyRetrieval=true
db.username=root
db.password=
server.host=0.0.0.0
server.http.port=8080
server.websocket.port=8081
```

Cấu hình hiện tại dành cho môi trường phát triển cục bộ. Điền mật khẩu database nếu MySQL có mật khẩu.

**Đường dẫn ảnh:** server phục vụ ảnh từ thư mục tương đối `uploads` của thư mục chạy. Hãy chạy Maven từ thư mục `server` để việc xem và upload ảnh cùng dùng `server/uploads/products`.

## Chạy ứng dụng

Mở hai cửa sổ terminal tại thư mục dự án.

**Terminal 1 — server:**

```powershell
cd server
mvn compile exec:java "-Dexec.mainClass=com.fashionshop.server.ServerApplication"
```

Giữ terminal server hoạt động. Kiểm tra:

- Server: http://localhost:8080/api/health
- Database: http://localhost:8080/api/health/db
- Sản phẩm: http://localhost:8080/api/products
- WebSocket thực tế: `ws://localhost:8080/ws/chat` (chung cổng HTTP 8080; cấu hình 8081 hiện không tạo listener riêng).

**Terminal 2 — giao diện chính:**

```powershell
cd client-customer
mvn javafx:run
```

Cửa sổ **Fashion Shop** sẽ xuất hiện. Client chính có cả chức năng khách hàng và quản trị theo vai trò đăng nhập.

Có thể chạy client quản trị riêng bằng một terminal khác:

```powershell
cd client-admin
mvn javafx:run
```

Dừng client bằng cách đóng cửa sổ; dừng server bằng `Ctrl+C` trong terminal server.

## Cách sử dụng

1. Mở ứng dụng, xem danh sách sản phẩm, tìm kiếm hoặc chọn danh mục.
2. Nhấn **Đăng nhập**, nhập tài khoản đã có trong database.
3. Với tài khoản khách hàng: chọn sản phẩm, thêm vào giỏ, kiểm tra số lượng và đặt hàng; xem lịch sử ở **Đơn hàng**, trao đổi qua **Chat**.
4. Với tài khoản quản trị: mở giao diện quản trị để quản lý sản phẩm, đơn hàng, khách hàng, điều chỉnh tồn kho và trả lời chat.

Script seed tạo các email `admin@fashion.local`, `user1@fashion.local`, `user2@fashion.local`. Công cụ `PasswordHashGenerator` trong server sử dụng mật khẩu mẫu `Admin@123` và `User@123`. Nếu mật khẩu không khớp dữ liệu đang có, chạy công cụ sau để tạo hash mới rồi cập nhật `users.password_hash` cho tài khoản tương ứng:

```powershell
# Chạy từ thư mục server
mvn compile exec:java "-Dexec.mainClass=com.fashionshop.server.security.PasswordHashGenerator"
```

## Chạy client ở máy khác trong mạng LAN

Server và MySQL chạy trên máy chủ; client chính kết nối bằng địa chỉ IP của máy chủ:

```powershell
# Chạy trong client-customer; thay IP bằng IP máy chủ
$env:FASHION_SERVER_URL = "http://192.168.1.10:8080"
mvn javafx:run
```

Hai máy cần kết nối được với nhau, firewall máy chủ cho phép cổng HTTP đã cấu hình. Thiết lập này áp dụng cho `client-customer`; kiểm tra cấu hình URL riêng của `client-admin` nếu dùng client đó. Không dùng `localhost` để trỏ tới máy khác.

## Lưu ý và xử lý lỗi

- **Không kết nối database:** kiểm tra MySQL đã bật, cổng 3306, tên database, tài khoản và mật khẩu trong `application.properties`.
- **Client không tải dữ liệu:** chạy server trước, kiểm tra API health và địa chỉ `FASHION_SERVER_URL`.
- **Cổng 8080 đang dùng:** dừng tiến trình chiếm cổng hoặc đổi `server.http.port`, đồng thời đổi URL phía client.
- **Ảnh không hiển thị:** kiểm tra đường dẫn static ở trên và file ảnh tương ứng. Một số ảnh mà seed tham chiếu có thể chưa có trong mã nguồn; bổ sung ảnh hoặc cập nhật sản phẩm qua quản trị.
- **Lỗi `release version 21 not supported`:** Maven đang dùng JDK cũ; chỉnh `JAVA_HOME` về JDK 21.
- **Maven không tải được thư viện:** kiểm tra Internet, proxy và cấu hình Maven. Lần đầu không dùng chế độ offline `-o`.
- **Dữ liệu mẫu:** chỉ dùng cho học tập/thử nghiệm. Thay mật khẩu mẫu và `jwt.secret` trước khi triển khai thật; không công khai thông tin kết nối thật trên GitHub.
- **Triển khai:** cấu hình hiện tại dùng HTTP, tài khoản database `root` và khóa JWT mẫu. Khi triển khai cần HTTPS, tài khoản database có quyền phù hợp và khóa JWT riêng. Dự án chưa kèm quy trình triển khai production.
- Không commit thư mục `target/`, log, cấu hình IDE cá nhân hoặc `_backup/`. Các file này được loại qua `.gitignore`.

## Kiểm tra biên dịch

Chạy trong từng module cần sử dụng:

```powershell
mvn compile
```

Dự án gồm các module Maven riêng; chạy lệnh trong từng thư mục có `pom.xml`.

