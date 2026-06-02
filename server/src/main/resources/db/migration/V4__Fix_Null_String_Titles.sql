-- Fix 'null' strings in anime table
UPDATE anime SET title_native = NULL WHERE title_native = 'null';
UPDATE anime SET title_romaji = NULL WHERE title_romaji = 'null';
UPDATE anime SET title_en = NULL WHERE title_en = 'null';
UPDATE anime SET title_cn = NULL WHERE title_cn = 'null';

-- Fix 'null' strings in mapping table
UPDATE mapping SET title_native = NULL WHERE title_native = 'null';
UPDATE mapping SET title_romaji = NULL WHERE title_romaji = 'null';
UPDATE mapping SET title_en = NULL WHERE title_en = 'null';
UPDATE mapping SET title_cn = NULL WHERE title_cn = 'null';
