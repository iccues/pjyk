<script setup lang="ts">
import { ElButton } from "element-plus";
import type { User } from "oidc-client-ts";
import { onMounted, ref } from "vue";

import { logout, oidcConfig, oidcManager } from "@/auth/oidc";

const user = ref<User | null>(null);

onMounted(async () => {
  user.value = (await oidcManager?.getUser()) ?? null;
});
</script>

<template>
  <div class="flex min-h-[80vh] items-center justify-center">
    <div class="w-full max-w-md rounded-lg bg-white p-8 shadow-md">
      <p v-if="!oidcConfig.enabled" class="text-center text-gray-500">当前环境无需登录</p>
      <div v-if="user" class="text-center">
        <div class="mb-8 space-y-3">
          <div>
            <p class="mb-1 text-sm text-gray-500">用户名</p>
            <p class="font-medium text-gray-900">
              {{ user.profile.name || user.profile.preferred_username || "未设置" }}
            </p>
          </div>

          <div>
            <p class="mb-1 text-sm text-gray-500">邮箱</p>
            <p class="font-medium text-gray-900">
              {{ user.profile.email || "未设置" }}
            </p>
          </div>
        </div>

        <el-button type="primary" size="large" @click="logout" class="w-full"> 登出 </el-button>
      </div>
    </div>
  </div>
</template>
