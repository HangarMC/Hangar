<script setup lang="ts">
import type { FunctionalComponent } from "vue";
import IconMdiWrench from "~icons/mdi/wrench";
import IconMdiKey from "~icons/mdi/key";
import IconMdiCalendar from "~icons/mdi/calendar";
import { NamedPermission } from "#shared/types/backend";
import type { HangarOrganization, User } from "#shared/types/backend";
import { useOrganizations, useOrganizationVisibility, usePinned, usePossibleAlts, useProjects, useStarred, useWatching } from "~/composables/useData";

const props = defineProps<{
  user?: User;
  organization?: HangarOrganization;
}>();

const i18n = useI18n();
const authStore = useAuthStore();
const config = useRuntimeConfig();
const route = useRoute("user");
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
  const params = {
    limit,
    offset: page.value * limit,
  } as { limit: number; offset: number; q: string; sort: string };
  if (query.value) {
    params.q = query.value;
  }
  if (activeSorter.value) {
    params.sort = activeSorter.value;
  }
  return params;
});

// todo we can skip some of those if user is org
const { organizationVisibility } = useOrganizationVisibility(() => route.params.user);
const { possibleAlts } = usePossibleAlts(() => route.params.user);
const { projects, projectsStatus } = useProjects(() => ({ member: route.params.user, ...requestParams.value }), router);
const { starred } = useStarred(() => route.params.user);
const { watching } = useWatching(() => route.params.user);
const { pinned } = usePinned(() => route.params.user);
const { organizations } = useOrganizations(() => route.params.user);

const orgRoles = useBackendData.orgRoles.filter((role) => role.assignable);

interface UserButton {
  icon: FunctionalComponent;
  action?: () => void;
  attr: {
    to?: string;
    href?: string;
  };
  name: string;
}

const buttons = computed(() => {
  const list = [] as UserButton[];
  if (!props.user?.isOrganization) {
    if (hasPerms(NamedPermission.EditAllUserSettings)) {
      list.push({ icon: IconMdiKey, attr: { to: "/auth/settings/api-keys" }, name: "apiKeys" });
    }
    if (hasPerms(NamedPermission.ModNotesAndFlags) || hasPerms(NamedPermission.Reviewer)) {
      list.push({ icon: IconMdiCalendar, attr: { to: `/admin/activities/${props.user?.name}` }, name: "activity" });
    }
  }
  if (hasPerms(NamedPermission.EditAllUserSettings)) {
    list.push({ icon: IconMdiWrench, attr: { to: "/admin/user/" + props.user?.name }, name: "admin" });
  }

  return list;
});

const isCurrentUser = computed<boolean>(() => authStore.user?.name === props.user?.name);

const description = computed(() => (props.user?.tagline ? props.user.tagline + " - " : "") + "Download " + props.user?.name + "'s plugins on Hangar.");
useSeo(
  computed(() => ({
    title: props.user?.name,
    description: description.value,
    route,
    image: props.user?.avatarUrl,
    additionalScripts: [
      {
        key: "profilePage",
        type: "application/ld+json",
        textContent: JSON.stringify({
          "@context": "https://schema.org",
          "@type": "ProfilePage",
          mainEntity: {
            "@type": "Person",
            name: props.user?.name,
            url: config.public.host + "/" + route.path,
            description: props.user?.tagline,
            image: props.user?.avatarUrl,
            interactionStatistic: [
              {
                "@type": "InteractionCounter",
                interactionType: "https://schema.org/CreateAction",
                userInteractionCount: props.user?.projectCount,
              },
              {
                "@type": "InteractionCounter",
                interactionType: "https://schema.org/LikeAction",
                userInteractionCount: starred.value?.result?.length || 0,
              },
              {
                "@type": "InteractionCounter",
                interactionType: "https://schema.org/FollowAction",
                userInteractionCount: watching.value?.result?.length || 0,
              },
            ],
          },
        }),
      },
    ],
  }))
);
</script>

<template>
  <UserHeader :viewing-user="user" :organization="organization" />

  <!-- eslint-disable-next-line vue/no-multiple-template-root -->
  <div class="flex gap-4 flex-basis-full flex-col lg:flex-row">
    <div class="flex-basis-full flex flex-col gap-3 flex-grow lg:max-w-7/10 lg:min-w-6/10">
      <template v-if="pinned?.length">
        <h2 v-if="user" class="text-xl font-bold">{{ i18n.t("author.pinnedPlugins") }}</h2>
        <ProjectCard v-for="project in pinned" :key="project.namespace.slug" :project pinned :can-edit="hasPerms(NamedPermission.EditOwnUserSettings)" />
        <hr class="my-1 border-gray-300 dark:border-gray-700" />
      </template>

      <div class="flex flex-wrap gap-2">
        <div class="min-w-60 flex-1">
          <InputText v-model="query" :label="i18n.t('hangar.projectSearch.query')" />
        </div>
        <div class="w-56">
          <InputSelect v-model="activeSorter" :values="sorters" item-text="label" item-value="id" :label="i18n.t('hangar.projectSearch.sortBy')" />
        </div>
      </div>

      <ProjectList
        :projects="projects"
        :loading="projectsStatus === 'loading'"
        :can-edit="hasPerms(NamedPermission.EditOwnUserSettings)"
        :pinned
        @update:page="(newPage: number) => (page = newPage)"
      />
    </div>

    <div class="flex-basis-full flex flex-col gap-4 flex-grow lg:max-w-3/10 lg:min-w-2/10">
      <Card v-if="!user">
        <template #header>
          <Skeleton />
        </template>
        <Skeleton class="h-50" />
      </Card>

      <Card v-if="user && (buttons.length > 0 || (organization && hasPerms(NamedPermission.IsSubjectOwner)))">
        <template #header>
          <h2>{{ i18n.t("author.management") }}</h2>
        </template>
        <div class="flex flex-wrap gap-2">
          <Button v-for="btn in buttons" :key="btn.name" v-bind="btn.attr" variant="outline" tone="neutral" size="sm">
            <component :is="btn.icon" />
            {{ i18n.t("author.tooltips." + btn.name) }}
          </Button>

          <template v-if="organization && hasPerms(NamedPermission.IsSubjectOwner)">
            <OrgTransferModal :organization="user.name" />
            <OrgDeleteModal :organization="user.name" />
          </template>
          <LockUserModal v-if="!isCurrentUser && hasPerms(NamedPermission.IsStaff)" :user="user" />
          <DeleteUserModal v-if="!isCurrentUser && hasPerms(NamedPermission.ManualValueChanges)" :user="user" />
        </div>
      </Card>

      <Card v-if="possibleAlts?.length">
        <template #header>
          <h2>{{ i18n.t("author.sharesAddress") }}</h2>
        </template>
        <ul class="flex flex-col gap-0.5">
          <li v-for="name in possibleAlts" :key="name">
            <Link :to="'/' + name">{{ name }}</Link>
          </li>
        </ul>
      </Card>

      <template v-if="user && !user?.isOrganization && organizations">
        <Card>
          <template #header>
            <div class="flex items-center gap-2">
              <h2 class="min-w-0 flex-grow truncate">{{ i18n.t("author.orgs") }}</h2>
              <OrgVisibilityModal v-if="organizationVisibility && organizations && Object.keys(organizations).length > 0" v-model="organizationVisibility" />
            </div>
          </template>

          <ul v-if="organizations && Object.keys(organizations).length > 0" class="divide-y divide-gray-300 dark:divide-gray-700">
            <li v-for="(org, orgName) in organizations" :key="orgName">
              <NuxtLink :to="'/' + orgName" class="flex items-center gap-2 py-2 transition-colors hover:color-primary">
                <UserAvatar :username="orgName + ''" :avatar-url="org.avatarUrl" size="xs" disable-link class="flex-shrink-0" />
                <span class="min-w-0 flex-1 truncate font-semibold">{{ orgName }}</span>
                <IconMdiEyeOffOutline v-if="organizationVisibility && organizationVisibility[orgName + '']" class="flex-shrink-0 text-gray-secondary" />
                <Tag :color="{ background: getRole(org.roleId)?.color }" :name="getRole(org.roleId)?.title" class="flex-shrink-0" />
              </NuxtLink>
            </li>
          </ul>
          <p v-else class="text-sm text-gray-secondary">{{ i18n.t("author.noOrgs", [user.name]) }}</p>
        </Card>

        <Card>
          <template #header>
            <div class="flex items-center gap-2">
              <h2 class="flex-grow">{{ i18n.t("author.stars") }}</h2>
              <span v-if="starred?.result?.length" class="text-base font-normal text-gray-secondary tabular-nums">{{ starred.result.length }}</span>
            </div>
          </template>

          <Skeleton v-if="!starred" />
          <ul v-else-if="starred?.result?.length" class="divide-y divide-gray-300 dark:divide-gray-700">
            <li v-for="star in starred.result" :key="star.namespace.owner + '/' + star.name">
              <NuxtLink :to="'/' + star.namespace.owner + '/' + star.namespace.slug" class="flex items-center gap-2 py-2 transition-colors hover:color-primary">
                <UserAvatar
                  :username="star.namespace.owner"
                  :img-src="star.avatarUrl"
                  :monogram-name="star.name"
                  size="xs"
                  disable-link
                  class="flex-shrink-0"
                />
                <span class="min-w-0 flex-1">
                  <span class="block truncate font-semibold">{{ star.name }}</span>
                  <span class="block truncate text-sm text-gray-secondary">{{ star.namespace.owner }}</span>
                </span>
              </NuxtLink>
            </li>
          </ul>
          <p v-else class="text-sm text-gray-secondary">{{ i18n.t("author.noStarred", [user.name]) }}</p>
        </Card>

        <Card>
          <template #header>
            <div class="flex items-center gap-2">
              <h2 class="flex-grow">{{ i18n.t("author.watching") }}</h2>
              <span v-if="watching?.result?.length" class="text-base font-normal text-gray-secondary tabular-nums">{{ watching.result.length }}</span>
            </div>
          </template>

          <Skeleton v-if="!watching" />
          <ul v-else-if="watching?.result?.length" class="divide-y divide-gray-300 dark:divide-gray-700">
            <li v-for="watched in watching.result" :key="watched.namespace.owner + '/' + watched.name">
              <NuxtLink
                :to="'/' + watched.namespace.owner + '/' + watched.namespace.slug"
                class="flex items-center gap-2 py-2 transition-colors hover:color-primary"
              >
                <UserAvatar
                  :username="watched.namespace.owner"
                  :img-src="watched.avatarUrl"
                  :monogram-name="watched.name"
                  size="xs"
                  disable-link
                  class="flex-shrink-0"
                />
                <span class="min-w-0 flex-1">
                  <span class="block truncate font-semibold">{{ watched.name }}</span>
                  <span class="block truncate text-sm text-gray-secondary">{{ watched.namespace.owner }}</span>
                </span>
              </NuxtLink>
            </li>
          </ul>
          <p v-else class="text-sm text-gray-secondary">{{ i18n.t("author.noWatching", [user?.name]) }}</p>
        </Card>
      </template>
      <MemberList v-else-if="organization" :members="organization.members" :roles="orgRoles" organization :author="user?.name" />
    </div>
  </div>
</template>
