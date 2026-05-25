<template>
  <div class="animate-pulse">
    <div class="flex flex-col items-center gap-8 sm:flex-row sm:items-start">
      <div class="w-[16rem] shrink-0 sm:w-60">
        <div class="aspect-[1/1.4] w-full rounded-2xl bg-gray-200 shadow-lg"></div>
      </div>

      <div
        class="flex w-full flex-col items-center justify-center gap-5 text-center sm:items-start sm:justify-start sm:text-left"
      >
        <div class="h-8 w-full max-w-[28rem] rounded-full bg-gray-200"></div>
        <div class="flex w-full max-w-[24rem] flex-col items-center gap-2 sm:items-start">
          <div class="h-4 w-2/3 rounded-full bg-gray-200"></div>
          <div class="h-4 w-3/4 rounded-full bg-gray-200"></div>
          <div class="h-4 w-1/2 rounded-full bg-gray-200"></div>
        </div>
        <div class="mt-2 h-4 w-32 rounded-full bg-gray-200"></div>

        <div class="mt-4 flex flex-wrap items-center justify-center gap-6 sm:justify-start">
          <div class="flex flex-col items-center gap-2 sm:items-start">
            <div class="h-4 w-16 rounded-full bg-gray-200"></div>
            <div class="h-8 w-24 rounded-full bg-gray-200"></div>
          </div>
          <div class="flex flex-col items-center gap-2 sm:items-start">
            <div class="h-4 w-12 rounded-full bg-gray-200"></div>
            <div class="h-8 w-28 rounded-full bg-gray-200"></div>
          </div>
        </div>
      </div>
    </div>

    <div class="mt-10">
      <div class="mb-4 h-7 w-32 rounded-full bg-gray-200"></div>
      <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <div
          v-for="index in 3"
          :key="index"
          class="flex flex-col gap-3 rounded-2xl border border-gray-100 bg-gray-50/50 p-5"
        >
          <div class="flex items-center gap-3">
            <div class="h-7 w-7 rounded-md bg-gray-200"></div>
            <div class="h-4 w-24 rounded-full bg-gray-200"></div>
          </div>
          <div class="grid grid-cols-2 gap-y-2">
            <div class="h-4 w-16 rounded-full bg-gray-200"></div>
            <div class="h-4 w-12 rounded-full bg-gray-200"></div>
            <div class="h-4 w-20 rounded-full bg-gray-200"></div>
            <div class="h-4 w-14 rounded-full bg-gray-200"></div>
            <div class="h-4 w-16 rounded-full bg-gray-200"></div>
            <div class="h-4 w-18 rounded-full bg-gray-200"></div>
            <div class="h-4 w-20 rounded-full bg-gray-200"></div>
            <div class="h-4 w-16 rounded-full bg-gray-200"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
