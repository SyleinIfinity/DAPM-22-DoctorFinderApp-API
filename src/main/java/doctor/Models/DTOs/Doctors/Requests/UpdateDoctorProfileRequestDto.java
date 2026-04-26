package doctor.Models.DTOs.Doctors.Requests;

public record UpdateDoctorProfileRequestDto(
        String chuyenKhoa,
        String trinhDoChuyenMon,
        String loaiHinhBacSi,
        String tenCoSoYTe,
        String diaChiLamViec,
        String maChungChiHanhNghe,
        String moTaBanThan) {}
