package doctor.Models.DTOs.Admin.Responses;

public record PendingDoctorProfileResponseDto(
        Integer maBacSi,
        Integer maTaiKhoan,
        Integer maNguoiDung,
        String hoTenDayDu,
        String soDienThoai,
        String email,
        String chuyenKhoa,
        String maChungChiHanhNghe,
        String trangThaiHoSo,
        int soLuongTaiLieu) {}
