<script setup lang="ts">
import { RouterLink } from "vue-router";

import type { AnimeCardFragment } from "@/graphql/generated/graphql";

const props = defineProps<{
  anime: AnimeCardFragment;
}>();
</script>

<template>
  <RouterLink :to="`/anime/${anime.animeId}`" class="block no-underline">
    <div class="group flex w-[12.5rem] flex-col gap-2">
      <div
        class="relative aspect-[1/1.4] w-full overflow-hidden rounded-3xl bg-gray-100 transition-all duration-300"
      >
        <div class="h-full w-full transition-all duration-300 group-hover:brightness-80">
          <img
            class="block h-full w-full object-cover"
            :src="anime.coverImage"
            :alt="anime.title.titleCn || anime.title.titleNative || 'Anime Cover'"
          />
          <div
            class="absolute inset-x-0 bottom-0 h-12 bg-gradient-to-t from-black/70 to-transparent"
          ></div>
          <div
            v-if="anime.averageScore"
            class="absolute right-3 bottom-1 text-[16px] font-bold text-white"
          >
            {{ anime.averageScore.toFixed(0) }}
          </div>
        </div>
      </div>

      <h3
        class="m-0 line-clamp-2 h-[40px] px-2 text-[14px] leading-[1.4] font-medium text-gray-800"
        :title="anime.title.titleCn || anime.title.titleNative || ''"
      >
        {{ anime.title.titleCn || anime.title.titleNative }}
      </h3>
    </div>
  </RouterLink>
</template>
