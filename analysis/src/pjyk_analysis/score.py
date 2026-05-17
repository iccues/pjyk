import pandas

from pjyk_analysis.queries import get_platform_score


def get_score_describe() -> pandas.DataFrame:
    return pandas.concat([
        get_platform_score("Bangumi").describe().rename(columns={"raw_score": "Bangumi"}),
        get_platform_score("AniList").describe().rename(columns={"raw_score": "AniList"}),
        get_platform_score("MyAnimeList").describe().rename(columns={"raw_score": "MyAnimeList"}),
    ], axis=1)
