<script lang="ts" setup>
import type { Tab } from "#shared/types/components/design/Tabs";

definePageMeta({
  globalPermsRequired: ["ModNotesAndFlags"],
});

const i18n = useI18n();
const route = useRoute("admin-flags");

const selectedTab = ref("unresolved");
const selectedTabs = [
  { value: "unresolved", header: i18n.t("flagReview.unresolved") },
  { value: "resolved", header: i18n.t("flagReview.resolved") },
] as const satisfies Tab<string>[];

useSeo(computed(() => ({ title: i18n.t("flagReview.title"), route })));
</script>

<template>
  <div>
    <PageTitle>{{ i18n.t("flagReview.title") }}</PageTitle>
    <Tabs v-model="selectedTab" :tabs="selectedTabs" :vertical="false">
      <template #unresolved>
        <Flags :resolved="false" />
      </template>
      <template #resolved>
        <Flags resolved />
      </template>
    </Tabs>
  </div>
</template>
