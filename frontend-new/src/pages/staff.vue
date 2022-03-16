<script lang="ts" setup>
import { useContext } from "vite-ssr/vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { useStaff } from "~/composables/useApiHelper";
import { handleRequestError } from "~/composables/useErrorHandling";
import SortableTable, { Header } from "~/components/SortableTable.vue";
import { prettyDate } from "~/composables/useDate";

const ctx = useContext();
const i18n = useI18n();
const { params } = useRoute();
const staff = await useStaff().catch((e) => handleRequestError(e, ctx, i18n));
const headers = [
  { name: "pic", title: "", sortable: false },
  { name: "name", title: "Username", sortable: true },
  { name: "joinDate", title: "Joined", sortable: true },
] as Header[];
</script>

<template>
  <h1>Staff</h1>
  <SortableTable :headers="headers" :items="staff?.result">
    <!-- todo avatar -->
    <template #item_pic="{ item }">pic {{ item.name }}</template>
    <template #item_joinDate="{ item }">{{ prettyDate(item.joinDate) }}</template>
  </SortableTable>
</template>
