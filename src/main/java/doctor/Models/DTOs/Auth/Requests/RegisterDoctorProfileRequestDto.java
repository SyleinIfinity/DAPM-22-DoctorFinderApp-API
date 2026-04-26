package doctor.Models.DTOs.Auth.Requests;

public record RegisterDoctorProfileRequestDto(
        String chuyenKhoa,
        String trinhDoChuyenMon,
        String loaiHinhBacSi,
        String tenCoSoYTe,
        String diaChiLamViec,
        String maChungChiHanhNghe,
        String moTaBanThan) {}
