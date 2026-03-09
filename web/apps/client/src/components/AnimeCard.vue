<script setup lang="ts">
import { ref } from "vue";

import type { AnimeCardFragment } from "@/graphql/generated/graphql";

import AnimeScoreItem from "./AnimeScoreItem.vue";

const props = defineProps<{
  anime: AnimeCardFragment;
}>();

const flipped = ref(true);
</script>

<template>
  <div class="flex w-[12.5rem] flex-col gap-2">
    <div
      class="group relative aspect-[1/1.4] w-full overflow-hidden rounded-3xl bg-gray-100 transition-all duration-300"
      @click="flipped = !flipped"
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
      <Transition
        enter-active-class="transition-opacity duration-300"
        leave-active-class="transition-opacity duration-300"
        enter-from-class="opacity-0"
        enter-to-class="opacity-100"
        leave-from-class="opacity-100"
        leave-to-class="opacity-0"
      >
        <div
          v-if="!flipped"
          class="absolute inset-0 flex flex-col gap-3 bg-black/20 p-3 backdrop-blur-lg"
        >
          <AnimeScoreItem
            v-for="mapping in anime.mappings"
            :key="mapping.mappingId"
            :mapping="mapping"
          />
        </div>
      </Transition>
    </div>
    <h3
      class="m-0 line-clamp-2 h-[40px] px-2 text-[14px] leading-[1.4] font-medium text-gray-800"
      :title="anime.title.titleCn || anime.title.titleNative || ''"
    >
      {{ anime.title.titleCn || anime.title.titleNative }}
    </h3>
  </div>
</template>
