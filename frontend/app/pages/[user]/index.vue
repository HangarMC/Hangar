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
  <div class="grid grid-cols-1 items-start gap-4 lg:grid-cols-[minmax(0,1fr)_300px] xl:grid-cols-[minmax(0,1fr)_320px]">
    <main class="min-w-0 space-y-4">
      <template v-if="pinned?.length">
        <section>
          <div class="mb-2 flex items-center gap-2">
            <IconMdiPin class="color-primary" />
            <h2 v-if="user" class="text-xl font-bold">{{ user.name }}'s pinned plugins</h2>
          </div>
          <div class="space-y-2">
            <ProjectCard
              v-for="project in pinned"
              :key="project.namespace.slug"
              :project="project"
              pinned
              :can-edit="hasPerms(NamedPermission.EditOwnUserSettings)"
            />
          </div>
        </section>
      </template>

      <section>
        <Card class="mb-4 flex items-center justify-between gap-4">
          <div class="relative flex h-10.5 min-w-0 flex-grow rounded-md transition-all duration-200">
            <input
              v-model="query"
              name="query"
              class="min-w-30 basis-full truncate rounded-lg border border-transparent p-2 px-9 outline-none transition-all duration-200 hover:border-gray-700 focus:border-gray-700 dark:bg-gray-800"
              type="text"
              :placeholder="i18n.t('hangar.projectSearch.query', [projects?.pagination.count])"
            />
            <IconMdiMagnify class="absolute top-3 left-3 text-gray-500" />
            <button v-if="query.length > 0" class="transition-all duration-250" @click="query = ''">
              <IconMdiClose class="absolute top-3 right-3 text-gray-500 hover:text-white" />
            </button>
          </div>
          <div class="w-69 flex-shrink-0 [&>*]:!w-full">
            <DropdownButton :button-arrow="true" button-size="medium" button-type="transparent" button-class="!w-full" match-menu-width spread-arrow>
              <template #button-label>
                <div class="flex min-w-0 flex-1 items-center justify-start gap-1">
                  <IconMdiSwapVertical class="flex-shrink-0" />
                  <div class="truncate">{{ sorters.find((s) => s.id === activeSorter)!.label }}</div>
                </div>
              </template>
              <template #default="{ close }">
                <div class="flex max-h-lg w-full max-w-lg flex-col gap-1 overflow-y-auto overflow-x-visible">
                  <DropdownItem
                    v-for="sorter in sorters"
                    :key="sorter.id"
                    :style="
                      activeSorter === sorter.id
                        ? {
                            backgroundColor: 'color-mix(in srgb, var(--primary-500) 25%, transparent)',
                            borderColor: 'var(--primary-500)',
                          }
                        : {}
                    "
                    @click="
                      activeSorter = sorter.id;
                      close();
                    "
                  >
                    {{ sorter.label }}
                  </DropdownItem>
                </div>
              </template>
            </DropdownButton>
          </div>
        </Card>
        <div v-if="projectsStatus === 'loading' || projects?.result?.length" class="flex flex-col gap-4">
          <ProjectList
            :projects="projects"
            :loading="projectsStatus === 'loading'"
            :can-edit="(isCurrentUser && hasPerms(NamedPermission.EditOwnUserSettings)) || (organization && hasPerms(NamedPermission.IsSubjectOwner))"
            :pinned
            @update:page="(newPage: number) => (page = newPage)"
          />
        </div>
        <Card v-else class="!p-0 overflow-hidden">
          <div class="flex flex-col items-center px-6 py-10 text-center">
            <span class="inline-flex h-12 w-12 items-center justify-center rounded-lg bg-gray-100 text-2xl text-gray dark:bg-charcoal-500">
              <IconMdiPackageVariantClosed />
            </span>
            <h2 class="mt-3 text-xl font-bold">{{ query ? "No matching projects" : "No projects yet" }}</h2>
            <p class="mt-1 max-w-md text-sm text-gray">
              {{
                query
                  ? "Try a different search term or clear the current search."
                  : organization
                    ? `${user?.name} does not have any projects yet.`
                    : `${user?.name} has not published any projects yet.`
              }}
            </p>
            <Button v-if="query" class="mt-4" button-type="secondary" size="medium" @click="query = ''">
              <IconMdiClose class="mr-1" />
              Clear search
            </Button>
            <Link v-else-if="organization && hasPerms(NamedPermission.IsSubjectOwner)" to="/new" class="mt-4">
              <Button size="medium">
                <IconMdiPlus class="mr-1" />
                Create project
              </Button>
            </Link>
          </div>
        </Card>
      </section>
    </main>

    <aside class="space-y-4 lg:sticky lg:top-4">
      <Card v-if="!user" class="!p-0 overflow-hidden">
        <template #header>
          <div class="px-4 pt-3.5 pb-1"><Skeleton /></div>
        </template>
        <Skeleton class="m-3 h-40" />
      </Card>

      <Card v-if="user && (buttons.length > 0 || (organization && hasPerms(NamedPermission.IsSubjectOwner)))" class="!p-0 overflow-hidden">
        <template #header>
          <div class="flex items-center gap-2 px-4 pt-3.5 pb-2">
            <div>
              <h2>{{ i18n.t("author.management") }}</h2>
              <p v-if="organization" class="text-xs font-normal text-gray">Organization ownership and deletion controls.</p>
            </div>
          </div>
        </template>
        <div class="grid grid-cols-2 gap-2 px-3 pb-3">
          <template v-if="organization && hasPerms(NamedPermission.IsSubjectOwner)">
            <Tooltip class="!block !w-full min-w-0">
              <template #content>{{ i18n.t("author.tooltips.transfer") }}</template>
              <OrgTransferModal :organization="user.name" />
            </Tooltip>
            <Tooltip class="!block !w-full min-w-0">
              <template #content>{{ i18n.t("author.tooltips.delete") }}</template>
              <OrgDeleteModal :organization="user.name" />
            </Tooltip>
          </template>

          <Tooltip v-for="btn in buttons" :key="btn.name">
            <template #content>{{ i18n.t(`author.tooltips.${btn.name}`) }}</template>
            <Link v-bind="btn.attr">
              <Button size="medium"><component :is="btn.icon" /></Button>
            </Link>
          </Tooltip>

          <LockUserModal v-if="!isCurrentUser && hasPerms(NamedPermission.IsStaff)" :user="user" />
          <DeleteUserModal v-if="!isCurrentUser && hasPerms(NamedPermission.ManualValueChanges)" :user="user" />
        </div>
      </Card>

      <Card v-if="possibleAlts?.length" class="!p-0 overflow-hidden">
        <template #header><h2 class="px-4 pt-3.5 pb-2">Shares address with</h2></template>
        <ul class="space-y-1 px-3 pb-3">
          <li v-for="name in possibleAlts" :key="name">
            <Link :to="'/' + name" class="block rounded-md border px-3 py-2 font-semibold transition-colors hover:background-card dark:border-gray-800">
              {{ name }}
            </Link>
          </li>
        </ul>
      </Card>

      <template v-if="user && !user?.isOrganization && organizations">
        <Card class="!p-0 overflow-hidden">
          <template #header>
            <div class="flex w-full items-center gap-2 px-4 pt-3.5 pb-2">
              <h2 class="flex-grow">{{ i18n.t("author.orgs") }}</h2>
              <OrgVisibilityModal v-if="organizationVisibility && organizations && Object.keys(organizations).length > 0" v-model="organizationVisibility" />
            </div>
          </template>

          <ul class="flex flex-col gap-1.5 px-3 pt-1 pb-3">
            <li v-for="(org, orgName) in organizations" :key="orgName">
              <div
                class="inline-flex w-full items-center rounded-md border border-gray-200 bg-gray-100/60 transition-colors hover:border-gray-300 dark:border-gray-800 dark:bg-charcoal-500/60 dark:hover:border-gray-700"
              >
                <NuxtLink :to="'/' + orgName" class="group flex min-w-0 flex-grow items-center gap-2 rounded-md p-2">
                  <UserAvatar :username="orgName + ''" :avatar-url="org.avatarUrl" size="xs" :disable-link="true" class="flex-shrink-0" />
                  <div class="min-w-0 flex-grow">
                    <span
                      class="background-default mb-0.5 inline-flex items-center rounded-md border px-1.5 py-0 text-[0.65rem] font-medium"
                      :style="{ borderColor: getRole(org.roleId)?.color, color: getRole(org.roleId)?.color }"
                    >
                      {{ getRole(org.roleId)?.title }}
                    </span>
                    <p class="truncate font-semibold leading-tight transition-colors group-hover:color-primary">{{ orgName }}</p>
                  </div>
                  <IconMdiEyeOffOutline v-if="organizationVisibility && organizationVisibility[orgName + '']" class="flex-shrink-0 text-gray" />
                </NuxtLink>
              </div>
            </li>
          </ul>

          <span v-if="!organizations || Object.keys(organizations).length === 0" class="block px-4 pb-4 text-sm text-gray">
            {{ i18n.t("author.noOrgs", [user.name]) }}
          </span>
        </Card>

        <Card class="!p-0 overflow-hidden">
          <template #header>
            <div class="flex items-center gap-2 px-3 py-2.5">
              <h2 class="flex-grow">{{ i18n.t("author.stars") }}</h2>
              <span class="rounded-md border border-gray-200 bg-gray-100 px-2 py-0.5 text-xs text-gray dark:border-gray-700 dark:bg-charcoal-500">
                {{ starred?.result?.length || 0 }}
              </span>
            </div>
          </template>

          <Skeleton v-if="!starred" class="m-3 h-20" />

          <ul
            v-else-if="starred?.result?.length"
            class="max-h-[13.5rem] divide-y divide-gray-200 overflow-y-auto border-t border-gray-200 dark:divide-gray-800 dark:border-gray-800"
          >
            <li v-for="star in starred.result" :key="star.name" class="min-w-0">
              <Link
                :to="'/' + star.namespace.owner + '/' + star.namespace.slug"
                class="block h-9 min-w-0 truncate px-3 py-2 text-sm transition-colors hover:background-card"
              >
                <span class="text-gray">{{ star.namespace.owner }}/</span><strong>{{ star.name }}</strong>
              </Link>
            </li>
          </ul>

          <span v-else class="block px-4 pb-4 text-sm text-gray">No starred projects</span>
        </Card>

        <Card class="!p-0 overflow-hidden">
          <template #header>
            <div class="flex items-center gap-2 px-3 py-2.5">
              <h2 class="flex-grow">{{ i18n.t("author.watching") }}</h2>
              <span class="rounded-md border border-gray-200 bg-gray-100 px-2 py-0.5 text-xs text-gray dark:border-gray-700 dark:bg-charcoal-500">
                {{ watching?.result?.length || 0 }}
              </span>
            </div>
          </template>

          <Skeleton v-if="!watching" class="m-3 h-20" />

          <ul
            v-else-if="watching?.result?.length"
            class="max-h-[13.5rem] divide-y divide-gray-200 overflow-y-auto border-t border-gray-200 dark:divide-gray-800 dark:border-gray-800"
          >
            <li v-for="watched in watching.result" :key="watched.name" class="min-w-0">
              <Link
                :to="'/' + watched.namespace.owner + '/' + watched.namespace.slug"
                class="block h-9 min-w-0 truncate px-3 py-2 text-sm transition-colors hover:background-card"
              >
                <span class="text-gray">{{ watched.namespace.owner }}/</span><strong>{{ watched.name }}</strong>
              </Link>
            </li>
          </ul>

          <span v-else class="block border-t border-gray-200 px-3 py-3 text-sm text-gray dark:border-gray-800">
            {{ i18n.t("author.noWatching", [user?.name]) }}
          </span>
        </Card>
      </template>
      <MemberList v-else-if="organization" :members="organization.members" :roles="orgRoles" organization :author="user?.name" />
    </aside>
  </div>
</template>
