<script setup lang="ts">
import { Popover, PopoverButton, PopoverPanel, Menu, MenuButton, MenuItems } from "@headlessui/vue";
import { useI18n } from "vue-i18n";
import { useThemeStore } from "~/store/theme";
import Announcement from "~/components/Announcement.vue";
import DropdownButton from "~/components/design/DropdownButton.vue";
import DropdownItem from "~/components/design/DropdownItem.vue";

import hangarLogo from "~/assets/logo.svg";

import IconMdiHome from "~icons/mdi/home";
import IconMdiAccountGroup from "~icons/mdi/account-group";
import IconMdiForum from "~icons/mdi/forum";
import IconMdiCodeBraces from "~icons/mdi/code-braces";
import IconMdiBookOpen from "~icons/mdi/book-open";
import IconMdiLanguageJava from "~icons/mdi/language-java";
import IconMdiPuzzle from "~icons/mdi/puzzle";
import IconMdiDownloadCircle from "~icons/mdi/download-circle";
import IconMdiKey from "~icons/mdi/key";

import { useAuthStore } from "~/store/auth";
import { useAuth } from "~/composables/useAuth";
import { useBackendDataStore } from "~/store/backendData";
import { authLog } from "~/composables/useLog";
import UserAvatar from "~/components/UserAvatar.vue";

const theme = useThemeStore();
const { t } = useI18n();
const backendData = useBackendDataStore();

const navBarLinks = [
  { link: "index", label: "Home" },
  { link: "staff", label: "Team" },
  { link: "authors", label: "Authors" },
];

const navBarMenuLinksHangar = [
  { link: "index", label: "Home", icon: IconMdiHome },
  { link: "staff", label: "Team", icon: IconMdiAccountGroup },
  { link: "authors", label: "Authors", icon: IconMdiAccountGroup },
];

const navBarMenuLinksMoreFromPaper = [
  { link: "https://papermc.io/", label: t("nav.hangar.home"), icon: IconMdiHome },
  { link: "https://forums.papermc.io/", label: t("nav.hangar.forums"), icon: IconMdiForum },
  { link: "https://github.com/PaperMC", label: t("nav.hangar.code"), icon: IconMdiCodeBraces },
  { link: "https://docs.papermc.io/", label: t("nav.hangar.docs"), icon: IconMdiBookOpen },
  { link: "https://papermc.io/javadocs", label: t("nav.hangar.javadocs"), icon: IconMdiLanguageJava },
  { link: "/", label: t("nav.hangar.hangar"), icon: IconMdiPuzzle },
  { link: "https://papermc.io/downloads", label: t("nav.hangar.downloads"), icon: IconMdiDownloadCircle },
  { link: "https://papermc.io/community", label: t("nav.hangar.community"), icon: IconMdiAccountGroup },
  { link: "https://hangar-auth.benndorf.dev/", label: t("nav.hangar.auth"), icon: IconMdiKey },
];

const authStore = useAuthStore();
const auth = useAuth;
authLog("render with user " + authStore.user?.name);
</script>

<template>
  <header class="background-header">
    <div v-if="backendData.announcements">
      <Announcement v-for="(announcement, idx) in backendData.announcements" :key="idx" :announcement="announcement" />
    </div>

    <nav class="container mx-auto flex justify-between px-4 py-2">
      <!-- Left side items -->
      <div class="flex items-center gap-4">
        <Popover class="relative">
          <PopoverButton v-slot="{ open }" class="flex">
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
            <PopoverPanel
              class="fixed z-10 w-9/10 background-header top-1/14 left-1/20 drop-shadow-md rounded-md border-top-primary text-xs p-[20px]"
              md="absolute w-max top-10 rounded-none rounded-bl-md rounded-r-md"
            >
              <p class="text-base font-semibold color-primary mb-4">Hangar</p>
              <div class="grid grid-cols-2">
                <router-link
                  v-for="link in navBarMenuLinksHangar"
                  :key="link.label"
                  :to="{ name: link.link }"
                  class="flex items-center rounded-md px-6 py-2"
                  hover="text-primary-100 bg-primary-50"
                >
                  <component :is="link.icon" class="mr-3 text-[1.2em]" />
                  {{ link.label }}
                </router-link>
              </div>

              <p class="text-base font-semibold color-primary mb-4 mt-10">More from Paper</p>
              <div class="grid grid-cols-2">
                <a
                  v-for="link in navBarMenuLinksMoreFromPaper"
                  :key="link.label"
                  class="flex items-center rounded-md px-6 py-2"
                  :href="link.link"
                  hover="text-primary-100 bg-primary-50"
                >
                  <component :is="link.icon" class="mr-3 text-[1.2em]" />
                  {{ link.label }}
                </a>
              </div>
            </PopoverPanel>
          </transition>
        </Popover>

        <!-- Site logo -->
        <router-link to="/">
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
        <div v-if="authStore.user" class="flex items-center">
          <DropdownButton name="Create">
            <DropdownItem to="/new">{{ t("nav.new.project") }}</DropdownItem>
            <DropdownItem to="/organizations/new">{{ t("nav.new.organization") }}</DropdownItem>
          </DropdownButton>
        </div>
        <button class="flex rounded-md p-2" hover="text-primary-100 bg-primary-50" @click="theme.toggleDarkMode()">
          <icon-mdi-weather-night v-if="theme.darkMode" class="text-[1.2em]"></icon-mdi-weather-night>
          <icon-mdi-white-balance-sunny v-else class="text-[1.2em]"></icon-mdi-white-balance-sunny>
        </button>
        <!-- Profile dropdown -->
        <div v-if="authStore.user">
          <Menu>
            <MenuButton>
              <div class="flex items-center gap-2 rounded-md p-2" hover="text-primary-100 bg-primary-50">
                <UserAvatar :username="authStore.user.name" size="xs" :background="false"></UserAvatar>
                {{ authStore.user.name }}
              </div>
            </MenuButton>
            <MenuItems
              class="absolute top-24 flex flex-col mt-1 z-10 py-1 rounded border-t-2 border-primary-100 bg-background-light-0 dark:bg-background-dark-80 drop-shadow-xl"
            >
              <DropdownItem :to="'/' + authStore.user.name">{{ t("nav.user.profile") }}</DropdownItem>
              <DropdownItem to="/notifications">{{ t("nav.user.notifications") }}</DropdownItem>
              <hr />
              <DropdownItem to="/admin/flags">{{ t("nav.user.flags") }}</DropdownItem>
              <DropdownItem to="/admin/approval/projects">{{ t("nav.user.projectApprovals") }}</DropdownItem>
              <DropdownItem to="/admin/approval/versions">{{ t("nav.user.versionApprovals") }}</DropdownItem>
              <DropdownItem to="/admin/stats">{{ t("nav.user.stats") }}</DropdownItem>
              <DropdownItem to="/admin/health">{{ t("nav.user.health") }}</DropdownItem>
              <DropdownItem to="/admin/log">{{ t("nav.user.log") }}</DropdownItem>
              <DropdownItem to="/admin/versions">{{ t("nav.user.platformVersions") }}</DropdownItem>
              <hr />
              <DropdownItem @click="auth.logout()">{{ t("nav.user.logout") }}</DropdownItem>
            </MenuItems>
          </Menu>
        </div>

        <!-- Login/register buttons -->
        <div v-else class="flex gap-2">
          <a class="flex items-center rounded-md p-2" :href="auth.loginUrl($route.fullPath)" hover="text-primary-100 bg-primary-50">
            <icon-mdi-key-outline class="mr-1 text-[1.2em]" />
            {{ t("nav.login") }}
          </a>
          <router-link class="flex items-center rounded-md p-2" to="/signup" hover="text-primary-100 bg-primary-50">
            <icon-mdi-clipboard-outline class="mr-1 text-[1.2em]" />
            {{ t("nav.signup") }}
          </router-link>
        </div>
      </div>
    </nav>
  </header>
</template>

<style lang="css" scoped>
nav .router-link-active {
  color: #4080ff;
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
