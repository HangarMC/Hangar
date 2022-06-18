<script lang="ts" setup>
import PageTitle from "~/components/design/PageTitle.vue";
import { useI18n } from "vue-i18n";
import Link from "~/components/design/Link.vue";
import Card from "~/components/design/Card.vue";
import { useApi, useInternalApi } from "~/composables/useApi";
import { PaginatedResult, Project } from "hangar-api";
import { useRoute } from "vue-router";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useContext } from "vite-ssr/vue";
import { OrganizationRoleTable } from "hangar-internal";
import { computed } from "vue";
import SortableTable, { Header } from "~/components/SortableTable.vue";
import InputCheckbox from "~/components/ui/InputCheckbox.vue";
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";
import { authUrl, forumUserUrl } from "~/composables/useUrlHelper";

const i18n = useI18n();
const route = useRoute();
const ctx = useContext();

const projects = await useApi<PaginatedResult<Project>>("projects", false, "get", {
  owner: route.params.user,
}).catch((e) => handleRequestError(e, ctx, i18n));
const orgs = await useInternalApi<{ [key: string]: OrganizationRoleTable }>(`organizations/${route.params.user}/userOrganizations`, false).catch((e) =>
  handleRequestError(e, ctx, i18n)
);

const projectsConfig = [
  { title: i18n.t("userAdmin.project"), name: "name" },
  { title: i18n.t("userAdmin.owner"), name: "owner" },
  { title: i18n.t("userAdmin.role"), name: "role" },
  { title: i18n.t("userAdmin.accepted"), name: "accepted" },
] as Header[];

const orgConfig = [
  { title: i18n.t("userAdmin.organization"), name: "name" },
  { title: i18n.t("userAdmin.owner"), name: "owner" },
  { title: i18n.t("userAdmin.role"), name: "role" },
  { title: i18n.t("userAdmin.accepted"), name: "accepted" },
] as Header[];

const orgList = computed(() => {
  return orgs
    ? Object.keys(orgs).map((name) => {
        return { name };
      })
    : [];
});

const _forumUserUrl = computed(() => forumUserUrl(route.params.user as string));
const _authUrl = computed(() => authUrl(route.params.user as string));

useHead(useSeo(i18n.t("userAdmin.title") + " " + route.params.user, null, route, null));
</script>

<template>
  <PageTitle
    >{{ i18n.t("userAdmin.title") }}
    <Link :to="'/' + $route.params.user">
      {{ $route.params.user }}
    </Link>
  </PageTitle>
  <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
    <Card md="col-start-1">
      <template #header>{{ i18n.t("userAdmin.organizations") }}</template>

      <SortableTable :items="orgList" :headers="orgConfig">
        <template #item_name="{ item }">
          <Link :to="'/' + item.name">
            {{ item.name }}
          </Link>
        </template>
        <template #item_owner="{ item }">
          <Link :to="'/' + orgs[item.name].ownerName">
            {{ orgs[item.name].ownerName }}
          </Link>
        </template>
        <template #item_role="{ item }">
          {{ orgs[item.name].role.title }}
        </template>
        <template #item_accepted="{ item }">
          <InputCheckbox v-model="orgs[item.name].accepted" :disabled="true" />
        </template>
      </SortableTable>
    </Card>
    <Card md="col-start-1">
      <template #header>{{ i18n.t("userAdmin.projects") }}</template>

      <SortableTable :items="projects.result" :headers="projectsConfig">
        <template #item_name="{ item }">
          <Link :to="'/' + item.namespace.owner + '/' + item.name">
            {{ item.name }}
          </Link>
        </template>
        <template #item_owner="{ item }">
          <Link :to="'/' + item.namespace.owner">
            {{ item.namespace.owner }}
          </Link>
        </template>
        <template #item_role="{ item }">
          <!-- todo add role -->
          &lt;{{ item.name }}'s role&gt;
        </template>
        <template #item_accepted="{ item }">
          <InputCheckbox :model-value="item.visibility === 'public'" :disabled="true" />
        </template>
      </SortableTable>
    </Card>
    <Card md="col-start-2 row-start-1 row-end-2">
      <template #header>{{ i18n.t("userAdmin.sidebar") }}</template>

      <ul>
        <li>
          <Link :href="_authUrl">{{ i18n.t("userAdmin.hangarAuth") }}</Link>
        </li>
        <li>
          <Link :href="_forumUserUrl">{{ i18n.t("userAdmin.forum") }}</Link>
        </li>
      </ul>
    </Card>
  </div>
</template>

<route lang="yaml">
meta:
  requireGlobalPerm: ["EDIT_ALL_USER_SETTINGS"]
</route>
