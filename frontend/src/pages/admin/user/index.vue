<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { useRoute, useRouter } from "vue-router";
import { useHead } from "@unhead/vue";
import { computed, ref } from "vue";
import type { PaginatedResult, User } from "hangar-api";
import PageTitle from "~/components/design/PageTitle.vue";
import Link from "~/components/design/Link.vue";
import Tag from "~/components/Tag.vue";
import { useApi } from "~/composables/useApi";
import { useSeo } from "~/composables/useSeo";
import { definePageMeta, watch } from "#imports";
import { useUsers } from "~/composables/useApiHelper";
import InputCheckbox from "~/components/ui/InputCheckbox.vue";
import InputText from "~/components/ui/InputText.vue";
import type { Header } from "~/types/components/SortableTable";
import SortableTable from "~/components/SortableTable.vue";
import { getRole } from "~/store/backendData";

definePageMeta({
  globalPermsRequired: ["EDIT_ALL_USER_SETTINGS"],
});

const i18n = useI18n();
const route = useRoute();
const router = useRouter();

const headers: Header[] = [
  { name: "pic", title: "", sortable: false },
  { name: "name", title: i18n.t("pages.headers.username"), sortable: true },
  { name: "roles", title: i18n.t("pages.headers.roles"), sortable: true },
  { name: "createdAt", title: i18n.t("pages.headers.joined"), sortable: true },
  { name: "projectCount", title: i18n.t("pages.headers.projects"), sortable: true },
  { name: "locked", title: i18n.t("pages.headers.locked"), sortable: true },
  { name: "org", title: i18n.t("pages.headers.organization"), sortable: true },
];

const users = await useUsers();
const page = ref(0);
const sort = ref<string[]>([]);
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
  users.value = await useApi<PaginatedResult<User>>("users", "GET", requestParams.value);
}

useHead(useSeo(i18n.t("userList.title"), null, route, null));
</script>

<template>
  <div>
    <PageTitle>{{ i18n.t("userList.title") }}</PageTitle>

    <div class="mb-4">
      <InputText v-model="query" label="Username" />
    </div>

    <SortableTable :headers="headers" :items="users?.result" :server-pagination="users?.pagination" @update:sort="updateSort" @update:page="updatePage">
      <template #item_pic="{ item }">
        <UserAvatar :username="item.name" :avatar-url="item.avatarUrl" size="xs" />
      </template>
      <template #item_createdAt="{ item }">{{ i18n.d(item?.createdAt, "date") }}</template>
      <template #item_name="{ item }">
        <Link :to="'/' + item.name">{{ item.name }}</Link>
      </template>
      <template #item_locked="{ item }">
        <InputCheckbox disabled :model-value="item.locked" />
      </template>
      <template #item_org="{ item }">
        <InputCheckbox disabled :model-value="item.isOrganization" />
      </template>
      <template #item_roles="{ item }">
        <div class="space-x-1">
          <Tag v-for="roleId in item.roles" :key="roleId" :color="{ background: getRole(roleId).color }" :name="getRole(roleId).title" />
        </div>
      </template>
    </SortableTable>
  </div>
</template>
