🎬 CinemaManagement - Ứng dụng quản lý rạp chiếu phim
CinemaManagement là một ứng dụng Android được phát triển bằng Java trên Android Studio, hỗ trợ các chức năng quản lý rạp chiếu phim hiện đại. Ứng dụng sử dụng Firebase để lưu trữ dữ liệu, xác thực người dùng và xử lý backend; đồng thời tích hợp Cloudinary để lưu trữ hình ảnh và video (phim, poster, trailer...).

🔧 Công nghệ sử dụng
Java – Ngôn ngữ chính để phát triển Android app

Android Studio – Môi trường phát triển

Firebase:

Authentication – Đăng ký / đăng nhập tài khoản người dùng và admin

Firestore – Lưu trữ dữ liệu thời gian thực (phim, suất chiếu, tài khoản, hóa đơn...)

Cloudinary – Lưu trữ và quản lý hình ảnh, video chất lượng cao

⚙️ Chức năng chính
🎥 Quản lý phim
Thêm, sửa, xóa phim

Lưu poster, trailer qua Cloudinary

Hiển thị thông tin phim: tên, mô tả, thể loại, độ dài, giới hạn tuổi, hình ảnh, trailer

🕒 Quản lý suất chiếu
Quản lý lịch chiếu theo rạp, ngày, giờ

Giao diện chọn rạp → ngày → giờ chiếu

Chức năng cho admin chỉnh sửa/xóa suất chiếu

🍿 Đặt vé và chọn món
Người dùng chọn vé xem phim, ghế ngồi

Tùy chọn món ăn/combo khi đặt vé

Tính tổng tiền vé + món ăn, hiển thị hóa đơn

🛒 Quản lý đồ ăn
Admin thêm, sửa, xóa món ăn, combo

Người dùng chọn món ăn khi đặt vé

🧾 Quản lý hóa đơn / lịch sử đặt vé
Hiển thị thông tin hóa đơn sau khi đặt vé

Người dùng xem lại lịch sử đặt vé

🔐 Quản lý tài khoản và phân quyền
Hệ thống đăng nhập: phân quyền admin và khách hàng

Admin có thể quản lý người dùng (xem, sửa, xóa)
