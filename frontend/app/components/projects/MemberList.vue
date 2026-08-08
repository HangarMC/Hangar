<script setup lang="ts">
import { NamedPermission } from "#shared/types/backend";
import Card from "~/components/design/Card.vue";
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
    bare?: boolean;
    manage?: boolean;
    settingsLink?: string;
    title?: string;
    description?: string;
  }>(),
  {
    organization: false,
    class: "",
    author: undefined,
    slug: undefined,
    bare: false,
    manage: true,
    settingsLink: undefined,
    title: undefined,
    description: undefined,
  }
);

// unranked last, then by name -- a comparator that never returns 0 sorts unstably across engines
const sortedMembers = computed(() =>
  props.members.toSorted((r1, r2) => {
    const rank1 = getRole(r1.role.roleId)?.rank ?? Number.MAX_SAFE_INTEGER;
    const rank2 = getRole(r2.role.roleId)?.rank ?? Number.MAX_SAFE_INTEGER;
    return rank1 === rank2 ? r1.user.name.localeCompare(r2.user.name) : rank1 - rank2;
  })
);

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

const inviteRoleId = ref<number>();

function isPending(member: JoinableMemberProjectRoleTable | JoinableMemberOrganizationRoleTable): boolean {
  return !member.role.accepted;
}

function canEditRole(member: JoinableMemberProjectRoleTable | JoinableMemberOrganizationRoleTable): boolean {
  return props.manage && canEdit.value && (getRole(member.role.roleId)?.assignable ?? false);
}

function isPendingTransfer(member: JoinableMemberProjectRoleTable | JoinableMemberOrganizationRoleTable): boolean {
  return isPending(member) && !getRole(member.role.roleId)?.assignable;
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

function setRole(member: JoinableMemberProjectRoleTable | JoinableMemberOrganizationRoleTable, roleId?: number) {
  if (roleId === undefined || roleId === member.role.roleId) return;
  const editableMember: EditableMember = convertMember(member);
  editableMember.roleId = roleId;
  post(editableMember, "edit");
}

function invite() {
  if (!search.value || inviteRoleId.value === undefined) return;
  post({ name: search.value, roleId: inviteRoleId.value }, "add");
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
  <component :is="bare ? 'div' : Card" v-if="sortedMembers.length > 0 || canEdit" :class="props.class">
    <template v-if="!bare" #header>
      <div class="flex items-center gap-1">
        <h2>{{ i18n.t("project.members") }}</h2>
        <div class="flex-grow" />
        <MemberLeaveModal v-if="canLeave && author" :author="author" :organization="organization" :slug="slug" />
        <Button
          v-if="settingsLink && canEdit"
          :to="settingsLink"
          variant="ghost"
          tone="neutral"
          size="sm"
          icon-only
          :title="i18n.t('general.edit')"
          :aria-label="i18n.t('general.edit')"
        >
          <IconMdiPencil />
        </Button>
      </div>
    </template>

    <div v-if="bare && (title || (canLeave && author))" class="mb-3 flex items-start gap-3">
      <div class="min-w-0 flex-1">
        <h2 v-if="title" class="text-lg font-semibold">{{ i18n.t(title) }}</h2>
        <p v-if="description" class="mt-0.5 text-sm text-gray-secondary">{{ i18n.t(description) }}</p>
      </div>
      <div v-if="canLeave && author" class="flex-shrink-0">
        <MemberLeaveModal :author="author" :organization="organization" :slug="slug" />
      </div>
    </div>

    <ul class="divide-y divide-gray-300 rounded-md border border-gray-300 dark:divide-gray-700 dark:border-gray-700">
      <li v-for="member in sortedMembers" :key="member.user.name" class="flex flex-wrap items-center gap-x-3 gap-y-2 px-3 py-2">
        <UserAvatar :username="member.user.name" :avatar-url="member.user.avatarUrl" size="sm" class="flex-shrink-0" />

        <div class="min-w-30 flex-1">
          <p class="min-w-0 flex items-center gap-x-2">
            <Link :to="'/' + member.user.name" class="min-w-0 truncate font-semibold">{{ member.user.name }}</Link>
            <Chip
              v-if="isPending(member)"
              tone="amber"
              class="flex-shrink-0"
              :title="i18n.t('form.memberList.invitedAs', [getRole(member.role.roleId)?.title])"
            >
              <IconMdiClockOutline />
              {{ i18n.t("form.memberList.pending") }}
            </Chip>
          </p>
          <p v-if="!canEditRole(member)" class="text-sm text-gray-secondary">{{ getRole(member.role.roleId)?.title }}</p>
        </div>

        <DropdownButton
          v-if="canEditRole(member)"
          class="ml-auto flex-shrink-0"
          :name="getRole(member.role.roleId)?.title"
          button-variant="outline"
          button-tone="neutral"
          button-size="sm"
        >
          <template #default="{ close }">
            <DropdownItem
              v-for="role of roles"
              :key="role.roleId"
              :selected="role.roleId === member.role.roleId"
              :disabled="saving"
              @click="
                setRole(member, role.roleId);
                close();
              "
            >
              {{ role.title }}
            </DropdownItem>
          </template>
        </DropdownButton>

        <Button
          v-if="canEditRole(member)"
          class="flex-shrink-0"
          variant="ghost"
          tone="danger"
          size="sm"
          icon-only
          :disabled="saving"
          :title="isPending(member) ? i18n.t('form.memberList.cancelInvite') : i18n.t('form.memberList.remove')"
          :aria-label="isPending(member) ? i18n.t('form.memberList.cancelInvite') : i18n.t('form.memberList.remove')"
          @click="removeMember(member)"
        >
          <IconMdiAccountRemove />
        </Button>
        <Button
          v-else-if="manage && canEdit && isPendingTransfer(member)"
          variant="outline"
          tone="danger"
          size="sm"
          :disabled="saving"
          @click="cancelTransfer()"
        >
          {{ i18n.t("form.memberList.cancelTransfer") }}
        </Button>
      </li>
    </ul>

    <div v-if="canEdit && manage" class="mt-3 flex flex-wrap items-center gap-2">
      <div class="min-w-50 flex-1">
        <InputAutocomplete
          id="membersearch"
          v-model="search"
          :values="result"
          :label="i18n.t('form.memberList.addUser')"
          :error-messages="addErrors"
          @search="doSearch"
        />
      </div>
      <DropdownButton
        :name="inviteRoleId === undefined ? i18n.t('form.memberList.selectRole') : getRole(inviteRoleId)?.title"
        button-variant="outline"
        button-tone="neutral"
        button-size="lg"
      >
        <template #default="{ close }">
          <DropdownItem
            v-for="role of roles"
            :key="role.roleId"
            :selected="role.roleId === inviteRoleId"
            :disabled="saving"
            @click="
              inviteRoleId = role.roleId;
              close();
            "
          >
            {{ role.title }}
          </DropdownItem>
        </template>
      </DropdownButton>
      <Button size="lg" :disabled="!search || inviteRoleId === undefined || saving" @click="invite">
        <IconMdiAccountPlus />
        {{ i18n.t("form.memberList.invite") }}
      </Button>
    </div>
  </component>
</template>
