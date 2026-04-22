package doctor.Data;

import doctor.Models.Entities.BacSi;
import doctor.Models.Entities.ChiTietLich;
import doctor.Models.Entities.CuocHoiThoai;
import doctor.Models.Entities.DanhGiaBacSi;
import doctor.Models.Entities.DanhSachTheoDoi;
import doctor.Models.Entities.DanhSachTheoDoiId;
import doctor.Models.Entities.KhungGio;
import doctor.Models.Entities.LichLamViec;
import doctor.Models.Entities.NguoiDung;
import doctor.Models.Entities.PhieuDatLich;
import doctor.Models.Entities.TaiKhoan;
import doctor.Models.Entities.TaiLieuBacSi;
import doctor.Models.Entities.TinNhan;
import doctor.Repositories.Interfaces.BacSiRepository;
import doctor.Repositories.Interfaces.ChiTietLichRepository;
import doctor.Repositories.Interfaces.CuocHoiThoaiRepository;
import doctor.Repositories.Interfaces.DanhGiaBacSiRepository;
import doctor.Repositories.Interfaces.DanhSachTheoDoiRepository;
import doctor.Repositories.Interfaces.KhungGioRepository;
import doctor.Repositories.Interfaces.LichLamViecRepository;
import doctor.Repositories.Interfaces.NguoiDungRepository;
import doctor.Repositories.Interfaces.PhieuDatLichRepository;
import doctor.Repositories.Interfaces.TaiKhoanRepository;
import doctor.Repositories.Interfaces.TaiLieuBacSiRepository;
import doctor.Repositories.Interfaces.TinNhanRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DataContext {
    private final TaiKhoanRepository taiKhoanRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final BacSiRepository bacSiRepository;
    private final CuocHoiThoaiRepository cuocHoiThoaiRepository;
    private final TinNhanRepository tinNhanRepository;
    private final TaiLieuBacSiRepository taiLieuBacSiRepository;
    private final DanhSachTheoDoiRepository danhSachTheoDoiRepository;
    private final DanhGiaBacSiRepository danhGiaBacSiRepository;
    private final KhungGioRepository khungGioRepository;
    private final LichLamViecRepository lichLamViecRepository;
    private final ChiTietLichRepository chiTietLichRepository;
    private final PhieuDatLichRepository phieuDatLichRepository;

    // -------------------------
    // CRUD: TAI_KHOAN
    // -------------------------
    @Transactional
    public TaiKhoan insertTaiKhoan(TaiKhoan entity) {
        return taiKhoanRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public Optional<TaiKhoan> selectTaiKhoanById(Integer maTaiKhoan) {
        return taiKhoanRepository.findById(maTaiKhoan);
    }

    @Transactional(readOnly = true)
    public List<TaiKhoan> selectAllTaiKhoan() {
        return taiKhoanRepository.findAll();
    }

    @Transactional
    public TaiKhoan updateTaiKhoan(TaiKhoan entity) {
        return taiKhoanRepository.save(entity);
    }

    @Transactional
    public void deleteTaiKhoan(Integer maTaiKhoan) {
        taiKhoanRepository.deleteById(maTaiKhoan);
    }

    // -------------------------
    // CRUD: NGUOI_DUNG
    // -------------------------
    @Transactional
    public NguoiDung insertNguoiDung(NguoiDung entity) {
        return nguoiDungRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public Optional<NguoiDung> selectNguoiDungById(Integer maNguoiDung) {
        return nguoiDungRepository.findById(maNguoiDung);
    }

    @Transactional(readOnly = true)
    public List<NguoiDung> selectAllNguoiDung() {
        return nguoiDungRepository.findAll();
    }

    @Transactional
    public NguoiDung updateNguoiDung(NguoiDung entity) {
        return nguoiDungRepository.save(entity);
    }

    @Transactional
    public void deleteNguoiDung(Integer maNguoiDung) {
        nguoiDungRepository.deleteById(maNguoiDung);
    }

    // -------------------------
    // CRUD: BAC_SI
    // -------------------------
    @Transactional
    public BacSi insertBacSi(BacSi entity) {
        return bacSiRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public Optional<BacSi> selectBacSiById(Integer maBacSi) {
        return bacSiRepository.findById(maBacSi);
    }

    @Transactional(readOnly = true)
    public List<BacSi> selectAllBacSi() {
        return bacSiRepository.findAll();
    }

    @Transactional
    public BacSi updateBacSi(BacSi entity) {
        return bacSiRepository.save(entity);
    }

    @Transactional
    public void deleteBacSi(Integer maBacSi) {
        bacSiRepository.deleteById(maBacSi);
    }

    // -------------------------
    // CRUD: CUOC_HOI_THOAI
    // -------------------------
    @Transactional
    public CuocHoiThoai insertCuocHoiThoai(CuocHoiThoai entity) {
        return cuocHoiThoaiRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public Optional<CuocHoiThoai> selectCuocHoiThoaiById(Integer maCuocHoiThoai) {
        return cuocHoiThoaiRepository.findById(maCuocHoiThoai);
    }

    @Transactional(readOnly = true)
    public List<CuocHoiThoai> selectAllCuocHoiThoai() {
        return cuocHoiThoaiRepository.findAll();
    }

    @Transactional
    public CuocHoiThoai updateCuocHoiThoai(CuocHoiThoai entity) {
        return cuocHoiThoaiRepository.save(entity);
    }

    @Transactional
    public void deleteCuocHoiThoai(Integer maCuocHoiThoai) {
        cuocHoiThoaiRepository.deleteById(maCuocHoiThoai);
    }

    // -------------------------
    // CRUD: TIN_NHAN
    // -------------------------
    @Transactional
    public TinNhan insertTinNhan(TinNhan entity) {
        return tinNhanRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public Optional<TinNhan> selectTinNhanById(Integer maTinNhan) {
        return tinNhanRepository.findById(maTinNhan);
    }

    @Transactional(readOnly = true)
    public List<TinNhan> selectAllTinNhan() {
        return tinNhanRepository.findAll();
    }

    @Transactional
    public TinNhan updateTinNhan(TinNhan entity) {
        return tinNhanRepository.save(entity);
    }

    @Transactional
    public void deleteTinNhan(Integer maTinNhan) {
        tinNhanRepository.deleteById(maTinNhan);
    }

    // -------------------------
    // CRUD: TAI_LIEU_BAC_SI
    // -------------------------
    @Transactional
    public TaiLieuBacSi insertTaiLieuBacSi(TaiLieuBacSi entity) {
        return taiLieuBacSiRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public Optional<TaiLieuBacSi> selectTaiLieuBacSiById(Integer maTaiLieu) {
        return taiLieuBacSiRepository.findById(maTaiLieu);
    }

    @Transactional(readOnly = true)
    public List<TaiLieuBacSi> selectAllTaiLieuBacSi() {
        return taiLieuBacSiRepository.findAll();
    }

    @Transactional
    public TaiLieuBacSi updateTaiLieuBacSi(TaiLieuBacSi entity) {
        return taiLieuBacSiRepository.save(entity);
    }

    @Transactional
    public void deleteTaiLieuBacSi(Integer maTaiLieu) {
        taiLieuBacSiRepository.deleteById(maTaiLieu);
    }

    // -------------------------
    // CRUD: DANH_SACH_THEO_DOI (PK kép)
    // -------------------------
    @Transactional
    public DanhSachTheoDoi insertDanhSachTheoDoi(DanhSachTheoDoi entity) {
        return danhSachTheoDoiRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public Optional<DanhSachTheoDoi> selectDanhSachTheoDoi(Integer maNguoiDung, Integer maBacSi) {
        return danhSachTheoDoiRepository.findById(new DanhSachTheoDoiId(maNguoiDung, maBacSi));
    }

    @Transactional(readOnly = true)
    public List<DanhSachTheoDoi> selectAllDanhSachTheoDoi() {
        return danhSachTheoDoiRepository.findAll();
    }

    @Transactional
    public DanhSachTheoDoi updateDanhSachTheoDoi(DanhSachTheoDoi entity) {
        return danhSachTheoDoiRepository.save(entity);
    }

    @Transactional
    public void deleteDanhSachTheoDoi(Integer maNguoiDung, Integer maBacSi) {
        danhSachTheoDoiRepository.deleteById(new DanhSachTheoDoiId(maNguoiDung, maBacSi));
    }

    // -------------------------
    // CRUD: DANH_GIA_BAC_SI
    // -------------------------
    @Transactional
    public DanhGiaBacSi insertDanhGiaBacSi(DanhGiaBacSi entity) {
        return danhGiaBacSiRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public Optional<DanhGiaBacSi> selectDanhGiaBacSiById(Integer maDanhGia) {
        return danhGiaBacSiRepository.findById(maDanhGia);
    }

    @Transactional(readOnly = true)
    public List<DanhGiaBacSi> selectAllDanhGiaBacSi() {
        return danhGiaBacSiRepository.findAll();
    }

    @Transactional
    public DanhGiaBacSi updateDanhGiaBacSi(DanhGiaBacSi entity) {
        return danhGiaBacSiRepository.save(entity);
    }

    @Transactional
    public void deleteDanhGiaBacSi(Integer maDanhGia) {
        danhGiaBacSiRepository.deleteById(maDanhGia);
    }

    // -------------------------
    // CRUD: KHUNG_GIO
    // -------------------------
    @Transactional
    public KhungGio insertKhungGio(KhungGio entity) {
        return khungGioRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public Optional<KhungGio> selectKhungGioById(Integer maKhungGio) {
        return khungGioRepository.findById(maKhungGio);
    }

    @Transactional(readOnly = true)
    public List<KhungGio> selectAllKhungGio() {
        return khungGioRepository.findAll();
    }

    @Transactional
    public KhungGio updateKhungGio(KhungGio entity) {
        return khungGioRepository.save(entity);
    }

    @Transactional
    public void deleteKhungGio(Integer maKhungGio) {
        khungGioRepository.deleteById(maKhungGio);
    }

    // -------------------------
    // CRUD: LICH_LAM_VIEC
    // -------------------------
    @Transactional
    public LichLamViec insertLichLamViec(LichLamViec entity) {
        return lichLamViecRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public Optional<LichLamViec> selectLichLamViecById(Integer maLichLamViec) {
        return lichLamViecRepository.findById(maLichLamViec);
    }

    @Transactional(readOnly = true)
    public List<LichLamViec> selectAllLichLamViec() {
        return lichLamViecRepository.findAll();
    }

    @Transactional
    public LichLamViec updateLichLamViec(LichLamViec entity) {
        return lichLamViecRepository.save(entity);
    }

    @Transactional
    public void deleteLichLamViec(Integer maLichLamViec) {
        lichLamViecRepository.deleteById(maLichLamViec);
    }

    // -------------------------
    // CRUD: CHI_TIET_LICH
    // -------------------------
    @Transactional
    public ChiTietLich insertChiTietLich(ChiTietLich entity) {
        return chiTietLichRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public Optional<ChiTietLich> selectChiTietLichById(Integer maChiTiet) {
        return chiTietLichRepository.findById(maChiTiet);
    }

    @Transactional(readOnly = true)
    public List<ChiTietLich> selectAllChiTietLich() {
        return chiTietLichRepository.findAll();
    }

    @Transactional
    public ChiTietLich updateChiTietLich(ChiTietLich entity) {
        return chiTietLichRepository.save(entity);
    }

    @Transactional
    public void deleteChiTietLich(Integer maChiTiet) {
        chiTietLichRepository.deleteById(maChiTiet);
    }

    // -------------------------
    // CRUD: PHIEU_DAT_LICH
    // -------------------------
    @Transactional
    public PhieuDatLich insertPhieuDatLich(PhieuDatLich entity) {
        return phieuDatLichRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public Optional<PhieuDatLich> selectPhieuDatLichById(Integer maPhieuDatLich) {
        return phieuDatLichRepository.findById(maPhieuDatLich);
    }

    @Transactional(readOnly = true)
    public List<PhieuDatLich> selectAllPhieuDatLich() {
        return phieuDatLichRepository.findAll();
    }

    @Transactional
    public PhieuDatLich updatePhieuDatLich(PhieuDatLich entity) {
        return phieuDatLichRepository.save(entity);
    }

    @Transactional
    public void deletePhieuDatLich(Integer maPhieuDatLich) {
        phieuDatLichRepository.deleteById(maPhieuDatLich);
    }

    // -------------------------
    // Actions: đặt lịch (gắn với trigger khóa slot trong DB)
    // -------------------------
    @Transactional
    public PhieuDatLich datLichChoXacNhan(PhieuDatLich phieu) {
        if (phieu.getTrangThaiPhieu() == null || phieu.getTrangThaiPhieu().isBlank()) {
            phieu.setTrangThaiPhieu("CHO_XAC_NHAN");
        }
        return phieuDatLichRepository.saveAndFlush(phieu);
    }

    @Transactional
    public PhieuDatLich xacNhanPhieuDatLich(Integer maPhieuDatLich) {
        PhieuDatLich phieu =
                phieuDatLichRepository
                        .findById(maPhieuDatLich)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Không tìm thấy PHIEU_DAT_LICH: " + maPhieuDatLich));
        phieu.setTrangThaiPhieu("DA_XAC_NHAN");
        return phieuDatLichRepository.saveAndFlush(phieu);
    }

    @Transactional
    public PhieuDatLich huyPhieuDatLich(Integer maPhieuDatLich, String lyDo) {
        PhieuDatLich phieu =
                phieuDatLichRepository
                        .findById(maPhieuDatLich)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Không tìm thấy PHIEU_DAT_LICH: " + maPhieuDatLich));
        phieu.setTrangThaiPhieu("DA_HUY");
        phieu.setLyDoTuChoi(lyDo);
        return phieuDatLichRepository.saveAndFlush(phieu);
    }
}

