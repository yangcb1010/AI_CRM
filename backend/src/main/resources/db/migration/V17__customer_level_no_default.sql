UPDATE crm_custom_field
SET default_value = NULL,
    update_time = NOW()
WHERE entity_type = 'customer'
  AND field_source = 'system'
  AND field_name = 'level';

ALTER TABLE crm_customer ALTER COLUMN level DROP DEFAULT;
