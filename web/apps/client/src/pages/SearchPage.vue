<script setup lang="ts">
import { Search } from "@element-plus/icons-vue";
import { useQuery } from "@urql/vue";

import AnimeList from "@/components/AnimeList.vue";
import { useSearchHead } from "@/composables/useSearchHead";
import { useSearchQuery } from "@/composables/useSearchQuery";
import { GetAnimeListBySearchDocument } from "@/graphql/generated/graphql";

const { searchInput, searchParams, handleSearch, handlePageChange } = useSearchQuery();

useSearchHead(searchParams);

const { data, fetching, error } = useQuery({
  query: GetAnimeListBySearchDocument,
  variables: searchParams,
  // 仅在有查询参数时发送请求
  pause: () => !searchParams.value.query || searchParams.value.query.trim() === "",
});
</script>

<template>
  <div class="mx-auto max-w-[1400px] p-5">
    <!-- Search Box -->
    <div class="mt-4 mb-8 grid grid-cols-[repeat(auto-fill,12.5rem)] justify-center gap-5">
      <div
        class="col-span-full flex w-full max-w-md items-center overflow-hidden rounded-full border border-gray-300 bg-white transition-shadow"
      >
        <input
          v-model="searchInput"
          type="text"
          placeholder="输入番剧标题进行搜索..."
          class="w-full border-none bg-transparent px-4 py-2 text-[15px] text-gray-900 placeholder:text-gray-400 focus:outline-none"
          @keyup.enter="handleSearch"
        />
        <button
          @click="handleSearch"
          class="mr-2 flex items-center justify-center p-2 text-gray-400 transition-colors hover:text-indigo-600 focus:outline-none"
          title="搜索"
        >
          <el-icon :size="18"><Search /></el-icon>
        </button>
      </div>
    </div>

    <!-- Anime List Results -->
    <AnimeList
      :animeList="data?.animeListBySearch"
      :fetching="fetching"
      :error="error"
      v-on:page-change="handlePageChange"
    />
  </div>
</template>
