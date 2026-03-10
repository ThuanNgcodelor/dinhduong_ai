po# Hướng Dẫn Deploy Dự Án Spring Boot Với Cloudflare Tunnel
set OLLAMA_HOST=0.0.0.0
ollama serve


mvn clean package -DskipTests
java -jar target/NUTRITION_TRACNKER-0.0.1-SNAPSHOT.jar


cloudflared tunnel --config d:\Spring\NUTRITION_TRACKER\src\main\resources\docs\cloudflared-config.yml run dinhduongcantho


Tài liệu này hướng dẫn cách đưa ứng dụng Spring Boot chạy trên `localhost:8080` lên mạng internet thông qua tên miền `dinhduongcantho.io.vn` sử dụng công cụ Cloudflared (Tunnel).

---

## I. Thao tác chạy hàng ngày (Khi tắt máy tính và bật lại)

Mỗi khi bạn tắt máy tính đi ngủ, ứng dụng và Tunnel sẽ tự động đóng. Hôm sau bật lại máy, bạn chỉ cần làm **2 bước** cực kỳ đơn giản sau để website có thể tiếp tục hoạt động:

### Bước 1: Khởi động Ứng dụng Spring Boot
1. Mở dự án trong IntelliJ IDEA (hoặc IDE của bạn).
2. Chạy ứng dụng (nút Run ứng dụng Spring Boot bình thường) và đảm bảo backend đang chạy lắng nghe ở cổng mặc định: `http://localhost:8080`

### Bước 2: Bật Cloudflare Tunnel
1. Mở **PowerShell** hoặc **Command Prompt (CMD)**.
2. Chạy đoạn lệnh sau để Tunnel kết nối ứng dụng localhost lên máy chủ Cloudflare:
   ```powershell
   cloudflared tunnel --config d:\Spring\NUTRITION_TRACKER\src\main\resources\docs\cloudflared-config.yml run dinhduongcantho
   ```
3. Đợi vài giây, khi màn hình Terminal hiện ra dòng chữ `Registered tunnel connection...` thì quá trình chạy thành công. 
4. Bạn có thể mở trình duyệt trên điện thoại hoặc máy tính và truy cập `https://dinhduongcantho.io.vn` để sử dụng!

*(Lưu ý: Không được tắt cái cửa sổ Terminal của Tunnel đi, cứ thu nhỏ đóng xuống taskbar để nó tự chạy ngầm nhé).*

---

## II. Cấu hình Cài đặt Lần đầu (Setup từ đầu đến cuối)
*(Phần này bạn **CHỈ CẦN LÀM MỘT LẦN DUY NHẤT**. Nếu lỡ cài đặt bị lỗi, hoặc bị trùng tên tunnel, hoặc đem qua máy tính tính khác chạy thì bạn mới kéo xuống đọc lại phần này).*

### Bước 1: Cài đặt phần mềm Cloudflared
Mở PowerShell (quyền Administrator) và chạy lệnh cài đặt:
```powershell
winget install --id Cloudflare.cloudflared
```

### Bước 2: Đăng nhập cấp quyền (Lấy Authorize cert)
Chạy lệnh sau để máy tính lấy chứng chỉ đăng nhập tên miền:
```powershell
cloudflared tunnel login
```
Trình duyệt web sẽ tự mở ra trang của Cloudflare. Bạn chọn tên miền muốn sử dụng (`dinhduongcantho.io.vn`) và bấm nút **Authorize**. Khi trình duyệt báo *Success* tức là thành công chứng chỉ đã lưu vào máy tính.

### Bước 3: Tạo đường hầm (Tunnel) mới
Chạy lệnh tạo một tunnel được định danh bởi cái tên bạn thiết lập:
```powershell
cloudflared tunnel create dinhduongcantho
```
Lệnh này sẽ sinh ra 1 đoạn mã **ID định danh kiểu UUID** khá dài (ví dụ: `5c94920d-xxxx-xxxx...`). **Chú ý Bôi Đen Tức Thì và Copy đoạn mã UUID này** lại.

*(Lưu ý: Nếu bị lỗi Tunnel Already Exists, hãy gõ `cloudflared tunnel list` tìm các Tunnel bị trùng, copy ID của chúng và gõ `cloudflared tunnel delete <ID>` để xóa đi nhé).*

### Bước 4: Cập nhật file cấu hình YAML (.yml)
Mở file `d:\Spring\NUTRITION_TRACKER\src\main\resources\docs\cloudflared-config.yml` có sẵn trên máy lên.
Bạn tìm vị trí dòng `credentials-file` và thay mã UUID của bạn vừa copy vào đúng cú pháp:
```yaml
tunnel: dinhduongcantho
credentials-file: C:\Users\<Username-Máy-Bạn>\.cloudflared\<UUID-BẠN-VỪA-COPY>.json

ingress:
  - hostname: dinhduongcantho.io.vn
    service: http://localhost:8080
  # ... (giữ nguyên nửa dưới file)
```
*(Nhớ Ctrl+S lưu file lại)*

### Bước 5: Cập nhật Nameserver bên bán tên miền (ZoneDNS)
Bạn phải uỷ quyền quản lý DNS tên miền cho Cloudflare:
1. Đăng nhập trang quản lý bên bán tên miền (như Mắt Bão, iNet, ZoneDNS...) -> Phần Cập nhật NameServer (Đổi DNS).
2. Trỏ 2 dòng DNS 1 và DNS 2 về địa chỉ của Cloudflare cung cấp (thường Cloudflare sẽ cấp ngẫu nhiên 2 cụm như `lynn.ns.cloudflare.com` và `nancy.ns.cloudflare.com`).
3. Nếu hệ thống bên bán bắt buộc (**có dấu * đỏ**) điền cả IP thì dùng lệnh `nslookup <tên-dns>` trên CMD để tra IP (hoặc điền mẫu IP `173.245.59.200` và `108.162.193.208`).
4. Ghi đè lưu lại cấu hình, sau đó đợi bên bảng của Cloudflare báo trạng thái tên miền thành **Active** (thường mất 15 phút - 2 tiếng đồng hồ tuỳ nhà mạng).

### Bước 6: Định tuyến tự động Tên miền gắn với Tunnel
Sau khi Cloudflare Web Dashboard đã báo dấu tick **Active**, bạn chạy lệnh sau trên PowerShell để tự động tạo bản ghi CNAME trỏ về cái Tunnel của bạn:
```powershell
cloudflared tunnel route dns dinhduongcantho dinhduongcantho.io.vn
```
*(Nếu nó phản hồi thành công thì quá trình Setup ban đầu của bạn đã chính thức OK!)*

-> Bạn có thể cuộn lên đầu tài liệu làm tiếp **Phần I: Thao tác chạy hàng ngày** để mở server lên!
