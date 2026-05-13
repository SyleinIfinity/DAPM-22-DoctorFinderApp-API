-- PostgreSQL migration for appointment completion support in dapm-doctor
-- This migration keeps the historical booking records compatible while adding a completed state.

ALTER TABLE phieu_dat_lich
    ADD COLUMN IF NOT EXISTS trangthaiphieu VARCHAR(50) NOT NULL DEFAULT 'CHO_XAC_NHAN';

UPDATE phieu_dat_lich
SET trangthaiphieu = 'DA_KHAM'
WHERE trangthaiphieu = 'DA_XAC_NHAN_DA_KHAM';

-- Recommended enum semantics for the backend:
-- CHO_XAC_NHAN: waiting for doctor confirmation
-- DA_XAC_NHAN: confirmed but not yet examined
-- DA_KHAM: appointment completed, eligible for review
-- DA_HUY: cancelled by member
-- TU_CHOI: rejected by doctor
