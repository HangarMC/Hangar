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
import UserAvatar from "~/components/UserAvatar.vue";
import Link from "~/components/design/Link.vue";

const ctx = useContext();
const i18n = useI18n();
const route = useRoute();
const staff = await useStaff().catch((e) => handleRequestError(e, ctx, i18n));
// TODO i18n
const headers = [
  { name: "pic", title: "", sortable: false },
  { name: "name", title: "Username", sortable: true },
  { name: "roles", title: "Roles", sortable: true },
  { name: "joinDate", title: "Joined", sortable: true },
] as Header[];

useHead(useSeo(i18n.t("pages.staffTitle"), null, route, null));
</script>

<template>
  <PageTitle>Staff</PageTitle>
  <SortableTable :headers="headers" :items="staff?.result">
    <template #item_pic="{ item }"><UserAvatar :username="item.name" size="xs"></UserAvatar></template>
    <template #item_joinDate="{ item }">{{ i18n.d(item.joinDate, "date") }}</template>
    <template #item_roles="{ item }">
      <span v-for="role in item.roles" :key="role.roleId" :style="{ backgroundColor: role.color }" class="rounded p-1 w-max items-end">{{ role.title }}</span>
    </template>
    <template #item_name="{ item }">
      <Link :to="'/' + item.name">{{ item.name }}</Link>
    </template>
  </SortableTable>
</template>
