package doctor.Models.Enums;

public enum TrangThaiLichLamViec {
    SAP_DIEN_RA,
    DANG_DIEN_RA,
    DA_KET_THUC,
    TAM_DUNG_NHAN_LICH,
    DA_HUY;

    public boolean choPhepDatLich() {
        return this == SAP_DIEN_RA || this == DANG_DIEN_RA;
    }

    public boolean coTheCapNhatLich() {
        return this == SAP_DIEN_RA || this == TAM_DUNG_NHAN_LICH;
    }

    public boolean daDongVongDoi() {
        return this == DA_KET_THUC || this == DA_HUY;
    }
}
