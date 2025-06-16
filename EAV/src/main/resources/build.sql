CREATE TABLE IF NOT EXISTS eav_entity (
    id serial4 NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    CONSTRAINT eav_entity_pkey PRIMARY KEY (id),
    UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS eav_attribute (
    id serial4 NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    valueType VARCHAR(50) NOT NULL,
    dataType VARCHAR(50) NOT NULL,
    scope VARCHAR(50) NOT NULL,
    entityId INT NOT NULL,
    CONSTRAINT eav_attribute_pkey PRIMARY KEY (id),
    UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS eav_attribute_value (
    id serial8 NOT NULL,
    attribute_id BIGINT NOT NULL,
    value json NULL,
    CONSTRAINT eav_attribute_value_pkey PRIMARY KEY (id),
    UNIQUE (entity_id, attribute_id)
);

SELECT * FROM eav_entity;

CREATE OR REPLACE FUNCTION getAttributeValues(v refcursor, entityId INT)
RETURNS refcursor AS $$
LANGUAGE plpgsql
AS $function$
BEGIN
    OPEN v FOR
        SELECT a.id, a.name, a.valueType, a.dataType, a.scope, av.value
        FROM eav_attribute AS a
        JOIN eav_attribute_value AS av ON a.id = av.attribute_id
        WHERE av.entity_id = entityId;
    RETURN v;
END;
$function$;