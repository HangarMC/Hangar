<script setup lang="ts">
import { Popover, PopoverButton, PopoverPanel } from "@headlessui/vue";
import { useI18n } from "vue-i18n";
import { useSettingsStore } from "~/store/settings";
import Announcement from "~/components/Announcement.vue";
import DropdownButton from "~/lib/components/design/DropdownButton.vue";
import DropdownItem from "~/lib/components/design/DropdownItem.vue";
import Popper from "~/lib/components/design/Popper.vue";

import hangarLogo from "~/lib/assets/hangar-logo.svg";

import IconMdiHome from "~icons/mdi/home";
import IconMdiAccountGroup from "~icons/mdi/account-group";
import IconMdiForum from "~icons/mdi/forum";
import IconMdiCodeBraces from "~icons/mdi/code-braces";
import IconMdiBookOpen from "~icons/mdi/book-open";
import IconMdiLanguageJava from "~icons/mdi/language-java";
import IconMdiDownloadCircle from "~icons/mdi/download-circle";
import IconMdiKey from "~icons/mdi/key";
import IconMdiFileCodumentAlert from "~icons/mdi/file-document-alert";
import IconMdiBellOutline from "~icons/mdi/bell-outline";
import IconMdiBellBadge from "~icons/mdi/bell-badge";
import IconMdiAlertOutline from "~icons/mdi/alert-outline";
import IconMdiInformationOutline from "~icons/mdi/information-outline";
import IconMdiMessageOutline from "~icons/mdi/message-outline";
import IconMdiCheck from "~icons/mdi/check";
import IconMdiFolderPlusOutline from "~icons/mdi/folder-plus-outline";

import { useAuthStore } from "~/store/auth";
import { useAuth } from "~/composables/useAuth";
import { useBackendDataStore } from "~/store/backendData";
import { authLog } from "~/lib/composables/useLog";
import { lastUpdated } from "~/lib/composables/useTime";
import { hasPerms } from "~/composables/usePerm";
import { NamedPermission } from "~/types/enums";
import UserAvatar from "~/components/UserAvatar.vue";
import Button from "~/lib/components/design/Button.vue";
import { useRecentNotifications, useUnreadNotificationsCount } from "~/composables/useApiHelper";
import { handleRequestError } from "~/composables/useErrorHandling";
import { HangarNotification } from "hangar-internal";
import { useContext } from "vite-ssr/vue";
import { ref } from "vue";
import Link from "~/lib/components/design/Link.vue";
import { useInternalApi } from "~/composables/useApi";

const settings = useSettingsStore();
const { t } = useI18n();
const backendData = useBackendDataStore();
const ctx = useContext();
const i18n = useI18n();
const authStore = useAuthStore();

const notifications = ref<HangarNotification[]>([]);
const unreadNotifications = ref<number>(0);
const loadedUnreadNotifications = ref<number>(0);
const projectApprovalQueue = ref<number>(0);
const versionApprovalQueue = ref<number>(0);
const reportQueue = ref<number>(0);
if (authStore.user) {
  useUnreadNotificationsCount().then((v) => {
    if (v && v.value) {
      unreadNotifications.value = v.value;
    }
  });
  useRecentNotifications(true, 30)
    .then((v) => {
      if (v && v.value) {
        // Only show notifications that are recent or unread (from the last 30 notifications)
        let filteredAmount = 0;
        notifications.value = v.value.filter((notification) => {
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
    })
    .catch((e) => handleRequestError(e, ctx, i18n));

  if (hasPerms(NamedPermission.MOD_NOTES_AND_FLAGS)) {
    useInternalApi<number>("admin/approval/projectneedingapproval", false)
      .then((v) => {
        if (v) {
          projectApprovalQueue.value = v;
        }
      })
      .catch((e) => handleRequestError(e, ctx, i18n));
    useInternalApi<number>("admin/approval/versionsneedingapproval", false)
      .then((v) => {
        if (v) {
          versionApprovalQueue.value = v;
        }
      })
      .catch((e) => handleRequestError(e, ctx, i18n));
    useInternalApi<number>("flags/unresolvedamount", false)
      .then((v) => {
        if (v) {
          reportQueue.value = v;
        }
      })
      .catch((e) => handleRequestError(e, ctx, i18n));
  }
}

const navBarLinks = [
  { link: "index", label: "Home" },
  { link: "authors", label: "Authors" },
  { link: "staff", label: "Team" },
];

const navBarMenuLinksHangar = [
  { link: "index", label: "Home", icon: IconMdiHome },
  { link: "guidelines", label: "Resource Guidelines", icon: IconMdiFileCodumentAlert },
  { link: "new", label: "Create Project", icon: IconMdiFolderPlusOutline },
  { link: "neworganization", label: "Create Organization", icon: IconMdiFolderPlusOutline },
  { link: "authors", label: "Authors", icon: IconMdiAccountGroup },
  { link: "staff", label: "Team", icon: IconMdiAccountGroup },
];
if (!authStore.user) {
  navBarMenuLinksHangar.splice(2, 2);
}

const auth = useAuth;
const authHost = import.meta.env.HANGAR_AUTH_HOST;
authLog("render with user " + authStore.user?.name);

const navBarMenuLinksMoreFromPaper = [
  { link: "https://papermc.io/", label: t("nav.hangar.home"), icon: IconMdiHome },
  { link: "https://forums.papermc.io/", label: t("nav.hangar.forums"), icon: IconMdiForum },
  { link: "https://github.com/PaperMC", label: t("nav.hangar.code"), icon: IconMdiCodeBraces },
  { link: "https://docs.papermc.io/", label: t("nav.hangar.docs"), icon: IconMdiBookOpen },
  { link: "https://papermc.io/javadocs", label: t("nav.hangar.javadocs"), icon: IconMdiLanguageJava },
  { link: "https://papermc.io/downloads", label: t("nav.hangar.downloads"), icon: IconMdiDownloadCircle },
  { link: "https://papermc.io/community", label: t("nav.hangar.community"), icon: IconMdiAccountGroup },
  { link: authHost, label: t("nav.hangar.auth"), icon: IconMdiKey },
];

function markNotificationsRead() {
  for (const notification of notifications.value) {
    markNotificationRead(notification);
  }
}

async function markNotificationRead(notification: HangarNotification) {
  if (!notification.read) {
    notification.read = true;
    unreadNotifications.value--;
    loadedUnreadNotifications.value--;
    useInternalApi(`notifications/${notification.id}`, true, "post").catch((e) => handleRequestError(e, ctx, i18n));
  }
}

function isRecent(date: string): boolean {
  const now: Date = new Date();
  return now.getTime() - new Date(date).getTime() < 60 * 60 * 24 * 1000 * 7;
}
</script>

<template>
  <header class="background-default shadow-md">
    <div v-if="backendData.announcements">
      <Announcement v-for="(announcement, idx) in backendData.announcements" :key="idx" :announcement="announcement" />
    </div>

    <nav class="max-w-screen-xl mx-auto flex justify-between px-4 py-2">
      <!-- Left side items -->
      <div class="flex items-center gap-4">
        <Popover v-slot="{ close }" class="relative">
          <PopoverButton v-slot="{ open }" class="flex" aria-label="Menu">
            <icon-mdi-menu class="transition-transform text-[1.2em]" :class="open ? 'transform rotate-90' : ''" />
          </PopoverButton>
          <transition
            enter-active-class="transition duration-200 ease-out"
            enter-from-class="translate-y-1 opacity-0"
            enter-to-class="translate-y-0 opacity-100"
            leave-active-class="transition duration-150 ease-in"
            leave-from-class="translate-y-0 opacity-100"
            leave-to-class="translate-y-1 opacity-0"
          >
            <!-- dummy diff to make the transition work on pages where template root has multiple elements -->
            <div id="#navbarMenuLinks">
              <PopoverPanel
                class="fixed z-10 w-9/10 background-default top-1/14 left-1/20 filter drop-shadow-md rounded-md border-top-primary text-xs p-[20px]"
                md="absolute w-max top-10 rounded-none rounded-bl-md rounded-r-md"
              >
                <p class="text-base font-semibold color-primary mb-4">Hangar</p>
                <div class="grid grid-cols-2">
                  <router-link
                    v-for="link in navBarMenuLinksHangar"
                    :key="link.label"
                    :to="{ name: link.link }"
                    class="flex items-center rounded-md px-6 py-2"
                    hover="text-primary-400 bg-primary-0"
                    @click="close()"
                  >
                    <component :is="link.icon" class="mr-3 text-[1.2em]" />
                    {{ link.label }}
                  </router-link>
                </div>

                <p class="text-base font-semibold color-primary mb-4 mt-10">{{ t("nav.hangar.title") }}</p>
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
            </div>
          </transition>
        </Popover>

        <!-- Site logo -->
        <router-link to="/" class="flex-shrink-0">
          <img alt="Hangar Logo" :src="hangarLogo" class="h-8" />
        </router-link>

        <!-- Desktop links -->
        <div class="gap-4 hidden sm:flex">
          <router-link
            v-for="navBarLink in navBarLinks"
            :key="navBarLink.label"
            :to="{ name: navBarLink.link }"
            class="relative"
            after="absolute content-DEFAULT block w-0 top-30px left-1/10 h-4px rounded-8px"
          >
            {{ navBarLink.label }}
          </router-link>
        </div>
      </div>

      <!-- Right side items -->
      <div class="flex items-center gap-2">
        <div v-if="authStore.user" class="flex items-center <sm:hidden">
          <DropdownButton name="Create">
            <DropdownItem to="/new">{{ t("nav.new.project") }}</DropdownItem>
            <DropdownItem to="/neworganization">{{ t("nav.new.organization") }}</DropdownItem>
          </DropdownButton>
        </div>
        <button class="flex rounded-md p-2" hover="text-primary-400 bg-primary-0" aria-label="Toogle dark mode" @click="settings.toggleDarkMode()">
          <icon-mdi-weather-night v-if="settings.darkMode" class="text-[1.2em]"></icon-mdi-weather-night>
          <icon-mdi-white-balance-sunny v-else class="text-[1.2em]"></icon-mdi-white-balance-sunny>
        </button>
        <div v-if="authStore.user">
          <Popper placement="bottom-end">
            <button class="flex items-center gap-2 rounded-md p-2 hover:(text-primary-400 bg-primary-0)" aria-label="Notifications">
              <IconMdiBellOutline v-if="unreadNotifications === 0" class="text-[1.2em]" />
              <IconMdiBellBadge v-if="unreadNotifications !== 0" class="text-[1.2em]" />
            </button>
            <template #content="{ close }">
              <div class="flex flex-col rounded border-t-2 border-primary-400 background-default filter drop-shadow-md overflow-auto max-w-150">
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

                  <router-link v-if="notification.action" :to="'/' + notification.action" active-class="" @click="markNotificationRead(notification)">
                    {{ i18n.t(notification.message[0], notification.message.slice(1)) }}
                    <div class="text-xs mt-1">{{ lastUpdated(new Date(notification.createdAt)) }}</div>
                  </router-link>
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
                  <span v-if="loadedUnreadNotifications !== 0" class="color-primary hover:(underline)" @click="markNotificationsRead">
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
            <button class="flex items-center gap-2 rounded-md p-2 hover:(text-primary-400 bg-primary-0)">
              <UserAvatar :username="authStore.user.name" size="xs" :background="false" :disable-link="true" />
              {{ authStore.user.name }}
            </button>
            <template #content="{ close }">
              <div class="py-1 rounded border-t-2 border-primary-400 background-default filter drop-shadow-md flex flex-col" @click="close()">
                <DropdownItem :to="'/' + authStore.user.name">{{ t("nav.user.profile") }}</DropdownItem>
                <DropdownItem to="/notifications">{{ t("nav.user.notifications") }}</DropdownItem>
                <DropdownItem :to="'/' + authStore.user.name + '/settings/api-keys'">{{ t("nav.user.apiKeys") }}</DropdownItem>
                <DropdownItem :href="authHost + '/account/settings'">{{ t("nav.user.settings") }}</DropdownItem>
                <hr />
                <DropdownItem v-if="hasPerms(NamedPermission.MOD_NOTES_AND_FLAGS)" to="/admin/flags">
                  {{ t("nav.user.flags") }}
                  <span v-if="reportQueue !== 0">{{ "(" + reportQueue + ")" }}</span>
                </DropdownItem>
                <DropdownItem v-if="hasPerms(NamedPermission.MOD_NOTES_AND_FLAGS)" to="/admin/approval/projects">
                  {{ t("nav.user.projectApprovals") }}
                  <span v-if="projectApprovalQueue !== 0">{{ "(" + projectApprovalQueue + ")" }}</span>
                </DropdownItem>
                <DropdownItem v-if="hasPerms(NamedPermission.REVIEWER)" to="/admin/approval/versions">
                  {{ t("nav.user.versionApprovals") }}
                  <span v-if="versionApprovalQueue !== 0">{{ "(" + versionApprovalQueue + ")" }}</span>
                </DropdownItem>
                <DropdownItem v-if="hasPerms(NamedPermission.VIEW_STATS)" to="/admin/stats">{{ t("nav.user.stats") }}</DropdownItem>
                <DropdownItem v-if="hasPerms(NamedPermission.VIEW_HEALTH)" to="/admin/health">{{ t("nav.user.health") }}</DropdownItem>
                <DropdownItem v-if="hasPerms(NamedPermission.VIEW_LOGS)" to="/admin/log">{{ t("nav.user.log") }}</DropdownItem>
                <DropdownItem v-if="hasPerms(NamedPermission.MANUAL_VALUE_CHANGES)" to="/admin/versions">
                  {{ t("nav.user.platformVersions") }}
                </DropdownItem>
                <hr />
                <DropdownItem @click="auth.logout()">{{ t("nav.user.logout") }}</DropdownItem>
              </div>
            </template>
          </Popper>
        </div>

        <!-- Login/register buttons -->
        <div v-else class="flex gap-2">
          <a class="flex items-center rounded-md p-2 hover:(text-primary-400 bg-primary-0)" :href="auth.loginUrl($route.fullPath)">
            <icon-mdi-key-outline class="mr-1 text-[1.2em]" />
            {{ t("nav.login") }}
          </a>
          <a class="flex items-center rounded-md p-2 hover:(text-primary-400 bg-primary-0)" href="/signup">
            <icon-mdi-clipboard-outline class="mr-1 text-[1.2em]" />
            {{ t("nav.signup") }}
          </a>
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
