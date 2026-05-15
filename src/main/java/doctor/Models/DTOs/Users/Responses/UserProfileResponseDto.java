package doctor.Models.DTOs.Users.Responses;

public record UserProfileResponseDto(
        Integer maNguoiDung,
        Integer maTaiKhoan,
        String tenDangNhap,
        String vaiTro,
        String trangThaiTaiKhoan,
        String hoLot,
        String ten,
        String hoTenDayDu,
        String soDienThoai,
        String email,
        String cccd,
        String anhDaiDien,
        String anhDaiDienPublicId) {}
