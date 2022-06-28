<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { JoinableMember } from "hangar-internal";
import { NamedPermission } from "~/types/enums";
import Card from "~/lib/components/design/Card.vue";
import UserAvatar from "~/components/UserAvatar.vue";
import Link from "~/lib/components/design/Link.vue";
import DropdownButton from "~/lib/components/design/DropdownButton.vue";
import DropdownItem from "~/lib/components/design/DropdownItem.vue";
import { avatarUrl } from "~/composables/useUrlHelper";
import { hasPerms } from "~/composables/usePerm";
import { useBackendDataStore } from "~/store/backendData";
import { PaginatedResult, Role, User } from "hangar-api";
import { useRoute, useRouter } from "vue-router";
import { useApi, useInternalApi } from "~/composables/useApi";
import { useContext } from "vite-ssr/vue";
import IconMdiClock from "~icons/mdi/clock";
import Tooltip from "~/lib/components/design/Tooltip.vue";
import InputAutocomplete from "~/lib/components/ui/InputAutocomplete.vue";

const props = withDefaults(
  defineProps<{
    members: JoinableMember[];
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
  if (r1.role.role.rank) {
    if (r2.role.role.rank) {
      return r1.role.role.rank - r2.role.role.rank;
    }
    return 1;
  }
  return r2.role.role.rank ? -1 : 1;
});

const i18n = useI18n();
const store = useBackendDataStore();
const route = useRoute();
const router = useRouter();
const ctx = useContext();
const roles: Role[] = props.organization ? store.orgRoles : store.projectRoles;

const canEdit = computed<boolean>(() => {
  return hasPerms(NamedPermission.EDIT_SUBJECT_SETTINGS);
});
const saving = ref<boolean>(false);
const search = ref<string>("");
const addErrors = ref<string[]>([]);
const result = ref<string[]>([]);

watch(search, () => {
  addErrors.value = [];
});

function removeMember(member: JoinableMember) {
  post(convertMember(member), "remove");
}

function setRole(member: JoinableMember, role: Role) {
  const editableMember: EditableMember = convertMember(member);
  editableMember.roleId = role.roleId;
  post(editableMember, "edit");
}

function invite(member: string, role: Role) {
  const editableMember: EditableMember = { name: member, roleId: role.roleId };
  post(editableMember, "add");
  return "";
}

async function post(member: EditableMember, action: "edit" | "add" | "remove") {
  addErrors.value = [];
  if (member.name.length === 0) {
    addErrors.value.push(i18n.t("general.error.nameEmpty"));
    return;
  }
  if (saving.value) {
    return;
  }

  saving.value = true;
  const url = props.organization ? `organizations/org/${props.author}/members/${action}` : `projects/project/${props.author}/${props.slug}/members/${action}`;
  useInternalApi(url, true, "post", member)
    .then(() => {
      router.go(0);
    })
    .catch((e) => {
      addErrors.value.push(i18n.te(e.response?.data.message) ? i18n.t(e.response?.data.message, e.response?.data.messageArgs) : e.response?.data.message);
    })
    .finally(() => {
      saving.value = false;
    });
}

function convertMember(member: JoinableMember): EditableMember {
  return {
    name: member.user.name,
    roleId: member.role.role.roleId,
  };
}

async function doSearch(val: string) {
  result.value = ["Dum", "Dum2"];
  const users = await useApi<PaginatedResult<User>>("users", false, "get", {
    query: val,
    limit: 25,
    offset: 0,
  });
  result.value = users.result.filter((u) => !props.members.some((m) => m.user.name === u.name)).map((u) => u.name);
}

interface EditableMember {
  name: string;
  roleId: number;
}
</script>

<template>
  <Card :class="props.class">
    <template #header>
      <div class="inline-flex w-full flex-cols space-between">
        <span class="flex-grow">{{ i18n.t("project.members") }}</span>
      </div>
    </template>

    <div
      v-for="member in sortedMembers"
      :key="member.user.name"
      class="p-2 w-full border border-gray-100 dark:border-gray-800 rounded inline-flex flex-row space-x-4"
    >
      <UserAvatar :username="member.user.name" :avatar-url="avatarUrl(member.user.name)" size="sm" class="flex-shrink-0" />
      <div class="flex-grow truncate">
        <p class="font-semibold">
          <Link :to="'/' + member.user.name">{{ member.user.name }}</Link>
        </p>
        <Tooltip v-if="!member.role.accepted">
          <template #content>
            {{ i18n.t("form.memberList.invitedAs", [member.role.role.title]) }}
          </template>
          <span class="items-center inline-flex"> {{ member.role.role.title }} <IconMdiClock class="ml-1" /> </span>
        </Tooltip>
        <span v-else class="items-center inline-flex"> {{ member.role.role.title }}</span>
      </div>
      <!-- todo confirmation modal -->
      <DropdownButton v-if="canEdit && member.role.role.assignable" :name="i18n.t('general.edit')">
        <template #button-label>
          <IconMdiPencil />
        </template>
        <DropdownItem v-for="role of roles" :key="role.title" :disabled="saving" @click="setRole(member, role)">
          {{ role.title }}
        </DropdownItem>
        <hr />
        <DropdownItem @click="removeMember(member)">Remove</DropdownItem>
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
        <DropdownItem v-for="role of roles" :key="role.title" :disabled="saving" @click="invite(search, role)">
          {{ role.title }}
        </DropdownItem>
      </DropdownButton>
    </div>
  </Card>
</template>
