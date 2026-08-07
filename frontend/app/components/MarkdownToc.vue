<script setup lang="ts">
defineProps<{
  headings: { id: string; text?: string; level: number }[];
}>();

const i18n = useI18n();
</script>

<template>
  <Popper placement="bottom-end">
    <Button variant="ghost" tone="neutral" size="sm" icon-only :title="i18n.t('page.overview')" :aria-label="i18n.t('page.overview')">
      <IconMdiFormatListBulleted />
    </Button>
    <template #content="{ close }">
      <div
        class="mt-1 max-h-lg max-w-lg flex flex-col overflow-auto rounded-md border border-gray-300 background-default p-1 shadow-default dark:border-gray-700"
      >
        <!-- eslint-disable vue/no-v-html -->
        <a
          v-for="heading in headings"
          :key="heading.id"
          class="cursor-pointer px-4 py-1.5 text-sm decoration-none hover:background-card"
          :class="'toc-' + heading.level"
          :href="`#${heading.id}`"
          @click="close"
          v-html="heading.text"
        />
        <!-- eslint-enable vue/no-v-html -->
      </div>
    </template>
  </Popper>
</template>

<style lang="scss" scoped>
.toc-1 {
  font-weight: 700;
}
.toc-2 {
  padding-left: 2rem;
}
.toc-3 {
  padding-left: 3rem;
}
.toc-4 {
  padding-left: 4rem;
}
</style>
