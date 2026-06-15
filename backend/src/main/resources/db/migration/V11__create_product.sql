CREATE TABLE IF NOT EXISTS crm_product_category (
    category_id BIGINT NOT NULL,
    parent_id BIGINT NOT NULL DEFAULT 0,
    category_name VARCHAR(100) NOT NULL,
    category_path VARCHAR(500) NOT NULL,
    level SMALLINT NOT NULL DEFAULT 1,
    sort_order INTEGER NOT NULL DEFAULT 0,
    status SMALLINT NOT NULL DEFAULT 1,
    del_flag SMALLINT NOT NULL DEFAULT 0,
    create_user_id BIGINT,
    update_user_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (category_id)
);

CREATE INDEX IF NOT EXISTS idx_product_category_parent
    ON crm_product_category (parent_id, sort_order, category_id)
    WHERE del_flag = 0;

CREATE UNIQUE INDEX IF NOT EXISTS uk_product_category_parent_name
    ON crm_product_category (parent_id, category_name)
    WHERE del_flag = 0;

DROP TRIGGER IF EXISTS trg_product_category_update_time ON crm_product_category;
CREATE TRIGGER trg_product_category_update_time
    BEFORE UPDATE ON crm_product_category
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

CREATE TABLE IF NOT EXISTS crm_product (
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    product_code VARCHAR(100),
    main_image VARCHAR(500),
    category_id BIGINT,
    product_type VARCHAR(50) NOT NULL DEFAULT 'goods',
    unit VARCHAR(50),
    standard_price NUMERIC(18, 2),
    cost_price NUMERIC(18, 2),
    owner_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    description TEXT,
    del_flag SMALLINT NOT NULL DEFAULT 0,
    create_user_id BIGINT,
    update_user_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (product_id)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_product_code
    ON crm_product (product_code)
    WHERE del_flag = 0 AND product_code IS NOT NULL AND product_code <> '';

CREATE INDEX IF NOT EXISTS idx_product_owner
    ON crm_product (owner_id, status, update_time DESC)
    WHERE del_flag = 0;

CREATE INDEX IF NOT EXISTS idx_product_category
    ON crm_product (category_id)
    WHERE del_flag = 0;

CREATE INDEX IF NOT EXISTS idx_product_status
    ON crm_product (status)
    WHERE del_flag = 0;

DROP TRIGGER IF EXISTS trg_product_update_time ON crm_product;
CREATE TRIGGER trg_product_update_time
    BEFORE UPDATE ON crm_product
    FOR EACH ROW EXECUTE FUNCTION update_timestamp();

INSERT INTO crm_product_category (
    category_id,
    parent_id,
    category_name,
    category_path,
    level,
    sort_order,
    status,
    del_flag,
    create_user_id,
    update_user_id,
    create_time,
    update_time
) VALUES (
    1,
    0,
    '未分类',
    '未分类',
    1,
    0,
    1,
    0,
    1,
    1,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (category_id) DO NOTHING;

COMMENT ON TABLE crm_product IS 'Product catalog';
COMMENT ON TABLE crm_product_category IS 'Product categories';
COMMENT ON COLUMN crm_product.status IS 'active=enabled, inactive=disabled';
