<script lang="ts" setup>
import { useContext } from "vite-ssr/vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { useStaff } from "~/composables/useApiHelper";
import { handleRequestError } from "~/composables/useErrorHandling";
import SortableTable, { Header } from "~/components/SortableTable.vue";
import PageTitle from "~/components/design/PageTitle.vue";
import { useSeo } from "~/composables/useSeo";
import { useHead } from "@vueuse/head";

const ctx = useContext();
const i18n = useI18n();
const route = useRoute();
const staff = await useStaff().catch((e) => handleRequestError(e, ctx, i18n));
const headers = [
  { name: "pic", title: "", sortable: false },
  { name: "name", title: "Username", sortable: true },
  { name: "joinDate", title: "Joined", sortable: true },
] as Header[];

useHead(useSeo(i18n.t("pages.staffTitle"), null, route, null));
</script>

<template>
  <PageTitle>Staff</PageTitle>
  <SortableTable :headers="headers" :items="staff?.result">
    <!-- todo avatar -->
    <template #item_pic="{ item }">pic {{ item.name }}</template>
    <template #item_joinDate="{ item }">{{ i18n.d(item.joinDate, "date") }}</template>
  </SortableTable>
</template>
