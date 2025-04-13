CREATE TABLE rules
(
    id           BIGINT PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    type         VARCHAR(50)  NOT NULL,
    value        DECIMAL(19, 4) NOT NULL,
    formula      TEXT,
    "references" VARCHAR(100)
);

-- LIST GROUP IS OR
CREATE TABLE condition_groups
(
    id          BIGINT PRIMARY KEY,
    rule_id     BIGINT NOT NULL,
    group_order INT    NOT NULL, -- For ordering groups
    FOREIGN KEY (rule_id) REFERENCES rules (id)
);


-- EACH CONDITION IS AND
CREATE TABLE conditions
(
    id                 BIGINT PRIMARY KEY,
    condition_group_id BIGINT       NOT NULL,
    name               VARCHAR(255) NOT NULL,
    operator           VARCHAR(50)  NOT NULL,
    value_type         VARCHAR(50)  NOT NULL,
    value_text         TEXT,                  -- Store all values as JSON
    condition_order    INT          NOT NULL, -- For ordering conditions
    FOREIGN KEY (condition_group_id) REFERENCES condition_groups (id)
);