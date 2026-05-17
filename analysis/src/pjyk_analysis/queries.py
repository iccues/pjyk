import pandas
from pjyk_analysis.engine import engine


def get_platform_score(platform: str) -> pandas.DataFrame:
    """读取指定平台的原始评分。"""
    query = """
    SELECT m.raw_score
    FROM mapping m
    JOIN public.anime a ON a.anime_id = m.anime_id
    WHERE m.source_platform = %(platform)s
      AND a.review_status = 'APPROVED'
    """
    return pandas.read_sql(query, engine, params={"platform": platform})

def get_platform_popularity(platform: str) -> pandas.DataFrame:
    """读取指定平台的原始流行度。"""
    query = """
    SELECT m.raw_popularity
    FROM mapping m
    JOIN public.anime a ON a.anime_id = m.anime_id
    WHERE m.source_platform = %(platform)s
      AND a.review_status = 'APPROVED'
    """
    return pandas.read_sql(query, engine, params={"platform": platform})

def get_score_data() -> pandas.DataFrame:
    """读取每部动漫跨平台的评分数据。"""
    query = """
    SELECT
        a.anime_id,
        MAX(CASE WHEN m.source_platform = 'Bangumi' THEN m.raw_score END) as bangumi,
        MAX(CASE WHEN m.source_platform = 'AniList' THEN m.raw_score END) as anilist,
        MAX(CASE WHEN m.source_platform = 'MyAnimeList' THEN m.raw_score END) as myanimelist
    FROM anime a
    JOIN mapping m ON a.anime_id = m.anime_id
    WHERE a.review_status = 'APPROVED'
    GROUP BY a.anime_id
    """
    df = pandas.read_sql(query, engine)
    return df.dropna()

def get_popularity_data() -> pandas.DataFrame:
    """读取每部动漫跨平台的流行度数据。"""
    query = """
    SELECT
        a.anime_id,
        MAX(CASE WHEN m.source_platform = 'Bangumi' THEN m.raw_popularity END) as bangumi,
        MAX(CASE WHEN m.source_platform = 'AniList' THEN m.raw_popularity END) as anilist,
        MAX(CASE WHEN m.source_platform = 'MyAnimeList' THEN m.raw_popularity END) as myanimelist
    FROM anime a
    JOIN mapping m ON a.anime_id = m.anime_id
    WHERE a.review_status = 'APPROVED'
    GROUP BY a.anime_id
    """
    df = pandas.read_sql(query, engine)
    return df.dropna()
