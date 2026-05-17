package doctor.Services.Business.Admin;

import doctor.Models.DTOs.Admin.Responses.AdminDashboardEventDto;
import doctor.Models.DTOs.Admin.Responses.AdminDashboardOverviewDto;
import doctor.Models.DTOs.Admin.Responses.AdminDoctorProfileTrafficReportDto;
import doctor.Models.DTOs.Admin.Responses.AdminReportDoctorRankDto;
import doctor.Models.DTOs.Admin.Responses.AdminReportKeywordDto;
import doctor.Models.DTOs.Admin.Responses.AdminReportSliceDto;
import doctor.Services.Interfaces.Admin.AdminAnalyticsService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAnalyticsServiceImpl implements AdminAnalyticsService {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardOverviewDto getOverview(Integer onlineWindowMinutes) {
        int window = onlineWindowMinutes == null || onlineWindowMinutes < 1 ? 15 : Math.min(onlineWindowMinutes, 24 * 60);
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(window);

        long online =
                ((Number)
                                em.createNativeQuery(
                                                """
                                                SELECT COUNT(DISTINCT sub.ma) FROM (
                                                  SELECT mataikhoan AS ma FROM luot_xem_ho_so_bac_si
                                                   WHERE thoigian >= :cutoff AND mataikhoan IS NOT NULL
                                                  UNION
                                                  SELECT mataikhoan AS ma FROM lich_su_tim_kiem_bac_si
                                                   WHERE thoigian >= :cutoff AND mataikhoan IS NOT NULL
                                                ) sub
                                                """)
                                        .setParameter("cutoff", cutoff)
                                        .getSingleResult())
                        .longValue();

        long members =
                ((Number) em.createNativeQuery("SELECT COUNT(*) FROM nguoi_dung").getSingleResult())
                        .longValue();

        long doctors =
                ((Number)
                                em.createNativeQuery(
                                                "SELECT COUNT(*) FROM bac_si WHERE trangthaihoso = 'DA_DUYET'")
                                        .getSingleResult())
                        .longValue();

        return new AdminDashboardOverviewDto(online, members, doctors, window);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminDashboardEventDto> getRecentEvents(int hours, int limit) {
        int h = hours < 1 ? 24 : Math.min(hours, 24 * 14);
        int lim = limit < 1 ? 20 : Math.min(limit, 50);
        LocalDateTime since = LocalDateTime.now().minusHours(h);

        @SuppressWarnings("unchecked")
        List<Object[]> accRows =
                em.createNativeQuery(
                                """
                                SELECT t.ngaytao, t.tendangnhap, n.holot, n.ten
                                  FROM tai_khoan t
                                  LEFT JOIN nguoi_dung n ON n.mataikhoan = t.mataikhoan
                                 WHERE t.ngaytao >= :since
                                 ORDER BY t.ngaytao DESC
                                 LIMIT 50
                                """)
                        .setParameter("since", since)
                        .getResultList();

        @SuppressWarnings("unchecked")
        List<Object[]> docRows =
                em.createNativeQuery(
                                """
                                SELECT b.ngayduyethoso, n.holot, n.ten
                                  FROM bac_si b
                                  LEFT JOIN nguoi_dung n ON n.mataikhoan = b.mataikhoan
                                 WHERE b.ngayduyethoso IS NOT NULL AND b.ngayduyethoso >= :since
                                 ORDER BY b.ngayduyethoso DESC
                                 LIMIT 50
                                """)
                        .setParameter("since", since)
                        .getResultList();

        List<AdminDashboardEventDto> merged = new ArrayList<>();
        for (Object[] r : accRows) {
            LocalDateTime at = toLocalDateTime(r[0]);
            String tenDn = r[1] != null ? String.valueOf(r[1]) : "";
            String name = fullName(r[2], r[3], tenDn);
            merged.add(
                    new AdminDashboardEventDto(
                            "USER_JOINED",
                            name + " vừa tham gia vào hệ thống",
                            at));
        }
        for (Object[] r : docRows) {
            LocalDateTime at = toLocalDateTime(r[0]);
            String name = fullName(r[1], r[2], "Bác sĩ");
            merged.add(
                    new AdminDashboardEventDto(
                            "DOCTOR_APPROVED",
                            name + " đã được duyệt trở thành bác sĩ",
                            at));
        }

        merged.sort(Comparator.comparing(AdminDashboardEventDto::occurredAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return merged.stream().limit(lim).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDoctorProfileTrafficReportDto getProfileTraffic(LocalDateTime from, LocalDateTime to, int topN) {
        LocalDateTime[] range = normalizeRange(from, to);
        LocalDateTime f = range[0];
        LocalDateTime t = range[1];
        int n = topN < 1 ? 7 : Math.min(topN, 20);

        Object totalObj =
                em.createNativeQuery(
                                "SELECT COUNT(*) FROM luot_xem_ho_so_bac_si WHERE thoigian BETWEEN :from AND :to")
                        .setParameter("from", f)
                        .setParameter("to", t)
                        .getSingleResult();
        long total = totalObj == null ? 0L : ((Number) totalObj).longValue();
        if (total == 0L) {
            return new AdminDoctorProfileTrafficReportDto(List.of(), 0L);
        }

        Query q =
                em.createNativeQuery(
                        """
                        SELECT v.mabacsi, COUNT(*) AS cnt, n.holot, n.ten, b.chuyenkhoa, b.trangthaihoso
                          FROM luot_xem_ho_so_bac_si v
                          JOIN bac_si b ON b.mabacsi = v.mabacsi
                          LEFT JOIN nguoi_dung n ON n.mataikhoan = b.mataikhoan
                         WHERE v.thoigian BETWEEN :from AND :to
                         GROUP BY v.mabacsi, n.holot, n.ten, b.chuyenkhoa, b.trangthaihoso
                         ORDER BY cnt DESC
                         LIMIT :lim
                        """);
        q.setParameter("from", f);
        q.setParameter("to", t);
        q.setParameter("lim", n);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();

        long topSum = 0L;
        List<AdminReportSliceDto> slices = new ArrayList<>();
        for (Object[] r : rows) {
            long cnt = ((Number) r[1]).longValue();
            topSum += cnt;
            String specialty = r[4] != null ? String.valueOf(r[4]) : "Chưa rõ";
            String label = "BS " + fullName(r[2], r[3], "ID " + r[0]) + " • " + specialty;
            slices.add(new AdminReportSliceDto(label, cnt, 0.0));
        }
        long other = total - topSum;
        if (other > 0) {
            slices.add(new AdminReportSliceDto("Khác", other, 0.0));
        }
        for (int i = 0; i < slices.size(); i++) {
            AdminReportSliceDto s = slices.get(i);
            double pct = total > 0 ? (100.0 * s.value() / total) : 0.0;
            slices.set(i, new AdminReportSliceDto(s.label(), s.value(), Math.round(pct * 10.0) / 10.0));
        }
        return new AdminDoctorProfileTrafficReportDto(slices, total);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminReportDoctorRankDto> getTopDoctorsByViews(LocalDateTime from, LocalDateTime to, int limit) {
        LocalDateTime[] range = normalizeRange(from, to);
        int lim = limit < 1 ? 10 : Math.min(limit, 50);
        Query q =
                em.createNativeQuery(
                        """
                        SELECT v.mabacsi, COUNT(*) AS cnt, n.holot, n.ten, b.chuyenkhoa, b.trangthaihoso
                          FROM luot_xem_ho_so_bac_si v
                          JOIN bac_si b ON b.mabacsi = v.mabacsi
                          LEFT JOIN nguoi_dung n ON n.mataikhoan = b.mataikhoan
                         WHERE v.thoigian BETWEEN :from AND :to
                         GROUP BY v.mabacsi, n.holot, n.ten, b.chuyenkhoa, b.trangthaihoso
                         ORDER BY cnt DESC
                         LIMIT :lim
                        """);
        q.setParameter("from", range[0]);
        q.setParameter("to", range[1]);
        q.setParameter("lim", lim);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        List<AdminReportDoctorRankDto> out = new ArrayList<>();
        int rank = 1;
        for (Object[] r : rows) {
            Integer id = ((Number) r[0]).intValue();
            long cnt = ((Number) r[1]).longValue();
            String name = fullName(r[2], r[3], "Bác sĩ #" + id);
            String specialty = r[4] != null ? String.valueOf(r[4]) : "Chưa rõ";
            String status = r[5] != null ? String.valueOf(r[5]) : "UNKNOWN";
            out.add(new AdminReportDoctorRankDto(rank++, id, name, specialty, status, cnt));
        }
        return out;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminReportDoctorRankDto> getTopDoctorsByFollows(LocalDateTime from, LocalDateTime to, int limit) {
        LocalDateTime[] range = normalizeRange(from, to);
        int lim = limit < 1 ? 10 : Math.min(limit, 50);
        Query q =
                em.createNativeQuery(
                        """
                        SELECT d.mabacsi, COUNT(*) AS cnt, n.holot, n.ten, b.chuyenkhoa, b.trangthaihoso
                          FROM danh_sach_theo_doi d
                          JOIN bac_si b ON b.mabacsi = d.mabacsi
                          LEFT JOIN nguoi_dung n ON n.mataikhoan = b.mataikhoan
                         WHERE d.ngaytheodoi BETWEEN :from AND :to
                         GROUP BY d.mabacsi, n.holot, n.ten, b.chuyenkhoa, b.trangthaihoso
                         ORDER BY cnt DESC
                         LIMIT :lim
                        """);
        q.setParameter("from", range[0]);
        q.setParameter("to", range[1]);
        q.setParameter("lim", lim);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        List<AdminReportDoctorRankDto> out = new ArrayList<>();
        int rank = 1;
        for (Object[] r : rows) {
            Integer id = ((Number) r[0]).intValue();
            long cnt = ((Number) r[1]).longValue();
            String name = fullName(r[2], r[3], "Bác sĩ #" + id);
            String specialty = r[4] != null ? String.valueOf(r[4]) : "Chưa rõ";
            String status = r[5] != null ? String.valueOf(r[5]) : "UNKNOWN";
            out.add(new AdminReportDoctorRankDto(rank++, id, name, specialty, status, cnt));
        }
        return out;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminReportKeywordDto> getTopKeywords(LocalDateTime from, LocalDateTime to, int limit) {
        LocalDateTime[] range = normalizeRange(from, to);
        int lim = limit < 1 ? 10 : Math.min(limit, 50);
        Query q =
                em.createNativeQuery(
                        """
                        SELECT kw, COUNT(*) AS cnt FROM (
                          SELECT NULLIF(TRIM(tukhoa), '') AS kw
                            FROM lich_su_tim_kiem_bac_si
                           WHERE thoigian BETWEEN :from AND :to
                          UNION ALL
                          SELECT NULLIF(TRIM(chuyenkhoa), '') AS kw
                            FROM lich_su_tim_kiem_bac_si
                           WHERE thoigian BETWEEN :from AND :to
                        ) t
                        WHERE kw IS NOT NULL
                        GROUP BY kw
                        ORDER BY cnt DESC
                        LIMIT :lim
                        """);
        q.setParameter("from", range[0]);
        q.setParameter("to", range[1]);
        q.setParameter("lim", lim);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        List<AdminReportKeywordDto> out = new ArrayList<>();
        int rank = 1;
        for (Object[] r : rows) {
            String kw = r[0] != null ? String.valueOf(r[0]) : "";
            long cnt = ((Number) r[1]).longValue();
            out.add(new AdminReportKeywordDto(rank++, kw, cnt));
        }
        return out;
    }

    private static LocalDateTime[] normalizeRange(LocalDateTime from, LocalDateTime to) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime t = to == null ? now : to;
        LocalDateTime f = from == null ? t.minusDays(30) : from;
        if (f.isAfter(t)) {
            LocalDateTime tmp = f;
            f = t;
            t = tmp;
        }
        return new LocalDateTime[] {f, t};
    }

    private static LocalDateTime toLocalDateTime(Object o) {
        if (o instanceof LocalDateTime ldt) {
            return ldt;
        }
        if (o instanceof java.sql.Timestamp ts) {
            return ts.toLocalDateTime();
        }
        return LocalDateTime.now();
    }

    private static String fullName(Object hoLot, Object ten, String fallback) {
        String h = hoLot != null ? String.valueOf(hoLot).trim() : "";
        String t = ten != null ? String.valueOf(ten).trim() : "";
        String combined = (h + " " + t).trim();
        return combined.isEmpty() ? fallback : combined;
    }

    public List<AdminReportDoctorRankDto> getTopDoctorsByRank(LocalDateTime from, LocalDateTime to, int limit) {
        return getTopDoctorsByViews(from, to, limit);
    }
}
