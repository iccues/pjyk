<script setup lang="ts">
import { login } from '../../auth/oidc';
</script>
<template>
    <button @click="login">Login</button>
</template>
