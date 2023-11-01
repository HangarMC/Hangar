<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { ref } from "vue";
import { useHead } from "@unhead/vue";
import PageTitle from "~/components/design/PageTitle.vue";
import { useSeo } from "~/composables/useSeo";
import Flags from "~/components/Flags.vue";
import Tabs from "~/components/design/Tabs.vue";
import { definePageMeta } from "#imports";
import type { Tab } from "~/types/components/design/Tabs";

definePageMeta({
  globalPermsRequired: ["MOD_NOTES_AND_FLAGS"],
});

const i18n = useI18n();
const route = useRoute();
const loading = ref<{ [key: number]: boolean }>({});

const selectedTab = ref("unresolved");
const selectedTabs: Tab[] = [
  { value: "unresolved", header: i18n.t("flagReview.unresolved") },
  { value: "resolved", header: i18n.t("flagReview.resolved") },
];

useHead(useSeo(i18n.t("flagReview.title"), null, route, null));
</script>

<template>
  <div>
    <PageTitle>{{ i18n.t("flagReview.title") }}</PageTitle>
    <Tabs v-model="selectedTab" :tabs="selectedTabs" :vertical="false">
      <template #unresolved>
        <Flags :resolved="false"></Flags>
      </template>
      <template #resolved>
        <Flags resolved></Flags>
      </template>
    </Tabs>
  </div>
</template>
