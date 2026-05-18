UPDATE anime SET title_native = '' WHERE title_native IS NULL;

ALTER TABLE anime
    ALTER COLUMN review_status SET NOT NULL,
    ALTER COLUMN title_native SET DEFAULT '',
    ALTER COLUMN title_native SET NOT NULL;

UPDATE mapping SET title_native = '' WHERE title_native IS NULL;

ALTER TABLE mapping
    ALTER COLUMN source_platform SET NOT NULL,
    ALTER COLUMN platform_id SET NOT NULL,
    ALTER COLUMN title_native SET DEFAULT '',
    ALTER COLUMN title_native SET NOT NULL;
