<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { logout, oidcManager } from '@/auth/oidc';
import type { User } from 'oidc-client-ts';

const user = ref<User | null>(null);

onMounted(async () => {
  user.value = await oidcManager.getUser();
});
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50">
    <div class="bg-white rounded-lg shadow-md p-8 max-w-md w-full">
      <div v-if="user" class="text-center">
        <div class="mb-8 space-y-3">
          <div>
            <p class="text-sm text-gray-500 mb-1">用户名</p>
            <p class="text-gray-900 font-medium">
              {{ user.profile.name || user.profile.preferred_username || '未设置' }}
            </p>
          </div>

          <div>
            <p class="text-sm text-gray-500 mb-1">邮箱</p>
            <p class="text-gray-900 font-medium">
              {{ user.profile.email || '未设置' }}
            </p>
          </div>
        </div>

        <el-button type="primary" size="large" @click="logout" class="w-full">
          登出
        </el-button>
      </div>
    </div>
  </div>
</template>
