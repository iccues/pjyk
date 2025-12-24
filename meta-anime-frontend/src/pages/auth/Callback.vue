<script setup lang="ts">
import { oidcManager } from '@/auth/oidc';
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter()

onMounted(async () => {
    try {
        await oidcManager.signinRedirectCallback();
        router.replace('/admin');
    } catch (e) {
        console.error('Auth callback failed', e);
    }
})
</script>
<template>
    <div>加载中...</div>
</template>
