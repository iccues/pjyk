<script setup lang="ts">
import { ref } from "vue";
import type { Anime } from "@/types/anime.ts";
import AnimeScoreItem from "./AnimeScoreItem.vue";

defineProps<{
  anime: Anime,
}>()

const flipped = ref(true);

</script>

<template>
  <div class="w-[200px] flex flex-col gap-2">
    <div
      class="relative w-full aspect-[3/4] overflow-hidden bg-gray-100 rounded-2xl transition-transform duration-300 hover:scale-[1.03]"
      @click="flipped = !flipped">
      <div class="w-full h-full">
        <img class="w-full h-full object-cover block" :src="anime.coverImage"
          :alt="anime.title.titleCn || anime.title.titleNative" />
        <div v-if="anime.averageScore"
          class="absolute bottom-2 right-2 text-white text-base font-bold drop-shadow-[0_1px_3px_rgba(0,0,0,0.8)]">
          {{ anime.averageScore.toFixed(0) }}
        </div>
      </div>
      <Transition enter-active-class="transition-opacity duration-300"
        leave-active-class="transition-opacity duration-300" enter-from-class="opacity-0" enter-to-class="opacity-100"
        leave-from-class="opacity-100" leave-to-class="opacity-0">
        <div v-if="!flipped" class="absolute inset-0 bg-black/20 p-4 flex flex-col gap-3 backdrop-blur-lg">
          <AnimeScoreItem v-for="mapping in anime.mappings" :key="mapping.mappingId" :mapping="mapping" />
        </div>
      </Transition>
    </div>
    <h3 class="text-sm font-medium text-gray-800 m-0 px-1 leading-[1.4] h-[calc(1.4em*2)] line-clamp-2">
      {{ anime.title.titleCn || anime.title.titleNative }}
    </h3>
  </div>
</template>