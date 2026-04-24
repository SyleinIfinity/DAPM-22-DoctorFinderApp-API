package doctor.Models.DTOs.Auth.Requests;

public record RegisterUserInfoRequestDto(
        String tenDangNhap,
        String matKhau,
        String xacNhanMatKhau,
        String hoLot,
        String ten,
        String soDienThoai,
        String email,
        String cccd,
        String anhDaiDien) {}
