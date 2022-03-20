<script lang="ts" setup>
import { useContext } from "vite-ssr/vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { useAuthors } from "~/composables/useApiHelper";
import { handleRequestError } from "~/composables/useErrorHandling";
import SortableTable, { Header } from "~/components/SortableTable.vue";
import { prettyDate } from "~/composables/useDate";
import PageTitle from "~/components/design/PageTitle.vue";

const ctx = useContext();
const i18n = useI18n();
const { params } = useRoute();
const authors = await useAuthors().catch((e) => handleRequestError(e, ctx, i18n));
const headers = [
  { name: "pic", title: "", sortable: false },
  { name: "name", title: "Username", sortable: true },
  { name: "joinDate", title: "Joined", sortable: true },
  { name: "projectCount", title: "Projects", sortable: true },
] as Header[];
</script>

<template>
  <PageTitle>Authors</PageTitle>
  <SortableTable :headers="headers" :items="authors?.result">
    <!-- todo avatar -->
    <template #item_pic="{ item }">pic {{ item.name }}</template>
    <template #item_joinDate="{ item }">{{ prettyDate(item?.joinDate) }}</template>
  </SortableTable>
</template>
