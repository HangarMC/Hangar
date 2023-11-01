<script setup lang="ts">
import { Popover, PopoverButton, PopoverPanel } from "@headlessui/vue";
import { useI18n } from "vue-i18n";
import type { HangarNotification, HangarUser } from "hangar-internal";
import { ref } from "vue";
import { useSettingsStore } from "~/store/useSettingsStore";
import Announcement from "~/components/Announcement.vue";
import DropdownButton from "~/components/design/DropdownButton.vue";
import DropdownItem from "~/components/design/DropdownItem.vue";
import Popper from "~/components/design/Popper.vue";

import hangarLogo from "~/assets/hangar-logo.svg";

import IconMdiHome from "~icons/mdi/home";
import IconMdiAccountGroup from "~icons/mdi/account-group";
import IconMdiForum from "~icons/mdi/forum";
import IconMdiCodeBraces from "~icons/mdi/code-braces";
import IconMdiBookOpen from "~icons/mdi/book-open";
import IconMdiLanguageJava from "~icons/mdi/language-java";
import IconMdiDownloadCircle from "~icons/mdi/download-circle";
import IconMdiFileDocumentAlert from "~icons/mdi/file-document-alert";
import IconMdiAlertOutline from "~icons/mdi/alert-outline";
import IconMdiInformationOutline from "~icons/mdi/information-outline";
import IconMdiMessageOutline from "~icons/mdi/message-outline";
import IconMdiCheck from "~icons/mdi/check";
import IconMdiFolderPlusOutline from "~icons/mdi/folder-plus-outline";
import IconMdiFolderWrenchOutline from "~icons/mdi/folder-wrench-outline";
import IconMdiFolderInformationOutline from "~icons/mdi/folder-information-outline";

import { useAuthStore } from "~/store/auth";
import { useAuth } from "~/composables/useAuth";
import { useBackendData } from "~/store/backendData";
import { authLog } from "~/composables/useLog";
import { lastUpdated } from "~/composables/useTime";
import { hasPerms } from "~/composables/usePerm";
import { NamedPermission } from "~/types/enums";
import UserAvatar from "~/components/UserAvatar.vue";
import Button from "~/components/design/Button.vue";
import { useRecentNotifications, useUnreadNotificationsCount } from "~/composables/useApiHelper";
import { handleRequestError } from "~/composables/useErrorHandling";
import Link from "~/components/design/Link.vue";
import { useInternalApi } from "~/composables/useApi";
import { unref } from "#imports";

// marker so that you can inspect backend data in dev tools
const backendData = useBackendData;

const settings = useSettingsStore();
const i18n = useI18n();
const t = i18n.t;
const authStore = useAuthStore();

const notifications = ref<HangarNotification[]>([]);
const unreadNotifications = ref<number>(0);
const loadedUnreadNotifications = ref<number>(0);

if (authStore.user) {
  updateNotifications();
}

const navBarLinks = [
  { link: "index", label: "Home" },
  { link: "authors", label: "Authors" },
  { link: "staff", label: "Team" },
];

const navBarMenuLinksHangar = [
  { link: "index", label: "Home", icon: IconMdiHome },
  { link: "guidelines", label: "Resource Guidelines", icon: IconMdiFileDocumentAlert },
  { link: "new", label: "Create Project", icon: IconMdiFolderPlusOutline },
  { link: "neworganization", label: "Create Organization", icon: IconMdiFolderPlusOutline },
  { link: "authors", label: "Authors", icon: IconMdiAccountGroup },
  { link: "staff", label: "Team", icon: IconMdiAccountGroup },
];
if (!authStore.user) {
  navBarMenuLinksHangar.splice(2, 2);
}

const navBarMenuLinksTools = [
  { link: "tools-importer", label: t("nav.tools.importer"), icon: IconMdiFolderPlusOutline },
  { link: "tools-bbcode", label: t("nav.tools.bbcode"), icon: IconMdiFolderWrenchOutline },
  { link: "tools-markdown", label: t("nav.tools.markdown"), icon: IconMdiFolderWrenchOutline },
  { link: "version", label: t("nav.tools.version"), icon: IconMdiFolderInformationOutline },
];

const auth = useAuth;
authLog("render with user " + authStore.user?.name);

const navBarMenuLinksMoreFromPaper = [
  { link: "https://papermc.io/", label: t("nav.hangar.home"), icon: IconMdiHome },
  { link: "https://forums.papermc.io/", label: t("nav.hangar.forums"), icon: IconMdiForum },
  { link: "https://github.com/PaperMC", label: t("nav.hangar.code"), icon: IconMdiCodeBraces },
  { link: "https://docs.papermc.io/", label: t("nav.hangar.docs"), icon: IconMdiBookOpen },
  { link: "https://papermc.io/javadocs", label: t("nav.hangar.javadocs"), icon: IconMdiLanguageJava },
  { link: "https://papermc.io/downloads", label: t("nav.hangar.downloads"), icon: IconMdiDownloadCircle },
];

function markNotificationsRead() {
  for (const notification of notifications.value) {
    markNotificationRead(notification);
  }
}

function markNotificationRead(notification: HangarNotification) {
  if (!notification.read) {
    notification.read = true;
    unreadNotifications.value--;
    loadedUnreadNotifications.value--;
    useInternalApi(`notifications/${notification.id}`, "post").catch((e) => handleRequestError(e));
  }
}

function updateNavData() {
  useInternalApi<HangarUser>("users/@me")
    .catch((e) => handleRequestError(e))
    .then((user) => {
      return (authStore.user = unref(user));
    });
}

function updateNotifications() {
  useUnreadNotificationsCount().then((v) => {
    if (v && v.value) {
      unreadNotifications.value = v.value;
    }
  });
  useRecentNotifications(30).then((v) => {
    if (v && v.value) {
      // Only show notifications that are recent or unread (from the last 30 notifications)
      let filteredAmount = 0;
      notifications.value = v.value.filter((notification: HangarNotification) => {
        if (filteredAmount < 8 && (!notification.read || isRecent(notification.createdAt))) {
          if (!notification.read) {
            loadedUnreadNotifications.value++;
          }

          filteredAmount++;
          return true;
        }
        return false;
      });
    }
  });
}

function isRecent(date: string): boolean {
  const now: Date = new Date();
  return now.getTime() - new Date(date).getTime() < 60 * 60 * 24 * 1000 * 7;
}
</script>

<template>
  <header class="background-default shadow-md">
    <div v-if="useBackendData.announcements">
      <Announcement v-for="(announcement, idx) in useBackendData.announcements" :key="idx" :announcement="announcement" />
    </div>

    <nav class="max-w-screen-xl mx-auto flex flex-wrap justify-center px-4 py-2 gap-3">
      <!-- Left side items -->
      <div class="flex items-center gap-4">
        <Popover v-slot="{ close }" class="relative">
          <PopoverButton v-slot="{ open }" class="flex" aria-label="Menu">
            <icon-mdi-menu class="transition-transform text-[1.2em]" :class="open ? 'transform rotate-90' : ''" />
          </PopoverButton>

          <!-- todo: Use Popper -->
          <PopoverPanel
            class="absolute top-10 z-10 w-max lt-sm:w-90vw background-default left-1/20 filter shadow-default rounded-r-md rounded-bl-md border-top-primary text-sm p-[20px]"
          >
            <p class="text-base font-semibold color-primary mb-3">Hangar</p>
            <div class="grid grid-cols-2">
              <NuxtLink
                v-for="link in navBarMenuLinksHangar"
                :key="link.label"
                :to="{ name: link.link }"
                class="flex items-center rounded-md px-6 py-2"
                hover="text-primary-400 bg-primary-0"
                @click="close()"
              >
                <component :is="link.icon" class="mr-3 text-[1.2em]" />
                {{ link.label }}
              </NuxtLink>
            </div>

            <p class="text-base font-semibold color-primary mb-3 mt-6">{{ t("nav.hangar.tools") }}</p>
            <div class="grid grid-cols-2">
              <NuxtLink
                v-for="link in navBarMenuLinksTools"
                :key="link.label"
                :to="{ name: link.link }"
                class="flex items-center rounded-md px-6 py-2"
                hover="text-primary-400 bg-primary-0"
                @click="close()"
              >
                <component :is="link.icon" class="mr-3 text-[1.2em]" />
                {{ link.label }}
              </NuxtLink>
            </div>

            <p class="text-base font-semibold color-primary mb-3 mt-6">{{ t("nav.hangar.moreFrom") }}</p>
            <div class="grid grid-cols-2">
              <a
                v-for="link in navBarMenuLinksMoreFromPaper"
                :key="link.label"
                class="flex items-center rounded-md px-6 py-2 hover:(text-primary-400 bg-primary-0)"
                :href="link.link"
              >
                <component :is="link.icon" class="mr-3 text-[1.2em]" />
                {{ link.label }}
              </a>
            </div>
          </PopoverPanel>
        </Popover>

        <!-- Site logo -->
        <NuxtLink to="/" class="flex-shrink-0">
          <img alt="Hangar Logo" :src="hangarLogo" height="34" width="32" />
        </NuxtLink>

        <!-- Desktop links -->
        <div class="gap-4 hidden sm:flex sm:items-center">
          <NuxtLink
            v-for="navBarLink in navBarLinks"
            :key="navBarLink.label"
            :to="{ name: navBarLink.link }"
            class="relative"
            after="absolute content-empty block w-0 top-30px left-1/10 h-4px rounded-8px"
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
          <DropdownButton :name="t('nav.new.title')">
            <template #default="{ close }">
              <DropdownItem to="/new" @click="close()">{{ t("nav.new.project") }}</DropdownItem>
              <DropdownItem to="/neworganization" @click="close()">{{ t("nav.new.organization") }}</DropdownItem>
            </template>
          </DropdownButton>
        </div>
        <button class="flex rounded-md p-2" hover="text-primary-400 bg-primary-0" aria-label="Toogle dark mode" @click="settings.toggleDarkMode()">
          <icon-mdi-weather-night v-if="settings.darkMode" class="text-[1.2em]"></icon-mdi-weather-night>
          <icon-mdi-white-balance-sunny v-else class="text-[1.2em]"></icon-mdi-white-balance-sunny>
        </button>
        <div v-if="authStore.user">
          <Popper placement="bottom-end">
            <button
              class="flex items-center gap-2 rounded-md p-2 hover:(text-primary-400 bg-primary-0)"
              aria-label="Notifications"
              @click="updateNotifications"
            >
              <IconMdiBellOutline v-show="unreadNotifications === 0" class="text-[1.2em]" />
              <div v-show="unreadNotifications !== 0" class="relative">
                <!-- This is fine:tm: -->
                <IconMdiBellBadge class="text-[1.2em]" />
                <svg class="absolute top-0.6 left-3.3" width="24" height="24" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <circle style="fill: #c83737" r="3.75" cx="3.75" cy="3.75" />
                </svg>
              </div>
            </button>
            <template #content="{ close }">
              <div class="-mt-1 flex flex-col rounded border-t-2 border-primary-400 background-default filter shadow-default overflow-auto max-w-150">
                <div v-if="notifications.length === 0">
                  <span class="flex shadow-0 p-2 mt-2 ml-3 mr-2">{{ i18n.t("notifications.empty.recent") }}</span>
                </div>
                <div
                  v-for="notification in notifications"
                  v-else
                  :key="notification.id"
                  :class="'text-sm flex shadow-0 p-3 pt-2 pr-4 inline-flex items-center ' + (!notification.read ? 'bg-blue-100 dark:bg-slate-700' : '')"
                  @click="close()"
                >
                  <span class="text-lg mr-2">
                    <IconMdiInformationOutline v-if="notification.type === 'info'" class="text-sky-600" />
                    <IconMdiCheck v-else-if="notification.type === 'success'" class="text-lime-600" />
                    <IconMdiAlertOutline v-else-if="notification.type === 'warning'" class="text-red-600" />
                    <IconMdiMessageOutline v-else-if="notification.type === 'neutral'" />
                  </span>

                  <NuxtLink
                    v-if="notification.action"
                    :to="'/' + notification.action"
                    active-class=""
                    @click="markNotificationRead(notification)"
                    @click.middle="markNotificationRead(notification)"
                  >
                    {{ i18n.t(notification.message[0], notification.message.slice(1)) }}
                    <div class="text-xs mt-1">{{ lastUpdated(new Date(notification.createdAt)) }}</div>
                  </NuxtLink>
                  <div v-else>
                    {{ i18n.t(notification.message[0], notification.message.slice(1)) }}
                    <div class="text-xs mt-1">{{ lastUpdated(new Date(notification.createdAt)) }}</div>
                  </div>
                </div>
                <div class="p-2 mb-1 ml-2 space-x-3 text-sm">
                  <Link to="/notifications" @click="close()">
                    <span :class="loadedUnreadNotifications >= unreadNotifications ? 'font-normal' : ''">
                      {{
                        loadedUnreadNotifications >= unreadNotifications
                          ? i18n.t("notifications.viewAll")
                          : i18n.t("notifications.viewMoreUnread", [unreadNotifications - loadedUnreadNotifications])
                      }}
                    </span>
                  </Link>
                  <span v-if="loadedUnreadNotifications !== 0" class="color-primary hover:(underline cursor-pointer)" @click="markNotificationsRead">
                    {{ i18n.t("notifications.markAsRead") }}
                  </span>
                </div>
              </div>
            </template>
          </Popper>
        </div>
        <!-- Profile dropdown -->
        <div v-if="authStore.user">
          <Popper placement="bottom-end">
            <button class="flex items-center gap-2 rounded-md p-2 hover:(text-primary-400 bg-primary-0)" @click="updateNavData">
              <UserAvatar :username="authStore.user.name" :avatar-url="authStore.user.avatarUrl" size="xs" :disable-link="true" />
              {{ authStore.user.name }}
            </button>
            <template #content="{ close }">
              <div class="-mt-2 py-1 rounded border-t-2 border-primary-400 background-default filter shadow-default flex flex-col" @click="close()">
                <DropdownItem :to="'/' + authStore.user.name">{{ t("nav.user.profile") }}</DropdownItem>
                <DropdownItem to="/notifications">{{ t("nav.user.notifications") }}</DropdownItem>
                <DropdownItem to="/auth/settings/profile">{{ t("nav.user.settings") }}</DropdownItem>
                <hr />
                <DropdownItem v-if="hasPerms(NamedPermission.MOD_NOTES_AND_FLAGS)" to="/admin/flags">
                  {{ t("nav.user.flags") }}
                  <span v-if="authStore.user.headerData.unresolvedFlags !== 0">{{ "(" + authStore.user?.headerData.unresolvedFlags + ")" }}</span>
                </DropdownItem>
                <DropdownItem v-if="hasPerms(NamedPermission.MOD_NOTES_AND_FLAGS)" to="/admin/approval/projects">
                  {{ t("nav.user.projectApprovals") }}
                  <span v-if="authStore.user.headerData.projectApprovals !== 0">{{ "(" + authStore.user?.headerData.projectApprovals + ")" }}</span>
                </DropdownItem>
                <DropdownItem v-if="hasPerms(NamedPermission.REVIEWER)" to="/admin/approval/versions">
                  {{ t("nav.user.versionApprovals") }}
                  <span v-if="authStore.user.headerData.reviewQueueCount !== 0">{{ "(" + authStore.user?.headerData.reviewQueueCount + ")" }}</span>
                </DropdownItem>
                <DropdownItem v-if="hasPerms(NamedPermission.VIEW_STATS)" to="/admin/stats">{{ t("nav.user.stats") }}</DropdownItem>
                <!-- todo -->
                <!--<DropdownItem v-if="hasPerms(NamedPermission.VIEW_HEALTH)" to="/admin/health">{{ t("nav.user.health") }}</DropdownItem>-->
                <DropdownItem v-if="hasPerms(NamedPermission.VIEW_LOGS)" to="/admin/log">{{ t("nav.user.log") }}</DropdownItem>
                <DropdownItem v-if="hasPerms(NamedPermission.MANUAL_VALUE_CHANGES)" to="/admin/settings">
                  {{ t("nav.user.adminSettings") }}
                </DropdownItem>
                <DropdownItem v-if="hasPerms(NamedPermission.EDIT_ALL_USER_SETTINGS)" to="/admin/user/">
                  {{ t("nav.user.userList") }}
                </DropdownItem>
                <hr />
                <DropdownItem @click="auth.logout()">{{ t("nav.user.logout") }}</DropdownItem>
              </div>
            </template>
          </Popper>
        </div>

        <!-- Login/register buttons -->
        <div v-else class="flex gap-2">
          <NuxtLink class="flex items-center rounded-md p-2 hover:(text-primary-400 bg-primary-0)" :to="auth.loginUrl($route.fullPath)" rel="nofollow">
            <icon-mdi-key-outline class="mr-1 text-[1.2em]" />
            {{ t("nav.login") }}
          </NuxtLink>
          <NuxtLink class="flex items-center rounded-md p-2 hover:(text-primary-400 bg-primary-0)" :to="auth.signupUrl($route.fullPath)">
            <icon-mdi-clipboard-outline class="mr-1 text-[1.2em]" />
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

nav a.router-link-active:after {
  background: linear-gradient(-270deg, #004ee9 0%, #367aff 100%);
  transition: width 0.2s ease-in;
  width: 80%;
}

nav a:not(.router-link-active):hover:after {
  background: #d3e1f6;
  transition: width 0.2s ease-in;
  width: 80%;
}
</style>
