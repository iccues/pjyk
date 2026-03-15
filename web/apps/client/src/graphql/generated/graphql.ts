/* eslint-disable */
import type { TypedDocumentNode as DocumentNode } from '@graphql-typed-document-node/core';
export type Maybe<T> = T | null;
export type InputMaybe<T> = T | null | undefined;
export type Exact<T extends { [key: string]: unknown }> = { [K in keyof T]: T[K] };
export type MakeOptional<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]?: Maybe<T[SubKey]> };
export type MakeMaybe<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]: Maybe<T[SubKey]> };
export type MakeEmpty<T extends { [key: string]: unknown }, K extends keyof T> = { [_ in K]?: never };
export type Incremental<T> = T | { [P in keyof T]?: P extends ' $fragmentName' | '__typename' ? T[P] : never };
/** All built-in and custom scalars, mapped to their actual values */
export type Scalars = {
  ID: { input: number; output: number; }
  String: { input: string; output: string; }
  Boolean: { input: boolean; output: boolean; }
  Int: { input: number; output: number; }
  Float: { input: number; output: number; }
};

export type Anime = {
  __typename?: 'Anime';
  animeId: Scalars['ID']['output'];
  averageScore?: Maybe<Scalars['Float']['output']>;
  coverImage: Scalars['String']['output'];
  mappings: Array<Mapping>;
  popularity?: Maybe<Scalars['Float']['output']>;
  startDate?: Maybe<Scalars['String']['output']>;
  title: AnimeTitles;
};

export type AnimePage = {
  __typename?: 'AnimePage';
  content: Array<Anime>;
  pageInfo: PageInfo;
};

export type AnimeTitles = {
  __typename?: 'AnimeTitles';
  titleCn?: Maybe<Scalars['String']['output']>;
  titleEn?: Maybe<Scalars['String']['output']>;
  titleNative?: Maybe<Scalars['String']['output']>;
  titleRomaji?: Maybe<Scalars['String']['output']>;
};

export type Mapping = {
  __typename?: 'Mapping';
  mappingId: Scalars['ID']['output'];
  normalizedPopularity?: Maybe<Scalars['Float']['output']>;
  normalizedScore?: Maybe<Scalars['Float']['output']>;
  platformId: Scalars['String']['output'];
  rawPopularity?: Maybe<Scalars['Float']['output']>;
  rawScore?: Maybe<Scalars['Float']['output']>;
  sourcePlatform: Platform;
};

export type PageInfo = {
  __typename?: 'PageInfo';
  number: Scalars['Int']['output'];
  size: Scalars['Int']['output'];
  totalElements: Scalars['Int']['output'];
  totalPages: Scalars['Int']['output'];
};

export type Platform =
  | 'AniList'
  | 'Bangumi'
  | 'MyAnimeList';

export type Query = {
  __typename?: 'Query';
  anime?: Maybe<Anime>;
  animeList: AnimePage;
  animeListBySearch: AnimePage;
};


export type QueryAnimeArgs = {
  animeId: Scalars['ID']['input'];
};


export type QueryAnimeListArgs = {
  pageNumber?: InputMaybe<Scalars['Int']['input']>;
  pageSize?: InputMaybe<Scalars['Int']['input']>;
  season?: InputMaybe<Season>;
  sortBy?: InputMaybe<SortBy>;
  year?: InputMaybe<Scalars['Int']['input']>;
};


export type QueryAnimeListBySearchArgs = {
  pageNumber?: InputMaybe<Scalars['Int']['input']>;
  pageSize?: InputMaybe<Scalars['Int']['input']>;
  query?: InputMaybe<Scalars['String']['input']>;
};

export type Season =
  | 'FALL'
  | 'SPRING'
  | 'SUMMER'
  | 'WINTER';

export type SortBy =
  | 'POPULARITY'
  | 'SCORE';

export type GetAnimeListRowQueryVariables = Exact<{
  year?: InputMaybe<Scalars['Int']['input']>;
  season?: InputMaybe<Season>;
  pageNumber?: InputMaybe<Scalars['Int']['input']>;
  pageSize?: InputMaybe<Scalars['Int']['input']>;
  sortBy?: InputMaybe<SortBy>;
}>;


export type GetAnimeListRowQuery = { __typename?: 'Query', animeList: { __typename?: 'AnimePage', content: Array<{ __typename?: 'Anime', animeId: number, coverImage: string, averageScore?: number | null, title: { __typename?: 'AnimeTitles', titleNative?: string | null, titleCn?: string | null } }> } };

export type GetAnimeListQueryVariables = Exact<{
  year?: InputMaybe<Scalars['Int']['input']>;
  season?: InputMaybe<Season>;
  pageNumber?: InputMaybe<Scalars['Int']['input']>;
  pageSize?: InputMaybe<Scalars['Int']['input']>;
  sortBy?: InputMaybe<SortBy>;
}>;


export type GetAnimeListQuery = { __typename?: 'Query', animeList: { __typename?: 'AnimePage', content: Array<{ __typename?: 'Anime', animeId: number, coverImage: string, averageScore?: number | null, title: { __typename?: 'AnimeTitles', titleNative?: string | null, titleCn?: string | null } }>, pageInfo: { __typename?: 'PageInfo', size: number, number: number, totalElements: number, totalPages: number } } };

export type GetAnimeListBySearchQueryVariables = Exact<{
  query?: InputMaybe<Scalars['String']['input']>;
  pageNumber?: InputMaybe<Scalars['Int']['input']>;
  pageSize?: InputMaybe<Scalars['Int']['input']>;
}>;


export type GetAnimeListBySearchQuery = { __typename?: 'Query', animeListBySearch: { __typename?: 'AnimePage', content: Array<{ __typename?: 'Anime', animeId: number, coverImage: string, averageScore?: number | null, title: { __typename?: 'AnimeTitles', titleNative?: string | null, titleCn?: string | null } }>, pageInfo: { __typename?: 'PageInfo', size: number, number: number, totalElements: number, totalPages: number } } };

export type AnimeListFragment = { __typename?: 'AnimePage', content: Array<{ __typename?: 'Anime', animeId: number, coverImage: string, averageScore?: number | null, title: { __typename?: 'AnimeTitles', titleNative?: string | null, titleCn?: string | null } }>, pageInfo: { __typename?: 'PageInfo', size: number, number: number, totalElements: number, totalPages: number } };

export type AnimeCardFragment = { __typename?: 'Anime', animeId: number, coverImage: string, averageScore?: number | null, title: { __typename?: 'AnimeTitles', titleNative?: string | null, titleCn?: string | null } };

export type GetAnimeDetailQueryVariables = Exact<{
  animeId: Scalars['ID']['input'];
}>;


export type GetAnimeDetailQuery = { __typename?: 'Query', anime?: { __typename?: 'Anime', animeId: number, coverImage: string, startDate?: string | null, averageScore?: number | null, popularity?: number | null, title: { __typename?: 'AnimeTitles', titleNative?: string | null, titleRomaji?: string | null, titleEn?: string | null, titleCn?: string | null }, mappings: Array<{ __typename?: 'Mapping', mappingId: number, sourcePlatform: Platform, platformId: string, rawScore?: number | null, normalizedScore?: number | null, rawPopularity?: number | null, normalizedPopularity?: number | null }> } | null };

export type MappingItemFragment = { __typename?: 'Mapping', sourcePlatform: Platform, platformId: string, rawScore?: number | null, normalizedScore?: number | null, rawPopularity?: number | null, normalizedPopularity?: number | null };

export const AnimeCardFragmentDoc = {"kind":"Document","definitions":[{"kind":"FragmentDefinition","name":{"kind":"Name","value":"AnimeCard"},"typeCondition":{"kind":"NamedType","name":{"kind":"Name","value":"Anime"}},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"animeId"}},{"kind":"Field","name":{"kind":"Name","value":"title"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"titleNative"}},{"kind":"Field","name":{"kind":"Name","value":"titleCn"}}]}},{"kind":"Field","name":{"kind":"Name","value":"coverImage"}},{"kind":"Field","name":{"kind":"Name","value":"averageScore"}}]}}]} as unknown as DocumentNode<AnimeCardFragment, unknown>;
export const AnimeListFragmentDoc = {"kind":"Document","definitions":[{"kind":"FragmentDefinition","name":{"kind":"Name","value":"AnimeList"},"typeCondition":{"kind":"NamedType","name":{"kind":"Name","value":"AnimePage"}},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"content"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"animeId"}},{"kind":"FragmentSpread","name":{"kind":"Name","value":"AnimeCard"}}]}},{"kind":"Field","name":{"kind":"Name","value":"pageInfo"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"size"}},{"kind":"Field","name":{"kind":"Name","value":"number"}},{"kind":"Field","name":{"kind":"Name","value":"totalElements"}},{"kind":"Field","name":{"kind":"Name","value":"totalPages"}}]}}]}},{"kind":"FragmentDefinition","name":{"kind":"Name","value":"AnimeCard"},"typeCondition":{"kind":"NamedType","name":{"kind":"Name","value":"Anime"}},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"animeId"}},{"kind":"Field","name":{"kind":"Name","value":"title"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"titleNative"}},{"kind":"Field","name":{"kind":"Name","value":"titleCn"}}]}},{"kind":"Field","name":{"kind":"Name","value":"coverImage"}},{"kind":"Field","name":{"kind":"Name","value":"averageScore"}}]}}]} as unknown as DocumentNode<AnimeListFragment, unknown>;
export const MappingItemFragmentDoc = {"kind":"Document","definitions":[{"kind":"FragmentDefinition","name":{"kind":"Name","value":"MappingItem"},"typeCondition":{"kind":"NamedType","name":{"kind":"Name","value":"Mapping"}},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"sourcePlatform"}},{"kind":"Field","name":{"kind":"Name","value":"platformId"}},{"kind":"Field","name":{"kind":"Name","value":"rawScore"}},{"kind":"Field","name":{"kind":"Name","value":"normalizedScore"}},{"kind":"Field","name":{"kind":"Name","value":"rawPopularity"}},{"kind":"Field","name":{"kind":"Name","value":"normalizedPopularity"}}]}}]} as unknown as DocumentNode<MappingItemFragment, unknown>;
export const GetAnimeListRowDocument = {"kind":"Document","definitions":[{"kind":"OperationDefinition","operation":"query","name":{"kind":"Name","value":"GetAnimeListRow"},"variableDefinitions":[{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"year"}},"type":{"kind":"NamedType","name":{"kind":"Name","value":"Int"}}},{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"season"}},"type":{"kind":"NamedType","name":{"kind":"Name","value":"Season"}}},{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"pageNumber"}},"type":{"kind":"NamedType","name":{"kind":"Name","value":"Int"}}},{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"pageSize"}},"type":{"kind":"NamedType","name":{"kind":"Name","value":"Int"}}},{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"sortBy"}},"type":{"kind":"NamedType","name":{"kind":"Name","value":"SortBy"}}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"animeList"},"arguments":[{"kind":"Argument","name":{"kind":"Name","value":"year"},"value":{"kind":"Variable","name":{"kind":"Name","value":"year"}}},{"kind":"Argument","name":{"kind":"Name","value":"season"},"value":{"kind":"Variable","name":{"kind":"Name","value":"season"}}},{"kind":"Argument","name":{"kind":"Name","value":"pageNumber"},"value":{"kind":"Variable","name":{"kind":"Name","value":"pageNumber"}}},{"kind":"Argument","name":{"kind":"Name","value":"pageSize"},"value":{"kind":"Variable","name":{"kind":"Name","value":"pageSize"}}},{"kind":"Argument","name":{"kind":"Name","value":"sortBy"},"value":{"kind":"Variable","name":{"kind":"Name","value":"sortBy"}}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"content"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"animeId"}},{"kind":"FragmentSpread","name":{"kind":"Name","value":"AnimeCard"}}]}}]}}]}},{"kind":"FragmentDefinition","name":{"kind":"Name","value":"AnimeCard"},"typeCondition":{"kind":"NamedType","name":{"kind":"Name","value":"Anime"}},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"animeId"}},{"kind":"Field","name":{"kind":"Name","value":"title"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"titleNative"}},{"kind":"Field","name":{"kind":"Name","value":"titleCn"}}]}},{"kind":"Field","name":{"kind":"Name","value":"coverImage"}},{"kind":"Field","name":{"kind":"Name","value":"averageScore"}}]}}]} as unknown as DocumentNode<GetAnimeListRowQuery, GetAnimeListRowQueryVariables>;
export const GetAnimeListDocument = {"kind":"Document","definitions":[{"kind":"OperationDefinition","operation":"query","name":{"kind":"Name","value":"GetAnimeList"},"variableDefinitions":[{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"year"}},"type":{"kind":"NamedType","name":{"kind":"Name","value":"Int"}}},{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"season"}},"type":{"kind":"NamedType","name":{"kind":"Name","value":"Season"}}},{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"pageNumber"}},"type":{"kind":"NamedType","name":{"kind":"Name","value":"Int"}}},{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"pageSize"}},"type":{"kind":"NamedType","name":{"kind":"Name","value":"Int"}}},{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"sortBy"}},"type":{"kind":"NamedType","name":{"kind":"Name","value":"SortBy"}}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"animeList"},"arguments":[{"kind":"Argument","name":{"kind":"Name","value":"year"},"value":{"kind":"Variable","name":{"kind":"Name","value":"year"}}},{"kind":"Argument","name":{"kind":"Name","value":"season"},"value":{"kind":"Variable","name":{"kind":"Name","value":"season"}}},{"kind":"Argument","name":{"kind":"Name","value":"pageNumber"},"value":{"kind":"Variable","name":{"kind":"Name","value":"pageNumber"}}},{"kind":"Argument","name":{"kind":"Name","value":"pageSize"},"value":{"kind":"Variable","name":{"kind":"Name","value":"pageSize"}}},{"kind":"Argument","name":{"kind":"Name","value":"sortBy"},"value":{"kind":"Variable","name":{"kind":"Name","value":"sortBy"}}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"FragmentSpread","name":{"kind":"Name","value":"AnimeList"}}]}}]}},{"kind":"FragmentDefinition","name":{"kind":"Name","value":"AnimeCard"},"typeCondition":{"kind":"NamedType","name":{"kind":"Name","value":"Anime"}},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"animeId"}},{"kind":"Field","name":{"kind":"Name","value":"title"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"titleNative"}},{"kind":"Field","name":{"kind":"Name","value":"titleCn"}}]}},{"kind":"Field","name":{"kind":"Name","value":"coverImage"}},{"kind":"Field","name":{"kind":"Name","value":"averageScore"}}]}},{"kind":"FragmentDefinition","name":{"kind":"Name","value":"AnimeList"},"typeCondition":{"kind":"NamedType","name":{"kind":"Name","value":"AnimePage"}},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"content"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"animeId"}},{"kind":"FragmentSpread","name":{"kind":"Name","value":"AnimeCard"}}]}},{"kind":"Field","name":{"kind":"Name","value":"pageInfo"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"size"}},{"kind":"Field","name":{"kind":"Name","value":"number"}},{"kind":"Field","name":{"kind":"Name","value":"totalElements"}},{"kind":"Field","name":{"kind":"Name","value":"totalPages"}}]}}]}}]} as unknown as DocumentNode<GetAnimeListQuery, GetAnimeListQueryVariables>;
export const GetAnimeListBySearchDocument = {"kind":"Document","definitions":[{"kind":"OperationDefinition","operation":"query","name":{"kind":"Name","value":"GetAnimeListBySearch"},"variableDefinitions":[{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"query"}},"type":{"kind":"NamedType","name":{"kind":"Name","value":"String"}}},{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"pageNumber"}},"type":{"kind":"NamedType","name":{"kind":"Name","value":"Int"}}},{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"pageSize"}},"type":{"kind":"NamedType","name":{"kind":"Name","value":"Int"}}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"animeListBySearch"},"arguments":[{"kind":"Argument","name":{"kind":"Name","value":"query"},"value":{"kind":"Variable","name":{"kind":"Name","value":"query"}}},{"kind":"Argument","name":{"kind":"Name","value":"pageNumber"},"value":{"kind":"Variable","name":{"kind":"Name","value":"pageNumber"}}},{"kind":"Argument","name":{"kind":"Name","value":"pageSize"},"value":{"kind":"Variable","name":{"kind":"Name","value":"pageSize"}}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"FragmentSpread","name":{"kind":"Name","value":"AnimeList"}}]}}]}},{"kind":"FragmentDefinition","name":{"kind":"Name","value":"AnimeCard"},"typeCondition":{"kind":"NamedType","name":{"kind":"Name","value":"Anime"}},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"animeId"}},{"kind":"Field","name":{"kind":"Name","value":"title"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"titleNative"}},{"kind":"Field","name":{"kind":"Name","value":"titleCn"}}]}},{"kind":"Field","name":{"kind":"Name","value":"coverImage"}},{"kind":"Field","name":{"kind":"Name","value":"averageScore"}}]}},{"kind":"FragmentDefinition","name":{"kind":"Name","value":"AnimeList"},"typeCondition":{"kind":"NamedType","name":{"kind":"Name","value":"AnimePage"}},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"content"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"animeId"}},{"kind":"FragmentSpread","name":{"kind":"Name","value":"AnimeCard"}}]}},{"kind":"Field","name":{"kind":"Name","value":"pageInfo"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"size"}},{"kind":"Field","name":{"kind":"Name","value":"number"}},{"kind":"Field","name":{"kind":"Name","value":"totalElements"}},{"kind":"Field","name":{"kind":"Name","value":"totalPages"}}]}}]}}]} as unknown as DocumentNode<GetAnimeListBySearchQuery, GetAnimeListBySearchQueryVariables>;
export const GetAnimeDetailDocument = {"kind":"Document","definitions":[{"kind":"OperationDefinition","operation":"query","name":{"kind":"Name","value":"GetAnimeDetail"},"variableDefinitions":[{"kind":"VariableDefinition","variable":{"kind":"Variable","name":{"kind":"Name","value":"animeId"}},"type":{"kind":"NonNullType","type":{"kind":"NamedType","name":{"kind":"Name","value":"ID"}}}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"anime"},"arguments":[{"kind":"Argument","name":{"kind":"Name","value":"animeId"},"value":{"kind":"Variable","name":{"kind":"Name","value":"animeId"}}}],"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"animeId"}},{"kind":"Field","name":{"kind":"Name","value":"coverImage"}},{"kind":"Field","name":{"kind":"Name","value":"startDate"}},{"kind":"Field","name":{"kind":"Name","value":"averageScore"}},{"kind":"Field","name":{"kind":"Name","value":"popularity"}},{"kind":"Field","name":{"kind":"Name","value":"title"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"titleNative"}},{"kind":"Field","name":{"kind":"Name","value":"titleRomaji"}},{"kind":"Field","name":{"kind":"Name","value":"titleEn"}},{"kind":"Field","name":{"kind":"Name","value":"titleCn"}}]}},{"kind":"Field","name":{"kind":"Name","value":"mappings"},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"mappingId"}},{"kind":"FragmentSpread","name":{"kind":"Name","value":"MappingItem"}}]}}]}}]}},{"kind":"FragmentDefinition","name":{"kind":"Name","value":"MappingItem"},"typeCondition":{"kind":"NamedType","name":{"kind":"Name","value":"Mapping"}},"selectionSet":{"kind":"SelectionSet","selections":[{"kind":"Field","name":{"kind":"Name","value":"sourcePlatform"}},{"kind":"Field","name":{"kind":"Name","value":"platformId"}},{"kind":"Field","name":{"kind":"Name","value":"rawScore"}},{"kind":"Field","name":{"kind":"Name","value":"normalizedScore"}},{"kind":"Field","name":{"kind":"Name","value":"rawPopularity"}},{"kind":"Field","name":{"kind":"Name","value":"normalizedPopularity"}}]}}]} as unknown as DocumentNode<GetAnimeDetailQuery, GetAnimeDetailQueryVariables>;