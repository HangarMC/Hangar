<script setup lang="ts">
import type { Announcement as AnnouncementObject } from "hangar-api";
import { useInitialState } from "~/composables/useInitialState";
import { useInternalApi } from "~/composables/useApi";

// not sure if they need to be part of the initial state, since we directly render them, would only save a request on page switch at most, but I guess its a good demonstration
const announcements = await useInitialState<AnnouncementObject[]>(
    "announcements",
    async () => await useInternalApi<AnnouncementObject[]>("data/announcements", false)
);
</script>

<template>
    <template v-if="announcements">
        <Announcement v-for="(announcement, idx) in announcements" :key="idx" :announcement="announcement" />
    </template>
    <header class="bg-white">
        <div class="inner-header flex items-center">
            <div class="logo-and-nav flex items-center">
                <button class="mr-4">
                    <icon-mdi-menu style="font-size: 1.2em;"/>
                </button>


                <div class="site-logo mr-4 h-60px">
                    <img alt="Hangar Logo" src="/logo.svg" class="h-60px"/>
                </div>
                <nav>
                    <router-link :to="{ name: 'index' }">Home</router-link>
                    <router-link :to="{ name: 'staff' }">Team</router-link>
                </nav>
            </div>

            <div class="login-buttons">
                <p>login</p>
            </div>
        </div>
    </header>
</template>

<style lang="css" scoped>

.inner-header {
    max-width: 1200px;
    margin-left: auto;
    margin-right: auto;
    justify-content: space-between;
}

nav .router-link-active {
    color: #4080FF;
    font-weight: 700;
}
nav a.router-link-active:after {
    position: absolute;
    background: linear-gradient(-270deg, #004ee9 0%, #367aff 100%);
    transition: width .2s ease-in;
    width: 80%;
}

nav a {
    margin-right: 1em;
    position: relative;
}

nav a:after {
    content: "";
    width: 0;
    top: 30px;
    left: 10%;
    height: 4px;
    border-radius: 8px;
}

nav a:not(.router-link-active):hover:after {
    position: absolute;
    background: #d3e1f6;
    transition: width .2s ease-in;
    width: 80%;
}




</style>

