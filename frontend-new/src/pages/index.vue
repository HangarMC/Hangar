<script setup lang="ts">
import {Menu, MenuButton, MenuItem, MenuItems} from "@headlessui/vue";

const {t} = useI18n();

const sorters = [
    {id: 'stars', label: t("project.sorting.mostStars")},
    {id: 'downloads', label: t("project.sorting.mostDownloads")},
    {id: 'views', label: t("project.sorting.mostViews")},
    {id: 'newest', label: t("project.sorting.newest")},
    {id: 'updated', label: t("project.sorting.recentlyUpdated")},
];

const versions = [
    {version: '1.18.1'},
    {version: '1.18'},
    {version: '1.17.1'},
    {version: '1.17'},
    {version: '1.16.5'},
    {version: '1.16.4'},
    {version: '1.16.3'},
    {version: '1.16.2'},
    {version: '1.16.1'},
    {version: '1.16'},
];

const categories = [
    {id: 'admin_tools', label: t("project.category.admin_tools")},
    {id: 'chat', label: t("project.category.chat")},
    {id: 'devel_tools', label: t("project.category.dev_tools")},
    {id: 'economy', label: t("project.category.economy")},
    {id: 'gameplay', label: t("project.category.gameplay")},
    {id: 'games', label: t("project.category.games")},
    {id: 'protection', label: t("project.category.protection")},
    {id: 'role_playing', label: t("project.category.role_playing")},
    {id: 'world_management', label: t("project.category.world_management")},
    {id: 'misc', label: t("project.category.misc")},
];

const platforms = [
    {id: 'paper', label: 'Paper'},
    {id: 'velocity', label: 'Velocity'},
    {id: 'waterfall', label: 'Waterfall'},
];

const licenses = [
    {id: 'gpl-3', label: 'GPL-3'},
    {id: 'mit', label: 'MIT'},
    {id: 'apache', label: 'APACHE'},
];
</script>

<template>
    <div class="flex flex-col items-center pt-10">
        <h2 class="text-3xl font-bold uppercase">Find your favorite plugins</h2>
        <!-- Search Bar & Sorting button -->
        <div class="flex flex-row mt-6 rounded-md big-box-shadow">
            <!-- Search Bar -->
            <input
                class="rounded-l-md p-3 w-[80vw] max-w-800px focus-visible:(border-white) text-black" type="text"
                placeholder="Search in 1 projects, proudly made by the community...">
            <!-- Sorting Button -->
            <div class="rounded-r-md w-100px bg-gradient-to-r from-[#004ee9] to-[#367aff]">
                <Menu>
                    <MenuButton
                        class="rounded-r-md h-1/1 text-left font-semibold flex flex-row items-center gap-2 text-white p-2">
                        <span>Sort by</span>
                        <icon-mdi-sort-variant class="text-[1.2em] pointer-events-none overflow-hidden"/>
                    </MenuButton>
                    <transition
                        enter-active-class="transition duration-100 ease-out"
                        enter-from-class="transform scale-95 opacity-0"
                        enter-to-class="transform scale-100 opacity-100"
                        leave-active-class="transition duration-75 ease-out"
                        leave-from-class="transform scale-100 opacity-100"
                        leave-to-class="transform scale-95 opacity-0"
                    >
                        <MenuItems
                            class="absolute flex flex-col z-10 background-header shadow1 rounded-md border-top-primary">
                            <MenuItem
                                v-for='sorter in sorters'
                                :key='sorter.id'
                                v-slot="{ active }">
                                <a
                                    :class='{ "bg-gradient-to-r from-[#004ee9] to-[#367aff] text-white": active }'
                                    class="p-2">
                                    {{ sorter.label }}
                                </a>
                            </MenuItem>
                        </MenuItems>
                    </transition>
                </Menu>
            </div>
        </div>

    </div>
    <div
        class="projectsAndSidebar p-4 mt-5 w-screen max-w-1200px flex justify-around m-auto flex-col md:flex-row gap-y-6 md:gap-y-0 md:gap-x-6">
        <!-- Projects -->
        <div class="md:(w-2/3 min-w-2/3 max-w-2/3) min-h-800px bg-gray-200 rounded-md">

        </div>
        <!-- Sidebar -->
        <div
            class="flex flex-col gap-4 bg-gradient-to-r from-[#004ee9] to-[#367aff] rounded-md min-w-300px min-h-800px p-4 text-white">

            <div class="versions">
                <h3 class="font-bold">Minecraft versions</h3>
                <div class="max-h-30 overflow-auto flex flex-col gap-2">
                    <label
                        v-for='version in versions' :key='version.version'
                        class="group relative cursor-pointer pl-30px customCheckboxContainer">
                        {{ version.version }}
                        <input :id='version.version' :name=version.version type="checkbox" class="hidden">
                        <span
                            class="absolute top-5px left-0 h-15px w-15px rounded-sm bg-white group-hover:(bg-gray-300) after:(absolute hidden content-DEFAULT top-3px left-6px w-5px h-10px border-solid border-r-3px border-b-3px)"/>
                    </label>
                </div>
            </div>
            <hr>
            <div class="categories">
                <h3 class="font-bold">Categories</h3>
                <div class="flex flex-col gap-2">
                    <label
                        v-for='category in categories' :key='category.id'
                        class="group relative cursor-pointer pl-30px customCheckboxContainer">
                        {{ category.label }}
                        <input type="checkbox" class="hidden">
                        <span
                            class="absolute top-5px left-0 h-15px w-15px rounded-sm bg-white group-hover:(bg-gray-300) after:(absolute hidden content-DEFAULT top-3px left-6px w-5px h-10px border-solid border-r-3px border-b-3px)"/>
                    </label>
                </div>
            </div>
            <hr>
            <div class="platforms">
                <h3 class="font-bold">Platforms</h3>
                <div class="flex flex-col gap-2">
                    <label
                        v-for='platform in platforms' :key='platform.id'
                        class="group relative cursor-pointer pl-30px customCheckboxContainer">
                        {{ platform.label }}
                        <input type="checkbox" class="hidden">
                        <span
                            class="absolute top-5px left-0 h-15px w-15px rounded-sm bg-white group-hover:(bg-gray-300) after:(absolute hidden content-DEFAULT top-3px left-6px w-5px h-10px border-solid border-r-3px border-b-3px)"/>
                    </label>
                </div>
            </div>
            <hr>
            <div class="licenses">
                <h3 class="font-bold">Licenses</h3>
                <div class="flex flex-col gap-2">
                    <label
                        v-for='license in licenses' :key='license.id'
                        class="group relative cursor-pointer pl-30px customCheckboxContainer">
                        {{ license.label }}
                        <input type="checkbox" class="hidden">
                        <span
                            class="absolute top-5px left-0 h-15px w-15px rounded-sm bg-white group-hover:(bg-gray-300) after:(absolute hidden content-DEFAULT top-3px left-6px w-5px h-10px border-solid border-r-3px border-b-3px)"/>
                    </label>
                </div>
            </div>
        </div>
    </div>
</template>

<route lang="yaml">
meta:
layout: default
</route>

<style lang="css" scoped>

.big-box-shadow {
    box-shadow: 0 0 10px 0 #004ee99e;
}

/*This is needed, because you cannot have more than one parent group in tailwind/windi*/
.customCheckboxContainer input:checked ~ span {
    background-color: #000 !important;
}

/*The tailwind/windi utility class rotate-45 is BROKEN*/
.customCheckboxContainer input:checked ~ span:after {
    display: block;
    transform: rotate(45deg);
}
</style>
