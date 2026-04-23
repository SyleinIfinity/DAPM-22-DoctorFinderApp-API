package doctor.Models.DTOs.Auth.Requests;

public record RegisterRequestDto(
        String tenDangNhap,
        String matKhau,
        String xacNhanMatKhau,
        String vaiTro,
        String hoLot,
        String ten,
        String soDienThoai,
        String email,
        String cccd,
        String anhDaiDien,
        String chuyenKhoa,
        String trinhDoChuyenMon,
        String loaiHinhBacSi,
        String tenCoSoYTe,
        String diaChiLamViec,
        String maChungChiHanhNghe,
        String moTaBanThan) {}
