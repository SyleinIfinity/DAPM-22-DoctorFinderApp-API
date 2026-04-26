package doctor.Services.Business.Messaging;

import doctor.Models.DTOs.Messaging.Requests.CreateConversationRequestDto;
import doctor.Models.DTOs.Messaging.Requests.SendMessageRequestDto;
import doctor.Models.DTOs.Messaging.Responses.ConversationSummaryResponseDto;
import doctor.Models.DTOs.Messaging.Responses.MessageResponseDto;
import doctor.Models.Entities.BacSi;
import doctor.Models.Entities.CuocHoiThoai;
import doctor.Models.Entities.NguoiDung;
import doctor.Models.Entities.TaiKhoan;
import doctor.Models.Entities.TinNhan;
import doctor.Repositories.Interfaces.BacSiRepository;
import doctor.Repositories.Interfaces.CuocHoiThoaiRepository;
import doctor.Repositories.Interfaces.NguoiDungRepository;
import doctor.Repositories.Interfaces.TaiKhoanRepository;
import doctor.Repositories.Interfaces.TinNhanRepository;
import doctor.Services.Interfaces.Messaging.MessagingService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessagingServiceImpl implements MessagingService {
    private static final int DEFAULT_LIMIT = 50;
    private static final String DEFAULT_CONTENT_TYPE = "TEXT";

    private final CuocHoiThoaiRepository cuocHoiThoaiRepository;
    private final TinNhanRepository tinNhanRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final BacSiRepository bacSiRepository;
    private final TaiKhoanRepository taiKhoanRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ConversationSummaryResponseDto> getConversations(Integer maNguoiDung, Integer maBacSi) {
        Integer normalizedMaNguoiDung = normalizeNullableId(maNguoiDung);
        Integer normalizedMaBacSi = normalizeNullableId(maBacSi);

        if ((normalizedMaNguoiDung == null && normalizedMaBacSi == null)
                || (normalizedMaNguoiDung != null && normalizedMaBacSi != null)) {
            throw new IllegalArgumentException("Chi duoc truyen maNguoiDung hoac maBacSi");
        }

        List<CuocHoiThoai> conversations;
        if (normalizedMaNguoiDung != null) {
            requireNguoiDung(normalizedMaNguoiDung);
            conversations = cuocHoiThoaiRepository.findByMaNguoiDung(normalizedMaNguoiDung);
        } else {
            requireBacSi(normalizedMaBacSi);
            conversations = cuocHoiThoaiRepository.findByMaBacSi(normalizedMaBacSi);
        }

        List<ConversationSummaryResponseDto> results = new ArrayList<>();
        for (CuocHoiThoai conversation : conversations) {
            results.add(mapToConversationSummary(conversation));
        }

        results.sort(
                Comparator.comparing(
                                (ConversationSummaryResponseDto item) ->
                                        item.thoiGianGuiCuoi() != null ? item.thoiGianGuiCuoi() : item.ngayTao(),
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .reversed());
        return results;
    }

    @Override
    @Transactional
    public ConversationSummaryResponseDto createOrGetConversation(CreateConversationRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }

        Integer maNguoiDung = normalizePositiveId(request.maNguoiDung(), "maNguoiDung");
        Integer maBacSi = normalizePositiveId(request.maBacSi(), "maBacSi");
        requireNguoiDung(maNguoiDung);
        requireBacSi(maBacSi);

        CuocHoiThoai existing =
                cuocHoiThoaiRepository.findByMaNguoiDungAndMaBacSi(maNguoiDung, maBacSi).orElse(null);
        if (existing != null) {
            return mapToConversationSummary(existing);
        }

        CuocHoiThoai created = new CuocHoiThoai();
        created.setMaNguoiDung(maNguoiDung);
        created.setMaBacSi(maBacSi);
        created.setNgayTao(LocalDateTime.now());
        created = cuocHoiThoaiRepository.insert(created);

        return mapToConversationSummary(created);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponseDto> getMessages(Integer maCuocHoiThoai, Integer limit, LocalDateTime before) {
        Integer normalizedMaCuocHoiThoai = normalizePositiveId(maCuocHoiThoai, "maCuocHoiThoai");
        int normalizedLimit = normalizeLimit(limit);
        requireConversation(normalizedMaCuocHoiThoai);

        List<TinNhan> messages;
        if (before == null) {
            messages = tinNhanRepository.findLatestByMaCuocHoiThoai(normalizedMaCuocHoiThoai, normalizedLimit);
        } else {
            messages =
                    tinNhanRepository.findByMaCuocHoiThoaiBefore(
                            normalizedMaCuocHoiThoai, before, normalizedLimit);
        }

        return messages.stream().map(this::mapToMessageResponse).toList();
    }

    @Override
    @Transactional
    public MessageResponseDto sendMessage(Integer maCuocHoiThoai, SendMessageRequestDto request) {
        Integer normalizedMaCuocHoiThoai = normalizePositiveId(maCuocHoiThoai, "maCuocHoiThoai");
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }

        Integer maTaiKhoanGui = normalizePositiveId(request.maTaiKhoanGui(), "maTaiKhoanGui");
        String noiDung = requireNotBlank(request.noiDungTinNhan(), "noiDungTinNhan");
        String loaiNoiDung = normalizeLoaiNoiDung(request.loaiNoiDung());

        CuocHoiThoai conversation = requireConversation(normalizedMaCuocHoiThoai);
        assertSenderBelongsToConversation(conversation, maTaiKhoanGui);

        taiKhoanRepository
                .selectById(maTaiKhoanGui)
                .orElseThrow(() -> new IllegalArgumentException("Tai khoan gui khong ton tai"));

        TinNhan tinNhan = new TinNhan();
        tinNhan.setMaCuocHoiThoai(normalizedMaCuocHoiThoai);
        tinNhan.setMaTaiKhoanGui(maTaiKhoanGui);
        tinNhan.setLoaiNoiDung(loaiNoiDung);
        tinNhan.setNoiDungTinNhan(noiDung);
        tinNhan.setThoiGianGui(LocalDateTime.now());

        TinNhan created = tinNhanRepository.insert(tinNhan);
        return mapToMessageResponse(created);
    }

    private ConversationSummaryResponseDto mapToConversationSummary(CuocHoiThoai conversation) {
        if (conversation == null || conversation.getMaCuocHoiThoai() == null) {
            throw new IllegalArgumentException("conversation is required");
        }

        NguoiDung benhNhan =
                nguoiDungRepository
                        .selectById(conversation.getMaNguoiDung())
                        .orElseThrow(() -> new IllegalStateException("Khong tim thay nguoi dung"));

        BacSi bacSi =
                bacSiRepository
                        .selectById(conversation.getMaBacSi())
                        .orElseThrow(() -> new IllegalStateException("Khong tim thay bac si"));

        NguoiDung thongTinBacSi = nguoiDungRepository.findByMaTaiKhoan(bacSi.getMaTaiKhoan()).orElse(null);

        String hoTenBenhNhan = buildHoTenDayDu(benhNhan.getHoLot(), benhNhan.getTen());
        String hoTenBacSi =
                thongTinBacSi == null
                        ? null
                        : buildHoTenDayDu(thongTinBacSi.getHoLot(), thongTinBacSi.getTen());

        TinNhan last = tinNhanRepository.findLastByMaCuocHoiThoai(conversation.getMaCuocHoiThoai()).orElse(null);

        return new ConversationSummaryResponseDto(
                conversation.getMaCuocHoiThoai(),
                conversation.getMaNguoiDung(),
                hoTenBenhNhan,
                benhNhan.getAnhDaiDien(),
                conversation.getMaBacSi(),
                hoTenBacSi,
                thongTinBacSi == null ? null : thongTinBacSi.getAnhDaiDien(),
                bacSi.getChuyenKhoa(),
                bacSi.getTenCoSoYTe(),
                bacSi.getDiaChiLamViec(),
                conversation.getNgayTao(),
                last == null ? null : last.getMaTinNhan(),
                last == null ? null : last.getMaTaiKhoanGui(),
                last == null ? null : last.getLoaiNoiDung(),
                last == null ? null : last.getNoiDungTinNhan(),
                last == null ? null : last.getThoiGianGui());
    }

    private void assertSenderBelongsToConversation(CuocHoiThoai conversation, Integer maTaiKhoanGui) {
        if (conversation == null || maTaiKhoanGui == null) {
            throw new IllegalArgumentException("conversation va maTaiKhoanGui la bat buoc");
        }

        NguoiDung benhNhan =
                nguoiDungRepository
                        .selectById(conversation.getMaNguoiDung())
                        .orElseThrow(() -> new IllegalStateException("Khong tim thay nguoi dung"));

        BacSi bacSi =
                bacSiRepository
                        .selectById(conversation.getMaBacSi())
                        .orElseThrow(() -> new IllegalStateException("Khong tim thay bac si"));

        Integer maTaiKhoanBenhNhan = benhNhan.getMaTaiKhoan();
        Integer maTaiKhoanBacSi = bacSi.getMaTaiKhoan();
        if (maTaiKhoanGui.equals(maTaiKhoanBenhNhan) || maTaiKhoanGui.equals(maTaiKhoanBacSi)) {
            return;
        }

        throw new IllegalArgumentException("Tai khoan gui khong thuoc cuoc hoi thoai");
    }

    private MessageResponseDto mapToMessageResponse(TinNhan tinNhan) {
        if (tinNhan == null) {
            throw new IllegalArgumentException("tinNhan is required");
        }

        return new MessageResponseDto(
                tinNhan.getMaTinNhan(),
                tinNhan.getMaCuocHoiThoai(),
                tinNhan.getMaTaiKhoanGui(),
                tinNhan.getLoaiNoiDung(),
                tinNhan.getNoiDungTinNhan(),
                tinNhan.getThoiGianGui());
    }

    private CuocHoiThoai requireConversation(Integer maCuocHoiThoai) {
        return cuocHoiThoaiRepository
                .selectById(maCuocHoiThoai)
                .orElseThrow(() -> new IllegalArgumentException("Cuoc hoi thoai khong ton tai"));
    }

    private NguoiDung requireNguoiDung(Integer maNguoiDung) {
        return nguoiDungRepository
                .selectById(maNguoiDung)
                .orElseThrow(() -> new IllegalArgumentException("Nguoi dung khong ton tai"));
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

    private Integer normalizeNullableId(Integer id) {
        if (id == null) {
            return null;
        }
        if (id <= 0) {
            throw new IllegalArgumentException("id khong hop le");
        }
        return id;
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        int value = limit;
        if (value <= 0) {
            throw new IllegalArgumentException("limit phai > 0");
        }
        return Math.min(value, 200);
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

    private String normalizeLoaiNoiDung(String loaiNoiDung) {
        String normalized = normalizeOptional(loaiNoiDung);
        if (normalized == null) {
            return DEFAULT_CONTENT_TYPE;
        }
        String upper = normalized.toUpperCase();
        if (!DEFAULT_CONTENT_TYPE.equals(upper)) {
            throw new IllegalArgumentException("loaiNoiDung chi ho tro TEXT o giai doan nay");
        }
        return upper;
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
}
