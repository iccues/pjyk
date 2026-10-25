<script setup lang="ts">
import { useQuery } from "@urql/vue";
import { computed } from "vue";
import { useRoute } from "vue-router";

import AnimeDetailSkeleton from "@/components/AnimeDetailSkeleton.vue";
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
  <!-- 加载态与详情内容保持相同上边距。 -->
  <div v-if="fetching" class="container-detail pt-10">
    <AnimeDetailSkeleton />
  </div>
  <div v-else-if="error" class="container-detail py-20 text-center text-red-500">
    {{ error.message }}
  </div>
  <div v-else-if="!anime" class="container-detail py-20 text-center text-gray-500">
    未找到该动画
  </div>

  <template v-else>
    <AnimeHero :anime="anime" />

    <div v-if="anime.mappings.length > 0" class="container-detail pt-10">
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
</template>
