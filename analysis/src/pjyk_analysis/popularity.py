import pandas
import numpy

from pjyk_analysis.queries import get_platform_popularity, get_popularity_data


def get_popularity_describe() -> pandas.DataFrame:
    return pandas.concat([
        get_platform_popularity("Bangumi").describe().rename(columns={"raw_popularity": "Bangumi"}),
        get_platform_popularity("AniList").describe().rename(columns={"raw_popularity": "AniList"}),
        get_platform_popularity("MyAnimeList").describe().rename(columns={"raw_popularity": "MyAnimeList"}),
    ], axis=1)


def get_popularity_weights():
    df = get_popularity_data()

    # 算斯皮尔曼相关矩阵
    corr = df[['bangumi','anilist','myanimelist']].corr(method='spearman')

    inv_corr = numpy.linalg.inv(corr.values)
    weights = inv_corr.sum(axis=1)
    weights = weights / weights.sum() * 3

    return weights
