ALTER TABLE crm_chat_session
    ADD COLUMN IF NOT EXISTS employee_id BIGINT,
    ADD COLUMN IF NOT EXISTS relation_id BIGINT,
    ADD COLUMN IF NOT EXISTS product_id BIGINT,
    ADD COLUMN IF NOT EXISTS project_id BIGINT,
    ADD COLUMN IF NOT EXISTS project_task_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_chat_session_employee_id
    ON crm_chat_session (employee_id);

CREATE INDEX IF NOT EXISTS idx_chat_session_relation_id
    ON crm_chat_session (relation_id);

CREATE INDEX IF NOT EXISTS idx_chat_session_product_id
    ON crm_chat_session (product_id);

CREATE INDEX IF NOT EXISTS idx_chat_session_project_id
    ON crm_chat_session (project_id);

CREATE INDEX IF NOT EXISTS idx_chat_session_project_task_id
    ON crm_chat_session (project_task_id);

COMMENT ON COLUMN crm_chat_session.employee_id IS 'Employee object chat context';
COMMENT ON COLUMN crm_chat_session.relation_id IS 'Relation object chat context';
COMMENT ON COLUMN crm_chat_session.product_id IS 'Product object chat context';
COMMENT ON COLUMN crm_chat_session.project_id IS 'Project object chat context';
COMMENT ON COLUMN crm_chat_session.project_task_id IS 'Project task object chat context';
