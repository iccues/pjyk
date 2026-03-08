<script setup lang="ts">
import { login } from "@/auth/oidc";
</script>

<template>
  <div class="flex min-h-screen items-center justify-center bg-gray-50">
    <div class="text-center">
      <el-button type="primary" size="large" @click="login"> 登录 </el-button>
    </div>
  </div>
</template>
