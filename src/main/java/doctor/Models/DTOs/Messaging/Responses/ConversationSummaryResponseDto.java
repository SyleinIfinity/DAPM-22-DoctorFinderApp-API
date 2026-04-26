package doctor.Models.DTOs.Messaging.Responses;

import java.time.LocalDateTime;

public record ConversationSummaryResponseDto(
        Integer maCuocHoiThoai,
        Integer maNguoiDung,
        String hoTenBenhNhan,
        String anhDaiDienBenhNhan,
        Integer maBacSi,
        String hoTenBacSi,
        String anhDaiDienBacSi,
        String chuyenKhoa,
        String tenCoSoYTe,
        String diaChiLamViec,
        LocalDateTime ngayTao,
        Integer maTinNhanCuoi,
        Integer maTaiKhoanGuiCuoi,
        String loaiNoiDungCuoi,
        String noiDungCuoi,
        LocalDateTime thoiGianGuiCuoi) {}

