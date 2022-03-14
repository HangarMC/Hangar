<template>
  <main>
    <div class="min-h-[60vh]">
      <Suspense>
        <router-view v-bind="$attrs" />
        <template #fallback> Loading...</template>
      </Suspense>
    </div>
  </main>
</template>
