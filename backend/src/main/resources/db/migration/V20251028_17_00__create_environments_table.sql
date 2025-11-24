CREATE TABLE environments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    api_key VARCHAR(255) NOT NULL UNIQUE,
    color VARCHAR(7),
    icon VARCHAR(50),
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_protected BOOLEAN DEFAULT false,
    is_active BOOLEAN DEFAULT true,
    last_used_at TIMESTAMP,
    usage_count BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255) NOT NULL
);

INSERT INTO environments (key, name, description, color, icon, sort_order, is_protected, api_key, created_by)
VALUES
    ('development', 'Development', 'Local development', '#3498db', 'code', 1, false,
     'sdk_dev_' || replace(gen_random_uuid()::text, '-', ''), 'system'),
    ('staging', 'Staging', 'Pre-production', '#f39c12', 'flask', 2, true,
     'sdk_stg_' || replace(gen_random_uuid()::text, '-', ''), 'system'),
    ('production', 'Production', 'Live environment', '#e74c3c', 'rocket', 3, true,
     'sdk_prod_' || replace(gen_random_uuid()::text, '-', ''), 'system');

CREATE INDEX idx_environments_api_key ON environments(api_key) WHERE is_active = true;
CREATE INDEX idx_environments_active ON environments(is_active, sort_order);
