<script setup lang="ts">
import { NamedPermission } from "#shared/types/backend";
import Card from "~/components/design/Card.vue";
import type { JoinableMemberOrganizationRoleTable, JoinableMemberProjectRoleTable, PermissionGroup } from "#shared/types/backend";

type Member = JoinableMemberProjectRoleTable | JoinableMemberOrganizationRoleTable;

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

const i18n = useI18n();
const router = useRouter();
const authStore = useAuthStore();

const groups: PermissionGroup[] = (props.organization ? useBackendData.organizationPermissions : useBackendData.projectPermissions) ?? [];
const defaultInvitePermissions = props.organization
  ? [NamedPermission.CreateProject, NamedPermission.CreateVersion]
  : [NamedPermission.CreateVersion, NamedPermission.EditPage];
const grantedCount = (member: Member) => member.role.permissions.filter((p) => p !== NamedPermission.IsSubjectMember).length;

const sortedMembers = computed(() =>
  props.members.toSorted((r1, r2) => {
    if (r1.role.owner !== r2.role.owner) return r1.role.owner ? -1 : 1;
    const diff = grantedCount(r2) - grantedCount(r1);
    return diff === 0 ? r1.user.name.localeCompare(r2.user.name) : diff;
  })
);

const canEdit = computed<boolean>(() => hasPerms(NamedPermission.EditSubjectSettings));
const canLeave = computed<boolean>(() => {
  if (!authStore.user) return false;
  return props.members.some((member) => member.user.id === authStore.user?.id && member.user.name !== props.author);
});

const saving = ref<boolean>(false);
const search = ref<string>();
const addErrors = ref<string[]>([]);
const memberNames = computed(() => props.members.map((m) => m.user.name));

watch(search, () => {
  addErrors.value = [];
});

const editing = ref<Member>();
const inviting = ref(false);
const draftTitle = ref("");
const draftPerms = ref<NamedPermission[]>([]);

function isPending(member: Member): boolean {
  return !member.role.accepted;
}

function isPendingTransfer(member: Member): boolean {
  return isPending(member) && member.role.owner;
}

function canEditMember(member: Member): boolean {
  return props.manage && canEdit.value && !isPendingTransfer(member);
}

function openInvite() {
  editing.value = undefined;
  inviting.value = true;
  search.value = undefined;
  draftTitle.value = "";
  draftPerms.value = [...defaultInvitePermissions];
}

function openEdit(member: Member) {
  inviting.value = false;
  editing.value = member;
  draftTitle.value = member.role.title;
  draftPerms.value = [...member.role.permissions];
}

function closeEditor() {
  editing.value = undefined;
  inviting.value = false;
  addErrors.value = [];
}

function toggleGroup(group: PermissionGroup) {
  const all = group.permissions.every((p) => draftPerms.value.includes(p));
  draftPerms.value = all ? draftPerms.value.filter((p) => !group.permissions.includes(p)) : [...new Set([...draftPerms.value, ...group.permissions])];
}

function groupState(group: PermissionGroup): "all" | "some" | "none" {
  const picked = group.permissions.filter((p) => draftPerms.value.includes(p)).length;
  if (picked === 0) return "none";
  return picked === group.permissions.length ? "all" : "some";
}

const editingOwner = computed(() => editing.value?.role.owner === true);
const draftValid = computed(() => draftTitle.value.trim().length > 0 && draftTitle.value.length <= 32);

function save() {
  if (!draftValid.value) return;
  const name = editing.value ? editing.value.user.name : search.value?.trim();
  if (!name) {
    addErrors.value.push(i18n.t("general.error.nameEmpty"));
    return;
  }
  post({ name, title: draftTitle.value.trim(), permissions: draftPerms.value }, editing.value ? "edit" : "add");
}

function removeMember(member: Member) {
  post({ name: member.user.name }, "remove");
}

function cancelTransfer() {
  if (saving.value) return;
  saving.value = true;
  const url = props.organization ? `organizations/org/${props.author}/canceltransfer` : `projects/project/${props.slug}/canceltransfer`;
  useInternalApi(url, "post")
    .then(() => router.go(0))
    .catch((err) => handleRequestError(err))
    .finally(() => (saving.value = false));
}

function post(member: { name: string; title?: string; permissions?: NamedPermission[] }, action: "edit" | "add" | "remove") {
  addErrors.value = [];
  if (saving.value) return;

  saving.value = true;
  const url = props.organization ? `organizations/org/${props.author}/members/${action}` : `projects/project/${props.slug}/members/${action}`;
  useInternalApi(url, "post", member)
    .then(() => router.go(0))
    .catch((err) => handleRequestError(err))
    .finally(() => (saving.value = false));
}
</script>

<template>
  <component :is="bare ? 'div' : Card" v-if="sortedMembers.length > 0 || canEdit" :class="props.class">
    <template v-if="!bare" #header>
      <div class="flex items-center gap-2">
        <h2>{{ i18n.t("project.members") }}</h2>
        <span class="text-sm font-normal text-gray-secondary tabular-nums">{{ sortedMembers.length }}</span>
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

    <div v-if="bare" class="flex flex-wrap items-start gap-x-3 gap-y-2 border-b border-gray-300 pb-3 dark:border-gray-700">
      <div class="min-w-50 flex-1">
        <h2 v-if="title" class="flex items-center gap-2 text-lg font-semibold">
          {{ i18n.t(title) }}
          <span class="text-sm font-normal text-gray-secondary tabular-nums">{{ sortedMembers.length }}</span>
        </h2>
        <p v-if="description" class="mt-0.5 text-sm text-gray-secondary">{{ i18n.t(description) }}</p>
      </div>
      <div v-if="canLeave && author" class="flex-shrink-0">
        <MemberLeaveModal :author="author" :organization="organization" :slug="slug" />
      </div>
    </div>

    <p v-if="sortedMembers.length === 0" class="py-3 text-sm text-gray-secondary">{{ i18n.t("form.memberList.noMembers") }}</p>
    <ul v-else class="divide-y divide-gray-300 dark:divide-gray-700">
      <li v-for="member in sortedMembers" :key="member.user.name" class="py-2">
        <div class="flex flex-wrap items-center gap-x-3 gap-y-2">
          <UserAvatar :username="member.user.name" :avatar-url="member.user.avatarUrl" size="xs" class="flex-shrink-0" />

          <div class="min-w-30 flex-1">
            <p class="min-w-0 flex items-center gap-x-2">
              <Link :to="'/' + member.user.name" class="min-w-0 truncate font-semibold">{{ member.user.name }}</Link>
              <Chip v-if="isPending(member)" tone="amber" class="flex-shrink-0" :title="i18n.t('form.memberList.invitedAs', [member.role.title])">
                <IconMdiClockOutline />
                {{ i18n.t("form.memberList.pending") }}
              </Chip>
            </p>
            <p class="truncate text-sm text-gray-secondary">{{ member.role.title }}</p>
          </div>

          <!-- edit stays last so it lines up down the column, whether or not the row can be removed -->
          <div class="ml-auto flex flex-shrink-0 items-center gap-2">
            <Button
              v-if="canEditMember(member) && !member.role.owner"
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
            <Button
              v-if="canEditMember(member)"
              variant="outline"
              tone="neutral"
              size="sm"
              :disabled="saving"
              @click="editing === member ? closeEditor() : openEdit(member)"
            >
              <IconMdiPencil />
              {{ i18n.t("general.edit") }}
            </Button>
          </div>
        </div>

        <MemberPermissionEditor
          v-if="editing === member"
          v-model:title="draftTitle"
          v-model:permissions="draftPerms"
          class="mt-3"
          :groups="groups"
          :locked="editingOwner"
          :locked-hint="i18n.t('form.memberList.ownerLocked')"
          :saving="saving"
          :valid="draftValid"
          :group-state="groupState"
          @toggle-group="toggleGroup"
          @save="save"
          @cancel="closeEditor"
        />
      </li>
    </ul>

    <div v-if="canEdit && manage" class="mt-3 border-t border-gray-300 pt-4 dark:border-gray-700">
      <div v-if="!inviting" class="flex">
        <Button variant="outline" tone="neutral" @click="openInvite">
          <IconMdiAccountPlus />
          {{ i18n.t("form.memberList.invite") }}
        </Button>
      </div>

      <div v-else>
        <div class="max-w-md">
          <UserSearchInput v-model="search" name="membersearch" :label="i18n.t('form.memberList.addUser')" :error-messages="addErrors" :exclude="memberNames" />
        </div>
        <MemberPermissionEditor
          v-model:title="draftTitle"
          v-model:permissions="draftPerms"
          class="mt-3"
          :groups="groups"
          :saving="saving"
          :valid="draftValid && !!search"
          :save-label="i18n.t('form.memberList.invite')"
          :group-state="groupState"
          @toggle-group="toggleGroup"
          @save="save"
          @cancel="closeEditor"
        />
      </div>
    </div>
  </component>
</template>
