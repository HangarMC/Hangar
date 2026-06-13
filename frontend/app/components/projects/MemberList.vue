<script setup lang="ts">
import { NamedPermission } from "#shared/types/backend";
import type { JoinableMemberOrganizationRoleTable, JoinableMemberProjectRoleTable, PaginatedResultUser, RoleData } from "#shared/types/backend";

interface EditableMember {
  name: string;
  roleId: number;
}

const props = withDefaults(
  defineProps<{
    members: JoinableMemberProjectRoleTable[] | JoinableMemberOrganizationRoleTable[];
    disableSaving?: boolean;
    class?: string;
    organization?: boolean;
    author?: string;
    slug?: string;
  }>(),
  {
    organization: false,
    class: "",
    author: undefined,
    slug: undefined,
  }
);

const sortedMembers = props.members.toSorted((r1, r2) => {
  const role1 = getRole(r1.role.roleId);
  const role2 = getRole(r2.role.roleId);
  if (role1?.rank) {
    if (role2?.rank) {
      return role1.rank - role2.rank;
    }
    return 1;
  }
  return role2?.rank ? -1 : 1;
});

const i18n = useI18n();
const router = useRouter();
const authStore = useAuthStore();
const roles: RoleData[] = (props.organization ? useBackendData.orgRoles : useBackendData.projectRoles).filter((role) => role.assignable);

const canLeave = computed<boolean>(() => {
  if (!authStore.user) {
    return false;
  }

  return props.members.some((member) => member.user.id === authStore.user?.id && member.user.name !== props.author);
});
const canEdit = computed<boolean>(() => hasPerms(NamedPermission.EditSubjectSettings));
const saving = ref<boolean>(false);
const search = ref<string>("");
const addErrors = ref<string[]>([]);
const result = ref<string[]>([]);

watch(search, () => {
  addErrors.value = [];
});

function filteredRoles(currentRole: number): RoleData[] {
  return roles.filter((r) => r.roleId !== currentRole);
}

function removeMember(member: JoinableMemberProjectRoleTable | JoinableMemberOrganizationRoleTable) {
  post(convertMember(member), "remove");
}

function cancelTransfer() {
  if (saving.value) {
    return;
  }

  saving.value = true;
  const url = props.organization ? `organizations/org/${props.author}/canceltransfer` : `projects/project/${props.slug}/canceltransfer`;
  useInternalApi(url, "post")
    .then(() => router.go(0))
    .catch((err) => handleRequestError(err))
    .finally(() => (saving.value = false));
}

function setRole(member: JoinableMemberProjectRoleTable | JoinableMemberOrganizationRoleTable, role: RoleData) {
  const editableMember: EditableMember = convertMember(member);
  editableMember.roleId = role.roleId;
  post(editableMember, "edit");
}

function invite(member: string, role: RoleData) {
  const editableMember: EditableMember = { name: member, roleId: role.roleId };
  post(editableMember, "add");
  return "";
}

function post(member: EditableMember, action: "edit" | "add" | "remove") {
  addErrors.value = [];
  if (member.name.length === 0) {
    addErrors.value.push(i18n.t("general.error.nameEmpty"));
    return;
  }
  if (saving.value) {
    return;
  }

  saving.value = true;
  const url = props.organization ? `organizations/org/${props.author}/members/${action}` : `projects/project/${props.slug}/members/${action}`;
  useInternalApi(url, "post", member)
    .then(() => {
      router.go(0);
    })
    .catch((err) => {
      handleRequestError(err);
      // addErrors.value.push(i18n.te(e.response?.data.message) ? i18n.t(e.response?.data.message, e.response?.data.messageArgs) : e.response?.data.message);
    })
    .finally(() => {
      saving.value = false;
    });
}

function convertMember(member: JoinableMemberProjectRoleTable | JoinableMemberOrganizationRoleTable): EditableMember {
  return {
    name: member.user.name,
    roleId: member.role.roleId,
  };
}

async function doSearch(val?: string) {
  result.value = [];
  const users = await useApi<PaginatedResultUser>("users", "get", {
    query: val,
    limit: 25,
    offset: 0,
  });
  result.value = users.result.filter((u) => !props.members.some((m) => m.user.name === u.name)).map((u) => u.name);
}
</script>

<template>
  <Card v-if="sortedMembers.length > 0 || canEdit" :class="'p-0! overflow-hidden ' + props.class">
    <template #header>
      <div class="flex w-full items-center gap-2 px-4 pt-3.5 pb-1">
        <h2>{{ i18n.t("project.members") }}</h2>
        <Tooltip v-if="canEdit" class="text-base font-normal">
          <template #content>
            {{ i18n.t("form.memberList.info") }}
          </template>
          <IconMdiHelpCircleOutline class="text-gray-400" />
        </Tooltip>
        <div class="grow" />
        <MemberLeaveModal v-if="canLeave && author" :author="author" :organization="organization" :slug="slug" />
      </div>
    </template>

    <div class="flex flex-col gap-2 px-3 pt-1 pb-3">
      <div
        v-for="member in sortedMembers"
        :key="member.user.name"
        class="inline-flex w-full items-center rounded-lg border border-gray-200 bg-gray-100/60 transition-colors hover:border-gray-300 dark:border-gray-800 dark:bg-charcoal-500/60 dark:hover:border-gray-700"
      >
        <NuxtLink
          :to="'/' + member.user.name"
          class="group flex min-w-0 grow items-center gap-3 rounded-lg p-3 decoration-none focus-visible:outline-2 focus-visible:outline-primary"
        >
          <UserAvatar :username="member.user.name" :avatar-url="member.user.avatarUrl" size="sm" disable-link class="shrink-0" />
          <div class="min-w-0 grow">
            <Tooltip v-if="!member.role.accepted" class="mb-1 text-xs">
              <template #content>
                {{ i18n.t("form.memberList.invitedAs", [getRole(member.role.roleId)?.title]) }}
              </template>
              <span
                class="inline-flex items-center gap-1 rounded-md border border-gray-300 bg-gray-200 px-2 py-0.5 text-xs font-medium text-gray dark:border-gray-600 dark:bg-gray-700 dark:text-gray-300"
              >
                {{ getRole(member.role.roleId)?.title }}
                <IconMdiClock />
              </span>
            </Tooltip>
            <span
              v-else
              class="background-default mb-1 inline-flex items-center rounded-md border border-primary-400 px-2 py-0.5 text-xs font-medium color-primary"
            >
              {{ getRole(member.role.roleId)?.title }}
            </span>
            <p class="truncate font-semibold leading-tight transition-colors group-hover:color-primary">
              {{ member.user.name }}
            </p>
          </div>
        </NuxtLink>
        <!-- todo confirmation modal -->
        <DropdownButton
          v-if="canEdit && getRole(member.role.roleId)?.assignable"
          :name="i18n.t('general.edit')"
          class="mr-2.5"
          :button-arrow="false"
          button-class="!w-10.5 !h-10.5 !p-0"
        >
          <template #button-label>
            <IconMdiPencil />
          </template>
          <DropdownItem v-for="role of filteredRoles(member.role.roleId)" :key="role.title" :disabled="saving" @click="setRole(member, role)">
            {{ role.title }}
          </DropdownItem>
          <hr class="my-1 border-t border-gray-700/40 dark:border-gray-700/40" />
          <DropdownItem @click="removeMember(member)">{{ i18n.t("form.memberList.remove") }}</DropdownItem>
        </DropdownButton>
        <DropdownButton v-if="canEdit && !getRole(member.role.roleId)?.assignable && !member.role.accepted" :name="i18n.t('general.edit')" class="mr-2.5">
          <template #button-label>
            <IconMdiPencil />
          </template>
          <DropdownItem @click="cancelTransfer()">{{ i18n.t("form.memberList.cancelTransfer") }}</DropdownItem>
        </DropdownButton>
      </div>
    </div>
    <div v-if="canEdit" class="flex w-full items-start bg-transparent px-3 pb-3">
      <div class="min-w-0 grow">
        <InputAutocomplete
          id="membersearch"
          v-model="search"
          :values="result"
          :label="i18n.t('form.memberList.addUser')"
          :error-messages="addErrors"
          no-error-tooltip
          @search="doSearch"
        />
      </div>
      <DropdownButton :name="i18n.t('general.add')" class="ml-2" :button-arrow="false" button-class="!h-10.5 !w-10.5 !p-0">
        <template #button-label>
          <IconMdiAccountPlus />
        </template>
        <DropdownItem v-for="role of roles" :key="role.value" :disabled="saving" @click="invite(search, role)">
          {{ role.title }}
        </DropdownItem>
      </DropdownButton>
    </div>
  </Card>
</template>
