<script setup lang="ts">
import { onMounted } from "vue";
import { useRouter } from "vue-router";
import { oidcManager } from "@/auth/oidc";

const router = useRouter();

onMounted(async () => {
  try {
    await oidcManager.signinRedirectCallback();
    router.replace("/admin");
  } catch (e) {
    console.error("Auth callback failed", e);
  }
});
</script>
<template>
    <div>加载中...</div>
</template>
