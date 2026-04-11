-- Phase 3: resume file metadata, extracted text, deterministic ATS-style match score.

ALTER TABLE applications
    ADD COLUMN resume_storage_key VARCHAR(512),
    ADD COLUMN resume_original_filename VARCHAR(255),
    ADD COLUMN resume_mime_type VARCHAR(128),
    ADD COLUMN resume_uploaded_at TIMESTAMPTZ,
    ADD COLUMN resume_parsed_text TEXT,
    ADD COLUMN match_score INTEGER,
    ADD COLUMN match_reasons TEXT;

COMMENT ON COLUMN applications.resume_storage_key IS 'Opaque key/path for stored file (local path segment or future object key)';
COMMENT ON COLUMN applications.match_reasons IS 'JSON array of short explanation strings for recruiters';
