CREATE TABLE IF NOT EXISTS crm_project_task_attachment (
    attachment_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    task_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    file_url TEXT,
    file_path TEXT,
    file_size BIGINT,
    mime_type VARCHAR(255),
    create_user_id BIGINT,
    create_user_name VARCHAR(100),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (attachment_id)
);

CREATE INDEX IF NOT EXISTS idx_crm_project_task_attachment_project
    ON crm_project_task_attachment(project_id, create_time DESC);

CREATE INDEX IF NOT EXISTS idx_crm_project_task_attachment_task
    ON crm_project_task_attachment(task_id, create_time DESC);

COMMENT ON TABLE crm_project_task_attachment IS 'Project task attachments';
COMMENT ON COLUMN crm_project_task_attachment.file_path IS 'Stored file path';
COMMENT ON COLUMN crm_project_task_attachment.file_size IS 'File size in bytes';
COMMENT ON COLUMN crm_project_task_attachment.mime_type IS 'File MIME type';
