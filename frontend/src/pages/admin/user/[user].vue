<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { PaginatedResult, Project, User } from "hangar-api";
import { useRoute, useRouter } from "vue-router";
import { OrganizationRoleTable } from "hangar-internal";
import { computed, ref } from "vue";
import { useHead } from "@vueuse/head";
import { AxiosError } from "axios";
import PageTitle from "~/lib/components/design/PageTitle.vue";
import Link from "~/lib/components/design/Link.vue";
import Card from "~/lib/components/design/Card.vue";
import { useApi, useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import SortableTable, { Header } from "~/components/SortableTable.vue";
import InputCheckbox from "~/lib/components/ui/InputCheckbox.vue";
import { useSeo } from "~/composables/useSeo";
import { authUrl, forumUserUrl } from "~/composables/useUrlHelper";
import { useUser } from "~/composables/useApiHelper";
import Tag from "~/components/Tag.vue";
import InputSelect from "~/lib/components/ui/InputSelect.vue";
import { useBackendDataStore } from "~/store/backendData";
import Button from "~/lib/components/design/Button.vue";
import { definePageMeta } from "#imports";

definePageMeta({
  globalPermsRequired: ["EDIT_ALL_USER_SETTINGS"],
});

const i18n = useI18n();
const route = useRoute();
const router = useRouter();
const backendData = useBackendDataStore();

const projects = await useApi<PaginatedResult<Project>>("projects", "get", {
  owner: route.params.user,
}).catch((e) => handleRequestError(e, i18n));
const orgs = await useInternalApi<{ [key: string]: OrganizationRoleTable }>(`organizations/${route.params.user}/userOrganizations`).catch((e) =>
  handleRequestError(e, i18n)
);
const user = await useUser(route.params.user as string).catch((e) => handleRequestError(e, i18n));

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

const selectedRole = ref();
async function processRole(add: boolean) {
  try {
    await useInternalApi("/admin/user/" + route.params.user + "/" + selectedRole.value, add ? "POST" : "DELETE");
    if (user) {
      user.value = await useApi<User>(("users/" + route.params.user) as string);
    }
  } catch (e) {
    handleRequestError(e as AxiosError, i18n);
  }
}

useHead(useSeo(i18n.t("userAdmin.title") + " " + route.params.user, null, route, null));
</script>

<template>
  <div>
    <PageTitle
      >{{ i18n.t("userAdmin.title") }}
      <Link :to="'/' + $route.params.user">
        {{ $route.params.user }}
      </Link>
    </PageTitle>
    <div class="flex <md:flex-col mb-2 gap-2">
      <Card class="basis-full md:basis-8/12">
        <template #header>{{ i18n.t("userAdmin.roles") }}</template>
        <div class="space-x-1">
          <Tag v-for="role in user.roles" :key="role.value" :color="{ background: role.color }" :name="role.title" />
        </div>

        <div class="flex mt-2">
          <div class="flex-grow">
            <InputSelect v-model="selectedRole" :values="backendData.globalRoles" item-text="title" item-value="value"></InputSelect>
          </div>
          <div>
            <Button size="medium" :disabled="!selectedRole || user.roles.some((r) => r.value === selectedRole)" @click="processRole(true)">
              {{ i18n.t("general.add") }}
            </Button>
          </div>
          <div class="ml-2">
            <Button size="medium" :disabled="!selectedRole || !user.roles.some((r) => r.value === selectedRole)" @click="processRole(false)">
              {{ i18n.t("general.delete") }}
            </Button>
          </div>
        </div>
      </Card>
      <Card class="basis-full md:basis-4/12">
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

    <Card md="mb-2">
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

      <SortableTable v-if="projects" :items="projects.result" :headers="projectsConfig">
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
  </div>
</template>
