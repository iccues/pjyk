/* eslint-disable */
import * as types from './graphql';
import type { TypedDocumentNode as DocumentNode } from '@graphql-typed-document-node/core';

/**
 * Map of all GraphQL operations in the project.
 *
 * This map has several performance disadvantages:
 * 1. It is not tree-shakeable, so it will include all operations in the project.
 * 2. It is not minifiable, so the string of a GraphQL query will be multiple times inside the bundle.
 * 3. It does not support dead code elimination, so it will add unused operations.
 *
 * Therefore it is highly recommended to use the babel or swc plugin for production.
 * Learn more about it here: https://the-guild.dev/graphql/codegen/plugins/presets/preset-client#reducing-bundle-size
 */
type Documents = {
    "query GetAnimeListRow($year: Int, $season: Season, $pageNumber: Int, $pageSize: Int, $sortBy: SortBy) {\n  animeList(\n    year: $year\n    season: $season\n    pageNumber: $pageNumber\n    pageSize: $pageSize\n    sortBy: $sortBy\n  ) {\n    content {\n      animeId\n      ...AnimeCard\n    }\n  }\n}\n\nquery GetAnimeList($year: Int, $season: Season, $pageNumber: Int, $pageSize: Int, $sortBy: SortBy) {\n  animeList(\n    year: $year\n    season: $season\n    pageNumber: $pageNumber\n    pageSize: $pageSize\n    sortBy: $sortBy\n  ) {\n    ...AnimeList\n  }\n}\n\nquery GetAnimeListBySearch($query: String, $pageNumber: Int, $pageSize: Int) {\n  animeListBySearch(query: $query, pageNumber: $pageNumber, pageSize: $pageSize) {\n    ...AnimeList\n  }\n}\n\nfragment AnimeList on AnimePage {\n  content {\n    animeId\n    ...AnimeCard\n  }\n  pageInfo {\n    size\n    number\n    totalElements\n    totalPages\n  }\n}\n\nfragment AnimeCard on Anime {\n  title {\n    titleNative\n    titleCn\n  }\n  coverImage\n  averageScore\n  mappings {\n    mappingId\n    ...ScoreItem\n  }\n}\n\nfragment ScoreItem on Mapping {\n  sourcePlatform\n  platformId\n  rawScore\n}": typeof types.GetAnimeListRowDocument,
};
const documents: Documents = {
    "query GetAnimeListRow($year: Int, $season: Season, $pageNumber: Int, $pageSize: Int, $sortBy: SortBy) {\n  animeList(\n    year: $year\n    season: $season\n    pageNumber: $pageNumber\n    pageSize: $pageSize\n    sortBy: $sortBy\n  ) {\n    content {\n      animeId\n      ...AnimeCard\n    }\n  }\n}\n\nquery GetAnimeList($year: Int, $season: Season, $pageNumber: Int, $pageSize: Int, $sortBy: SortBy) {\n  animeList(\n    year: $year\n    season: $season\n    pageNumber: $pageNumber\n    pageSize: $pageSize\n    sortBy: $sortBy\n  ) {\n    ...AnimeList\n  }\n}\n\nquery GetAnimeListBySearch($query: String, $pageNumber: Int, $pageSize: Int) {\n  animeListBySearch(query: $query, pageNumber: $pageNumber, pageSize: $pageSize) {\n    ...AnimeList\n  }\n}\n\nfragment AnimeList on AnimePage {\n  content {\n    animeId\n    ...AnimeCard\n  }\n  pageInfo {\n    size\n    number\n    totalElements\n    totalPages\n  }\n}\n\nfragment AnimeCard on Anime {\n  title {\n    titleNative\n    titleCn\n  }\n  coverImage\n  averageScore\n  mappings {\n    mappingId\n    ...ScoreItem\n  }\n}\n\nfragment ScoreItem on Mapping {\n  sourcePlatform\n  platformId\n  rawScore\n}": types.GetAnimeListRowDocument,
};

/**
 * The graphql function is used to parse GraphQL queries into a document that can be used by GraphQL clients.
 *
 *
 * @example
 * ```ts
 * const query = graphql(`query GetUser($id: ID!) { user(id: $id) { name } }`);
 * ```
 *
 * The query argument is unknown!
 * Please regenerate the types.
 */
export function graphql(source: string): unknown;

/**
 * The graphql function is used to parse GraphQL queries into a document that can be used by GraphQL clients.
 */
export function graphql(source: "query GetAnimeListRow($year: Int, $season: Season, $pageNumber: Int, $pageSize: Int, $sortBy: SortBy) {\n  animeList(\n    year: $year\n    season: $season\n    pageNumber: $pageNumber\n    pageSize: $pageSize\n    sortBy: $sortBy\n  ) {\n    content {\n      animeId\n      ...AnimeCard\n    }\n  }\n}\n\nquery GetAnimeList($year: Int, $season: Season, $pageNumber: Int, $pageSize: Int, $sortBy: SortBy) {\n  animeList(\n    year: $year\n    season: $season\n    pageNumber: $pageNumber\n    pageSize: $pageSize\n    sortBy: $sortBy\n  ) {\n    ...AnimeList\n  }\n}\n\nquery GetAnimeListBySearch($query: String, $pageNumber: Int, $pageSize: Int) {\n  animeListBySearch(query: $query, pageNumber: $pageNumber, pageSize: $pageSize) {\n    ...AnimeList\n  }\n}\n\nfragment AnimeList on AnimePage {\n  content {\n    animeId\n    ...AnimeCard\n  }\n  pageInfo {\n    size\n    number\n    totalElements\n    totalPages\n  }\n}\n\nfragment AnimeCard on Anime {\n  title {\n    titleNative\n    titleCn\n  }\n  coverImage\n  averageScore\n  mappings {\n    mappingId\n    ...ScoreItem\n  }\n}\n\nfragment ScoreItem on Mapping {\n  sourcePlatform\n  platformId\n  rawScore\n}"): (typeof documents)["query GetAnimeListRow($year: Int, $season: Season, $pageNumber: Int, $pageSize: Int, $sortBy: SortBy) {\n  animeList(\n    year: $year\n    season: $season\n    pageNumber: $pageNumber\n    pageSize: $pageSize\n    sortBy: $sortBy\n  ) {\n    content {\n      animeId\n      ...AnimeCard\n    }\n  }\n}\n\nquery GetAnimeList($year: Int, $season: Season, $pageNumber: Int, $pageSize: Int, $sortBy: SortBy) {\n  animeList(\n    year: $year\n    season: $season\n    pageNumber: $pageNumber\n    pageSize: $pageSize\n    sortBy: $sortBy\n  ) {\n    ...AnimeList\n  }\n}\n\nquery GetAnimeListBySearch($query: String, $pageNumber: Int, $pageSize: Int) {\n  animeListBySearch(query: $query, pageNumber: $pageNumber, pageSize: $pageSize) {\n    ...AnimeList\n  }\n}\n\nfragment AnimeList on AnimePage {\n  content {\n    animeId\n    ...AnimeCard\n  }\n  pageInfo {\n    size\n    number\n    totalElements\n    totalPages\n  }\n}\n\nfragment AnimeCard on Anime {\n  title {\n    titleNative\n    titleCn\n  }\n  coverImage\n  averageScore\n  mappings {\n    mappingId\n    ...ScoreItem\n  }\n}\n\nfragment ScoreItem on Mapping {\n  sourcePlatform\n  platformId\n  rawScore\n}"];

export function graphql(source: string) {
  return (documents as any)[source] ?? {};
}

export type DocumentType<TDocumentNode extends DocumentNode<any, any>> = TDocumentNode extends DocumentNode<  infer TType,  any>  ? TType  : never;