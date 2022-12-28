<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { useHead } from "@vueuse/head";
import { computed, ref } from "vue";
import { PaginatedResult, User } from "hangar-api";
import { useStaff } from "~/composables/useApiHelper";
import { handleRequestError } from "~/composables/useErrorHandling";
import SortableTable, { Header } from "~/components/SortableTable.vue";
import PageTitle from "~/lib/components/design/PageTitle.vue";
import { useSeo } from "~/composables/useSeo";
import UserAvatar from "~/components/UserAvatar.vue";
import Link from "~/lib/components/design/Link.vue";
import Tag from "~/components/Tag.vue";
import { useApi } from "~/composables/useApi";

const i18n = useI18n();
const route = useRoute();

const headers = [
  { name: "pic", title: "", sortable: false },
  { name: "name", title: i18n.t("pages.headers.username"), sortable: true },
  { name: "roles", title: i18n.t("pages.headers.roles"), sortable: true },
  { name: "joinDate", title: i18n.t("pages.headers.joined"), sortable: true },
] as Header[];

const page = ref(0);
const sort = ref<string[]>(["roles"]);
const requestParams = computed(() => {
  const limit = 25;
  return {
    limit,
    offset: page.value * limit,
    sort: sort.value,
  };
});
const staff = await useStaff(requestParams.value);

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
  staff.value = await useApi<PaginatedResult<User>>("staff", "GET", requestParams.value);
}

useHead(useSeo(i18n.t("pages.staffTitle"), null, route, null));
</script>

<template>
  <div>
    <PageTitle>Staff</PageTitle>
    <SortableTable
      :headers="headers"
      :items="staff?.result"
      :server-pagination="staff?.pagination"
      :initial-sorter="{ roles: 1 }"
      @update:sort="updateSort"
      @update:page="updatePage"
    >
      <template #item_pic="{ item }"><UserAvatar :username="item.name" size="xs"></UserAvatar></template>
      <template #item_joinDate="{ item }">{{ i18n.d(item.joinDate, "date") }}</template>
      <template #item_roles="{ item }">
        <div class="space-x-1">
          <Tag v-for="role in item.roles" :key="role.roleId" :color="{ background: role.color }" :name="role.title" />
        </div>
      </template>
      <template #item_name="{ item }">
        <Link :to="'/' + item.name">{{ item.name }}</Link>
      </template>
    </SortableTable>
  </div>
</template>
