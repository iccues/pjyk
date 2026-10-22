<script setup lang="ts">
import type { RouteLocationRaw } from "vue-router";

import { generateYearOptionsFrom, SEASON_OPTIONS, SORT_BY_OPTIONS } from "@/constants/ui-options";
import type { GetAnimeListQueryVariables } from "@/graphql/generated/graphql";

import LinkDropdownFilter from "./LinkDropdownFilter.vue";
import LinkSegmentedFilter from "./LinkSegmentedFilter.vue";

defineProps<{
  filters: GetAnimeListQueryVariables;
  filterLink: (changes: Partial<GetAnimeListQueryVariables>) => RouteLocationRaw;
}>();

const yearOptions = generateYearOptionsFrom(1990);
</script>

<template>
  <section class="mb-6 flex flex-wrap items-center gap-x-5 gap-y-4" aria-label="动画筛选">
    <LinkDropdownFilter
      label="年份"
      :options="yearOptions"
      :value="filters.year ?? undefined"
      :option-link="(value) => filterLink({ year: value })"
    />

    <LinkSegmentedFilter
      label="季度"
      :options="SEASON_OPTIONS"
      :value="filters.season ?? undefined"
      :option-link="(value) => filterLink({ season: value })"
      :disabled="filters.year == null"
    />

    <LinkSegmentedFilter
      label="排序"
      :options="SORT_BY_OPTIONS"
      :value="filters.sortBy ?? undefined"
      :option-link="(value) => filterLink({ sortBy: value })"
    />
  </section>
</template>
