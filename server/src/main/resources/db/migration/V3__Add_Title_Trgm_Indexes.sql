CREATE INDEX idx_anime_title_native_trgm ON anime USING GIN (title_native gin_trgm_ops);
CREATE INDEX idx_anime_title_romaji_trgm ON anime USING GIN (title_romaji gin_trgm_ops);
CREATE INDEX idx_anime_title_en_trgm     ON anime USING GIN (title_en     gin_trgm_ops);
CREATE INDEX idx_anime_title_cn_trgm     ON anime USING GIN (title_cn     gin_trgm_ops);
