<script setup lang="ts">
import type { PaginatedResult, Project, User } from "hangar-api";
import { useI18n } from "vue-i18n";
import { useRoute } from "vue-router";
import { useHead } from "@unhead/vue";
import type { Organization } from "hangar-internal";
import { computed, type FunctionalComponent, ref } from "vue";
import { watchDebounced } from "@vueuse/core";
import ProjectList from "~/components/projects/ProjectList.vue";
import Card from "~/components/design/Card.vue";
import UserAvatar from "~/components/UserAvatar.vue";
import Link from "~/components/design/Link.vue";
import MemberList from "~/components/projects/MemberList.vue";
import { useUserData } from "~/composables/useApiHelper";
import { getRole, useBackendData } from "~/store/backendData";
import { useAuthStore } from "~/store/auth";
import { useSeo } from "~/composables/useSeo";
import UserHeader from "~/components/UserHeader.vue";
import { hasPerms } from "~/composables/usePerm";
import { NamedPermission } from "~/types/enums";
import Button from "~/components/design/Button.vue";
import Tooltip from "~/components/design/Tooltip.vue";
import IconMdiWrench from "~icons/mdi/wrench";
import IconMdiKey from "~icons/mdi/key";
import IconMdiCalendar from "~icons/mdi/calendar";
import IconMdiEyeOffOutline from "~icons/mdi/eye-off-outline";
import OrgVisibilityModal from "~/components/modals/OrgVisibilityModal.vue";
import LockUserModal from "~/components/modals/LockUserModal.vue";
import ProjectCard from "~/components/projects/ProjectCard.vue";
import OrgTransferModal from "~/components/modals/OrgTransferModal.vue";
import OrgDeleteModal from "~/components/modals/OrgDeleteModal.vue";
import InputSelect from "~/components/ui/InputSelect.vue";
import { useApi } from "~/composables/useApi";
import { useRouter } from "#imports";
import InputText from "~/components/ui/InputText.vue";
import Tag from "~/components/Tag.vue";
import DeleteUserModal from "~/components/modals/DeleteUserModal.vue";

const props = defineProps<{
  user: User;
  organization: Organization;
}>();
const i18n = useI18n();

const route = useRoute();
const router = useRouter();

const sorters = [
  { id: "-updated", label: i18n.t("project.sorting.recentlyUpdated") },
  { id: "-newest", label: i18n.t("project.sorting.newest") },
  { id: "-stars", label: i18n.t("project.sorting.mostStars") },
  { id: "-downloads", label: i18n.t("project.sorting.mostDownloads") },
  { id: "-recent_downloads", label: i18n.t("project.sorting.recentDownloads") },
  { id: "slug", label: i18n.t("project.sorting.slug") },
];
const query = ref((route.query.q as string) || "");
const page = ref(route.query.page ? Number(route.query.page) : 0);
const activeSorter = ref((route.query.sort as string) || "-updated");
const requestParams = computed(() => {
  const limit = 10;
  const params: Record<string, any> = {
    limit,
    offset: page.value * limit,
  };
  if (query.value) {
    params.q = query.value;
  }
  if (activeSorter.value) {
    params.sort = activeSorter.value;
  }
  return params;
});

const userData = await useUserData(props.user.name, requestParams.value);
const { starred, watching, organizations, pinned, organizationVisibility, possibleAlts } = userData.value || { starred: null };
const projects = ref(userData.value?.projects);
const orgRoles = useBackendData.orgRoles.filter((role) => role.assignable);
const authStore = useAuthStore();

interface UserButton {
  icon: FunctionalComponent;
  action?: () => void;
  attr: {
    to?: string;
    href?: string;
  };
  name: string;
}

const buttons = computed<UserButton[]>(() => {
  const list = [] as UserButton[];
  if (!props.user.isOrganization) {
    if (hasPerms(NamedPermission.EDIT_ALL_USER_SETTINGS)) {
      list.push({ icon: IconMdiKey, attr: { to: "/" + props.user.name + "/settings/api-keys" }, name: "apiKeys" });
    }
    if (hasPerms(NamedPermission.MOD_NOTES_AND_FLAGS) || hasPerms(NamedPermission.REVIEWER)) {
      list.push({ icon: IconMdiCalendar, attr: { to: `/admin/activities/${props.user.name}` }, name: "activity" });
    }
  }
  if (hasPerms(NamedPermission.EDIT_ALL_USER_SETTINGS)) {
    list.push({ icon: IconMdiWrench, attr: { to: "/admin/user/" + props.user.name }, name: "admin" });
  }

  return list;
});

const isCurrentUser = computed<boolean>(() => authStore.user != null && authStore.user.name === props.user.name);

watchDebounced(
  requestParams,
  async () => {
    // dont want limit in url, its hardcoded in frontend
    // offset we dont want, we set page instead
    const { limit, offset, ...paramsWithoutLimit } = requestParams.value;
    // set the request params
    await router.replace({ query: { page: page.value, ...paramsWithoutLimit } });
    // do the update
    projects.value = await useApi<PaginatedResult<Project>>("projects", "get", { owner: props.user.name, ...requestParams.value });
  },
  { deep: true, debounce: 250 }
);

const description = props.user.name + " is an author on Hangar." + (props.user?.tagline ? " " + props.user.tagline : "");
useHead(useSeo(props.user.name, description, route, props.user.avatarUrl));
</script>

<template>
  <div v-if="user">
    <UserHeader :viewing-user="user" :organization="organization" />
    <div class="flex-basis-full flex flex-col gap-2 flex-grow lg:max-w-7/10 lg:min-w-6/10">
      <div v-for="project in pinned" :key="project.namespace">
        <ProjectCard :project="project" />
      </div>
    </div>

    <div class="flex gap-4 flex-basis-full flex-col lg:flex-row">
      <div class="flex-basis-full flex flex-col gap-2 flex-grow lg:max-w-7/10 lg:min-w-6/10">
        <div class="flex gap-2">
          <InputText v-model="query" :label="i18n.t('hangar.projectSearch.query')" />
          <InputSelect v-model="activeSorter" :values="sorters" item-text="label" item-value="id" :label="i18n.t('hangar.projectSearch.sortBy')" />
        </div>
        <ProjectList :projects="projects" @update:page="(newPage) => (page = newPage)" />
      </div>
      <div class="flex-basis-full flex-grow lg:max-w-3/10 lg:min-w-2/10">
        <Card
          v-if="buttons.length !== 0 || (organization && hasPerms(NamedPermission.IS_SUBJECT_OWNER))"
          class="mb-4 border-solid border-top-4 border-top-red-500 dark:border-top-red-500"
        >
          <template #header>{{ i18n.t("author.management") }}</template>
          <template v-if="organization && hasPerms(NamedPermission.IS_SUBJECT_OWNER)">
            <Tooltip :content="i18n.t('author.tooltips.transfer')">
              <OrgTransferModal :organization="user.name" />
            </Tooltip>
            <Tooltip :content="i18n.t('author.tooltips.delete')">
              <OrgDeleteModal :organization="user.name" />
            </Tooltip>
          </template>

          <Tooltip v-for="btn in buttons" :key="btn.name">
            <template #content>
              {{ i18n.t(`author.tooltips.${btn.name}`) }}
            </template>
            <Link v-bind="btn.attr">
              <Button size="small" class="mr-1 inline-flex"><component :is="btn.icon" /></Button>
            </Link>
          </Tooltip>

          <LockUserModal v-if="!isCurrentUser && hasPerms(NamedPermission.IS_STAFF)" :user="user" />
          <DeleteUserModal v-if="!isCurrentUser && hasPerms(NamedPermission.MANUAL_VALUE_CHANGES)" :user="user" />
        </Card>

        <Card v-if="possibleAlts?.length > 0" class="mb-4">
          <template #header> Shares address with </template>
          <ul>
            <li v-for="name in possibleAlts" :key="name">
              <Link :to="'/' + name">
                {{ name }}
              </Link>
            </li>
          </ul>
        </Card>

        <template v-if="!user.isOrganization && organizations">
          <Card class="mb-4" accent>
            <template #header>
              <div class="inline-flex w-full">
                <span class="flex-grow">{{ i18n.t("author.orgs") }}</span>
                <OrgVisibilityModal
                  v-if="organizationVisibility && organizations && Object.keys(organizations).length !== 0"
                  v-model="organizationVisibility"
                />
              </div>
            </template>

            <ul>
              <li v-for="(org, orgName) in organizations" :key="orgName">
                <NuxtLink :to="'/' + orgName" class="flex items-center mb-2">
                  <UserAvatar :username="orgName" :avatar-url="org.avatarUrl" size="xs" :disable-link="true" class="flex-shrink-0 mr-2" />
                  {{ orgName }}
                  <span class="flex-grow" />
                  <Tag :color="{ background: getRole(org.role.roleId).color }" :name="getRole(org.role.roleId).title" class="ml-1" />
                  <IconMdiEyeOffOutline v-if="organizationVisibility && organizationVisibility[orgName]" class="ml-1" />
                </NuxtLink>
              </li>
            </ul>

            <span v-if="!organizations || Object.keys(organizations).length === 0">
              {{ i18n.t("author.noOrgs", [props.user.name]) }}
            </span>
          </Card>

          <Card class="mb-4" accent>
            <template #header>{{ i18n.t("author.stars") }}</template>

            <ul>
              <li v-for="star in starred?.result" :key="star.name">
                <Link :to="'/' + star.namespace.owner + '/' + star.namespace.slug">
                  {{ star.namespace.owner }}/<strong>{{ star.name }}</strong>
                </Link>
              </li>

              <span v-if="!starred || starred?.result?.length === 0">
                {{ i18n.t("author.noStarred", [props.user.name]) }}
              </span>
            </ul>
          </Card>

          <Card accent>
            <template #header>{{ i18n.t("author.watching") }}</template>

            <ul>
              <li v-for="watched in watching?.result" :key="watched.name">
                <Link :to="'/' + watched.namespace.owner + '/' + watched.namespace.slug">
                  {{ watched.namespace.owner }}/<strong>{{ watched.name }}</strong>
                </Link>
              </li>

              <span v-if="!watching || watching?.result?.length === 0">
                {{ i18n.t("author.noWatching", [props.user.name]) }}
              </span>
            </ul>
          </Card>
        </template>
        <MemberList
          v-else-if="organization"
          :members="organization.members"
          :roles="orgRoles"
          organization
          :author="user.name"
          :owner="organization.owner.userId"
        />
      </div>
    </div>
  </div>
</template>
