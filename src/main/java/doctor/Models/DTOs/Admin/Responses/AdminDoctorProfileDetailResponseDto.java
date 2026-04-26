package doctor.Models.DTOs.Admin.Responses;

import doctor.Models.DTOs.DoctorDocuments.Responses.DoctorDocumentResponseDto;
import java.util.List;

public record AdminDoctorProfileDetailResponseDto(
        Integer maBacSi,
        Integer maTaiKhoan,
        Integer maNguoiDung,
        String hoLot,
        String ten,
        String hoTenDayDu,
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
        String moTaBanThan,
        String trangThaiHoSo,
        List<DoctorDocumentResponseDto> documents) {}

