<script lang="ts" setup>
import type { Header } from "~/types/components/SortableTable";

const i18n = useI18n();
const route = useRoute("authors");

const headers = [
  { name: "pic", title: "", sortable: false },
  { name: "name", title: i18n.t("pages.headers.username"), sortable: true },
  { name: "createdAt", title: i18n.t("pages.headers.joined"), sortable: true },
  { name: "projectCount", title: i18n.t("pages.headers.projects"), sortable: true },
] as const satisfies Header<string>[];

const page = ref(0);
const sort = ref<string[]>(["-projectCount"]);
const query = ref<string>();
const requestParams = computed(() => {
  const limit = 25;
  return {
    query: query.value,
    limit,
    offset: page.value * limit,
    sort: sort.value,
  };
});
const { authors } = useAuthors(() => requestParams.value);

async function updateSort(col: string, sorter: Record<string, number>) {
  sort.value = [...Object.keys(sorter)]
    .map((k) => {
      const val = sorter[k];
      if (val === -1) return "-" + k;
      if (val === 1) return k;
      return null;
    })
    .filter((v) => v !== null) as string[];
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
      :items="authors?.result || []"
      :server-pagination="authors?.pagination"
      :initial-sorter="{ projectCount: -1 }"
      @update:sort="updateSort"
      @update:page="(p) => (page = p)"
    >
      <template #pic="{ item }">
        <UserAvatar :username="item.name" :avatar-url="item.avatarUrl" size="xs"></UserAvatar>
      </template>
      <template #createdAt="{ item }">{{ i18n.d(item?.createdAt, "date") }}</template>
      <template #name="{ item }">
        <Link :to="'/' + item.name">{{ item.name }}</Link>
      </template>
      <template #empty>
        <div class="text-center">No authors found. 😢</div>
      </template>
    </SortableTable>
  </div>
</template>
