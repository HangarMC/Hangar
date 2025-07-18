<script lang="ts" setup>
import type { AxiosError } from "axios";
import type { Header } from "#shared/types/components/SortableTable";
import type { OrganizationRoleTable } from "#shared/types/backend";

definePageMeta({
  globalPermsRequired: ["EditAllUserSettings"],
});

const i18n = useI18n();
const route = useRoute("admin-user-user");

const { projects } = useProjects(() => ({ owner: route.params.user }));
const orgs = (await useInternalApi<{ [key: string]: OrganizationRoleTable }>(`organizations/${route.params.user}/userOrganizations`).catch((err) =>
  handleRequestError(err)
)) as { [key: string]: OrganizationRoleTable };
const { user, refreshUser } = useUser(() => route.params.user);

const projectsConfig = [
  { title: i18n.t("userAdmin.project"), name: "name" },
  { title: i18n.t("userAdmin.owner"), name: "owner" },
  { title: i18n.t("userAdmin.role"), name: "role" },
  { title: i18n.t("userAdmin.accepted"), name: "accepted" },
] as const satisfies Header<string>[];

const orgConfig = [
  { title: i18n.t("userAdmin.organization"), name: "name" },
  { title: i18n.t("userAdmin.owner"), name: "owner" },
  { title: i18n.t("userAdmin.role"), name: "role" },
  { title: i18n.t("userAdmin.accepted"), name: "accepted" },
] as const satisfies Header<string>[];

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
    refreshUser();
  } catch (err) {
    handleRequestError(err as AxiosError);
  }
}

useSeo(computed(() => ({ title: i18n.t("userAdmin.title") + " " + route.params.user, route })));
</script>

<template>
  <div>
    <PageTitle
      >{{ i18n.t("userAdmin.title") }}
      <Link :to="'/' + route.params.user">
        {{ route.params.user }}
      </Link>
    </PageTitle>
    <div class="flex lt-md:flex-col mb-2 gap-2">
      <Card class="basis-full">
        <template #header>{{ i18n.t("userAdmin.roles") }}</template>
        <div class="space-x-1">
          <Tag v-for="roleId in user?.roles" :key="roleId" :color="{ background: getRole(roleId)?.color }" :name="getRole(roleId)?.title" />
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
          <Button
            size="medium"
            :disabled="!selectedRole || user?.roles.some((r) => getRole(r)?.value === selectedRole)"
            class="ml-1"
            @click="processRole(true)"
          >
            {{ i18n.t("general.add") }}
          </Button>
          <Button
            size="medium"
            :disabled="!selectedRole || !user?.roles.some((r) => getRole(r)?.value === selectedRole)"
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
        <template #name="{ item }">
          <Link :to="'/' + item.name">
            {{ item.name }}
          </Link>
        </template>
        <template #owner="{ item }">
          <Link :to="'/' + orgs[item.name]?.ownerName">
            {{ orgs[item.name]?.ownerName }}
          </Link>
        </template>
        <template #role="{ item }">
          {{ getRole(orgs[item.name]?.roleId)?.title }}
        </template>
        <template #accepted="{ item }">
          <IconMdiCheck v-if="orgs[item.name]?.accepted" class="text-green" />
          <IconMdiClose v-else class="text-red" />
        </template>
      </SortableTable>
    </Card>
    <Card md="col-start-1">
      <template #header>{{ i18n.t("userAdmin.projects") }}</template>

      <SortableTable v-if="projects" :items="projects.result" :headers="projectsConfig">
        <template #name="{ item }">
          <Link :to="'/' + item.namespace.owner + '/' + item.name">
            {{ item.name }}
          </Link>
        </template>
        <template #owner="{ item }">
          <Link :to="'/' + item.namespace.owner">
            {{ item.namespace.owner }}
          </Link>
        </template>
        <!-- todo -->
        <!--<template #role="{ item }">
          {{ item.name }}
        </template>-->
        <template #accepted="{ item }">
          <IconMdiCheck v-if="item.visibility === 'public'" class="text-green" />
          <IconMdiClose v-else class="text-red" />
        </template>
      </SortableTable>
    </Card>
  </div>
</template>
