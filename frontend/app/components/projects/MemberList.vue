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

const sortedMembers = [...props.members].sort((r1, r2) => {
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
  <Card v-if="sortedMembers.length > 0 || canEdit" :class="props.class">
    <template #header>
      <div class="inline-flex w-full flex-cols space-between items-center">
        <h2>{{ i18n.t("project.members") }}</h2>
        <Tooltip v-if="canEdit" class="text-base font-normal">
          <template #content>
            {{ i18n.t("form.memberList.info") }}
          </template>
          <IconMdiHelpCircleOutline class="ml-1 text-gray-400" />
        </Tooltip>
        <div class="flex-grow" />
        <MemberLeaveModal v-if="canLeave && author" :author="author" :organization="organization" :slug="slug" />
      </div>
    </template>

    <div
      v-for="member in sortedMembers"
      :key="member.user.name"
      class="p-2 w-full border border-gray-100 dark:border-gray-800 rounded inline-flex flex-row space-x-4"
    >
      <UserAvatar :username="member.user.name" :avatar-url="member.user.avatarUrl" size="sm" class="flex-shrink-0" />
      <div class="flex-grow truncate">
        <p class="font-semibold">
          <Link :to="'/' + member.user.name">{{ member.user.name }}</Link>
        </p>
        <Tooltip v-if="!member.role.accepted">
          <template #content>
            {{ i18n.t("form.memberList.invitedAs", [getRole(member.role.roleId)?.title]) }}
          </template>
          <span class="items-center inline-flex"> {{ getRole(member.role.roleId)?.title }} <IconMdiClock class="ml-1" /> </span>
        </Tooltip>
        <span v-else class="items-center inline-flex"> {{ getRole(member.role.roleId)?.title }}</span>
      </div>
      <!-- todo confirmation modal -->
      <DropdownButton v-if="canEdit && getRole(member.role.roleId)?.assignable" :name="i18n.t('general.edit')">
        <template #button-label>
          <IconMdiPencil />
        </template>
        <DropdownItem v-for="role of filteredRoles(member.role.roleId)" :key="role.title" :disabled="saving" @click="setRole(member, role)">
          {{ role.title }}
        </DropdownItem>
        <hr />
        <DropdownItem @click="removeMember(member)">{{ i18n.t("form.memberList.remove") }}</DropdownItem>
      </DropdownButton>
      <DropdownButton v-if="canEdit && !getRole(member.role.roleId)?.assignable && !member.role.accepted" :name="i18n.t('general.edit')">
        <template #button-label>
          <IconMdiPencil />
        </template>
        <DropdownItem @click="cancelTransfer()">{{ i18n.t("form.memberList.cancelTransfer") }}</DropdownItem>
      </DropdownButton>
    </div>
    <div v-if="canEdit" class="items-center inline-flex mt-3 w-full">
      <InputAutocomplete
        id="membersearch"
        v-model="search"
        :values="result"
        :label="i18n.t('form.memberList.addUser')"
        :error-messages="addErrors"
        @search="doSearch"
      />
      <DropdownButton :name="i18n.t('general.add')" class="ml-2">
        <template #button-label>
          <IconMdiAccountPlus class="ml-1" />
        </template>
        <DropdownItem v-for="role of roles" :key="role.value" :disabled="saving" @click="invite(search, role)">
          {{ role.title }}
        </DropdownItem>
      </DropdownButton>
    </div>
  </Card>
</template>
