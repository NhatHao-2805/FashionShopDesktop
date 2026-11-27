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
| `client-admin/` | Client chỉ dành cho quản trị viên, dùng chung các màn hình quản trị với client chính |
| `database/` | Script tạo bảng và dữ liệu mẫu |
| `server/uploads/products/` | Ảnh sản phẩm |

Yêu cầu: **JDK 21**, **Maven 3.9+**, **MySQL/MariaDB** (có thể dùng MySQL trong XAMPP), kết nối Internet cho lần đầu tải thư viện Maven. Đây là ứng dụng desktop Java, không cần chạy Apache/PHP.

Trên Windows, sau khi tải mã nguồn và cài các công cụ trên, có thể dùng mục **Chạy nhanh trên Windows** để bỏ qua phần thiết lập thủ công.

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

## Chạy nhanh trên Windows

Từ thư mục gốc, sau khi cài JDK 21, Maven và XAMPP/MariaDB:

```powershell
# Máy mới, tạo database và nạp dữ liệu mẫu một lần
powershell -NoProfile -ExecutionPolicy Bypass -File .\run-shop.ps1 -DatabaseAction Initialize

# Đang dùng database từ phiên bản trước, sao lưu và dừng server cũ trước khi nâng cấp
powershell -NoProfile -ExecutionPolicy Bypass -File .\run-shop.ps1 -DatabaseAction Migrate

# Các lần chạy tiếp theo
powershell -NoProfile -ExecutionPolicy Bypass -File .\run-shop.ps1
```

Script tạo cấu hình cục bộ với khóa JWT ngẫu nhiên, khởi động MariaDB của XAMPP khi cần và chờ máy chủ tải được danh mục trước khi mở ứng dụng chính. Ứng dụng chính có một màn hình đăng nhập dùng chung: tài khoản khách hàng vào cửa hàng, tài khoản quản trị vào bảng điều khiển admin. Nếu dùng schema mặc định, các lần chạy sau có thể kết nối lại máy chủ FashionShop đang chạy.

- Dùng `-Client Customer`, `-Client Admin` hoặc `-Client None` để chọn cửa sổ cần mở.
- Dùng `-XamppRoot D:\xampp` nếu script chưa tìm được XAMPP và `-Port 18085` nếu cần chọn cổng cụ thể.
- Sau khi Maven đã tải đủ thư viện, có thể dùng `-Offline` để chạy bằng bộ nhớ đệm cục bộ.
- Terminal hiển thị các bước khởi động, địa chỉ máy chủ và vị trí log trong thư mục `target` của từng mô-đun.

Để trình diễn bằng một bộ dữ liệu riêng, chạy lệnh sau từ bản sao mã nguồn chưa có `application.properties`:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\run-shop.ps1 -DatabaseAction Initialize -DatabaseName fashion_demo -Port 18111
```

Script ghi tên schema vào cấu hình của bản sao đó. Nếu schema hoặc cổng đã được dùng, chọn một tên hoặc cổng khác.

`Initialize` và `Migrate` dùng tài khoản root mặc định của XAMPP trên máy cục bộ. Nếu dùng cấu hình database hoặc tài khoản khác, nhập SQL bằng tài khoản của bạn rồi chạy script với cấu hình mặc định `-DatabaseAction Check`. `Initialize` từ chối chạy nếu database đã tồn tại.

## Chạy thủ công

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

Có thể chạy client quản trị riêng từ thư mục gốc dự án. Cài mô-đun giao diện dùng chung vào kho Maven cục bộ trước lần chạy đầu và sau mỗi lần sửa mô-đun đó:

```powershell
mvn -f client-customer/pom.xml install
mvn -f client-admin/pom.xml javafx:run
```

Client riêng yêu cầu tài khoản ADMIN. Sau khi đăng nhập, quản trị viên có thể mở đơn hàng, sản phẩm, khách hàng, kho và tin nhắn. **Tổng quan và thống kê** hiển thị số đơn theo trạng thái, số khách hàng, số sản phẩm, tồn kho và doanh thu từ đơn đã giao. Nếu server chạy ở cổng khác `8080`, đặt `FASHION_SERVER_URL` trong terminal trước khi chạy client, ví dụ `$env:FASHION_SERVER_URL = "http://127.0.0.1:18080"`.

Dừng client bằng cách đóng cửa sổ; dừng server bằng `Ctrl+C` trong terminal server.

## Cách sử dụng

Màn hình **Tổng quan** của quản trị viên có biểu đồ số đơn theo trạng thái, ẩn biểu đồ khi chưa có đơn. Có thể cuộn để xem đầy đủ thống kê trên màn hình nhỏ. Nút **Làm mới** hiển thị thời điểm cập nhật thành công và tạm khóa trong khi tải để tránh gửi yêu cầu trùng.

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

Hai máy cần kết nối được với nhau và firewall máy chủ cần cho phép cổng HTTP đã cấu hình. Cả `client-customer` và `client-admin` đều đọc `FASHION_SERVER_URL`. Dùng IP của máy chủ khi chạy client ở máy khác.

## Lưu ý và xử lý lỗi

- **Không kết nối database:** kiểm tra MySQL đã bật, cổng 3306, tên database, tài khoản và mật khẩu trong `application.properties`.
- **Client không tải dữ liệu:** chạy server trước, kiểm tra API health và địa chỉ `FASHION_SERVER_URL`.
- **Cổng 8080 đang dùng:** dừng tiến trình chiếm cổng hoặc đổi `server.http.port`, đồng thời đổi URL phía client.
- **Ảnh không hiển thị:** ảnh của các sản phẩm mẫu nằm trong `server/uploads/products`. Với sản phẩm tự thêm, kiểm tra tệp ảnh trên máy chạy server hoặc cập nhật ảnh qua màn hình quản trị sản phẩm.
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


## Cập nhật chọn size/màu

- Khách chọn size/màu trên cửa sổ chi tiết trước khi thêm vào giỏ.
- Giỏ và đơn hàng tách theo sản phẩm + size + màu; server kiểm tra lựa chọn thuộc cấu hình sản phẩm.
- Database cũ cần áp dụng `database/migrate_variant_stock.sql` trước khi khởi động bản mới. Nếu chưa có `selected_size` và `selected_color` trong `order_items`, áp dụng `database/migrate_size_color.sql` trước. Database mới có sẵn cả hai thay đổi trong script tạo bảng.
- Bản chống trùng đơn cần thêm `database/migrate_order_requests.sql` trên database cũ. Chế độ `-DatabaseAction Migrate` tự áp dụng bước này và giữ nguyên đơn hàng đã có.
- Trong **Kho hàng → Phân bổ size/màu**, nhập số lượng thực tế cho từng tổ hợp. Sản phẩm cũ tiếp tục bán theo tồn chung cho đến khi quản trị viên lưu phân bổ lần đầu. Từ thời điểm đó, chỉ phần đã phân bổ mới bán được. Sản phẩm mẫu trên database mới đã có số lượng riêng cho mỗi tổ hợp.
- Trong **Điều chỉnh kho**, nhập hàng để tăng tổng tồn, sau đó phân bổ số mới cho từng size/màu. Khi xuất kho, giảm số phân bổ của tổ hợp cần xuất để số đó trở về phần chưa phân bổ. Việc phân bổ giữ nguyên tổng tồn.
- Khi hủy đơn, hàng được hoàn về đúng biến thể đã trừ. Đơn tạo trước khi sản phẩm có phân bổ hoàn về phần chưa phân bổ; quản trị viên có thể phân bổ lại sau khi kiểm hàng. Các size/màu đã có dòng tồn kho cần được giữ trong cấu hình sản phẩm để bảo toàn lịch sử giao dịch.

## Quyền tài khoản ở API được bảo vệ

Mỗi yêu cầu tới `/api/protected/*` và `/api/admin/*` được kiểm tra JWT, sau đó đọc lại tài khoản từ database. Tài khoản bị khóa/xóa hoặc có vai trò không hợp lệ nhận HTTP 401; API quản trị từ chối vai trò hiện tại không phải ADMIN bằng HTTP 403. Khi database không truy cập được, trả HTTP 503, không dùng quyền cũ trong token.

Thay đổi này không chủ động đóng các kết nối WebSocket đã mở trước khi khóa tài khoản.

## Xác nhận giá, tồn kho và thông tin giao hàng

- Khi đặt hàng, client tải danh sách sản phẩm mới nhất trên luồng nền. Nếu giá hoặc tồn kho đổi, hiển thị thông tin và tổng tiền mới để khách xác nhận; hủy xác nhận thì không tạo đơn.
- Mỗi lần xác nhận đặt hàng có một mã yêu cầu. Nếu mất phản hồi, nút **Gửi lại yêu cầu cũ** gửi lại cùng mã và cùng nội dung. Máy chủ trả về đơn đã lưu nếu yêu cầu trước đó thành công. Hệ thống chỉ ghi đơn và trừ kho một lần cho mỗi mã của khách hàng.
- Khi mở lại màn hình đặt hàng sau khi khởi động lại, client kiểm tra mã yêu cầu còn lưu trên máy. Nếu đơn đã được tạo, client hiển thị lại mã đơn. Nếu chưa tìm thấy đơn, khách có thể nhập lại thông tin và lần gửi tiếp theo vẫn dùng cùng mã. Client chỉ lưu mã yêu cầu cục bộ, thông tin giao hàng không được lưu cùng mã.
- Nếu sản phẩm ngừng bán, lựa chọn size/màu không còn hợp lệ hoặc số lượng vượt tồn của biến thể, yêu cầu sửa giỏ hàng trước. Sản phẩm chưa phân bổ dùng tổng tồn chung.
- Client gửi `expectedPrice` cho từng dòng đơn. Server so sánh với giá trong database dưới khóa `FOR UPDATE`, rồi mới tạo đơn/trừ kho. Nếu giá đổi sau bước xác nhận, transaction bị hủy và khách phải kiểm tra lại.
- Cập nhật server và client cùng phiên bản: client cũ không gửi `expectedPrice` sẽ bị yêu cầu tải lại giỏ hàng.
- Tên người nhận: 2–100 ký tự; địa chỉ: 10–500 ký tự; ghi chú: tối đa 1000 ký tự.
- Số điện thoại hỗ trợ số di động Việt Nam 10 chữ số với đầu 03/05/07/08/09. Dạng `+84` được chuẩn hóa về `0`; khoảng trắng, dấu chấm, gạch ngang và ngoặc được bỏ qua. Đây là kiểm tra định dạng, chưa xác minh số điện thoại thực tế bằng OTP.

## Chạy kiểm thử tự động

PowerShell, từ thư mục gốc dự án, cần JDK 21:

```powershell
mvn -f server/pom.xml test
mvn -f client-customer/pom.xml test
```

JUnit kiểm tra quyền tài khoản, chuẩn hóa và giới hạn thông tin giao hàng, gộp size/màu, cập nhật giá/tồn kho và xóa giỏ khi đổi tài khoản. Test MySQL mặc định được bỏ qua nếu chưa cấu hình URL thử nghiệm.

Để chạy cả kiểm thử giao dịch MySQL (bật MySQL trước):

```powershell
$env:FASHION_TEST_DB_URL = "jdbc:mysql://127.0.0.1:3306/?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true"
$env:FASHION_TEST_DB_USER = "root"
# Nếu MySQL có mật khẩu, đặt FASHION_TEST_DB_PASSWORD trong môi trường cục bộ.
mvn -f server/pom.xml test
Remove-Item Env:FASHION_TEST_DB_URL
Remove-Item Env:FASHION_TEST_DB_USER
```

Test tạo database riêng `fashion_test_<UUID>` từ schema trong mã nguồn và xóa database đó sau khi hoàn tất; cần tài khoản được phép tạo/xóa database thử nghiệm. Không chạy kiểm thử giao dịch trên bảng cửa hàng. Các tình huống gồm mua đồng thời, hủy đơn không hoàn kho lặp, quyền sở hữu đơn, giá cũ, size không hợp lệ và rollback toàn bộ khi nhiều size cùng vượt tồn kho tổng.
