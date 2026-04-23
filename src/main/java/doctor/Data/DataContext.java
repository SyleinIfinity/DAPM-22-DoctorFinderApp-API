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
        return taiKhoanRepository.insert(entity);
    }

    @Transactional(readOnly = true)
    public Optional<TaiKhoan> selectTaiKhoanById(Integer maTaiKhoan) {
        return taiKhoanRepository.selectById(maTaiKhoan);
    }

    @Transactional(readOnly = true)
    public List<TaiKhoan> selectAllTaiKhoan() {
        return taiKhoanRepository.selectAll();
    }

    @Transactional
    public TaiKhoan updateTaiKhoan(TaiKhoan entity) {
        return taiKhoanRepository.update(entity);
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
        return nguoiDungRepository.insert(entity);
    }

    @Transactional(readOnly = true)
    public Optional<NguoiDung> selectNguoiDungById(Integer maNguoiDung) {
        return nguoiDungRepository.selectById(maNguoiDung);
    }

    @Transactional(readOnly = true)
    public List<NguoiDung> selectAllNguoiDung() {
        return nguoiDungRepository.selectAll();
    }

    @Transactional
    public NguoiDung updateNguoiDung(NguoiDung entity) {
        return nguoiDungRepository.update(entity);
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
        return bacSiRepository.insert(entity);
    }

    @Transactional(readOnly = true)
    public Optional<BacSi> selectBacSiById(Integer maBacSi) {
        return bacSiRepository.selectById(maBacSi);
    }

    @Transactional(readOnly = true)
    public List<BacSi> selectAllBacSi() {
        return bacSiRepository.selectAll();
    }

    @Transactional
    public BacSi updateBacSi(BacSi entity) {
        return bacSiRepository.update(entity);
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
        return cuocHoiThoaiRepository.insert(entity);
    }

    @Transactional(readOnly = true)
    public Optional<CuocHoiThoai> selectCuocHoiThoaiById(Integer maCuocHoiThoai) {
        return cuocHoiThoaiRepository.selectById(maCuocHoiThoai);
    }

    @Transactional(readOnly = true)
    public List<CuocHoiThoai> selectAllCuocHoiThoai() {
        return cuocHoiThoaiRepository.selectAll();
    }

    @Transactional
    public CuocHoiThoai updateCuocHoiThoai(CuocHoiThoai entity) {
        return cuocHoiThoaiRepository.update(entity);
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
        return tinNhanRepository.insert(entity);
    }

    @Transactional(readOnly = true)
    public Optional<TinNhan> selectTinNhanById(Integer maTinNhan) {
        return tinNhanRepository.selectById(maTinNhan);
    }

    @Transactional(readOnly = true)
    public List<TinNhan> selectAllTinNhan() {
        return tinNhanRepository.selectAll();
    }

    @Transactional
    public TinNhan updateTinNhan(TinNhan entity) {
        return tinNhanRepository.update(entity);
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
        return taiLieuBacSiRepository.insert(entity);
    }

    @Transactional(readOnly = true)
    public Optional<TaiLieuBacSi> selectTaiLieuBacSiById(Integer maTaiLieu) {
        return taiLieuBacSiRepository.selectById(maTaiLieu);
    }

    @Transactional(readOnly = true)
    public List<TaiLieuBacSi> selectAllTaiLieuBacSi() {
        return taiLieuBacSiRepository.selectAll();
    }

    @Transactional
    public TaiLieuBacSi updateTaiLieuBacSi(TaiLieuBacSi entity) {
        return taiLieuBacSiRepository.update(entity);
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
        return danhSachTheoDoiRepository.insert(entity);
    }

    @Transactional(readOnly = true)
    public Optional<DanhSachTheoDoi> selectDanhSachTheoDoi(Integer maNguoiDung, Integer maBacSi) {
        return danhSachTheoDoiRepository.selectById(new DanhSachTheoDoiId(maNguoiDung, maBacSi));
    }

    @Transactional(readOnly = true)
    public List<DanhSachTheoDoi> selectAllDanhSachTheoDoi() {
        return danhSachTheoDoiRepository.selectAll();
    }

    @Transactional
    public DanhSachTheoDoi updateDanhSachTheoDoi(DanhSachTheoDoi entity) {
        return danhSachTheoDoiRepository.update(entity);
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
        return danhGiaBacSiRepository.insert(entity);
    }

    @Transactional(readOnly = true)
    public Optional<DanhGiaBacSi> selectDanhGiaBacSiById(Integer maDanhGia) {
        return danhGiaBacSiRepository.selectById(maDanhGia);
    }

    @Transactional(readOnly = true)
    public List<DanhGiaBacSi> selectAllDanhGiaBacSi() {
        return danhGiaBacSiRepository.selectAll();
    }

    @Transactional
    public DanhGiaBacSi updateDanhGiaBacSi(DanhGiaBacSi entity) {
        return danhGiaBacSiRepository.update(entity);
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
        return khungGioRepository.insert(entity);
    }

    @Transactional(readOnly = true)
    public Optional<KhungGio> selectKhungGioById(Integer maKhungGio) {
        return khungGioRepository.selectById(maKhungGio);
    }

    @Transactional(readOnly = true)
    public List<KhungGio> selectAllKhungGio() {
        return khungGioRepository.selectAll();
    }

    @Transactional
    public KhungGio updateKhungGio(KhungGio entity) {
        return khungGioRepository.update(entity);
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
        return lichLamViecRepository.insert(entity);
    }

    @Transactional(readOnly = true)
    public Optional<LichLamViec> selectLichLamViecById(Integer maLichLamViec) {
        return lichLamViecRepository.selectById(maLichLamViec);
    }

    @Transactional(readOnly = true)
    public List<LichLamViec> selectAllLichLamViec() {
        return lichLamViecRepository.selectAll();
    }

    @Transactional
    public LichLamViec updateLichLamViec(LichLamViec entity) {
        return lichLamViecRepository.update(entity);
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
        return chiTietLichRepository.insert(entity);
    }

    @Transactional(readOnly = true)
    public Optional<ChiTietLich> selectChiTietLichById(Integer maChiTiet) {
        return chiTietLichRepository.selectById(maChiTiet);
    }

    @Transactional(readOnly = true)
    public List<ChiTietLich> selectAllChiTietLich() {
        return chiTietLichRepository.selectAll();
    }

    @Transactional
    public ChiTietLich updateChiTietLich(ChiTietLich entity) {
        return chiTietLichRepository.update(entity);
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
        return phieuDatLichRepository.insert(entity);
    }

    @Transactional(readOnly = true)
    public Optional<PhieuDatLich> selectPhieuDatLichById(Integer maPhieuDatLich) {
        return phieuDatLichRepository.selectById(maPhieuDatLich);
    }

    @Transactional(readOnly = true)
    public List<PhieuDatLich> selectAllPhieuDatLich() {
        return phieuDatLichRepository.selectAll();
    }

    @Transactional
    public PhieuDatLich updatePhieuDatLich(PhieuDatLich entity) {
        return phieuDatLichRepository.update(entity);
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
        return phieuDatLichRepository.insert(phieu);
    }

    @Transactional
    public PhieuDatLich xacNhanPhieuDatLich(Integer maPhieuDatLich) {
        PhieuDatLich phieu =
                phieuDatLichRepository
                        .selectById(maPhieuDatLich)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Không tìm thấy PHIEU_DAT_LICH: " + maPhieuDatLich));
        phieu.setTrangThaiPhieu("DA_XAC_NHAN");
        return phieuDatLichRepository.update(phieu);
    }

    @Transactional
    public PhieuDatLich huyPhieuDatLich(Integer maPhieuDatLich, String lyDo) {
        PhieuDatLich phieu =
                phieuDatLichRepository
                        .selectById(maPhieuDatLich)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Không tìm thấy PHIEU_DAT_LICH: " + maPhieuDatLich));
        phieu.setTrangThaiPhieu("DA_HUY");
        phieu.setLyDoTuChoi(lyDo);
        return phieuDatLichRepository.update(phieu);
    }
}
