ALTER TABLE anime
    ALTER COLUMN title_native DROP NOT NULL,
    ALTER COLUMN title_native DROP DEFAULT;

ALTER TABLE mapping
    ALTER COLUMN title_native DROP NOT NULL,
    ALTER COLUMN title_native DROP DEFAULT;
