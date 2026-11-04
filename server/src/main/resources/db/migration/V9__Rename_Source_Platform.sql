ALTER TABLE mapping
    RENAME COLUMN source_platform TO platform;

ALTER TABLE mapping
    RENAME CONSTRAINT uc_mapping_source_platform TO uc_mapping_platform;

ALTER TABLE mapping_metric_history
    RENAME COLUMN source_platform TO platform;
