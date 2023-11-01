<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { useHead } from "@unhead/vue";
import type { PaginatedResult, User } from "hangar-api";
import { computed, ref } from "vue";
import { useAuthors } from "~/composables/useApiHelper";
import SortableTable from "~/components/SortableTable.vue";
import PageTitle from "~/components/design/PageTitle.vue";
import UserAvatar from "~/components/UserAvatar.vue";
import { useSeo } from "~/composables/useSeo";
import Link from "~/components/design/Link.vue";
import { useApi } from "~/composables/useApi";
import type { Header } from "~/types/components/SortableTable";
import { watch } from "#imports";
import InputText from "~/components/ui/InputText.vue";
const i18n = useI18n();
const route = useRoute();

const headers: Header[] = [
  { name: "pic", title: "", sortable: false },
  { name: "name", title: i18n.t("pages.headers.username"), sortable: true },
  { name: "createdAt", title: i18n.t("pages.headers.joined"), sortable: true },
  { name: "projectCount", title: i18n.t("pages.headers.projects"), sortable: true },
];

const page = ref(0);
const sort = ref<string[]>(["-projectCount"]);
const query = ref();
const requestParams = computed(() => {
  const limit = 25;
  return {
    query: query.value,
    limit,
    offset: page.value * limit,
    sort: sort.value,
  };
});
watch(query, update);
const authors = await useAuthors(requestParams.value);

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
  authors.value = await useApi<PaginatedResult<User>>("authors", "GET", requestParams.value);
}

useHead(useSeo(i18n.t("pages.authorsTitle"), "Hangar Project Authors", route, null));
</script>

<template>
  <div>
    <PageTitle>Authors</PageTitle>

    <div class="mb-4">
      <InputText v-model="query" label="Username" />
    </div>

    <SortableTable
      :headers="headers"
      :items="authors?.result"
      :server-pagination="authors?.pagination"
      :initial-sorter="{ projectCount: -1 }"
      @update:sort="updateSort"
      @update:page="updatePage"
    >
      <template #item_pic="{ item }"><UserAvatar :username="item.name" :avatar-url="item.avatarUrl" size="xs"></UserAvatar></template>
      <template #item_createdAt="{ item }">{{ i18n.d(item?.createdAt, "date") }}</template>
      <template #item_name="{ item }">
        <Link :to="'/' + item.name">{{ item.name }}</Link>
      </template>
    </SortableTable>
  </div>
</template>
