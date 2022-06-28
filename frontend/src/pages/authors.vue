<script lang="ts" setup>
import { useContext } from "vite-ssr/vue";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { useAuthors } from "~/composables/useApiHelper";
import { handleRequestError } from "~/composables/useErrorHandling";
import SortableTable, { Header } from "~/components/SortableTable.vue";
import PageTitle from "~/lib/components/design/PageTitle.vue";
import UserAvatar from "~/components/UserAvatar.vue";
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";
import Link from "~/lib/components/design/Link.vue";

const ctx = useContext();
const i18n = useI18n();
const route = useRoute();
const authors = await useAuthors().catch((e) => handleRequestError(e, ctx, i18n));

const headers = [
  { name: "pic", title: "", sortable: false },
  { name: "name", title: i18n.t("pages.headers.username"), sortable: true },
  { name: "joinDate", title: i18n.t("pages.headers.joined"), sortable: true },
  { name: "projectCount", title: i18n.t("pages.headers.projects"), sortable: true },
] as Header[];

useHead(useSeo(i18n.t("pages.authorsTitle"), null, route, null));
</script>

<template>
  <PageTitle>Authors</PageTitle>
  <SortableTable :headers="headers" :items="authors?.result">
    <template #item_pic="{ item }"><UserAvatar :username="item.name" size="xs"></UserAvatar></template>
    <template #item_joinDate="{ item }">{{ i18n.d(item?.joinDate, "date") }}</template>
    <template #item_name="{ item }">
      <Link :to="'/' + item.name">{{ item.name }}</Link>
    </template>
  </SortableTable>
</template>
