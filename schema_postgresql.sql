-- PostgreSQL schema updates for dapm-doctor
-- Appointment booking status now distinguishes completed appointments.
-- Use this when creating or validating the `phieu_dat_lich` table.

ALTER TABLE phieu_dat_lich
    ADD COLUMN IF NOT EXISTS trangthaiphieu VARCHAR(50) NOT NULL DEFAULT 'CHO_XAC_NHAN';

-- Optional data normalization for existing records.
UPDATE phieu_dat_lich
SET trangthaiphieu = 'DA_KHAM'
WHERE trangthaiphieu = 'DA_XAC_NHAN_DA_KHAM';

-- Ensure the application-level statuses are valid:
-- CHO_XAC_NHAN, DA_XAC_NHAN, DA_KHAM, DA_HUY, TU_CHOI
