<script lang="ts" setup>
const route = useRoute("user-project-channels");

definePageMeta({
  projectPermsRequired: ["EditChannels"],
});

await navigateTo(`/${route.params.user}/${route.params.project}/settings/channels`, { redirectCode: 301 });
</script>

<template>
  <div />
</template>
