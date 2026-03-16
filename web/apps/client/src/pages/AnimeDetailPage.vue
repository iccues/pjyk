<script setup lang="ts">
import { useQuery } from "@urql/vue";
import { computed } from "vue";
import { useRoute } from "vue-router";

import AnimeHero from "@/components/AnimeHero.vue";
import AnimePlatformCard from "@/components/AnimePlatformCard.vue";
import { useAnimeDetailHead } from "@/composables/useAnimeDetailHead";
import { GetAnimeDetailDocument } from "@/graphql/generated/graphql";

const route = useRoute();
const animeId = computed(() => Number(route.params.animeId));

const { data, fetching, error } = useQuery({
  query: GetAnimeDetailDocument,
  variables: computed(() => ({ animeId: animeId.value })),
});

const anime = computed(() => data.value?.anime);

// 动态 SEO 配置
useAnimeDetailHead(anime);
</script>

<template>
  <div class="mx-auto max-w-[1100px] p-5">
    <!-- States -->
    <div v-if="fetching" class="py-20 text-center text-gray-400">加载中...</div>
    <div v-else-if="error" class="py-20 text-center text-red-500">{{ error.message }}</div>
    <div v-else-if="!anime" class="py-20 text-center text-gray-500">未找到该番剧</div>

    <!-- Main Content -->
    <template v-else>
      <AnimeHero :anime="anime" />

      <!-- Platform Scores Section -->
      <div v-if="anime.mappings.length > 0" class="mt-10">
        <h2 class="mb-4 text-lg font-semibold text-gray-800">各平台数据</h2>
        <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <AnimePlatformCard
            v-for="mapping in anime.mappings"
            :key="mapping.mappingId"
            :mapping="mapping"
          />
        </div>
      </div>
    </template>
  </div>
</template>
