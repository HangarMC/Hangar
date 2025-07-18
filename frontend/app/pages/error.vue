<script lang="ts" setup>
const route = useRoute();

// eslint-disable-next-line vue/return-in-computed-property
const hangarApiError = computed(() => {
  if (route.query.errorCode || route.query.errorMessage) {
    return {
      errorCode: route.query.errorCode,
      errorMessage: route.query.errorMessage,
      returnUrl: route.query.returnUrl as string,
    };
  }
  return;
});

function back() {
  history.back();
}
</script>

<template>
  <div class="flex flex-col items-center justify-center min-h-50vh">
    <template v-if="hangarApiError">
      <h1 class="text-4xl font-bold">API Error</h1>
      <h2 class="text-xl font-bold">{{ hangarApiError.errorMessage }}</h2>
      <div class="mt-4 flex gap-2">
        <Button href="/">Back to homepage</Button>
        <Button v-if="hangarApiError.returnUrl" :href="hangarApiError.returnUrl">Back to last page</Button>
        <Button v-else @click="back()">Back to last page</Button>
      </div>
    </template>
  </div>
</template>
