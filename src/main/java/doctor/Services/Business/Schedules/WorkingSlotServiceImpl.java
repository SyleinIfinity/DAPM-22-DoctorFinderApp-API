package doctor.Services.Business.Schedules;

import doctor.Models.DTOs.Schedules.Requests.DoctorScheduleCalendarQueryDto;
import doctor.Models.DTOs.Schedules.Requests.WorkingSlotUpsertItemDto;
import doctor.Models.DTOs.Schedules.Responses.DoctorScheduleCalendarDayResponseDto;
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

        List<LichLamViec> schedulesToUse = loadSchedulesForDate(normalizedMaBacSi, normalizedDate);
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
    @Transactional(readOnly = true)
    public List<DoctorScheduleCalendarDayResponseDto> getCalendarDays(
            Integer maBacSi, DoctorScheduleCalendarQueryDto query) {
        Integer normalizedMaBacSi = normalizePositiveId(maBacSi, "maBacSi");
        requireBacSi(normalizedMaBacSi);
        if (query == null) {
            throw new IllegalArgumentException("query is required");
        }

        LocalDate fromDate = requireDate(query.fromDate(), "fromDate");
        LocalDate toDate = requireDate(query.toDate(), "toDate");
        if (toDate.isBefore(fromDate)) {
            throw new IllegalArgumentException("toDate phai sau hoac bang fromDate");
        }

        List<LichLamViec> allSchedules = lichLamViecRepository.findByMaBacSi(normalizedMaBacSi);
        List<DoctorScheduleCalendarDayResponseDto> result = new ArrayList<>();
        for (LocalDate current = fromDate; !current.isAfter(toDate); current = current.plusDays(1)) {
            result.add(buildCalendarDay(allSchedules, current));
        }
        return result;
    }

    @Override
    @Transactional
    public List<WorkingScheduleResponseDto> createWorkingSlots(
            Integer maBacSi, List<WorkingSlotUpsertItemDto> items) {
        return upsertWorkingSlotsInternal(maBacSi, items, ActionMode.CREATE);
    }

    @Override
    @Transactional
    public List<WorkingScheduleResponseDto> updateWorkingSlots(
            Integer maBacSi, List<WorkingSlotUpsertItemDto> items) {
        return upsertWorkingSlotsInternal(maBacSi, items, ActionMode.UPDATE);
    }

    @Override
    @Transactional
    public void deleteWorkingSlots(Integer maBacSi, List<WorkingSlotUpsertItemDto> items) {
        Integer normalizedMaBacSi = normalizePositiveId(maBacSi, "maBacSi");
        requireBacSi(normalizedMaBacSi);
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("items is required");
        }

        List<NormalizedItem> normalizedItems = normalizeItems(items);
        for (NormalizedItem item : normalizedItems) {
            cancelExistingSchedules(loadSchedulesByKey(normalizedMaBacSi, item.key()));
        }
    }

    private List<WorkingScheduleResponseDto> upsertWorkingSlotsInternal(
            Integer maBacSi, List<WorkingSlotUpsertItemDto> items, ActionMode mode) {
        Integer normalizedMaBacSi = normalizePositiveId(maBacSi, "maBacSi");
        requireBacSi(normalizedMaBacSi);
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("items is required");
        }

        List<NormalizedItem> normalizedItems = normalizeItems(items);
        List<WorkingScheduleResponseDto> results = new ArrayList<>();
        for (NormalizedItem item : normalizedItems) {
            List<LichLamViec> existingSchedules = loadSchedulesByKey(normalizedMaBacSi, item.key());
            boolean shouldAppend = mode == ActionMode.CREATE && !existingSchedules.isEmpty();
            boolean shouldReplace = mode == ActionMode.UPDATE && !existingSchedules.isEmpty();

            if (shouldReplace) {
                cancelExistingSchedules(existingSchedules);
            }

            LichLamViec targetSchedule;
            if (shouldAppend) {
                targetSchedule = pickAppendTarget(existingSchedules, item);
                extendScheduleWindow(targetSchedule, item);
                targetSchedule.setMaKhungGio(item.maKhungGio());
                targetSchedule.setSoLuongToiDa(Math.max(targetSchedule.getSoLuongToiDa(), item.soLuongToiDa()));
                targetSchedule.setTrangThaiLich(item.trangThaiLich());
                targetSchedule = lichLamViecRepository.update(targetSchedule);
            } else {
                targetSchedule = new LichLamViec();
                targetSchedule.setMaBacSi(normalizedMaBacSi);
                targetSchedule.setThuTrongTuan(item.key().thuTrongTuan());
                targetSchedule.setNgayCuThe(item.key().ngayCuThe());
                targetSchedule.setGioBatDau(item.gioBatDau());
                targetSchedule.setGioKetThuc(item.gioKetThuc());
                targetSchedule.setMaKhungGio(item.maKhungGio());
                targetSchedule.setSoLuongToiDa(item.soLuongToiDa());
                targetSchedule.setTrangThaiLich(item.trangThaiLich());
                targetSchedule = lichLamViecRepository.insert(targetSchedule);
            }

            List<WorkingScheduleSlotResponseDto> createdChiTiet = createChiTietSlots(targetSchedule, item);
            results.add(
                    new WorkingScheduleResponseDto(
                            targetSchedule.getMaLichLamViec(),
                            targetSchedule.getMaBacSi(),
                            targetSchedule.getThuTrongTuan(),
                            targetSchedule.getNgayCuThe(),
                            targetSchedule.getGioBatDau(),
                            targetSchedule.getGioKetThuc(),
                            targetSchedule.getMaKhungGio(),
                            item.thoiLuongPhut(),
                            targetSchedule.getSoLuongToiDa(),
                            targetSchedule.getTrangThaiLich(),
                            createdChiTiet));
        }

        results.sort(
                Comparator.comparing(
                                WorkingScheduleResponseDto::ngayCuThe,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(
                                WorkingScheduleResponseDto::thuTrongTuan,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(WorkingScheduleResponseDto::gioBatDau));
        return results;
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
            String actionScope = normalizeOptional(item.actionScope());
            normalized.add(
                    new NormalizedItem(
                            key,
                            gioBatDau,
                            gioKetThuc,
                            maKhungGio,
                            thoiLuongPhut,
                            soLuongToiDa,
                            trangThaiLich,
                            actionScope));
        }

        return normalized;
    }

    private DoctorScheduleCalendarDayResponseDto buildCalendarDay(List<LichLamViec> allSchedules, LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            return new DoctorScheduleCalendarDayResponseDto(date, "NGAY_DA_QUA", 0, 0, false);
        }

        List<LichLamViec> schedules = new ArrayList<>();
        for (LichLamViec lichLamViec : allSchedules) {
            if (lichLamViec == null) {
                continue;
            }
            if (!shouldExposeSchedule(lichLamViec)) {
                continue;
            }
            if (matchesDate(lichLamViec, date)) {
                schedules.add(lichLamViec);
            }
        }

        if (schedules.isEmpty()) {
            return new DoctorScheduleCalendarDayResponseDto(date, "CHUA_CO_LICH", 0, 0, false);
        }

        int totalSlots = 0;
        boolean hasBooked = false;
        for (LichLamViec lichLamViec : schedules) {
            List<ChiTietLich> chiTietList = chiTietLichRepository.findByMaLichLamViec(lichLamViec.getMaLichLamViec());
            totalSlots += chiTietList.size();
            for (ChiTietLich chiTiet : chiTietList) {
                if (chiTiet != null && chiTiet.getMaPhieuDatLichHienTai() != null) {
                    hasBooked = true;
                }
            }
        }

        String state = hasBooked ? "DA_CO_NGUOI_DAT" : "DA_TAO_LICH";
        return new DoctorScheduleCalendarDayResponseDto(date, state, schedules.size(), totalSlots, hasBooked);
    }

    private boolean matchesDate(LichLamViec lichLamViec, LocalDate date) {
        if (lichLamViec == null || date == null) {
            return false;
        }
        if (date.equals(lichLamViec.getNgayCuThe())) {
            return true;
        }
        Integer thuTrongTuan = lichLamViec.getThuTrongTuan();
        if (thuTrongTuan == null) {
            return false;
        }
        int dateThu = normalizeThuTrongTuanFromDate(date);
        return thuTrongTuan.equals(dateThu) || thuTrongTuan.equals(convertVietnamThuToJava(dateThu));
    }

    private void cancelExistingSchedules(List<LichLamViec> schedules) {
        if (schedules == null || schedules.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        for (LichLamViec existing : schedules) {
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

    private List<LichLamViec> loadSchedulesForDate(Integer maBacSi, LocalDate date) {
        List<LichLamViec> schedulesForDate =
                lichLamViecRepository.findByMaBacSiAndNgayCuThe(maBacSi, date).stream()
                        .filter(this::shouldExposeSchedule)
                        .toList();

        if (!schedulesForDate.isEmpty()) {
            return schedulesForDate;
        }

        int thuTheoNgay = normalizeThuTrongTuanFromDate(date);
        List<LichLamViec> weeklySchedules = new ArrayList<>(
                lichLamViecRepository.findByMaBacSiAndThuTrongTuan(maBacSi, thuTheoNgay));

        Integer javaThu = convertVietnamThuToJava(thuTheoNgay);
        if (javaThu != null) {
            weeklySchedules.addAll(
                    lichLamViecRepository.findByMaBacSiAndThuTrongTuan(maBacSi, javaThu));
        }

        return weeklySchedules.stream()
                .filter(this::shouldExposeSchedule)
                .sorted(Comparator.comparing(LichLamViec::getGioBatDau))
                .toList();
    }

    private LichLamViec pickAppendTarget(List<LichLamViec> existingSchedules, NormalizedItem item) {
        if (existingSchedules == null || existingSchedules.isEmpty() || item == null) {
            return null;
        }

        LichLamViec earliest = existingSchedules.get(0);
        if (earliest == null) {
            return null;
        }

        boolean sameDaySpecific = item.key().ngayCuThe() != null;
        if (!sameDaySpecific) {
            return earliest;
        }

        return earliest;
    }

    private void extendScheduleWindow(LichLamViec targetSchedule, NormalizedItem item) {
        if (targetSchedule == null || item == null) {
            return;
        }

        if (item.gioBatDau().isBefore(targetSchedule.getGioBatDau())) {
            targetSchedule.setGioBatDau(item.gioBatDau());
        }
        if (item.gioKetThuc().isAfter(targetSchedule.getGioKetThuc())) {
            targetSchedule.setGioKetThuc(item.gioKetThuc());
        }
    }

    private boolean shouldExposeSchedule(LichLamViec lichLamViec) {
        if (lichLamViec == null) {
            return false;
        }
        String trangThaiLich = normalizeOptional(lichLamViec.getTrangThaiLich());
        return !TrangThaiLichLamViec.DA_HUY.name().equals(trangThaiLich);
    }

    private boolean isOverlapping(LocalTime existingStart, LocalTime existingEnd, LocalTime newStart, LocalTime newEnd) {
        if (existingStart == null || existingEnd == null || newStart == null || newEnd == null) {
            return false;
        }
        return existingStart.isBefore(newEnd) && newStart.isBefore(existingEnd);
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
            String trangThaiLich,
            String actionScope) {}

    private enum ActionMode {
        CREATE,
        UPDATE
    }
}
