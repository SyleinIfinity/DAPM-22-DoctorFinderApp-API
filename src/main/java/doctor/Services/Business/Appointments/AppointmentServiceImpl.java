package doctor.Services.Business.Appointments;

import doctor.Models.DTOs.Appointments.Requests.CreateAppointmentRequestDto;
import doctor.Models.DTOs.Appointments.Requests.RejectAppointmentRequestDto;
import doctor.Models.DTOs.Appointments.Responses.AppointmentDetailResponseDto;
import doctor.Models.DTOs.Appointments.Responses.AppointmentRequestResponseDto;
import doctor.Models.DTOs.Appointments.Responses.AppointmentSummaryResponseDto;
import doctor.Models.Entities.BacSi;
import doctor.Models.Entities.ChiTietLich;
import doctor.Models.Entities.KhungGio;
import doctor.Models.Entities.LichLamViec;
import doctor.Models.Entities.NguoiDung;
import doctor.Models.Entities.PhieuDatLich;
import doctor.Models.Enums.TrangThaiChiTietLich;
import doctor.Models.Enums.TrangThaiLichLamViec;
import doctor.Models.Enums.TrangThaiPhieuDatLich;
import doctor.Repositories.Interfaces.BacSiRepository;
import doctor.Repositories.Interfaces.ChiTietLichRepository;
import doctor.Repositories.Interfaces.KhungGioRepository;
import doctor.Repositories.Interfaces.LichLamViecRepository;
import doctor.Repositories.Interfaces.NguoiDungRepository;
import doctor.Repositories.Interfaces.PhieuDatLichRepository;
import doctor.Repositories.Interfaces.DanhGiaBacSiRepository;
import doctor.Services.Interfaces.Appointments.AppointmentService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {
    private final PhieuDatLichRepository phieuDatLichRepository;
    private final ChiTietLichRepository chiTietLichRepository;
    private final LichLamViecRepository lichLamViecRepository;
    private final KhungGioRepository khungGioRepository;
    private final BacSiRepository bacSiRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final DanhGiaBacSiRepository danhGiaBacSiRepository;

    @Override
    @Transactional
    public AppointmentDetailResponseDto createAppointment(CreateAppointmentRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }

        Integer maNguoiDung = normalizePositiveId(request.maNguoiDung(), "maNguoiDung");
        Integer maChiTiet = normalizePositiveId(request.maChiTiet(), "maChiTiet");
        String loaiPhieu = requireNotBlank(request.loaiPhieu(), "loaiPhieu");
        String trieuChungGhiChu = normalizeOptional(request.trieuChungGhiChu());

        nguoiDungRepository
                .selectById(maNguoiDung)
                .orElseThrow(() -> new IllegalArgumentException("Nguoi dung khong ton tai"));

        ChiTietLich chiTiet =
                chiTietLichRepository
                        .selectByIdForUpdate(maChiTiet)
                        .orElseThrow(() -> new IllegalArgumentException("Chi tiet lich khong ton tai"));

        LichLamViec lichLamViec =
                lichLamViecRepository
                        .selectById(chiTiet.getMaLichLamViec())
                        .orElseThrow(() -> new IllegalStateException("Khong tim thay lich lam viec"));

        assertScheduleAllowsBooking(lichLamViec);
        assertChiTietAvailable(chiTiet);

        PhieuDatLich phieuDatLich = new PhieuDatLich();
        phieuDatLich.setMaNguoiDung(maNguoiDung);
        phieuDatLich.setMaChiTiet(maChiTiet);
        phieuDatLich.setLoaiPhieu(loaiPhieu);
        phieuDatLich.setTrieuChungGhiChu(trieuChungGhiChu);
        phieuDatLich.setTrangThaiPhieu(TrangThaiPhieuDatLich.CHO_XAC_NHAN.name());
        phieuDatLich.setLyDoTuChoi(null);

        PhieuDatLich created;
        try {
            created = phieuDatLichRepository.insert(phieuDatLich);
        } catch (RuntimeException ex) {
            throw translateLockSlotException(ex, "Khung gio dang duoc giu hoac da duoc dat");
        }

        return mapToAppointmentDetail(created);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentSummaryResponseDto> getAppointments(Integer maNguoiDung, String scope) {
        Integer normalizedMaNguoiDung = normalizePositiveId(maNguoiDung, "maNguoiDung");
        AppointmentScope normalizedScope = parseScope(scope);

        nguoiDungRepository
                .selectById(normalizedMaNguoiDung)
                .orElseThrow(() -> new IllegalArgumentException("Nguoi dung khong ton tai"));

        Set<String> allowedStatuses =
                normalizedScope == AppointmentScope.UPCOMING
                        ? Set.of(
                                TrangThaiPhieuDatLich.CHO_XAC_NHAN.name(),
                                TrangThaiPhieuDatLich.DA_XAC_NHAN.name())
                        : Set.of(
                                TrangThaiPhieuDatLich.DA_KHAM.name(),
                                TrangThaiPhieuDatLich.DA_HUY.name(),
                                TrangThaiPhieuDatLich.TU_CHOI.name());

        return phieuDatLichRepository.findByMaNguoiDung(normalizedMaNguoiDung).stream()
                .filter(phieu -> allowedStatuses.contains(phieu.getTrangThaiPhieu()))
                .map(this::mapToAppointmentSummary)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentDetailResponseDto getAppointmentDetail(Integer maPhieuDatLich) {
        Integer normalizedMaPhieuDatLich = normalizePositiveId(maPhieuDatLich, "maPhieuDatLich");
        PhieuDatLich phieu =
                phieuDatLichRepository
                        .selectById(normalizedMaPhieuDatLich)
                        .orElseThrow(() -> new IllegalArgumentException("Phieu dat lich khong ton tai"));
        return mapToAppointmentDetail(phieu);
    }

    @Override
    @Transactional
    public AppointmentDetailResponseDto cancelAppointment(Integer maPhieuDatLich) {
        Integer normalizedMaPhieuDatLich = normalizePositiveId(maPhieuDatLich, "maPhieuDatLich");
        PhieuDatLich phieu =
                phieuDatLichRepository
                        .selectById(normalizedMaPhieuDatLich)
                        .orElseThrow(() -> new IllegalArgumentException("Phieu dat lich khong ton tai"));

        String trangThai = normalizeOptional(phieu.getTrangThaiPhieu());
        if (TrangThaiPhieuDatLich.DA_HUY.name().equals(trangThai)) {
            throw new IllegalArgumentException("Phieu dat lich da huy");
        }
        if (TrangThaiPhieuDatLich.TU_CHOI.name().equals(trangThai)) {
            throw new IllegalArgumentException("Phieu dat lich da bi tu choi");
        }
        if (TrangThaiPhieuDatLich.DA_KHAM.name().equals(trangThai)) {
            throw new IllegalArgumentException("Phieu dat lich da kham khong the huy");
        }

        phieu.setTrangThaiPhieu(TrangThaiPhieuDatLich.DA_HUY.name());
        phieu.setLyDoTuChoi(null);

        try {
            phieu = phieuDatLichRepository.update(phieu);
        } catch (RuntimeException ex) {
            throw translateLockSlotException(ex, "Khong the huy phieu dat lich");
        }

        return mapToAppointmentDetail(phieu);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentRequestResponseDto> getAppointmentRequestsByDoctor(Integer maBacSi) {
        Integer normalizedMaBacSi = normalizePositiveId(maBacSi, "maBacSi");
        bacSiRepository
                .selectById(normalizedMaBacSi)
                .orElseThrow(() -> new IllegalArgumentException("Bac si khong ton tai"));

        return phieuDatLichRepository.findByMaBacSi(normalizedMaBacSi).stream()
                .filter(phieu -> {
                    String status = phieu.getTrangThaiPhieu();
                    return TrangThaiPhieuDatLich.CHO_XAC_NHAN.name().equals(status)
                            || TrangThaiPhieuDatLich.DA_XAC_NHAN.name().equals(status)
                            || TrangThaiPhieuDatLich.DA_KHAM.name().equals(status)
                            || TrangThaiPhieuDatLich.TU_CHOI.name().equals(status)
                            || TrangThaiPhieuDatLich.DA_HUY.name().equals(status);
                })
                .map(this::mapToAppointmentRequest)
                .toList();
    }

    @Override
    @Transactional
    public AppointmentDetailResponseDto approveAppointment(Integer maPhieuDatLich) {
        Integer normalizedMaPhieuDatLich = normalizePositiveId(maPhieuDatLich, "maPhieuDatLich");
        PhieuDatLich phieu =
                phieuDatLichRepository
                        .selectById(normalizedMaPhieuDatLich)
                        .orElseThrow(() -> new IllegalArgumentException("Phieu dat lich khong ton tai"));

        if (!TrangThaiPhieuDatLich.CHO_XAC_NHAN.name().equals(phieu.getTrangThaiPhieu())) {
            throw new IllegalArgumentException("Chi co the duyet phieu o trang thai CHO_XAC_NHAN");
        }

        phieu.setTrangThaiPhieu(TrangThaiPhieuDatLich.DA_XAC_NHAN.name());
        phieu.setLyDoTuChoi(null);

        try {
            phieu = phieuDatLichRepository.update(phieu);
        } catch (RuntimeException ex) {
            throw translateLockSlotException(ex, "Khong the duyet phieu dat lich");
        }

        return mapToAppointmentDetail(phieu);
    }

    @Override
    @Transactional
    public AppointmentDetailResponseDto markAppointmentAsCompleted(Integer maPhieuDatLich) {
        Integer normalizedMaPhieuDatLich = normalizePositiveId(maPhieuDatLich, "maPhieuDatLich");
        PhieuDatLich phieu =
                phieuDatLichRepository
                        .selectById(normalizedMaPhieuDatLich)
                        .orElseThrow(() -> new IllegalArgumentException("Phieu dat lich khong ton tai"));

        if (!TrangThaiPhieuDatLich.DA_XAC_NHAN.name().equals(phieu.getTrangThaiPhieu())) {
            throw new IllegalArgumentException("Chi co the chuyen sang DA_KHAM tu trang thai DA_XAC_NHAN");
        }

        phieu.setTrangThaiPhieu(TrangThaiPhieuDatLich.DA_KHAM.name());
        phieu.setLyDoTuChoi(null);

        try {
            phieu = phieuDatLichRepository.update(phieu);
        } catch (RuntimeException ex) {
            throw translateLockSlotException(ex, "Khong the cap nhat trang thai phieu dat lich");
        }

        return mapToAppointmentDetail(phieu);
    }

    @Override
    @Transactional
    public AppointmentDetailResponseDto rejectAppointment(
            Integer maPhieuDatLich, RejectAppointmentRequestDto request) {
        Integer normalizedMaPhieuDatLich = normalizePositiveId(maPhieuDatLich, "maPhieuDatLich");
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        String lyDoTuChoi = requireNotBlank(request.lyDoTuChoi(), "lyDoTuChoi");

        PhieuDatLich phieu =
                phieuDatLichRepository
                        .selectById(normalizedMaPhieuDatLich)
                        .orElseThrow(() -> new IllegalArgumentException("Phieu dat lich khong ton tai"));

        if (!TrangThaiPhieuDatLich.CHO_XAC_NHAN.name().equals(phieu.getTrangThaiPhieu())) {
            throw new IllegalArgumentException("Chi co the tu choi phieu o trang thai CHO_XAC_NHAN");
        }

        phieu.setTrangThaiPhieu(TrangThaiPhieuDatLich.TU_CHOI.name());
        phieu.setLyDoTuChoi(lyDoTuChoi);

        try {
            phieu = phieuDatLichRepository.update(phieu);
        } catch (RuntimeException ex) {
            throw translateLockSlotException(ex, "Khong the tu choi phieu dat lich");
        }

        return mapToAppointmentDetail(phieu);
    }

    private AppointmentSummaryResponseDto mapToAppointmentSummary(PhieuDatLich phieu) {
        AppointmentContext context = resolveContext(phieu);
        LichLamViec lichLamViec = context.lichLamViec();
        ChiTietLich chiTiet = context.chiTietLich();
        BacSi bacSi = context.bacSi();
        NguoiDung thongTinBacSi = context.thongTinBacSi();

        String hoTenBacSi =
                thongTinBacSi == null
                        ? null
                        : buildHoTenDayDu(thongTinBacSi.getHoLot(), thongTinBacSi.getTen());

        return new AppointmentSummaryResponseDto(
                phieu.getMaPhieuDatLich(),
                phieu.getMaNguoiDung(),
                bacSi.getMaBacSi(),
                phieu.getMaChiTiet(),
                phieu.getLoaiPhieu(),
                phieu.getTrieuChungGhiChu(),
                phieu.getTrangThaiPhieu(),
                phieu.getLyDoTuChoi(),
                canReviewAppointment(phieu),
                lichLamViec.getNgayCuThe(),
                lichLamViec.getThuTrongTuan(),
                chiTiet.getGioBatDau(),
                chiTiet.getGioKetThuc(),
                hoTenBacSi,
                bacSi.getChuyenKhoa(),
                bacSi.getTenCoSoYTe(),
                bacSi.getDiaChiLamViec());
    }

    private AppointmentRequestResponseDto mapToAppointmentRequest(PhieuDatLich phieu) {
        AppointmentContext context = resolveContext(phieu);
        LichLamViec lichLamViec = context.lichLamViec();
        ChiTietLich chiTiet = context.chiTietLich();
        NguoiDung benhNhan = context.benhNhan();

        String hoTenBenhNhan = buildHoTenDayDu(benhNhan.getHoLot(), benhNhan.getTen());
        return new AppointmentRequestResponseDto(
                phieu.getMaPhieuDatLich(),
                phieu.getMaNguoiDung(),
                phieu.getMaChiTiet(),
                hoTenBenhNhan,
                benhNhan.getSoDienThoai(),
                benhNhan.getEmail(),
                lichLamViec.getNgayCuThe(),
                lichLamViec.getThuTrongTuan(),
                chiTiet.getGioBatDau(),
                chiTiet.getGioKetThuc(),
                phieu.getLoaiPhieu(),
                phieu.getTrieuChungGhiChu(),
                phieu.getTrangThaiPhieu());
    }

    private AppointmentDetailResponseDto mapToAppointmentDetail(PhieuDatLich phieu) {
        AppointmentContext context = resolveContext(phieu);

        PhieuDatLich phieuDatLich = context.phieuDatLich();
        LichLamViec lichLamViec = context.lichLamViec();
        ChiTietLich chiTiet = context.chiTietLich();
        KhungGio khungGio = context.khungGio();
        BacSi bacSi = context.bacSi();
        NguoiDung benhNhan = context.benhNhan();
        NguoiDung thongTinBacSi = context.thongTinBacSi();

        String hoTenBenhNhan = buildHoTenDayDu(benhNhan.getHoLot(), benhNhan.getTen());
        String hoTenBacSi =
                thongTinBacSi == null
                        ? null
                        : buildHoTenDayDu(thongTinBacSi.getHoLot(), thongTinBacSi.getTen());

        return new AppointmentDetailResponseDto(
                phieuDatLich.getMaPhieuDatLich(),
                phieuDatLich.getTrangThaiPhieu(),
                phieuDatLich.getLyDoTuChoi(),
                phieuDatLich.getLoaiPhieu(),
                phieuDatLich.getTrieuChungGhiChu(),
                benhNhan.getMaNguoiDung(),
                hoTenBenhNhan,
                benhNhan.getSoDienThoai(),
                benhNhan.getEmail(),
                bacSi.getMaBacSi(),
                hoTenBacSi,
                bacSi.getChuyenKhoa(),
                bacSi.getTenCoSoYTe(),
                bacSi.getDiaChiLamViec(),
                lichLamViec.getNgayCuThe(),
                lichLamViec.getThuTrongTuan(),
                chiTiet.getGioBatDau(),
                chiTiet.getGioKetThuc(),
                chiTiet.getMaChiTiet(),
                lichLamViec.getMaLichLamViec(),
                lichLamViec.getMaKhungGio(),
                khungGio == null ? null : khungGio.getThoiLuongPhut(),
                lichLamViec.getTrangThaiLich());
    }

    private AppointmentContext resolveContext(PhieuDatLich phieuDatLich) {
        PhieuDatLich phieu = requirePhieu(phieuDatLich);
        NguoiDung benhNhan =
                nguoiDungRepository
                        .selectById(phieu.getMaNguoiDung())
                        .orElseThrow(() -> new IllegalStateException("Khong tim thay nguoi dung"));

        ChiTietLich chiTiet =
                chiTietLichRepository
                        .selectById(phieu.getMaChiTiet())
                        .orElseThrow(() -> new IllegalStateException("Khong tim thay chi tiet lich"));

        LichLamViec lichLamViec =
                lichLamViecRepository
                        .selectById(chiTiet.getMaLichLamViec())
                        .orElseThrow(() -> new IllegalStateException("Khong tim thay lich lam viec"));

        KhungGio khungGio = khungGioRepository.selectById(lichLamViec.getMaKhungGio()).orElse(null);

        BacSi bacSi =
                bacSiRepository
                        .selectById(lichLamViec.getMaBacSi())
                        .orElseThrow(() -> new IllegalStateException("Khong tim thay bac si"));

        NguoiDung thongTinBacSi = nguoiDungRepository.findByMaTaiKhoan(bacSi.getMaTaiKhoan()).orElse(null);

        return new AppointmentContext(
                phieu, benhNhan, bacSi, thongTinBacSi, lichLamViec, chiTiet, khungGio);
    }

    private PhieuDatLich requirePhieu(PhieuDatLich phieuDatLich) {
        if (phieuDatLich == null || phieuDatLich.getMaPhieuDatLich() == null) {
            throw new IllegalArgumentException("phieuDatLich is required");
        }
        return phieuDatLich;
    }

    private void assertScheduleAllowsBooking(LichLamViec lichLamViec) {
        String normalized = normalizeOptional(lichLamViec.getTrangThaiLich());
        if (normalized == null) {
            return;
        }
        try {
            if (!TrangThaiLichLamViec.valueOf(normalized).choPhepDatLich()) {
                throw new IllegalArgumentException("Lich hien tai khong nhan dat lich");
            }
        } catch (IllegalArgumentException ignored) {
            // Unknown status: do not block booking
        }
    }

    private void assertChiTietAvailable(ChiTietLich chiTiet) {
        String trangThai = normalizeOptional(chiTiet.getTrangThai());
        if (TrangThaiChiTietLich.TRONG.name().equals(trangThai)) {
            return;
        }

        if (TrangThaiChiTietLich.DANG_GIU.name().equals(trangThai)) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime khoaDen = chiTiet.getKhoaDen();
            if (khoaDen == null || !khoaDen.isAfter(now)) {
                return;
            }
        }

        throw new IllegalArgumentException("Khung gio dang duoc giu hoac da duoc dat");
    }

    private RuntimeException translateLockSlotException(RuntimeException ex, String fallbackMessage) {
        if (ex == null) {
            return new IllegalArgumentException(fallbackMessage);
        }
        return new IllegalArgumentException(fallbackMessage, ex);
    }

    private AppointmentScope parseScope(String scope) {
        String normalized = requireNotBlank(scope, "scope").toLowerCase();
        return switch (normalized) {
            case "upcoming" -> AppointmentScope.UPCOMING;
            case "history" -> AppointmentScope.HISTORY;
            default -> throw new IllegalArgumentException("scope phai la upcoming hoac history");
        };
    }

    private Integer normalizePositiveId(Integer id, String fieldName) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return id;
    }

    private String requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }

    private boolean canReviewAppointment(PhieuDatLich phieu) {
        if (phieu == null || phieu.getMaNguoiDung() == null || phieu.getMaChiTiet() == null) {
            return false;
        }
        if (!TrangThaiPhieuDatLich.DA_KHAM.name().equals(phieu.getTrangThaiPhieu())) {
            return false;
        }

        Integer maBacSi = resolveDoctorId(phieu);
        if (maBacSi == null) {
            return false;
        }

        return danhGiaBacSiRepository
                .findByMaNguoiDungAndMaBacSi(phieu.getMaNguoiDung(), maBacSi)
                .isEmpty();
    }

    private Integer resolveDoctorId(PhieuDatLich phieu) {
        ChiTietLich chiTiet =
                chiTietLichRepository.selectById(phieu.getMaChiTiet()).orElse(null);
        if (chiTiet == null) {
            return null;
        }
        LichLamViec lichLamViec =
                lichLamViecRepository.selectById(chiTiet.getMaLichLamViec()).orElse(null);
        return lichLamViec == null ? null : lichLamViec.getMaBacSi();
    }

    private String buildHoTenDayDu(String hoLot, String ten) {
        String normalizedHoLot = normalizeOptional(hoLot);
        String normalizedTen = normalizeOptional(ten);
        if (normalizedHoLot == null) {
            return normalizedTen;
        }
        if (normalizedTen == null) {
            return normalizedHoLot;
        }
        return normalizedHoLot + " " + normalizedTen;
    }

    private enum AppointmentScope {
        UPCOMING,
        HISTORY
    }

    private record AppointmentContext(
            PhieuDatLich phieuDatLich,
            NguoiDung benhNhan,
            BacSi bacSi,
            NguoiDung thongTinBacSi,
            LichLamViec lichLamViec,
            ChiTietLich chiTietLich,
            KhungGio khungGio) {}
}

