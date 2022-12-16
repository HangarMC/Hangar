<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { useHead } from "@vueuse/head";
import { PaginatedResult, User } from "hangar-api";
import { computed, ref } from "vue";
import { useAuthors } from "~/composables/useApiHelper";
import { handleRequestError } from "~/composables/useErrorHandling";
import SortableTable, { Header } from "~/components/SortableTable.vue";
import PageTitle from "~/lib/components/design/PageTitle.vue";
import UserAvatar from "~/components/UserAvatar.vue";
import { useSeo } from "~/composables/useSeo";
import Link from "~/lib/components/design/Link.vue";
import { useApi } from "~/composables/useApi";
const i18n = useI18n();
const route = useRoute();
const authors = await useAuthors().catch((e) => handleRequestError(e, i18n));

const headers = [
  { name: "pic", title: "", sortable: false },
  { name: "name", title: i18n.t("pages.headers.username"), sortable: true },
  { name: "joinDate", title: i18n.t("pages.headers.joined"), sortable: true },
  { name: "projectCount", title: i18n.t("pages.headers.projects"), sortable: true },
] as Header[];

const page = ref(0);
const sort = ref<string[]>([]);
const requestParams = computed(() => {
  const limit = 25;
  return {
    limit,
    offset: page.value * limit,
    sort: sort.value,
  };
});

async function updateSort(col: string, sorter: Record<string, number>) {
  sort.value = [...Object.keys(sorter)]
    .map((k) => {
      const val = sorter[k];
      if (val === -1) return "-" + k;
      if (val === 1) return k;
      return null;
    })
    .filter((v) => v !== null) as string[];

  await update();
}

async function updatePage(newPage: number) {
  page.value = newPage;
  await update();
}

async function update() {
  authors.value = await useApi<PaginatedResult<User>>("authors", false, "GET", requestParams.value);
}

useHead(useSeo(i18n.t("pages.authorsTitle"), "Hangar Project Authors", route, null));
</script>

<template>
  <div>
    <PageTitle>Authors</PageTitle>
    <SortableTable :headers="headers" :items="authors?.result" :server-pagination="authors?.pagination" @update:sort="updateSort" @update:page="updatePage">
      <template #item_pic="{ item }"><UserAvatar :username="item.name" size="xs"></UserAvatar></template>
      <template #item_joinDate="{ item }">{{ i18n.d(item?.joinDate, "date") }}</template>
      <template #item_name="{ item }">
        <Link :to="'/' + item.name">{{ item.name }}</Link>
      </template>
    </SortableTable>
  </div>
</template>
