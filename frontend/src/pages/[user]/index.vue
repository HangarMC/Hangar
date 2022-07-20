<script setup lang="ts">
import { User } from "hangar-api";
import ProjectList from "~/components/projects/ProjectList.vue";
import Card from "~/lib/components/design/Card.vue";
import { useI18n } from "vue-i18n";
import { avatarUrl } from "~/composables/useUrlHelper";
import UserAvatar from "~/components/UserAvatar.vue";
import Link from "~/lib/components/design/Link.vue";
import MemberList from "~/components/projects/MemberList.vue";
import { useContext } from "vite-ssr/vue";
import { useOrgVisibility, useUserData } from "~/composables/useApiHelper";
import { useBackendDataStore } from "~/store/backendData";
import { useAuthStore } from "~/store/auth";
import { useRoute } from "vue-router";
import { useSeo } from "~/composables/useSeo";
import { useHead } from "@vueuse/head";
import { Organization } from "hangar-internal";
import UserHeader from "~/components/UserHeader.vue";
import { computed, FunctionalComponent } from "vue";
import { hasPerms } from "~/composables/usePerm";
import { NamedPermission } from "~/types/enums";
import Button from "~/lib/components/design/Button.vue";
import Tooltip from "~/lib/components/design/Tooltip.vue";
import IconMdiWrench from "~icons/mdi/wrench";
import IconMdiKey from "~icons/mdi/key";
import IconMdiCalendar from "~icons/mdi/calendar";
import IconMdiEyeOffOutline from "~icons/mdi/eye-off-outline";
import OrgVisibilityModal from "~/components/modals/OrgVisibilityModal.vue";
import LockUserModal from "~/components/modals/LockUserModal.vue";
import ProjectCard from "~/components/projects/ProjectCard.vue";

const props = defineProps<{
  user: User;
  organization: Organization;
}>();
const i18n = useI18n();
const ctx = useContext();

const route = useRoute();
const { starred, watching, projects, organizations, pinned } = (await useUserData(props.user.name)).value || {};
let organizationVisibility = null;
if (props.user.name === useAuthStore().user?.name) {
  organizationVisibility = await useOrgVisibility(props.user.name);
}
const orgRoles = useBackendDataStore().orgRoles;
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
  }
  if ((hasPerms(NamedPermission.MOD_NOTES_AND_FLAGS) || hasPerms(NamedPermission.REVIEWER)) && !props.user.isOrganization) {
    list.push({ icon: IconMdiCalendar, attr: { to: `/admin/activities/${props.user.name}` }, name: "activity" });
  }
  if (hasPerms(NamedPermission.EDIT_ALL_USER_SETTINGS)) {
    list.push({ icon: IconMdiWrench, attr: { to: "/admin/user/" + props.user.name }, name: "admin" });
  }

  return list;
});

const isCurrentUser = computed<boolean>(() => authStore.user != null && authStore.user.name === props.user.name);

useHead(useSeo(props.user.name, props.user.tagline, route, avatarUrl(props.user.name)));
</script>

<template>
  <UserHeader :user="user" :organization="organization" />
  <div class="flex-basis-full flex flex-col gap-2 flex-grow md:max-w-2/3 md:min-w-1/3">
    <div v-for="project in pinned" :key="project.namespace">
      <ProjectCard :project="project"></ProjectCard>
    </div>
  </div>

  <div class="flex gap-4 flex-basis-full flex-col md:flex-row">
    <div class="flex-basis-full flex flex-col gap-2 flex-grow md:max-w-2/3 md:min-w-1/3">
      <ProjectList :projects="projects"></ProjectList>
    </div>
    <div class="flex-basis-full flex-grow md:max-w-1/3 md:min-w-1/3">
      <Card v-if="buttons.length !== 0" class="mb-4 border-solid border-top-4 border-top-red-500 dark:border-top-red-500">
        <template #header> Admin actions </template>

        <Tooltip v-for="btn in buttons" :key="btn.name">
          <template #content>
            {{ i18n.t(`author.tooltips.${btn.name}`) }}
          </template>
          <Link v-bind="btn.attr">
            <Button size="small" class="mr-1 inline-flex"><component :is="btn.icon" /></Button>
          </Link>
        </Tooltip>

        <LockUserModal v-if="!isCurrentUser && !user.isOrganization && hasPerms(NamedPermission.IS_STAFF)" :user="user" />
      </Card>

      <template v-if="!user.isOrganization">
        <Card class="mb-4" accent>
          <template #header>
            <div class="inline-flex w-full">
              <span class="flex-grow">{{ i18n.t("author.orgs") }}</span>
              <OrgVisibilityModal v-if="organizationVisibility && organizations && Object.keys(organizations).length !== 0" v-model="organizationVisibility" />
            </div>
          </template>

          <ul>
            <li v-for="(orgRole, orgName) in organizations" :key="orgName">
              <router-link :to="'/' + orgName" class="flex items-center mb-2">
                <UserAvatar :username="orgName" :avatar-url="avatarUrl(orgName)" size="xs" :disable-link="true" class="flex-shrink-0 mr-2" />
                {{ orgName }} ({{ orgRole.role.title }})
                <span class="flex-grow"></span>
                <IconMdiEyeOffOutline v-if="organizationVisibility && organizationVisibility[orgName]" class="ml-1" />
              </router-link>
            </li>
          </ul>

          <span v-if="!organizations || Object.keys(organizations).length === 0">
            {{ i18n.t("author.noOrgs", [props.user.name]) }}
          </span>
        </Card>

        <Card class="mb-4" accent>
          <template #header>{{ i18n.t("author.stars") }}</template>

          <ul>
            <li v-for="star in starred.result" :key="star.name">
              <Link :to="'/' + star.namespace.owner + '/' + star.namespace.slug">
                {{ star.namespace.owner }}/<strong>{{ star.name }}</strong>
              </Link>
            </li>

            <span v-if="!starred || starred.result.length === 0">
              {{ i18n.t("author.noStarred", [props.user.name]) }}
            </span>
          </ul>
        </Card>

        <Card accent>
          <template #header>{{ i18n.t("author.watching") }}</template>

          <ul>
            <li v-for="watch in watching.result" :key="watch.name">
              <Link :to="'/' + watch.namespace.owner + '/' + watch.namespace.slug"
                >{{ watch.namespace.owner }}/<strong>{{ watch.name }}</strong></Link
              >
            </li>

            <span v-if="!watching || watching.result.length === 0">
              {{ i18n.t("author.noWatching", [props.user.name]) }}
            </span>
          </ul>
        </Card>
      </template>
      <MemberList v-else :members="organization.members" :roles="orgRoles" organization :author="user.name" />
    </div>
  </div>
</template>
