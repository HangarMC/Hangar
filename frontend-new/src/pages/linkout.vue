<script lang="ts" setup>
import { useRoute } from "vue-router";

const target = useRoute().query.remoteUrl;
</script>
<template>
  <div>
    Hur dur danger ahead!<br />
    <a class="cursor-pointer" @click="$router.back()">Back to safety</a><br />
    <a :href="target" target="_self" rel="noopener noreferrer">Go ahead</a>
  </div>
</template>
