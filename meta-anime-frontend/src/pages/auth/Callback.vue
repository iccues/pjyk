<script setup lang="ts">
import { oidcManager } from '../../auth/oidc';

await oidcManager.signinRedirectCallback();
location.href = '/admin';
</script>
<template></template>
