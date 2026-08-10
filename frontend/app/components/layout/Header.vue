<script setup lang="ts">
import { Popover, PopoverButton, PopoverPanel } from "@headlessui/vue";
import { NuxtLink } from "#components";

import type { RouteLocationRaw } from "vue-router";
import type { RouteNamedMap } from "vue-router/auto-routes";
import hangarLogo from "~/assets/hangar-logo.svg";

import IconMdiHomeOutline from "~icons/mdi/home-outline";
import IconMdiAccountGroupOutline from "~icons/mdi/account-group-outline";
import IconMdiCodeBraces from "~icons/mdi/code-braces";
import IconMdiBookOpenOutline from "~icons/mdi/book-open-outline";
import IconMdiDownloadCircleOutline from "~icons/mdi/download-circle-outline";
import IconMdiFileDocumentAlertOutline from "~icons/mdi/file-document-alert-outline";
import IconMdiAlertOutline from "~icons/mdi/alert-outline";
import IconMdiInformationOutline from "~icons/mdi/information-outline";
import IconMdiMessageOutline from "~icons/mdi/message-outline";
import IconMdiCheck from "~icons/mdi/check";
import IconMdiFolderPlusOutline from "~icons/mdi/folder-plus-outline";
import IconMdiAccountMultiplePlusOutline from "~icons/mdi/account-multiple-plus-outline";
import IconMdiFolderWrenchOutline from "~icons/mdi/folder-wrench-outline";
import IconMdiFolderInformationOutline from "~icons/mdi/folder-information-outline";
import IconMdiSparklesOutline from "~icons/mdi/sparkles-outline";

import { unseenChangelog } from "#shared/changelog";
import { NamedPermission } from "#shared/types/backend";
import type { HangarNotification, HangarUser } from "#shared/types/backend";
import { useUnreadCount } from "~/composables/useData";

// @ts-expect-error marker so that you can inspect backend data in dev tools
const backendData = useBackendData;

const settings = useSettingsStore();
const i18n = useI18n();
const t = i18n.t;
const authStore = useAuthStore();
const route = useRoute();
const globalData = useGlobalData();

const notifications = ref<HangarNotification[]>([]);
const { unreadCount, refreshUnreadCount } = useUnreadCount();

const hasStaffLinks = computed(() =>
  [
    NamedPermission.ModNotesAndFlags,
    NamedPermission.Reviewer,
    NamedPermission.ViewStats,
    NamedPermission.ViewHealth,
    NamedPermission.ViewLogs,
    NamedPermission.ManualValueChanges,
    NamedPermission.EditAllUserSettings,
  ].some((permission) => hasPerms(permission))
);
// logged-in only: anonymous visitors have no durable seen state, so a badge would return every visit
const unseenChangelogCount = computed(() => (authStore.user ? unseenChangelog(authStore.user.lastSeenChangelogAt).length : 0));

const loadedUnreadNotifications = ref<number>(0);
const totalUnread = computed(() => (unreadCount?.value ? unreadCount.value.notifications + unreadCount.value.invites : 0));

type NavBarLinks = { link: keyof RouteNamedMap; label: string; icon?: any }[];

const navBarLinks: NavBarLinks = [
  { link: "index", label: t("nav.indexTitle") },
  { link: "authors", label: t("nav.usersTitle") },
];

const navBarMenuLinksHangar: NavBarLinks = [
  { link: "index", label: t("general.home"), icon: IconMdiHomeOutline },
  { link: "guidelines", label: t("guidelines.title"), icon: IconMdiFileDocumentAlertOutline },
  { link: "new", label: t("nav.links.createProject"), icon: IconMdiFolderPlusOutline },
  { link: "neworganization", label: t("nav.links.createOrganization"), icon: IconMdiAccountMultiplePlusOutline },
  { link: "authors", label: t("nav.usersTitle"), icon: IconMdiAccountGroupOutline },
];
if (!authStore.user) {
  navBarMenuLinksHangar.splice(2, 2);
}

const navBarMenuLinksTools: NavBarLinks = [
  { link: "tools-importer", label: t("nav.tools.importer"), icon: IconMdiFolderPlusOutline },
  { link: "tools-bbcode", label: t("nav.tools.bbcode"), icon: IconMdiFolderWrenchOutline },
  { link: "tools-markdown", label: t("nav.tools.markdown"), icon: IconMdiFolderWrenchOutline },
  { link: "version", label: t("nav.tools.version"), icon: IconMdiFolderInformationOutline },
];

const auth = useAuth;
authLog("render with user " + authStore.user?.name);

const navBarMenuLinksMoreFromPaper = [
  { link: "https://papermc.io/", label: t("nav.hangar.home"), icon: IconMdiHomeOutline },
  { link: "https://github.com/PaperMC", label: t("nav.hangar.code"), icon: IconMdiCodeBraces },
  { link: "https://docs.papermc.io/", label: t("nav.hangar.docs"), icon: IconMdiBookOpenOutline },
  { link: "https://papermc.io/downloads", label: t("nav.hangar.downloads"), icon: IconMdiDownloadCircleOutline },
];

type MenuSection = { label: string; external?: boolean; links: { link: string; label: string; icon?: any }[] };
const menuSections: MenuSection[] = [
  { label: "Hangar", links: navBarMenuLinksHangar },
  { label: t("nav.hangar.tools"), links: navBarMenuLinksTools },
  { label: t("nav.hangar.moreFrom"), links: navBarMenuLinksMoreFromPaper, external: true },
];

function markNotificationsRead() {
  for (const notification of notifications.value) {
    markNotificationRead(notification);
  }
}

function markNotificationRead(notification: HangarNotification) {
  if (notification.read) {
    return;
  }

  notification.read = true;
  unreadCount && unreadCount.value.notifications--;
  loadedUnreadNotifications.value--;
  useInternalApi(`notifications/${notification.id}`, "post").catch((err) => handleRequestError(err));
}

function updateNavData() {
  useInternalApi<HangarUser>("users/@me")
    .catch((err) => handleRequestError(err))
    .then((user) => {
      if (!user) return;
      authStore.user = unref(user);
    });
}

function updateNotifications() {
  refreshUnreadCount();
  // only actually load them when clicked
  useInternalApi<HangarNotification[]>("recentnotifications?amount=30")
    .catch(handleRequestError)
    .then((v) => {
      if (!v) {
        return;
      }

      // Only show notifications that are recent or unread (from the last 30 notifications)
      let filteredAmount = 0;
      loadedUnreadNotifications.value = 0;
      notifications.value = v.filter((notification: HangarNotification) => {
        if (filteredAmount < 8 && (!notification.read || isRecent(notification.createdAt))) {
          if (!notification.read) {
            loadedUnreadNotifications.value++;
          }

          filteredAmount++;
          return true;
        }
        return false;
      });
    });
}

watch(
  () => route.path,
  (_path, from) => {
    if (from === "/notifications") refreshUnreadCount();
  }
);

function isRecent(date: string): boolean {
  const now: Date = new Date();
  return now.getTime() - new Date(date).getTime() < 60 * 60 * 24 * 1000 * 7;
}
</script>

<template>
  <header class="background-default shadow-md">
    <div v-if="globalData?.announcements">
      <Announcement v-for="(announcement, idx) in globalData?.announcements" :key="idx" :announcement="announcement" />
    </div>

    <nav class="max-w-screen-xl mx-auto flex flex-wrap justify-end px-4 py-2 gap-3">
      <!-- Left side items -->
      <div class="flex items-center gap-4">
        <Popover v-slot="{ close, open }" class="relative">
          <PopoverButton
            id="menu-button"
            :aria-label="t('nav.menu')"
            :title="t('nav.menu')"
            class="header-icon-btn"
            v-on="useTracking('nav-burger-button', { open })"
          >
            <icon-mdi-menu class="transition-transform text-[1.2em]" :class="{ 'rotate-90': open }" />
          </PopoverButton>

          <!-- capture: a click on a NuxtLink never bubbles back up to this handler -->
          <PopoverPanel
            class="absolute left-0 top-full z-10 mt-2 max-w-[calc(100vw-2rem)] w-max overflow-hidden rounded-md border border-gray-300 background-default p-4 pt-5 text-sm shadow-default lt-sm:w-[calc(100vw-2rem)] dark:border-gray-700"
            @click.capture="close()"
          >
            <span class="absolute inset-x-0 top-0 h-1 bg-primary-400" />

            <section
              v-for="(section, index) in menuSections"
              :key="section.label"
              :class="index > 0 ? 'mt-4 border-t border-gray-300 pt-4 dark:border-gray-700' : ''"
            >
              <p class="px-3 pb-1.5 text-sm font-semibold color-primary">{{ section.label }}</p>
              <div class="grid grid-cols-1 gap-x-3 sm:grid-cols-2">
                <component
                  :is="section.external ? 'a' : NuxtLink"
                  v-for="link in section.links"
                  :key="link.label"
                  v-bind="section.external ? { href: link.link } : { to: { name: link.link } as RouteLocationRaw }"
                  class="header-menu-link"
                  v-on="useTracking('nav-burger-link', { link: link.link })"
                >
                  <component :is="link.icon" class="flex-shrink-0 text-[1.2em]" />
                  {{ link.label }}
                </component>
              </div>
            </section>
          </PopoverPanel>
        </Popover>

        <!-- Site logo -->
        <NuxtLink to="/" class="flex-shrink-0" v-on="useTracking('nav-logo')">
          <img alt="Hangar Logo" :src="hangarLogo" height="34" width="32" />
        </NuxtLink>

        <!-- Desktop links -->
        <div class="gap-4 hidden sm:flex sm:items-center">
          <NuxtLink
            v-for="navBarLink in navBarLinks"
            :key="navBarLink.label"
            :to="{ name: navBarLink.link } as RouteLocationRaw"
            class="header-link relative"
            after="absolute content-empty block w-0 top-30px left-1/10 h-4px rounded-8px"
            v-on="useTracking('nav-desktop-link', { link: navBarLink.link })"
          >
            {{ navBarLink.label }}
          </NuxtLink>
        </div>
      </div>

      <!-- Gap between the sides -->
      <div class="flex-grow-1" />

      <!-- Right side items -->
      <div class="flex items-center gap-2">
        <div v-if="authStore.user" class="flex items-center lt-sm:hidden">
          <DropdownButton :name="t('nav.new.title')" v-on="useTracking('nav-create-dropdwon')">
            <template #default="{ close }">
              <DropdownItem to="/new" @click="close()" v-on="useTracking('nav-new')">
                <IconMdiFolderPlusOutline class="flex-shrink-0" />
                {{ t("nav.new.project") }}
              </DropdownItem>
              <DropdownItem to="/neworganization" @click="close()" v-on="useTracking('nav-new-org')">
                <IconMdiAccountMultiplePlusOutline class="flex-shrink-0" />
                {{ t("nav.new.organization") }}
              </DropdownItem>
            </template>
          </DropdownButton>
        </div>
        <button
          class="header-icon-btn"
          :aria-label="settings.darkMode ? t('nav.darkModeOff') : t('nav.darkModeOn')"
          :title="settings.darkMode ? t('nav.darkModeOff') : t('nav.darkModeOn')"
          @click="settings.toggleDarkMode()"
          v-on="useTracking('nav-theme', { darkMode: settings.darkMode })"
        >
          <icon-mdi-weather-night v-if="settings.darkMode" class="text-[1.2em]" />
          <icon-mdi-white-balance-sunny v-else class="text-[1.2em]" />
        </button>
        <div v-if="authStore.user">
          <Popper placement="bottom-end">
            <button
              class="header-icon-btn relative"
              :aria-label="t('notifications.title')"
              :title="t('notifications.title')"
              @click="updateNotifications"
              v-on="useTracking('nav-notifications', () => ({ unread: totalUnread }))"
            >
              <IconMdiBellOutline class="text-[1.2em]" />
              <span
                v-if="totalUnread > 0"
                class="accent-fill absolute right-1 top-1 min-w-4 rounded-full px-1 text-center text-[10px] font-semibold leading-4 tabular-nums"
              >
                {{ totalUnread > 9 ? "9+" : totalUnread }}
              </span>
            </button>
            <template #content="{ close }">
              <ClientOnly>
                <div
                  class="max-h-[min(70vh,32rem)] max-w-[calc(100vw-2rem)] w-96 flex flex-col overflow-hidden rounded-md border border-gray-300 background-default shadow-default dark:border-gray-700"
                >
                  <div class="flex flex-shrink-0 items-center gap-2 border-b border-gray-300 px-4 py-2.5 dark:border-gray-700">
                    <h2 class="flex-grow font-bold">{{ i18n.t("notifications.recentNotifications") }}</h2>
                    <Chip v-if="totalUnread > 0" tone="primary" class="tabular-nums">{{ totalUnread }}</Chip>
                  </div>

                  <NuxtLink
                    v-if="unreadCount?.invites"
                    to="/notifications"
                    active-class=""
                    class="flex flex-shrink-0 items-center gap-3 border-b border-gray-300 px-4 py-2.5 transition-colors hover:background-card dark:border-gray-700"
                    @click="close()"
                  >
                    <span class="chip-primary h-8 w-8 flex flex-shrink-0 items-center justify-center rounded-lg color-primary">
                      <IconMdiAccountPlus />
                    </span>
                    <span class="flex-grow text-sm font-semibold">{{ i18n.t("notifications.invitesPending", [unreadCount.invites]) }}</span>
                    <IconMdiChevronRight class="flex-shrink-0 text-gray-secondary" />
                  </NuxtLink>

                  <div class="min-h-0 flex-1 overflow-y-auto">
                    <p v-if="notifications.length === 0" class="px-4 py-8 text-center text-sm text-gray-secondary">
                      {{ i18n.t("notifications.empty.recent") }}
                    </p>
                    <ul v-else class="divide-y divide-gray-300 dark:divide-gray-700">
                      <li
                        v-for="notification in notifications"
                        :key="notification.id"
                        class="relative flex items-start gap-3 px-4 py-3 transition-colors"
                        :class="notification.action ? 'hover:background-card' : ''"
                      >
                        <span class="mt-2.5 w-2 flex-shrink-0">
                          <span v-if="!notification.read" class="block h-2 w-2 rounded-full bg-primary-500" />
                        </span>

                        <span
                          class="h-8 w-8 flex flex-shrink-0 items-center justify-center rounded-lg"
                          :class="{
                            'bg-sky-500/15 text-sky-500': notification.type === 'info',
                            'bg-lime-500/15 text-lime-500': notification.type === 'success',
                            'bg-amber-500/15 text-amber-500': notification.type === 'warning',
                            'bg-red-500/15 text-red-500': notification.type === 'error',
                            'chip-neutral text-gray-600 dark:text-gray-300': notification.type === 'neutral',
                          }"
                        >
                          <IconMdiInformationOutline v-if="notification.type === 'info'" />
                          <IconMdiCheck v-else-if="notification.type === 'success'" />
                          <IconMdiAlertOutline v-else-if="notification.type === 'warning' || notification.type === 'error'" />
                          <IconMdiMessageOutline v-else />
                        </span>

                        <div class="min-w-0 flex-1 text-sm">
                          <NuxtLink
                            v-if="notification.action"
                            :to="'/' + notification.action"
                            active-class=""
                            class="line-clamp-2 after:(absolute inset-0 content-empty)"
                            :class="notification.read ? '' : 'font-semibold'"
                            @click="(markNotificationRead(notification), close())"
                            @click.middle="markNotificationRead(notification)"
                          >
                            {{ i18n.t(notification.message[0]!, notification.message.slice(1)) }}
                          </NuxtLink>
                          <span v-else class="line-clamp-2 block" :class="notification.read ? '' : 'font-semibold'">
                            {{ i18n.t(notification.message[0]!, notification.message.slice(1)) }}
                          </span>
                          <div class="mt-0.5 text-xs text-gray-secondary">{{ lastUpdated(new Date(notification.createdAt)) }}</div>
                        </div>
                      </li>
                    </ul>
                  </div>

                  <div class="flex flex-shrink-0 items-center gap-3 border-t border-gray-300 px-4 py-2.5 text-sm dark:border-gray-700">
                    <Link to="/notifications" class="flex-grow" @click="close()">
                      {{
                        !unreadCount || loadedUnreadNotifications >= unreadCount.notifications
                          ? i18n.t("notifications.viewAll")
                          : i18n.t("notifications.viewMoreUnread", [unreadCount.notifications - loadedUnreadNotifications])
                      }}
                    </Link>
                    <button
                      v-if="loadedUnreadNotifications !== 0"
                      type="button"
                      class="flex-shrink-0 color-primary hover:underline"
                      @click="markNotificationsRead"
                    >
                      {{ i18n.t("notifications.readAll") }}
                    </button>
                  </div>
                </div>
              </ClientOnly>
            </template>
          </Popper>
        </div>
      </div>
      <div class="flex items-center gap-2">
        <!-- Profile dropdown -->
        <div v-if="authStore.user">
          <Popper placement="bottom-end">
            <button class="header-icon-btn" @click="updateNavData" v-on="useTracking('nav-profile-dropdown')">
              <UserAvatar :username="authStore.user.name" :avatar-url="authStore.user.avatarUrl" size="xs" :disable-link="true" />
              {{ authStore.user.name }}
            </button>
            <template #content="{ close }">
              <DropdownPanel @click="close()">
                <DropdownItem :to="'/' + authStore.user.name"><IconMdiAccountOutline class="flex-shrink-0" />{{ t("nav.user.profile") }}</DropdownItem>
                <DropdownItem to="/notifications"><IconMdiBellOutline class="flex-shrink-0" />{{ t("nav.user.notifications") }}</DropdownItem>
                <DropdownItem to="/auth/settings/profile"><IconMdiCogOutline class="flex-shrink-0" />{{ t("nav.user.settings") }}</DropdownItem>
                <DropdownItem to="/changelog">
                  <IconMdiSparklesOutline class="flex-shrink-0" />
                  {{ t("nav.user.changelog") }}
                  <Chip v-if="unseenChangelogCount" tone="primary">{{ unseenChangelogCount }}</Chip>
                </DropdownItem>
                <hr v-if="hasStaffLinks" class="my-1 border-gray-300 dark:border-gray-700" />
                <DropdownItem v-if="hasPerms(NamedPermission.ModNotesAndFlags)" to="/admin/flags">
                  <IconMdiFlagOutline class="flex-shrink-0" />
                  {{ t("nav.user.flags") }}
                  <span v-if="authStore.user.headerData.unresolvedFlags !== 0">{{ "(" + authStore.user?.headerData.unresolvedFlags + ")" }}</span>
                </DropdownItem>
                <DropdownItem v-if="hasPerms(NamedPermission.ModNotesAndFlags)" to="/admin/approval/projects">
                  <IconMdiFolderCheckOutline class="flex-shrink-0" />
                  {{ t("nav.user.projectApprovals") }}
                  <span v-if="authStore.user.headerData.projectApprovals !== 0">{{ "(" + authStore.user?.headerData.projectApprovals + ")" }}</span>
                </DropdownItem>
                <DropdownItem v-if="hasPerms(NamedPermission.Reviewer)" to="/admin/approval/versions">
                  <IconMdiArchiveOutline class="flex-shrink-0" />
                  {{ t("nav.user.versionApprovals") }}
                  <span v-if="authStore.user.headerData.reviewQueueCount !== 0">{{ "(" + authStore.user?.headerData.reviewQueueCount + ")" }}</span>
                </DropdownItem>
                <DropdownItem v-if="hasPerms(NamedPermission.ViewStats)" to="/admin/stats">
                  <IconMdiChartLine class="flex-shrink-0" />{{ t("nav.user.stats") }}
                </DropdownItem>
                <DropdownItem v-if="hasPerms(NamedPermission.ViewHealth)" to="/admin/health">
                  <IconMdiPulse class="flex-shrink-0" />{{ t("nav.user.health") }}
                </DropdownItem>
                <DropdownItem v-if="hasPerms(NamedPermission.ViewLogs)" to="/admin/log">
                  <IconMdiHistory class="flex-shrink-0" />{{ t("nav.user.log") }}
                </DropdownItem>
                <!-- moderators only see the discovery section there, but that is theirs to manage -->
                <DropdownItem v-if="hasPerms(NamedPermission.Reviewer)" to="/admin/settings">
                  <IconMdiShieldCrownOutline class="flex-shrink-0" />
                  {{ t("nav.user.adminSettings") }}
                </DropdownItem>
                <DropdownItem v-if="hasPerms(NamedPermission.EditAllUserSettings)" to="/admin/user/">
                  <IconMdiAccountGroupOutline class="flex-shrink-0" />
                  {{ t("nav.user.userList") }}
                </DropdownItem>
                <hr class="my-1 border-gray-300 dark:border-gray-700" />
                <DropdownItem tone="danger" @click="auth.logout()"><IconMdiLogout class="flex-shrink-0" />{{ t("nav.user.logout") }}</DropdownItem>
              </DropdownPanel>
            </template>
          </Popper>
        </div>

        <!-- Login/register buttons -->
        <div v-else class="flex gap-2">
          <NuxtLink class="header-icon-btn" :to="auth.loginUrl(route.fullPath)" rel="nofollow">
            <icon-mdi-key-outline class="mr-1 flex-shrink-0 text-[1.2em]" />
            {{ t("nav.login") }}
          </NuxtLink>
          <NuxtLink class="header-icon-btn" :to="auth.signupUrl(route.fullPath)">
            <icon-mdi-clipboard-outline class="mr-1 flex-shrink-0 text-[1.2em]" />
            {{ t("nav.signup") }}
          </NuxtLink>
        </div>
      </div>
    </nav>
  </header>
</template>

<style lang="css" scoped>
nav .router-link-active {
  @apply color-primary;
  font-weight: 700;
}

.header-icon-btn {
  @apply flex items-center gap-2 rounded-md p-2 transition-colors;
  @apply hover:(text-primary-500 bg-primary-0) dark:hover:(text-white bg-zinc-700);
}

.header-menu-link {
  @apply flex items-center gap-2.5 whitespace-nowrap rounded-md px-3 py-2 transition-colors;
  @apply hover:background-card;
}

.header-icon-btn:focus-visible,
.header-menu-link:focus-visible {
  outline: 2px solid var(--primary-500);
  outline-offset: -2px;
}

.header-link.router-link-active:after {
  content: "";
  background: linear-gradient(-270deg, var(--primary-500) 0%, var(--primary-400) 100%);
  transition: width 0.2s ease-in;
  width: 80%;
}

.header-link:not(.router-link-active):hover:after {
  background: color-mix(in srgb, var(--primary-500) 45%, transparent);
  transition: width 0.2s ease-in;
  width: 80%;
}
</style>
