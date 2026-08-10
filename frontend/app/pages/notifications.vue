<script lang="ts" setup>
import { InviteType } from "#shared/types/backend";
import type { HangarNotification, HangarOrganizationInvite, HangarProjectInvite } from "#shared/types/backend";

definePageMeta({
  loginRequired: true,
});

type NotificationTab = "unread" | "all" | "invites";

const i18n = useI18n();
const route = useRoute("notifications");
const notificationStore = useNotificationStore();

const LIMIT = 25;
const page = ref(0);
const requestParams = computed(() => ({ limit: LIMIT, offset: page.value * LIMIT }));

const { invites } = useInvites();
const { unreadNotifications } = useUnreadNotifications(() => requestParams.value);
const { notifications: allNotifications } = useNotifications(() => requestParams.value);
const { refreshUnreadCount } = useUnreadCount();

const selectedTab = ref<NotificationTab>("unread");
watch(selectedTab, () => (page.value = 0));
const selectedInvitesTab = ref<"all" | "projects" | "organizations">("all");

const notifications = computed(() => (selectedTab.value === "unread" ? unreadNotifications.value : allNotifications.value));
const unreadCount = computed(() => unreadNotifications.value?.pagination.count ?? 0);
const inviteCount = computed(() => (invites.value?.project.length ?? 0) + (invites.value?.organization.length ?? 0));
const tabs = computed<{ value: NotificationTab; label: string; count?: number }[]>(() => [
  { value: "unread", label: i18n.t("notifications.unread"), count: unreadCount.value },
  { value: "all", label: i18n.t("notifications.all") },
  { value: "invites", label: i18n.t("notifications.invites"), count: inviteCount.value },
]);

const inviteFilters: { value: "all" | "projects" | "organizations"; label: string }[] = [
  { value: "all", label: i18n.t("notifications.invite.all") },
  { value: "projects", label: i18n.t("notifications.invite.projects") },
  { value: "organizations", label: i18n.t("notifications.invite.organizations") },
];

const filteredInvites = computed<(HangarProjectInvite | HangarOrganizationInvite)[]>(() => {
  if (!invites.value) return [];
  switch (selectedInvitesTab.value) {
    case "projects":
      return invites.value.project;
    case "organizations":
      return invites.value.organization;
    default:
      return [...invites.value.project, ...invites.value.organization];
  }
});

const isEmpty = computed(() => {
  if (selectedTab.value === "invites") return inviteCount.value === 0;
  return !notifications.value?.result.length;
});

const emptyTitle = computed(() => i18n.t(`notifications.emptyState.${selectedTab.value}.title`));
const emptyDescription = computed(() => i18n.t(`notifications.emptyState.${selectedTab.value}.description`));

useSeo(computed(() => ({ title: "Notifications", route })));

async function markAllAsRead() {
  try {
    await useInternalApi("markallread", "post");
  } catch (err) {
    handleRequestError(err);
    return;
  }

  if (unreadNotifications.value) {
    unreadNotifications.value.result = [];
    unreadNotifications.value.pagination.count = 0;
  }
  const hangarNotifications = allNotifications.value?.result ?? [];
  for (const notification of hangarNotifications) {
    notification.read = true;
  }
  await refreshUnreadCount();
}

async function markNotificationRead(notification: HangarNotification) {
  try {
    await useInternalApi(`notifications/${notification.id}`, "post");
  } catch (err) {
    handleRequestError(err);
    return;
  }

  notification.read = true;
  if (unreadNotifications.value) {
    unreadNotifications.value.result = unreadNotifications.value.result.filter((item) => item.id !== notification.id);
    unreadNotifications.value.pagination.count = Math.max(0, unreadNotifications.value.pagination.count - 1);
  }
  const notificationInAll = allNotifications.value?.result.find((item) => item.id === notification.id);
  if (notificationInAll) notificationInAll.read = true;
  await refreshUnreadCount();
}

async function updateInvite(invite: HangarOrganizationInvite | HangarProjectInvite, status: "accept" | "decline") {
  try {
    await useInternalApi(`invites/${invite.type}/${invite.roleId}/${status}`, "post");
  } catch (err) {
    handleRequestError(err);
    return;
  }

  if (!invites.value) return;
  if (invite.type === InviteType.Project) {
    invites.value.project = invites.value.project.filter((item) => item.roleId !== invite.roleId);
  } else {
    invites.value.organization = invites.value.organization.filter((item) => item.roleId !== invite.roleId);
  }
  notificationStore.success(i18n.t(`notifications.invite.msgs.${status}`, [invite.name]));
  await refreshUnreadCount();
}
</script>

<template>
  <div>
    <div class="mb-5 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <h1 class="text-3xl font-bold">{{ i18n.t("notifications.title") }}</h1>
        <p class="mt-1 text-gray-secondary">{{ i18n.t("notifications.subtitle") }}</p>
      </div>
      <Button v-if="unreadCount" variant="outline" tone="neutral" class="w-max" @click="markAllAsRead">
        <IconMdiCheck />{{ i18n.t("notifications.readAll") }}
      </Button>
    </div>

    <SegmentedControl v-model="selectedTab" :options="tabs" :aria-label="i18n.t('notifications.title')" />

    <Card v-if="isEmpty" flat class="mt-4 min-h-72 flex flex-col items-center justify-center text-center">
      <div class="mb-4 h-14 w-14 flex items-center justify-center rounded-full background-card text-2xl text-gray-secondary">
        <IconMdiBellOutline />
      </div>
      <h2 class="text-lg font-bold">{{ emptyTitle }}</h2>
      <p class="mt-1 max-w-md text-gray-secondary">{{ emptyDescription }}</p>
    </Card>

    <Card v-else-if="selectedTab !== 'invites'" flat padding="none" class="mt-4">
      <ul class="divide-y divide-gray-300 dark:divide-gray-700">
        <Pagination v-if="notifications?.result" :items="notifications.result" :server-pagination="notifications.pagination" @update:page="(p) => (page = p)">
          <template #default="{ item }">
            <li class="flex items-center gap-3 px-4 py-3 transition-colors hover:background-card">
              <span class="w-2 flex-shrink-0">
                <span v-if="!item.read" class="block h-2 w-2 rounded-full bg-primary-500" :title="i18n.t('notifications.unread')" />
              </span>

              <div
                class="h-9 w-9 flex flex-shrink-0 items-center justify-center rounded-lg text-lg"
                :class="{
                  'bg-sky-500/15 text-sky-500': item.type === 'info',
                  'bg-lime-500/15 text-lime-500': item.type === 'success',
                  'bg-amber-500/15 text-amber-500': item.type === 'warning',
                  'bg-red-500/15 text-red-500': item.type === 'error',
                  'icon-neutral text-gray-600 dark:text-gray-300': item.type === 'neutral',
                }"
              >
                <IconMdiInformationOutline v-if="item.type === 'info'" />
                <IconMdiCheck v-else-if="item.type === 'success'" />
                <IconMdiAlertOutline v-else-if="item.type === 'warning' || item.type === 'error'" />
                <IconMdiMessageOutline v-else />
              </div>

              <div class="min-w-0 flex-1">
                <NuxtLink
                  v-if="item.action"
                  :to="'/' + item.action"
                  active-class=""
                  :class="item.read ? 'font-medium text-gray-secondary' : 'font-semibold'"
                  @click="!item.read && markNotificationRead(item)"
                >
                  {{ i18n.t(item.message[0]!, item.message.slice(1)) }}
                </NuxtLink>
                <div v-else :class="item.read ? 'font-medium text-gray-secondary' : 'font-semibold'">
                  {{ i18n.t(item.message[0]!, item.message.slice(1)) }}
                </div>
                <div class="mt-0.5 text-xs text-gray-secondary">
                  <template v-if="item.originUserName">{{ item.originUserName }} &middot; </template>{{ lastUpdated(new Date(item.createdAt)) }}
                </div>
              </div>

              <Button
                v-if="!item.read"
                variant="ghost"
                tone="neutral"
                size="sm"
                icon-only
                :title="i18n.t('notifications.markAsRead')"
                :aria-label="i18n.t('notifications.markAsRead')"
                class="flex-shrink-0"
                @click="markNotificationRead(item)"
              >
                <IconMdiCheck />
              </Button>
            </li>
          </template>
          <template #pagination="{ page: current, pages, updatePage }">
            <li class="p-3">
              <PaginationButtons :page="current" :pages="pages" @update:page="updatePage" />
            </li>
          </template>
        </Pagination>
      </ul>
    </Card>

    <Card v-else flat padding="none" class="mt-4">
      <div class="p-3 pb-0">
        <SegmentedControl v-model="selectedInvitesTab" :options="inviteFilters" :aria-label="i18n.t('notifications.invites')" />
      </div>

      <ul class="mt-3 divide-y divide-gray-300 dark:divide-gray-700">
        <li v-for="invite in filteredInvites" :key="invite.type + invite.roleId" class="px-4 py-3">
          <div class="flex flex-col gap-3 sm:flex-row sm:items-center">
            <div class="min-w-0 flex-1">
              <div class="flex flex-wrap items-center gap-1">
                <NuxtLink :to="invite.url" exact class="font-semibold">
                  {{ invite.type === InviteType.Project ? invite.url.slice(1) : invite.name }}
                </NuxtLink>
                <span v-if="'representingOrg' in invite && invite.representingOrg">
                  {{ i18n.t("notifications.invite.invitedOrganizationAs", [invite.representingOrg]) }}
                </span>
                <span v-else>{{ i18n.t("notifications.invite.invitedYouAs") }}</span>
                <Chip tone="primary">{{ invite.title }}</Chip>
              </div>
              <div class="mt-1 text-xs text-gray-secondary">
                {{ i18n.t(`notifications.invite.types.${invite.type}`) }} &middot; {{ lastUpdated(new Date(invite.createdAt)) }}
              </div>
            </div>
            <div class="flex flex-shrink-0 gap-2 self-end sm:self-center">
              <Button size="sm" @click="updateInvite(invite, 'accept')">{{ i18n.t("notifications.invite.btns.accept") }}</Button>
              <Button variant="outline" tone="neutral" size="sm" @click="updateInvite(invite, 'decline')">
                {{ i18n.t("notifications.invite.btns.decline") }}
              </Button>
            </div>
          </div>
        </li>
        <li v-if="filteredInvites.length === 0" class="py-10 text-center text-gray-secondary">
          {{ i18n.t("notifications.empty.invites") }}
        </li>
      </ul>
    </Card>
  </div>
</template>

<style scoped>
.icon-neutral {
  background-color: color-mix(in srgb, var(--gray-500) 15%, transparent);
}
</style>
