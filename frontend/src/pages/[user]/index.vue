<script setup lang="ts">
import type { FunctionalComponent } from "vue";
import IconMdiWrench from "~icons/mdi/wrench";
import IconMdiKey from "~icons/mdi/key";
import IconMdiCalendar from "~icons/mdi/calendar";
import { type HangarOrganization, NamedPermission, type PaginatedResultProject, type User } from "~/types/backend";

const props = defineProps<{
  user: User;
  organization?: HangarOrganization;
}>();
const i18n = useI18n();

const config = useConfig();
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

const userData = await useUserData(route.params.user, requestParams.value);
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
    if (hasPerms(NamedPermission.EditAllUserSettings)) {
      list.push({ icon: IconMdiKey, attr: { to: "/auth/settings/api-keys" }, name: "apiKeys" });
    }
    if (hasPerms(NamedPermission.ModNotesAndFlags) || hasPerms(NamedPermission.Reviewer)) {
      list.push({ icon: IconMdiCalendar, attr: { to: `/admin/activities/${props.user.name}` }, name: "activity" });
    }
  }
  if (hasPerms(NamedPermission.EditAllUserSettings)) {
    list.push({ icon: IconMdiWrench, attr: { to: "/admin/user/" + props.user.name }, name: "admin" });
  }

  return list;
});

const isCurrentUser = computed<boolean>(() => authStore.user != null && authStore.user.name === props.user?.name);

watchDebounced(
  requestParams,
  async () => {
    // dont want limit in url, its hardcoded in frontend
    // offset we dont want, we set page instead
    const { limit, offset, ...paramsWithoutLimit } = requestParams.value;
    // set the request params
    await router.replace({ query: { page: page.value, ...paramsWithoutLimit } });
    // do the update
    projects.value = await useApi<PaginatedResultProject>("projects", "get", { member: props.user?.name, ...requestParams.value });
  },
  { deep: true, debounce: 250 }
);

const description = (props.user?.tagline ? props.user.tagline + " - " : "") + "Download " + props.user?.name + "'s plugins on Hangar.";
useHead(
  useSeo(props.user?.name, description, route, props.user?.avatarUrl, [
    {
      type: "application/ld+json",
      children: JSON.stringify({
        "@context": "https://schema.org",
        "@type": "ProfilePage",
        mainEntity: {
          "@type": "Person",
          name: props.user?.name,
          url: config.publicHost + "/" + route.path,
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
              userInteractionCount: userData.value?.starred?.result?.length || 0,
            },
            {
              "@type": "InteractionCounter",
              interactionType: "https://schema.org/FollowAction",
              userInteractionCount: userData.value?.watching?.result?.length || 0,
            },
          ],
        },
      }),
    },
  ])
);
</script>

<template>
  <div v-if="user">
    <UserHeader :viewing-user="user" :organization="organization" />
    <div class="flex-basis-full flex flex-col gap-2 flex-grow lg:max-w-7/10 lg:min-w-6/10">
      <div v-for="project in pinned" :key="project.namespace.slug">
        <ProjectCard :project="project" />
      </div>
    </div>

    <div class="flex gap-4 flex-basis-full flex-col lg:flex-row">
      <div class="flex-basis-full flex flex-col gap-2 flex-grow lg:max-w-7/10 lg:min-w-6/10">
        <div class="flex gap-2">
          <InputText v-model="query" :label="i18n.t('hangar.projectSearch.query')" />
          <InputSelect v-model="activeSorter" :values="sorters" item-text="label" item-value="id" :label="i18n.t('hangar.projectSearch.sortBy')" />
        </div>
        <h2 class="font-bold text-xl">{{ user.name }}'s Plugins</h2>
        <ProjectList :projects="projects" @update:page="(newPage) => (page = newPage)" />
      </div>
      <div class="flex-basis-full flex-grow lg:max-w-3/10 lg:min-w-2/10">
        <Card
          v-if="buttons.length !== 0 || (organization && hasPerms(NamedPermission.IsSubjectOwner))"
          class="mb-4 border-solid border-top-4 border-top-red-500 dark:border-top-red-500"
        >
          <template #header>
            <h2>{{ i18n.t("author.management") }}</h2>
          </template>
          <template v-if="organization && hasPerms(NamedPermission.IsSubjectOwner)">
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
              <Button size="small" class="mr-1 inline-flex">
                <component :is="btn.icon" />
              </Button>
            </Link>
          </Tooltip>

          <LockUserModal v-if="!isCurrentUser && hasPerms(NamedPermission.IsStaff)" :user="user" />
          <DeleteUserModal v-if="!isCurrentUser && hasPerms(NamedPermission.ManualValueChanges)" :user="user" />
        </Card>

        <Card v-if="possibleAlts?.length" class="mb-4">
          <template #header> Shares address with</template>
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
                <h2 class="flex-grow">{{ user.name }}'s {{ i18n.t("author.orgs") }}</h2>
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
                  <Tag :color="{ background: getRole(org.roleId)?.color }" :name="getRole(org.roleId)?.title" class="ml-1" />
                  <IconMdiEyeOffOutline v-if="organizationVisibility && organizationVisibility[orgName]" class="ml-1" />
                </NuxtLink>
              </li>
            </ul>

            <span v-if="!organizations || Object.keys(organizations).length === 0">
              {{ i18n.t("author.noOrgs", [props.user.name]) }}
            </span>
          </Card>

          <Card class="mb-4" accent>
            <template #header>
              <h2>{{ i18n.t("author.stars") }}</h2>
            </template>

            <ul v-if="starred?.result?.length">
              <li v-for="star in starred?.result" :key="star.name">
                <Link :to="'/' + star.namespace.owner + '/' + star.namespace.slug">
                  {{ star.namespace.owner }}/<strong>{{ star.name }}</strong>
                </Link>
              </li>
            </ul>

            <span v-else>
              {{ i18n.t("author.noStarred", [props.user.name]) }}
            </span>
          </Card>

          <Card accent>
            <template #header>
              <h2>{{ i18n.t("author.watching") }}</h2>
            </template>

            <ul v-if="watching?.result?.length">
              <li v-for="watched in watching?.result" :key="watched.name">
                <Link :to="'/' + watched.namespace.owner + '/' + watched.namespace.slug">
                  {{ watched.namespace.owner }}/<strong>{{ watched.name }}</strong>
                </Link>
              </li>
            </ul>

            <span v-else>
              {{ i18n.t("author.noWatching", [props.user.name]) }}
            </span>
          </Card>
        </template>
        <MemberList v-else-if="organization" :members="organization.members" :roles="orgRoles" organization :author="user.name" />
      </div>
    </div>
  </div>
</template>
