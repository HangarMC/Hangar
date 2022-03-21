<template>
  <main>
    <div class="min-h-[60vh]">
      <transition name="slide">
        <Suspense>
          <router-view v-bind="$attrs" />
          <template #fallback> Loading...</template>
        </Suspense>
      </transition>
    </div>
  </main>
</template>
