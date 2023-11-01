<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import type { User } from "hangar-api";
import { useRoute, useRouter } from "vue-router";
import type { OrganizationRoleTable } from "hangar-internal";
import { computed, ref } from "vue";
import { useHead } from "@unhead/vue";
import type { AxiosError } from "axios";
import PageTitle from "~/components/design/PageTitle.vue";
import Link from "~/components/design/Link.vue";
import Card from "~/components/design/Card.vue";
import { useApi, useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import SortableTable from "~/components/SortableTable.vue";
import { useSeo } from "~/composables/useSeo";
import { useProjects, useUser } from "~/composables/useApiHelper";
import Tag from "~/components/Tag.vue";
import InputSelect from "~/components/ui/InputSelect.vue";
import { getRole, useBackendData } from "~/store/backendData";
import Button from "~/components/design/Button.vue";
import { definePageMeta } from "#imports";
import type { Header } from "~/types/components/SortableTable";

definePageMeta({
  globalPermsRequired: ["EDIT_ALL_USER_SETTINGS"],
});

const i18n = useI18n();
const route = useRoute();
const router = useRouter();

const projects = await useProjects({ owner: route.params.user });
const orgs = await useInternalApi<{ [key: string]: OrganizationRoleTable }>(`organizations/${route.params.user}/userOrganizations`).catch((e) =>
  handleRequestError(e)
);
const user = await useUser(route.params.user as string);

const projectsConfig: Header[] = [
  { title: i18n.t("userAdmin.project"), name: "name" },
  { title: i18n.t("userAdmin.owner"), name: "owner" },
  { title: i18n.t("userAdmin.role"), name: "role" },
  { title: i18n.t("userAdmin.accepted"), name: "accepted" },
];

const orgConfig: Header[] = [
  { title: i18n.t("userAdmin.organization"), name: "name" },
  { title: i18n.t("userAdmin.owner"), name: "owner" },
  { title: i18n.t("userAdmin.role"), name: "role" },
  { title: i18n.t("userAdmin.accepted"), name: "accepted" },
];

const orgList = computed(() => {
  return orgs
    ? Object.keys(orgs).map((name) => {
        return { name };
      })
    : [];
});

const selectedRole = ref();
async function processRole(add: boolean) {
  try {
    await useInternalApi("admin/user/" + route.params.user + "/" + selectedRole.value, add ? "POST" : "DELETE");
    if (user?.value) {
      user.value = await useApi<User>(("users/" + route.params.user) as string);
    }
  } catch (e) {
    handleRequestError(e as AxiosError);
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
    <div class="flex lt-md:flex-col mb-2 gap-2">
      <Card class="basis-full">
        <template #header>{{ i18n.t("userAdmin.roles") }}</template>
        <div class="space-x-1">
          <Tag v-for="roleId in user?.roles" :key="roleId" :color="{ background: getRole(roleId).color }" :name="getRole(roleId).title" />
        </div>

        <div class="flex mt-2 items-center">
          <div class="flex-grow">
            <InputSelect
              v-model="selectedRole"
              :values="useBackendData.globalRoles.filter((r) => r.value !== 'Organization')"
              item-text="title"
              item-value="value"
            />
          </div>
          <Button size="medium" :disabled="!selectedRole || user?.roles.some((r) => getRole(r).value === selectedRole)" class="ml-1" @click="processRole(true)">
            {{ i18n.t("general.add") }}
          </Button>
          <Button
            size="medium"
            :disabled="!selectedRole || !user?.roles.some((r) => getRole(r).value === selectedRole)"
            class="ml-1"
            @click="processRole(false)"
          >
            {{ i18n.t("general.delete") }}
          </Button>
        </div>
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
          {{ getRole(orgs[item.name].roleId).title }}
        </template>
        <template #item_accepted="{ item }">
          <IconMdiCheck v-if="orgs[item.name].accepted" class="text-green" />
          <IconMdiClose v-else class="text-red" />
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
        <!-- todo -->
        <!--<template #item_role="{ item }">
          {{ item.name }}
        </template>-->
        <template #item_accepted="{ item }">
          <IconMdiCheck v-if="item.visibility === 'public'" class="text-green" />
          <IconMdiClose v-else class="text-red" />
        </template>
      </SortableTable>
    </Card>
  </div>
</template>
