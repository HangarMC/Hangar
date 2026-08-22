<script lang="ts" setup>
import type { AxiosError } from "axios";
import type { Header } from "#shared/types/components/SortableTable";
import type { OrganizationRoleTable } from "#shared/types/backend";
import { NamedPermission } from "#shared/types/backend";

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
  { title: i18n.t("userAdmin.visibility"), name: "visibility" },
] as const satisfies Header<string>[];

const orgConfig = [
  { title: i18n.t("userAdmin.organization"), name: "name" },
  { title: i18n.t("userAdmin.owner"), name: "owner" },
  { title: i18n.t("userAdmin.role"), name: "role" },
  { title: i18n.t("userAdmin.accepted"), name: "accepted" },
] as const satisfies Header<string>[];

const orgList = computed(() => (orgs ? Object.keys(orgs).map((name) => ({ name })) : []));

const selectedRole = ref();
const roles = computed(() => displayRoles(user.value?.roles));
const assignableRoles = computed(() =>
  useBackendData.globalRoles.filter((r) => r.value !== ORGANIZATION_ROLE && !user.value?.roles.some((roleId) => getRole(roleId)?.value === r.value))
);

async function processRole(role: string | undefined, add: boolean) {
  if (!role) return;
  try {
    await useInternalApi("admin/user/" + route.params.user + "/" + role, add ? "POST" : "DELETE");
    selectedRole.value = undefined;
    refreshUser();
  } catch (err) {
    handleRequestError(err as AxiosError);
  }
}

const newUsername = ref("");
const renaming = ref(false);
const renameModal = useTemplateRef("renameModal");

async function rename() {
  renaming.value = true;
  try {
    await useInternalApi(`admin/user/${route.params.user}/rename`, "POST", { content: newUsername.value });
    renameModal.value?.close();
    useNotificationStore().success(i18n.t("userAdmin.renamed", [newUsername.value]));
    await navigateTo("/admin/user/" + newUsername.value);
  } catch (err) {
    handleRequestError(err as AxiosError);
  } finally {
    renaming.value = false;
  }
}

function visibilityTitle(visibility: string) {
  const value = useBackendData.visibilities.find((v) => v.name === visibility);
  return value ? i18n.t(value.title) : visibility;
}

useSeo(computed(() => ({ title: i18n.t("userAdmin.title") + " " + route.params.user, route })));
</script>

<template>
  <div>
    <div class="mb-5 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
      <h1 class="text-3xl font-bold">{{ i18n.t("userAdmin.title") }}</h1>
      <div class="flex flex-wrap gap-2">
        <Button variant="outline" tone="neutral" :to="'/' + route.params.user">
          <IconMdiAccount />
          {{ i18n.t("userActivity.viewProfile") }}
        </Button>
        <Button v-if="hasPerms(NamedPermission.Reviewer)" variant="outline" tone="neutral" :to="'/admin/activities/' + route.params.user">
          <IconMdiCalendar />
          {{ i18n.t("author.tooltips.activity") }}
        </Button>
      </div>
    </div>

    <Card class="mb-4">
      <div class="flex flex-wrap items-center gap-3">
        <UserAvatar :username="route.params.user" :avatar-url="user?.avatarUrl" size="sm" disable-link />
        <div class="min-w-0 flex-1">
          <NuxtLink :to="'/' + route.params.user" class="text-lg font-bold">{{ route.params.user }}</NuxtLink>
          <div class="text-sm text-gray-secondary">
            <template v-if="user">
              {{ i18n.t(user.isOrganization ? "author.createdOn" : "author.memberSince", [i18n.d(user.createdAt, "date")]) }} &middot;
              {{ i18n.t("author.numProjects", [user.projectCount], user.projectCount) }}
            </template>
          </div>
        </div>
        <Modal ref="renameModal" :title="i18n.t('userAdmin.renameTitle')" @open="newUsername = route.params.user">
          <template #activator="{ on }">
            <Button variant="outline" tone="neutral" size="sm" v-on="on">
              <IconMdiRenameOutline />
              {{ i18n.t("userAdmin.rename") }}
            </Button>
          </template>
          <p class="mb-3 text-gray-secondary">{{ i18n.t("userAdmin.renameHint") }}</p>
          <InputText v-model.trim="newUsername" :label="i18n.t('userAdmin.newUsername')" :rules="[required()]" />
          <template #footer="{ on }">
            <Button variant="ghost" tone="neutral" v-on="on">{{ i18n.t("general.cancel") }}</Button>
            <Button :disabled="!newUsername || newUsername === route.params.user" :loading="renaming" @click="rename">
              {{ i18n.t("userAdmin.rename") }}
            </Button>
          </template>
        </Modal>
      </div>

      <hr class="my-4 border-gray-300 dark:border-gray-700" />

      <h2 class="mb-2 font-bold">{{ i18n.t("userAdmin.roles") }}</h2>
      <div v-if="roles.length > 0" class="flex flex-wrap items-center gap-1.5">
        <span v-for="role in roles" :key="role.roleId" class="inline-flex items-center gap-1 rounded background-card py-0.5 pl-0.5 pr-1">
          <Tag :color="{ background: role.color }" :name="role.title" />
          <Button
            variant="ghost"
            tone="danger"
            size="sm"
            icon-only
            class="!h-5 !w-5"
            :title="i18n.t('general.delete')"
            :aria-label="i18n.t('general.delete')"
            @click="processRole(role.value, false)"
          >
            <IconMdiClose />
          </Button>
        </span>
      </div>
      <p v-else class="text-sm text-gray-secondary">{{ i18n.t("userAdmin.noRoles") }}</p>

      <div class="mt-3 flex flex-wrap items-center gap-2">
        <InputDropdown
          v-model="selectedRole"
          :values="assignableRoles"
          item-text="title"
          item-value="value"
          button-size="md"
          :placeholder="i18n.t('userAdmin.selectRole')"
        />
        <Button :disabled="!selectedRole" @click="processRole(selectedRole, true)">
          <IconMdiPlus />
          {{ i18n.t("general.add") }}
        </Button>
      </div>
    </Card>

    <Card class="mb-4" padding="none">
      <div class="flex items-center gap-2 border-b border-gray-300 px-4 py-3 dark:border-gray-700">
        <h2 class="flex-grow text-lg font-bold">{{ i18n.t("userAdmin.organizations") }}</h2>
        <span class="text-sm text-gray-secondary tabular-nums">{{ orgList.length }}</span>
      </div>
      <SortableTable :items="orgList" :headers="orgConfig">
        <template #empty>{{ i18n.t("userAdmin.noOrganizations") }}</template>
        <template #name="{ item }">
          <Link :to="'/' + item.name">{{ item.name }}</Link>
        </template>
        <template #owner="{ item }">
          <Link :to="'/' + orgs[item.name]?.ownerName">{{ orgs[item.name]?.ownerName }}</Link>
        </template>
        <template #role="{ item }">
          {{ orgs[item.name]?.title }}
        </template>
        <template #accepted="{ item }">
          <IconMdiCheck v-if="orgs[item.name]?.accepted" class="text-lime-500" />
          <IconMdiClose v-else class="text-gray-secondary" />
        </template>
      </SortableTable>
    </Card>

    <Card padding="none">
      <div class="flex items-center gap-2 border-b border-gray-300 px-4 py-3 dark:border-gray-700">
        <h2 class="flex-grow text-lg font-bold">{{ i18n.t("userAdmin.projects") }}</h2>
        <span class="text-sm text-gray-secondary tabular-nums">{{ projects?.result?.length ?? 0 }}</span>
      </div>
      <SortableTable :items="projects?.result ?? []" :headers="projectsConfig">
        <template #empty>{{ i18n.t("userAdmin.noProjects") }}</template>
        <template #name="{ item }">
          <Link :to="'/' + item.namespace.owner + '/' + item.name">{{ item.name }}</Link>
        </template>
        <template #owner="{ item }">
          <Link :to="'/' + item.namespace.owner">{{ item.namespace.owner }}</Link>
        </template>
        <template #visibility="{ item }">
          <span class="inline-flex items-center gap-1.5">
            <IconMdiEye v-if="item.visibility === 'public'" class="flex-shrink-0 text-lime-500" />
            <IconMdiEyeOff v-else class="flex-shrink-0 text-amber-500" />
            {{ visibilityTitle(item.visibility) }}
          </span>
        </template>
      </SortableTable>
    </Card>
  </div>
</template>
