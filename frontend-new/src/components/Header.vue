<script setup lang="ts">
import type {Announcement as AnnouncementObject} from "hangar-api";
import {Popover, PopoverButton, PopoverPanel} from '@headlessui/vue'
import type {Ref} from 'vue';
import {useI18n} from 'vue-i18n';
import {ref} from 'vue';
import {useThemeStore} from '~/store/theme'
import {useAPI} from '~/store/api'
import Announcement from '~/components/Announcement.vue';

import hangarLogo from '/logo.svg'



const theme = useThemeStore()
const {t} = useI18n();

const api = useAPI();

const empty: AnnouncementObject[] = [];
const announcements: Ref<AnnouncementObject[]> = ref(empty);


api.getAnnouncements()
    .then((value) => {
        if (value) {
            const firstObject: AnnouncementObject | null = value[0];
            if (firstObject) {
                console.log(`Res: ${firstObject.text}`)
            } else {
                console.log("Res is undefined")
            }
        } else {
            console.log("value is null")
        }

        announcements.value = value;
    });


const navBarLinks = [
    {link: 'index', label: 'Home'},
    {link: 'staff', label: 'Team'},
];

const loggedIn = false; // TODO
</script>

<template>
    <template v-if="announcements">
        <Announcement v-for="(announcement, idx) in announcements" :key="idx" :announcement="announcement"/>
    </template>
    <header class="background-header">
        <div class="inner-header flex items-center max-w-1200px mx-auto justify-between h-65px w-[calc(100%-40px)]">
            <div class="logo-and-nav flex items-center">
                <Popover class="relative">
                    <PopoverButton v-slot="{ open }" class="flex mr-4">
                        <icon-mdi-menu
                            class="transition-transform text-[1.2em]"
                            :class="open
                            ? 'transform rotate-90'
                            : ''"/>
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
                            class="fixed z-10 w-9/10 background-header top-1/14 left-1/20 shadow1 rounded-md border-top-primary text-xs p-[20px]"
                            md="absolute w-max top-10 rounded-none rounded-bl-md rounded-r-md">
                            <p class="text-base font-semibold color-primary mb-4">Hangar</p>
                            <div class="grid grid-cols-2">
                                <router-link
                                    :to="{ name: 'index' }"
                                    class="flex items-center rounded-md px-6 py-2"
                                    hover="text-primary-100 bg-primary-50"
                                >
                                    Home
                                </router-link>
                                <router-link
                                    :to="{ name: 'staff' }"
                                    class="flex items-center rounded-md px-6 py-2"
                                    hover="text-primary-100 bg-primary-50"
                                >
                                    <icon-mdi-account-group class="mr-3 text-[1.2em]"/>
                                    Team
                                </router-link>
                            </div>

                            <p class="text-base font-semibold color-primary mb-4 mt-10">More from Paper</p>
                            <div class="grid grid-cols-2">
                                <a
                                    class="flex items-center rounded-md px-6 py-2" href="https://papermc.io/"
                                    hover="text-primary-100 bg-primary-50">
                                    <icon-mdi-home class="mr-3 text-[1.2em]"/>
                                    {{ t("nav.hangar.home") }}
                                </a>
                                <a
                                    class="flex items-center rounded-md px-6 py-2" href="https://forums.papermc.io/"
                                    hover="text-primary-100 bg-primary-50">
                                    <icon-mdi-forum class="mr-3 text-[1.2em]"/>
                                    {{ t("nav.hangar.forums") }}
                                </a>
                                <a
                                    class="flex items-center rounded-md px-6 py-2" href="https://github.com/PaperMC"
                                    hover="text-primary-100 bg-primary-50">
                                    <icon-mdi-code-braces class="mr-3 text-[1.2em]"/>
                                    {{ t("nav.hangar.code") }}
                                </a>
                                <a
                                    class="flex items-center rounded-md px-6 py-2"
                                    href="https://paper.readthedocs.io/en/latest/"
                                    hover="text-primary-100 bg-primary-50">
                                    <icon-mdi-book-open class="mr-3 text-[1.2em]"/>
                                    {{ t("nav.hangar.docs") }}
                                </a>
                                <a
                                    class="flex items-center rounded-md px-6 py-2" href="https://papermc.io/javadocs"
                                    hover="text-primary-100 bg-primary-50">
                                    <icon-mdi-language-java class="mr-3 text-[1.2em]"/>
                                    {{ t("nav.hangar.javadocs") }}
                                </a>
                                <a
                                    class="flex items-center rounded-md px-6 py-2" href="/"
                                    hover="text-primary-100 bg-primary-50">
                                    <icon-mdi-puzzle class="mr-3 text-[1.2em]"/>
                                    {{ t("nav.hangar.hangar") }}
                                </a>
                                <a
                                    class="flex items-center rounded-md px-6 py-2" href="https://papermc.io/downloads"
                                    hover="text-primary-100 bg-primary-50">
                                    <icon-mdi-download-circle class="mr-3 text-[1.2em]"/>
                                    {{ t("nav.hangar.downloads") }}
                                </a>
                                <a
                                    class="flex items-center rounded-md px-6 py-2" href="https://papermc.io/community"
                                    hover="text-primary-100 bg-primary-50">
                                    <icon-mdi-account-group class="mr-3 text-[1.2em]"/>
                                    {{ t("nav.hangar.community") }}
                                </a>
                                <a
                                    class="flex items-center rounded-md px-6 py-2"
                                    href="https://hangar-auth.benndorf.dev/" hover="text-primary-100 bg-primary-50">
                                    <icon-mdi-key class="mr-3 text-[1.2em]"/>
                                    {{ t("nav.hangar.auth") }}
                                </a>
                            </div>
                        </PopoverPanel>
                    </transition>
                </Popover>

                <div class="site-logo mr-4 h-60px flex items-center">
                    <img alt="Hangar Logo" :src="hangarLogo" class="h-50px object-cover"/>
                </div>
                <nav class="flex gap-3 invisible md:visible">
                    <router-link
                        v-for='navBarLink in navBarLinks'
                        :key='navBarLink.label'
                        :to="{ name: navBarLink.link }"
                        class="relative after:(absolute content-DEFAULT block w-0 top-30px left-1/10 h-4px rounded-8px)"
                    >
                        {{ navBarLink.label }}
                    </router-link>
                </nav>
            </div>

            <div class="login-buttons flex gap-2 items-center">
                <button class="flex mr-2" @click="theme.toggleDarkMode()">
                    <icon-mdi-weather-night v-if="theme.darkMode" class="text-[1.2em]"></icon-mdi-weather-night>
                    <icon-mdi-white-balance-sunny v-else class="text-[1.2em]"></icon-mdi-white-balance-sunny>
                </button>
                <div v-if="!loggedIn" class="flex">
                    <a
                        class="flex items-center rounded-md px-2 py-2"
                        href="https://hangar-auth.benndorf.dev/account/login" hover="text-primary-100 bg-primary-50">
                        <icon-mdi-key-outline class="mr-1 text-[1.2em]"/>
                        {{ t("nav.login") }}
                    </a>
                    <a
                        class="flex items-center rounded-md px-2 py-2"
                        href="https://hangar-auth.benndorf.dev/account/signup/" hover="text-primary-100 bg-primary-50">
                        <icon-mdi-clipboard-outline class="mr-1 text-[1.2em]"/>
                        {{ t("nav.signup") }}
                    </a>
                </div>

            </div>
        </div>
    </header>
</template>

<style lang="css" scoped>

nav .router-link-active {
    color: #4080FF;
    font-weight: 700;
}

nav a.router-link-active:after {
    background: linear-gradient(-270deg, #004ee9 0%, #367aff 100%);
    transition: width .2s ease-in;
    width: 80%;
}

nav a:not(.router-link-active):hover:after {
    background: #d3e1f6;
    transition: width .2s ease-in;
    width: 80%;
}
</style>

