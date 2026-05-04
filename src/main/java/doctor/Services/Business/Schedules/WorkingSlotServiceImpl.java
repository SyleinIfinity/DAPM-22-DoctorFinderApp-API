package doctor.Services.Business.Schedules;

import doctor.Models.DTOs.Schedules.Requests.UpsertDoctorWorkingSlotsRequestDto;
import doctor.Models.DTOs.Schedules.Requests.WorkingSlotUpsertItemDto;
import doctor.Models.DTOs.Schedules.Responses.WorkingScheduleResponseDto;
import doctor.Models.DTOs.Schedules.Responses.WorkingScheduleSlotResponseDto;
import doctor.Models.DTOs.Schedules.Responses.WorkingSlotResponseDto;
import doctor.Models.Entities.BacSi;
import doctor.Models.Entities.ChiTietLich;
import doctor.Models.Entities.KhungGio;
import doctor.Models.Entities.LichLamViec;
import doctor.Models.Enums.TrangThaiChiTietLich;
import doctor.Models.Enums.TrangThaiLichLamViec;
import doctor.Repositories.Interfaces.BacSiRepository;
import doctor.Repositories.Interfaces.ChiTietLichRepository;
import doctor.Repositories.Interfaces.KhungGioRepository;
import doctor.Repositories.Interfaces.LichLamViecRepository;
import doctor.Services.Interfaces.Schedules.WorkingSlotService;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkingSlotServiceImpl implements WorkingSlotService {
    private static final int THU_TRONG_TUAN_MIN = 2;
    private static final int THU_TRONG_TUAN_MAX = 8;

    private final BacSiRepository bacSiRepository;
    private final LichLamViecRepository lichLamViecRepository;
    private final ChiTietLichRepository chiTietLichRepository;
    private final KhungGioRepository khungGioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<WorkingSlotResponseDto> getWorkingSlotsByDoctorAndDate(Integer maBacSi, LocalDate date) {
        Integer normalizedMaBacSi = normalizePositiveId(maBacSi, "maBacSi");
        LocalDate normalizedDate = requireDate(date, "date");
        requireBacSi(normalizedMaBacSi);

        List<LichLamViec> schedulesForDate =
                lichLamViecRepository.findByMaBacSiAndNgayCuThe(normalizedMaBacSi, normalizedDate).stream()
                        .filter(this::shouldExposeSchedule)
                        .toList();

        List<LichLamViec> schedulesToUse = schedulesForDate;
        if (schedulesToUse.isEmpty()) {
            int thuTheoNgay = normalizeThuTrongTuanFromDate(normalizedDate);
            List<LichLamViec> weeklySchedules = new ArrayList<>(
                    lichLamViecRepository.findByMaBacSiAndThuTrongTuan(normalizedMaBacSi, thuTheoNgay));

            Integer javaThu = convertVietnamThuToJava(thuTheoNgay);
            if (javaThu != null) {
                weeklySchedules.addAll(
                        lichLamViecRepository.findByMaBacSiAndThuTrongTuan(normalizedMaBacSi, javaThu));
            }

            schedulesToUse =
                    weeklySchedules.stream()
                            .filter(this::shouldExposeSchedule)
                            .sorted(Comparator.comparing(LichLamViec::getGioBatDau))
                            .toList();
        }

        if (schedulesToUse.isEmpty()) {
            return List.of();
        }

        Map<Integer, Integer> thoiLuongPhutByMaKhungGio = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        List<WorkingSlotResponseDto> result = new ArrayList<>();
        for (LichLamViec lichLamViec : schedulesToUse) {
            Integer thoiLuongPhut =
                    resolveThoiLuongPhut(
                            thoiLuongPhutByMaKhungGio, lichLamViec.getMaKhungGio());

            for (ChiTietLich chiTiet : chiTietLichRepository.findByMaLichLamViec(lichLamViec.getMaLichLamViec())) {
                String effectiveTrangThai = resolveEffectiveChiTietTrangThai(chiTiet, now);
                result.add(
                        new WorkingSlotResponseDto(
                                chiTiet.getMaChiTiet(),
                                lichLamViec.getMaLichLamViec(),
                                lichLamViec.getMaBacSi(),
                                lichLamViec.getThuTrongTuan(),
                                lichLamViec.getNgayCuThe(),
                                chiTiet.getGioBatDau(),
                                chiTiet.getGioKetThuc(),
                                effectiveTrangThai,
                                chiTiet.getKhoaDen(),
                                chiTiet.getMaPhieuDatLichHienTai(),
                                lichLamViec.getMaKhungGio(),
                                thoiLuongPhut,
                                lichLamViec.getTrangThaiLich()));
            }
        }

        result.sort(Comparator.comparing(WorkingSlotResponseDto::gioBatDau));
        return result;
    }

    @Override
    @Transactional
    public List<WorkingScheduleResponseDto> upsertDoctorWorkingSlots(
            Integer maBacSi, UpsertDoctorWorkingSlotsRequestDto request) {
        Integer normalizedMaBacSi = normalizePositiveId(maBacSi, "maBacSi");
        requireBacSi(normalizedMaBacSi);

        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("items is required");
        }

        List<NormalizedItem> normalizedItems = normalizeItems(request.items());
        Set<ScheduleKey> affectedKeys = new HashSet<>();
        for (NormalizedItem item : normalizedItems) {
            affectedKeys.add(item.key());
        }

        cancelSchedulesForKeys(normalizedMaBacSi, affectedKeys);

        List<WorkingScheduleResponseDto> createdSchedules = new ArrayList<>();
        for (NormalizedItem item : normalizedItems) {
            LichLamViec lichLamViec = new LichLamViec();
            lichLamViec.setMaBacSi(normalizedMaBacSi);
            lichLamViec.setThuTrongTuan(item.key().thuTrongTuan());
            lichLamViec.setNgayCuThe(item.key().ngayCuThe());
            lichLamViec.setGioBatDau(item.gioBatDau());
            lichLamViec.setGioKetThuc(item.gioKetThuc());
            lichLamViec.setMaKhungGio(item.maKhungGio());
            lichLamViec.setSoLuongToiDa(item.soLuongToiDa());
            lichLamViec.setTrangThaiLich(item.trangThaiLich());

            LichLamViec created = lichLamViecRepository.insert(lichLamViec);
            List<WorkingScheduleSlotResponseDto> createdChiTiet = createChiTietSlots(created, item);

            createdSchedules.add(
                    new WorkingScheduleResponseDto(
                            created.getMaLichLamViec(),
                            created.getMaBacSi(),
                            created.getThuTrongTuan(),
                            created.getNgayCuThe(),
                            created.getGioBatDau(),
                            created.getGioKetThuc(),
                            created.getMaKhungGio(),
                            item.thoiLuongPhut(),
                            created.getSoLuongToiDa(),
                            created.getTrangThaiLich(),
                            createdChiTiet));
        }

        createdSchedules.sort(
                Comparator.comparing(
                                WorkingScheduleResponseDto::ngayCuThe,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(
                                WorkingScheduleResponseDto::thuTrongTuan,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(WorkingScheduleResponseDto::gioBatDau));
        return createdSchedules;
    }

    private List<NormalizedItem> normalizeItems(List<WorkingSlotUpsertItemDto> items) {
        List<NormalizedItem> normalized = new ArrayList<>();
        for (WorkingSlotUpsertItemDto item : items) {
            if (item == null) {
                throw new IllegalArgumentException("items khong hop le");
            }

            ScheduleKey key = normalizeKey(item.thuTrongTuan(), item.ngayCuThe());
            LocalTime gioBatDau = requireTime(item.gioBatDau(), "gioBatDau");
            LocalTime gioKetThuc = requireTime(item.gioKetThuc(), "gioKetThuc");
            if (!gioKetThuc.isAfter(gioBatDau)) {
                throw new IllegalArgumentException("gioKetThuc phai sau gioBatDau");
            }

            Integer maKhungGio = normalizePositiveId(item.maKhungGio(), "maKhungGio");
            KhungGio khungGio =
                    khungGioRepository
                            .selectById(maKhungGio)
                            .orElseThrow(() -> new IllegalArgumentException("Khung gio khong ton tai"));

            int thoiLuongPhut = Math.max(1, khungGio.getThoiLuongPhut());
            int maxSlots = computeMaxSlots(gioBatDau, gioKetThuc, thoiLuongPhut);

            int soLuongToiDa =
                    item.soLuongToiDa() == null
                            ? maxSlots
                            : normalizeSoLuongToiDa(item.soLuongToiDa(), maxSlots);

            String trangThaiLich = normalizeTrangThaiLich(item.trangThaiLich());
            normalized.add(
                    new NormalizedItem(
                            key,
                            gioBatDau,
                            gioKetThuc,
                            maKhungGio,
                            thoiLuongPhut,
                            soLuongToiDa,
                            trangThaiLich));
        }

        return normalized;
    }

    private void cancelSchedulesForKeys(Integer maBacSi, Set<ScheduleKey> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        for (ScheduleKey key : keys) {
            List<LichLamViec> existingSchedules = loadSchedulesByKey(maBacSi, key);
            for (LichLamViec existing : existingSchedules) {
                if (existing == null) {
                    continue;
                }

                String trangThai = normalizeOptional(existing.getTrangThaiLich());
                if (TrangThaiLichLamViec.DA_HUY.name().equals(trangThai)
                        || TrangThaiLichLamViec.DA_KET_THUC.name().equals(trangThai)) {
                    continue;
                }

                assertScheduleCanBeCancelled(existing, now);
                existing.setTrangThaiLich(TrangThaiLichLamViec.DA_HUY.name());
                lichLamViecRepository.update(existing);
            }
        }
    }

    private void assertScheduleCanBeCancelled(LichLamViec lichLamViec, LocalDateTime now) {
        for (ChiTietLich chiTiet : chiTietLichRepository.findByMaLichLamViec(lichLamViec.getMaLichLamViec())) {
            if (chiTiet == null) {
                continue;
            }

            String trangThai = normalizeOptional(chiTiet.getTrangThai());
            if (TrangThaiChiTietLich.DA_DAT.name().equals(trangThai)) {
                throw new IllegalArgumentException("Khong the cap nhat lich vi da co lich hen duoc xac nhan");
            }

            if (TrangThaiChiTietLich.DANG_GIU.name().equals(trangThai)) {
                LocalDateTime khoaDen = chiTiet.getKhoaDen();
                if (khoaDen == null || khoaDen.isAfter(now)) {
                    throw new IllegalArgumentException("Khong the cap nhat lich vi dang co nguoi giu khung gio");
                }
            }
        }
    }

    private List<LichLamViec> loadSchedulesByKey(Integer maBacSi, ScheduleKey key) {
        if (key == null) {
            return List.of();
        }

        List<LichLamViec> result = new ArrayList<>();
        if (key.ngayCuThe() != null) {
            result.addAll(lichLamViecRepository.findByMaBacSiAndNgayCuThe(maBacSi, key.ngayCuThe()));
        } else if (key.thuTrongTuan() != null) {
            result.addAll(lichLamViecRepository.findByMaBacSiAndThuTrongTuan(maBacSi, key.thuTrongTuan()));

            Integer javaThu = convertVietnamThuToJava(key.thuTrongTuan());
            if (javaThu != null) {
                result.addAll(lichLamViecRepository.findByMaBacSiAndThuTrongTuan(maBacSi, javaThu));
            }
        }

        if (result.isEmpty()) {
            return List.of();
        }

        Set<Integer> seen = new HashSet<>();
        List<LichLamViec> deduped = new ArrayList<>();
        for (LichLamViec item : result) {
            if (item == null || item.getMaLichLamViec() == null) {
                continue;
            }
            if (seen.add(item.getMaLichLamViec())) {
                deduped.add(item);
            }
        }
        deduped.sort(Comparator.comparing(LichLamViec::getGioBatDau));
        return deduped;
    }

    private List<WorkingScheduleSlotResponseDto> createChiTietSlots(LichLamViec created, NormalizedItem item) {
        List<WorkingScheduleSlotResponseDto> result = new ArrayList<>();
        LocalTime slotStart = item.gioBatDau();
        while (true) {
            LocalTime slotEnd = slotStart.plusMinutes(item.thoiLuongPhut());
            if (slotEnd.isAfter(item.gioKetThuc())) {
                break;
            }

            ChiTietLich chiTietLich = new ChiTietLich();
            chiTietLich.setMaLichLamViec(created.getMaLichLamViec());
            chiTietLich.setGioBatDau(slotStart);
            chiTietLich.setGioKetThuc(slotEnd);
            chiTietLich.setTrangThai(TrangThaiChiTietLich.TRONG.name());
            chiTietLich.setKhoaDen(null);
            chiTietLich.setMaPhieuDatLichHienTai(null);
            ChiTietLich createdChiTiet = chiTietLichRepository.insert(chiTietLich);

            result.add(
                    new WorkingScheduleSlotResponseDto(
                            createdChiTiet.getMaChiTiet(),
                            createdChiTiet.getGioBatDau(),
                            createdChiTiet.getGioKetThuc(),
                            createdChiTiet.getTrangThai(),
                            createdChiTiet.getKhoaDen(),
                            createdChiTiet.getMaPhieuDatLichHienTai()));

            slotStart = slotEnd;
        }
        return result;
    }

    private Integer resolveThoiLuongPhut(Map<Integer, Integer> cache, Integer maKhungGio) {
        if (maKhungGio == null) {
            return null;
        }
        if (cache.containsKey(maKhungGio)) {
            return cache.get(maKhungGio);
        }
        Integer thoiLuong =
                khungGioRepository
                        .selectById(maKhungGio)
                        .map(KhungGio::getThoiLuongPhut)
                        .orElse(null);
        cache.put(maKhungGio, thoiLuong);
        return thoiLuong;
    }

    private boolean shouldExposeSchedule(LichLamViec lichLamViec) {
        if (lichLamViec == null) {
            return false;
        }
        String trangThaiLich = normalizeOptional(lichLamViec.getTrangThaiLich());
        return !TrangThaiLichLamViec.DA_HUY.name().equals(trangThaiLich);
    }

    private String resolveEffectiveChiTietTrangThai(ChiTietLich chiTiet, LocalDateTime now) {
        String trangThai = normalizeOptional(chiTiet.getTrangThai());
        if (!TrangThaiChiTietLich.DANG_GIU.name().equals(trangThai)) {
            return trangThai;
        }

        LocalDateTime khoaDen = chiTiet.getKhoaDen();
        if (khoaDen == null || !khoaDen.isAfter(now)) {
            return TrangThaiChiTietLich.TRONG.name();
        }
        return TrangThaiChiTietLich.DANG_GIU.name();
    }

    private ScheduleKey normalizeKey(Integer thuTrongTuan, LocalDate ngayCuThe) {
        if (thuTrongTuan != null && ngayCuThe != null) {
            throw new IllegalArgumentException("Chi duoc chon thuTrongTuan hoac ngayCuThe");
        }
        if (thuTrongTuan == null && ngayCuThe == null) {
            throw new IllegalArgumentException("thuTrongTuan hoac ngayCuThe is required");
        }

        if (ngayCuThe != null) {
            return new ScheduleKey(null, ngayCuThe);
        }

        int normalizedThu = normalizeThuTrongTuanValue(thuTrongTuan);
        return new ScheduleKey(normalizedThu, null);
    }

    private int normalizeThuTrongTuanFromDate(LocalDate date) {
        int javaDayOfWeek = date.getDayOfWeek().getValue(); // 1..7 (Mon..Sun)
        return normalizeThuTrongTuanValue(javaDayOfWeek + 1); // 2..8
    }

    private int normalizeThuTrongTuanValue(Integer thuTrongTuan) {
        if (thuTrongTuan == null) {
            throw new IllegalArgumentException("thuTrongTuan is required");
        }
        int value = thuTrongTuan;
        if (value < THU_TRONG_TUAN_MIN || value > THU_TRONG_TUAN_MAX) {
            throw new IllegalArgumentException("thuTrongTuan khong hop le (2-8)");
        }
        return value;
    }

    private Integer convertVietnamThuToJava(Integer vietnamThu) {
        if (vietnamThu == null) {
            return null;
        }
        if (vietnamThu < THU_TRONG_TUAN_MIN || vietnamThu > THU_TRONG_TUAN_MAX) {
            return null;
        }
        return vietnamThu == 8 ? 7 : vietnamThu - 1;
    }

    private int computeMaxSlots(LocalTime gioBatDau, LocalTime gioKetThuc, int thoiLuongPhut) {
        long minutes = Duration.between(gioBatDau, gioKetThuc).toMinutes();
        if (minutes <= 0) {
            throw new IllegalArgumentException("gioKetThuc phai sau gioBatDau");
        }
        int maxSlots = (int) (minutes / Math.max(1, thoiLuongPhut));
        if (maxSlots <= 0) {
            throw new IllegalArgumentException("Khoang gio khong du de tao slot");
        }
        return maxSlots;
    }

    private int normalizeSoLuongToiDa(Integer soLuongToiDa, int maxSlots) {
        if (soLuongToiDa == null || soLuongToiDa <= 0) {
            throw new IllegalArgumentException("soLuongToiDa phai > 0");
        }
        if (soLuongToiDa > maxSlots) {
            throw new IllegalArgumentException("soLuongToiDa vuot qua so slot toi da theo gioBatDau/gioKetThuc");
        }
        return soLuongToiDa;
    }

    private String normalizeTrangThaiLich(String trangThaiLich) {
        String normalized = normalizeOptional(trangThaiLich);
        if (normalized == null) {
            return TrangThaiLichLamViec.SAP_DIEN_RA.name();
        }
        try {
            return TrangThaiLichLamViec.valueOf(normalized.toUpperCase()).name();
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("trangThaiLich khong hop le");
        }
    }

    private BacSi requireBacSi(Integer maBacSi) {
        return bacSiRepository
                .selectById(maBacSi)
                .orElseThrow(() -> new IllegalArgumentException("Bac si khong ton tai"));
    }

    private Integer normalizePositiveId(Integer id, String fieldName) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return id;
    }

    private LocalDate requireDate(LocalDate value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value;
    }

    private LocalTime requireTime(LocalTime value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value;
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }

    private record ScheduleKey(Integer thuTrongTuan, LocalDate ngayCuThe) {}

    private record NormalizedItem(
            ScheduleKey key,
            LocalTime gioBatDau,
            LocalTime gioKetThuc,
            Integer maKhungGio,
            int thoiLuongPhut,
            int soLuongToiDa,
            String trangThaiLich) {}
}
