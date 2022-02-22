<script setup lang="ts">
import type {Announcement as AnnouncementObject} from "hangar-api";
import { Popover, PopoverButton, PopoverPanel } from '@headlessui/vue'
import {useThemeStore} from '~/store/theme'

import {useInitialState} from "~/composables/useInitialState";
import {useInternalApi} from "~/composables/useApi";

// not sure if they need to be part of the initial state, since we directly render them, would only save a request on page switch at most, but I guess its a good demonstration
/* const announcements = await useInitialState<AnnouncementObject[]>(
    "announcements",
    async () => await useInternalApi<AnnouncementObject[]>("data/announcements", false)
); */ // TODO: This breaks click events

const theme = useThemeStore()


const navBarLinks = [
    {link: 'index', label: 'Home'},
    {link: 'staff', label: 'Team'},
];
</script>

<template>
    <template v-if="announcements">
        <Announcement v-for="(announcement, idx) in announcements" :key="idx" :announcement="announcement"/>
    </template>
    <header class="bg-white dark:bg-headerDark">
        <div class="inner-header flex items-center max-w-1200px mx-auto justify-between h-65px w-[calc(100%-40px)]">
            <div class="logo-and-nav flex items-center">
                <Popover class="relative">
                    <PopoverButton v-slot="{ open }" class="flex mr-4">
                        <icon-mdi-menu
                            class="transition-transform"
                            :class="open
                            ? 'transform rotate-90'
                            : ''" style="font-size: 1.2em;"/>
                    </PopoverButton>
                    <transition
                        enter-active-class="transition duration-200 ease-out"
                        enter-from-class="translate-y-1 opacity-0"
                        enter-to-class="translate-y-0 opacity-100"
                        leave-active-class="transition duration-150 ease-in"
                        leave-from-class="translate-y-0 opacity-100"
                        leave-to-class="translate-y-1 opacity-0"
                    >
                        <PopoverPanel class="fixed md:absolute z-10 w-9/10 md:w-max bg-white top-1/14 md:top-10 left-1/20 shadow1 rounded-md md:rounded-none md:rounded-bl-md md:rounded-r-md border-solid border-t-4 border-t-paperblue text-xs p-[20px]">
                            <p class="text-base font-semibold text-paperblue mb-4">Hangar</p>
                            <div class="grid grid-cols-2">
                                <router-link
                                    :to="{ name: 'index' }"
                                    class="flex items-center rounded-md px-6 py-2"
                                    hover="text-paperblue bg-paperlightblue"
                                >
                                    <icon-mdi-home class="mr-3" style="font-size: 1.2em;"/>
                                    Home
                                </router-link>
                                <router-link
                                    :to="{ name: 'staff' }"
                                    class="flex items-center rounded-md px-6 py-2"
                                    hover="text-paperblue bg-paperlightblue"
                                >
                                    <icon-mdi-account-group class="mr-3" style="font-size: 1.2em;"/>
                                    Team
                                </router-link>
                            </div>

                            <p class="text-base font-semibold text-paperblue mb-4 mt-10">More from Paper</p>
                            <div class="grid grid-cols-2">
                                <a class="flex items-center rounded-md px-6 py-2" href="https://papermc.io/" hover="text-paperblue bg-paperlightblue">
                                    <icon-mdi-home class="mr-3" style="font-size: 1.2em;"/>
                                    Paper Home
                                </a>
                                <a class="flex items-center rounded-md px-6 py-2" href="https://forums.papermc.io/" hover="text-paperblue bg-paperlightblue">
                                    <icon-mdi-forum class="mr-3" style="font-size: 1.2em;"/>
                                    Forums
                                </a>
                                <a class="flex items-center rounded-md px-6 py-2" href="https://github.com/PaperMC" hover="text-paperblue bg-paperlightblue">
                                    <icon-mdi-code-braces class="mr-3" style="font-size: 1.2em;"/>
                                    Code
                                </a>
                                <a class="flex items-center rounded-md px-6 py-2" href="https://paper.readthedocs.io/en/latest/" hover="text-paperblue bg-paperlightblue">
                                    <icon-mdi-book-open class="mr-3" style="font-size: 1.2em;"/>
                                    Docs
                                </a>
                                <a class="flex items-center rounded-md px-6 py-2" href="https://papermc.io/javadocs" hover="text-paperblue bg-paperlightblue">
                                    <icon-mdi-language-java class="mr-3" style="font-size: 1.2em;"/>
                                    JavaDocs
                                </a>
                                <a class="flex items-center rounded-md px-6 py-2" href="/" hover="text-paperblue bg-paperlightblue">
                                    <icon-mdi-puzzle class="mr-3" style="font-size: 1.2em;"/>
                                    Hangar (Plugins)
                                </a>
                                <a class="flex items-center rounded-md px-6 py-2" href="https://papermc.io/downloads" hover="text-paperblue bg-paperlightblue">
                                    <icon-mdi-download-circle class="mr-3" style="font-size: 1.2em;"/>
                                    Downloads
                                </a>
                                <a class="flex items-center rounded-md px-6 py-2" href="https://papermc.io/community" hover="text-paperblue bg-paperlightblue">
                                    <icon-mdi-account-group class="mr-3" style="font-size: 1.2em;"/>
                                    Community
                                </a>
                                <a class="flex items-center rounded-md px-6 py-2" href="https://hangar-auth.benndorf.dev/" hover="text-paperblue bg-paperlightblue">
                                    <icon-mdi-key class="mr-3" style="font-size: 1.2em;"/>
                                    Authentication Portal
                                </a>
                            </div>
                        </PopoverPanel>
                    </transition>
                </Popover>

                <div class="site-logo mr-4 h-60px flex items-center">
                    <img alt="Hangar Logo" src="/logo.svg" class="h-50px object-cover"/>
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
                <button class="flex" @click="theme.toggleDarkMode()">
                    <icon-mdi-weather-night v-if="theme.darkMode" style="font-size: 1.2em;"></icon-mdi-weather-night>
                    <icon-mdi-white-balance-sunny v-else style="font-size: 1.2em;"></icon-mdi-white-balance-sunny>
                </button>
                <p>login</p>
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

