ALTER TABLE crm_follow_up
    ADD COLUMN IF NOT EXISTS summary VARCHAR(500),
    ADD COLUMN IF NOT EXISTS scene_type VARCHAR(100),
    ADD COLUMN IF NOT EXISTS ai_generated SMALLINT DEFAULT 0;

CREATE TABLE IF NOT EXISTS crm_follow_up_attachment (
    attachment_id BIGINT PRIMARY KEY,
    follow_up_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    mime_type VARCHAR(255),
    sort INT DEFAULT 0,
    analysis_status VARCHAR(30) DEFAULT 'idle',
    analysis_content TEXT,
    analysis_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_follow_up_attachment_follow_up_id
    ON crm_follow_up_attachment (follow_up_id, sort, attachment_id);

DROP TRIGGER IF EXISTS trg_follow_up_attachment_update_time ON crm_follow_up_attachment;

CREATE TRIGGER trg_follow_up_attachment_update_time
    BEFORE UPDATE ON crm_follow_up_attachment
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();
