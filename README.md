# 🏦 Mini Banking System (E-Bank)

Ứng dụng ngân hàng mini xây dựng bằng **Spring Boot**, cho phép khách hàng quản lý tài khoản, chuyển khoản, xem lịch sử giao dịch; quản trị viên quản lý người dùng và tài khoản.

## ✨ Tính năng

**Khách hàng (role `user`)**
- Đăng ký tài khoản mới, đăng nhập / đăng xuất
- Dashboard: xem số dư, tài khoản thanh toán (`PAYMENT`) và tiết kiệm (`SAVING`)
- Chuyển khoản giữa các tài khoản (tra cứu người nhận theo số tài khoản)
- Xem lịch sử giao dịch, tải thêm giao dịch, đếm thông báo chưa đọc
- Gửi yêu cầu cập nhật thông tin cá nhân (chờ admin duyệt)

**Quản trị viên (role `admin`)**
- Tìm kiếm, xem chi tiết người dùng
- Duyệt / xử lý yêu cầu cập nhật thông tin
- Tạo tài khoản ngân hàng cho người dùng, xem chi tiết tài khoản

**Bảo mật & kỹ thuật**
- Spring Security (form login, phân quyền `/admin/**` và `/dashboard/**`), mật khẩu mã hoá **BCrypt**
- Rate limiting theo IP và theo người dùng (Caffeine cache)
- Optimistic locking (`@Version`) và ràng buộc `CHECK (balance >= 0)` ở DB để tránh sai lệch số dư
- Xử lý ngoại lệ tập trung (`GlobalExceptionHandler`)

## 🧰 Công nghệ sử dụng

| Thành phần | Phiên bản / Công nghệ |
|---|---|
| Ngôn ngữ | Java 21 |
| Framework | Spring Boot 4.1.1 (Web, Security, Data JPA, Validation) |
| Giao diện | Thymeleaf + HTML/CSS/JavaScript |
| Cơ sở dữ liệu | PostgreSQL |
| Cache | Caffeine |
| Build tool | Maven (có sẵn Maven Wrapper) |
| Container | Docker (multi-stage build) |

## 📋 Yêu cầu hệ thống

- **JDK 21** trở lên
- **PostgreSQL** (khuyến nghị 14+)
- **Git** (nếu clone từ repo)
- *(Không bắt buộc)* Maven — đã có `mvnw` đi kèm
- *(Không bắt buộc)* Docker — nếu muốn chạy bằng container

Kiểm tra nhanh:

```bash
java -version      # phải là 21+
psql --version
```

## 🚀 Cài đặt & chạy

### Bước 1: Lấy mã nguồn

```bash
git clone <url-repo-cua-ban> Mini-Banking-System
cd Mini-Banking-System
```

Hoặc giải nén file `Mini-Banking-System-main.zip` rồi `cd` vào thư mục vừa giải nén.

### Bước 2: Tạo database PostgreSQL

Mặc định ứng dụng kết nối tới `localhost:5432`, database `bankingdb`, user `postgres`, mật khẩu `123456`.

```bash
psql -U postgres -c "CREATE DATABASE bankingdb;"
```

Nếu PostgreSQL của bạn dùng tài khoản/mật khẩu khác, xem mục [Cấu hình](#️-cấu-hình) bên dưới.

### Bước 3: Chạy ứng dụng lần đầu (để Hibernate tạo bảng)

Linux / macOS:

```bash
./mvnw spring-boot:run
```

Windows:

```cmd
mvnw.cmd spring-boot:run
```

Khi thấy log `Started DemoApplication ...` là ứng dụng đã chạy. Lần này Hibernate (`ddl-auto=update`) sẽ tự tạo các bảng `users`, `accounts`, `transactions`, `user_update_requests`. Sau đó **dừng ứng dụng** (`Ctrl + C`).

### Bước 4: Nạp dữ liệu mẫu

> ⚠️ Cấu hình `spring.sql.init.mode=never` nên file `data.sql` **không tự chạy**. Bạn cần nạp thủ công.
>
> ⚠️ Script bắt đầu bằng lệnh `TRUNCATE` nên sẽ **xoá toàn bộ dữ liệu cũ** trong 4 bảng trên. Vì vậy cần chạy Bước 3 trước để các bảng đã tồn tại.

```bash
psql -U postgres -d bankingdb -f src/main/resources/data.sql
```

Script sẽ tạo: **100 người dùng**, **200 tài khoản** (100 thanh toán + 100 tiết kiệm) và khoảng **500 giao dịch** mẫu.

### Bước 5: Chạy lại ứng dụng

```bash
./mvnw spring-boot:run
```

Lúc khởi động, `DataSeeder` sẽ tự động mã hoá BCrypt các mật khẩu dạng văn bản thô trong DB (nên bước này là bắt buộc để đăng nhập được).

Truy cập: **http://localhost:8080**

## 🔑 Tài khoản demo

| Vai trò | Username | Mật khẩu |
|---|---|---|
| Khách hàng | `user` | `Demo@2026` |
| Quản trị viên | `admin` | `SecureAdmin!2026` |
| Khách hàng | `mai` | `Mai123@` |
| Khách hàng | `bach` | `Bach123@` |
| Khách hàng | `linh` | `Linh321@` |
| Khách hàng | `khachhang11` … `khachhang100` | `123456` |

Một số số tài khoản mẫu: `8800000001` (thanh toán của `user`), `8800000003` (của `mai`), `9900000001` (tiết kiệm của `user`).

> 🔒 Đây chỉ là dữ liệu demo. **Không dùng các mật khẩu này trong môi trường thật.**

## ⚙️ Cấu hình

Cấu hình nằm ở `src/main/resources/application.properties`. Thông tin kết nối DB đọc từ biến môi trường, nếu không có sẽ dùng giá trị mặc định:

| Biến môi trường | Mặc định |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/bankingdb` |
| `DB_USERNAME` | `postgres` |
| `DB_PASSWORD` | `123456` |

Ví dụ (Linux / macOS):

```bash
export DB_URL=jdbc:postgresql://localhost:5432/bankingdb
export DB_USERNAME=postgres
export DB_PASSWORD=mat_khau_cua_ban
./mvnw spring-boot:run
```

Ví dụ (Windows PowerShell):

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/bankingdb"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="mat_khau_cua_ban"
.\mvnw.cmd spring-boot:run
```

Đổi cổng chạy: sửa `server.port=8080` trong `application.properties`.

## 📦 Build file JAR

```bash
./mvnw clean package -DskipTests
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

## 🐳 Chạy bằng Docker

Dự án có sẵn `Dockerfile` (build bằng Maven + chạy bằng JRE 21). Ứng dụng cần một PostgreSQL để kết nối.

**1. Tạo mạng và chạy PostgreSQL:**

```bash
docker network create bank-net

docker run -d --name bank-db --network bank-net \
  -e POSTGRES_PASSWORD=123456 -e POSTGRES_DB=bankingdb \
  -p 5432:5432 postgres:16
```

**2. Build và chạy ứng dụng:**

```bash
docker build -t mini-banking .

docker run -d --name mini-banking --network bank-net -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://bank-db:5432/bankingdb \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=123456 \
  mini-banking
```

**3. Nạp dữ liệu mẫu** (sau khi ứng dụng đã khởi động và tạo xong bảng):

```bash
docker exec -i bank-db psql -U postgres -d bankingdb < src/main/resources/data.sql
docker restart mini-banking   # để DataSeeder mã hoá mật khẩu
```

Truy cập: **http://localhost:8080**

## 🗂️ Cấu trúc dự án

```
Mini-Banking-System/
├── Dockerfile
├── pom.xml
├── mvnw / mvnw.cmd
└── src/main/
    ├── java/com/example/demo/
    │   ├── config/security/   # SecurityConfig, RateLimitInterceptor, DataSeeder...
    │   ├── controller/        # Controller MVC + REST (Auth, Admin, Dashboard, Transfer, Transaction)
    │   ├── dto/               # Các đối tượng truyền dữ liệu
    │   ├── exception/         # Ngoại lệ tuỳ chỉnh + GlobalExceptionHandler
    │   ├── model/             # Entity: User, Account, Transaction, UserUpdateRequest
    │   ├── repository/        # Spring Data JPA repositories
    │   ├── service/           # Nghiệp vụ: chuyển khoản, người dùng, tài khoản
    │   └── util/
    └── resources/
        ├── application.properties
        ├── data.sql           # Dữ liệu mẫu (nạp thủ công)
        ├── static/            # CSS, JS, hình ảnh
        └── templates/         # Giao diện Thymeleaf
```

## 🌐 Các đường dẫn chính

| Đường dẫn | Mô tả | Quyền |
|---|---|---|
| `/` , `/login` | Trang đăng nhập | Công khai |
| `/register` | Đăng ký | Công khai |
| `/dashboard` | Trang chủ khách hàng | `user` |
| `/dashboard/transfer` | Chuyển khoản | `user` |
| `/dashboard/history` | Lịch sử giao dịch | `user` |
| `/admin` | Quản lý người dùng | `admin` |
| `/admin/account` | Quản lý tài khoản | `admin` |

## 🛠️ Xử lý sự cố thường gặp

| Lỗi | Nguyên nhân & cách xử lý |
|---|---|
| `Connection refused` / không kết nối được DB | PostgreSQL chưa chạy, hoặc sai `DB_URL` / user / mật khẩu |
| `password authentication failed` | Sai `DB_PASSWORD`, hãy đặt lại biến môi trường |
| `database "bankingdb" does not exist` | Chưa tạo database ở Bước 2 |
| `relation "users" does not exist` khi nạp `data.sql` | Chưa chạy ứng dụng ít nhất một lần (Bước 3) để tạo bảng |
| Có dữ liệu nhưng không đăng nhập được | Chưa khởi động lại ứng dụng sau khi nạp `data.sql` (Bước 5), mật khẩu chưa được mã hoá BCrypt |
| `Port 8080 was already in use` | Đổi `server.port` hoặc tắt tiến trình đang dùng cổng 8080 |
| `invalid target release: 21` | Máy đang dùng JDK thấp hơn 21, hãy cài JDK 21 và kiểm tra `JAVA_HOME` |
| Thao tác bị chặn "quá nhanh" (HTTP 429) | Do rate limit: tối thiểu 1 giây giữa các thao tác POST của cùng một người dùng |

## 📄 Giấy phép

Dự án phát hành theo giấy phép ghi trong file [LICENSE](LICENSE).
