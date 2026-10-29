<script lang="ts" setup>
definePageMeta({
  globalPermsRequired: ["ModNotesAndFlags"],
});

type FlagTab = "unresolved" | "resolved";

const i18n = useI18n();
const route = useRoute("admin-flags");

const selectedTab = ref<FlagTab>("unresolved");
const tabs: { value: FlagTab; label: string }[] = [
  { value: "unresolved", label: i18n.t("flagReview.unresolved") },
  { value: "resolved", label: i18n.t("flagReview.resolved") },
];

useSeo(computed(() => ({ title: i18n.t("flagReview.title"), route })));
</script>

<template>
  <div>
    <div class="mb-5">
      <h1 class="text-3xl font-bold">{{ i18n.t("flagReview.title") }}</h1>
      <p class="mt-1 text-gray-secondary">{{ i18n.t("flagReview.subtitle") }}</p>
    </div>

    <SegmentedControl v-model="selectedTab" :options="tabs" :aria-label="i18n.t('flagReview.title')" />

    <Flags v-if="selectedTab === 'unresolved'" :resolved="false" class="mt-4" />
    <Flags v-else resolved class="mt-4" />
  </div>
</template>
