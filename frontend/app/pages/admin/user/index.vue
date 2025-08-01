<script lang="ts" setup>
import type { Header } from "#shared/types/components/SortableTable";

definePageMeta({
  globalPermsRequired: ["EditAllUserSettings"],
});

const i18n = useI18n();
const route = useRoute("admin-user");

const headers = [
  { name: "pic", title: "", sortable: false },
  { name: "name", title: i18n.t("pages.headers.username"), sortable: true },
  { name: "roles", title: i18n.t("pages.headers.roles"), sortable: true },
  { name: "createdAt", title: i18n.t("pages.headers.joined"), sortable: true },
  { name: "projectCount", title: i18n.t("pages.headers.projects"), sortable: true },
  { name: "locked", title: i18n.t("pages.headers.locked"), sortable: true },
  { name: "org", title: i18n.t("pages.headers.organization"), sortable: true },
] as const satisfies Header<string>[];

const page = ref(0);
const sort = ref<string[]>([]);
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
const { users } = useUsers(() => requestParams.value);

function updateSort(col: string, sorter: Record<string, number>) {
  sort.value = Object.keys(sorter)
    .map((k) => {
      const val = sorter[k];
      if (val === -1) return "-" + k;
      if (val === 1) return k;
      return;
    })
    .filter((v) => v !== undefined) as string[];
}

useSeo(computed(() => ({ title: i18n.t("userList.title"), route })));
</script>

<template>
  <div>
    <PageTitle>{{ i18n.t("userList.title") }}</PageTitle>

    <div class="mb-4">
      <InputText v-model="query" label="Username" />
    </div>

    <SortableTable
      :headers="headers"
      :items="users?.result || []"
      :server-pagination="users?.pagination"
      @update:sort="updateSort"
      @update:page="(newPage) => (page = newPage)"
    >
      <template #pic="{ item }">
        <UserAvatar :username="item.name" :avatar-url="item.avatarUrl" size="xs" />
      </template>
      <template #createdAt="{ item }">{{ i18n.d(item?.createdAt, "date") }}</template>
      <template #name="{ item }">
        <Link :to="'/' + item.name">{{ item.name }}</Link>
      </template>
      <template #locked="{ item }">
        <InputCheckbox disabled :model-value="item.locked" />
      </template>
      <template #org="{ item }">
        <InputCheckbox disabled :model-value="item.isOrganization" />
      </template>
      <template #roles="{ item }">
        <div class="space-x-1">
          <Tag v-for="roleId in item.roles" :key="roleId" :color="{ background: getRole(roleId)?.color }" :name="getRole(roleId)?.title" />
        </div>
      </template>
    </SortableTable>
  </div>
</template>
