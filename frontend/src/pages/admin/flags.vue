<script lang="ts" setup>
import { useContext } from "vite-ssr/vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { useUnresolvedFlags } from "~/composables/useApiHelper";
import { handleRequestError } from "~/composables/useErrorHandling";
import { ref } from "vue";
import PageTitle from "~/lib/components/design/PageTitle.vue";
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";
import Flags from "~/components/Flags.vue";
import Tabs, { Tab } from "~/lib/components/design/Tabs.vue";

const ctx = useContext();
const i18n = useI18n();
const route = useRoute();
const flags = await useUnresolvedFlags().catch((e) => handleRequestError(e, ctx, i18n));
const loading = ref<{ [key: number]: boolean }>({});

const selectedTab = ref("unresolved");
const selectedTabs: Tab[] = [
  { value: "unresolved", header: i18n.t("flagReview.unresolved") },
  { value: "resolved", header: i18n.t("flagReview.resolved") },
];

useHead(useSeo(i18n.t("flagReview.title"), null, route, null));
</script>

<template>
  <PageTitle>{{ i18n.t("flagReview.title") }}</PageTitle>
  <Tabs v-model="selectedTab" :tabs="selectedTabs" :vertical="false">
    <template #unresolved>
      <Flags :resolved="false"></Flags>
    </template>
    <template #resolved>
      <Flags resolved></Flags>
    </template>
  </Tabs>
</template>

<route lang="yaml">
meta:
  requireGlobalPerm: ["MOD_NOTES_AND_FLAGS"]
</route>
