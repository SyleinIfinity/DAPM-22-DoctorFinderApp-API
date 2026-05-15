package doctor.Models.DTOs.Doctors.Responses;

public record DoctorProfileResponseDto(
        Integer maBacSi,
        Integer maTaiKhoan,
        Integer maNguoiDung,
        String tenDangNhap,
        String vaiTro,
        String trangThaiTaiKhoan,
        String hoLot,
        String ten,
        String hoTenDayDu,
        String soDienThoai,
        String email,
        String anhDaiDien,
        String anhDaiDienPublicId,
        String chuyenKhoa,
        String trinhDoChuyenMon,
        String loaiHinhBacSi,
        String tenCoSoYTe,
        String diaChiLamViec,
        String maChungChiHanhNghe,
        String moTaBanThan,
        String trangThaiHoSo) {}
