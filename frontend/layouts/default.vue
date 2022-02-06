<template>
    <v-app>
        <div class="announcements">
            <template v-if="announcements">
                <Announcement v-for="(announcement, idx) in announcements" :key="idx" :announcement="announcement" />
            </template>
        </div>
        <Header />
        <v-main>
            <v-container id="main" fluid>
                <DonationResult />
                <nuxt />
            </v-container>
        </v-main>
        <HangarSnackbar />
        <Footer />
    </v-app>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { Announcement as AnnouncementObject } from 'hangar-api';
import Header from '~/components/layouts/Header.vue';
import Footer from '~/components/layouts/Footer.vue';
import Announcement from '~/components/layouts/Announcement.vue';
import HangarSnackbar from '~/components/layouts/HangarSnackbar.vue';
import DonationResult from '~/components/donation/DonationResult.vue';

@Component({
    components: {
        DonationResult,
        Header,
        Footer,
        Announcement,
        HangarSnackbar,
    },
})
export default class DefaultLayout extends Vue {
    title = 'Hangar';
    announcements: AnnouncementObject[] = [];

    async fetch() {
        this.announcements = await this.$api.requestInternal<AnnouncementObject[]>('data/announcements', false).catch<any>(this.$util.handlePageRequestError);
    }
}
</script>

<style lang="scss">
#main {
    padding: 0;
    min-height: 60vh;
}
.v-main {
    padding-bottom: 12px;
}

.theme--light.v-sheet.v-card:not(.v-sheet--outlined),
.theme--light.v-sheet.v-list,
.theme--light.v-application .accent {
    //This adds the box shadow from the PaperMC forums to boxes all over the site
    border-width: 0;
    border-style: solid;
    border-top-color: #d3e1f6;
    border-right-color: #d3e1f6;
    border-bottom-color: #d3e1f6;
    border-left-color: #d3e1f6;
    border-radius: 8px;
    background-color: #ffffff !important;
    box-shadow: 0 0 10px 1px rgb(88 106 153 / 27%) !important;
}
.theme--light.v-sheet.v-card:not(.v-sheet--outlined) *,
.theme--light.v-sheet.v-list *,
.theme--light.v-application .accent * {
    //This removes the double box shadow of sub-boxes
    box-shadow: none !important;
    border: none !important;
}

.theme--light.v-application,
.theme--light.v-footer {
    background: #f6f9ff !important;
}

@media (min-width: 769px) {
    ::-webkit-scrollbar {
        width: 10px;
    }
}
* {
    scrollbar-color: var(--v-primary-base) #f6f9ff;
    scrollbar-width: thin;
}

.theme--light::-webkit-scrollbar-track {
    background: #f6f9ff;
}
.theme--dark::-webkit-scrollbar-track {
    background: #121212;
}

::-webkit-scrollbar-thumb {
    background: var(--v-primary-base);
}
::-webkit-scrollbar-thumb:hover {
    background: #444;
}

.v-main {
    padding-top: 3em !important;
}

body,
.v-application,
*,
.v-application .text-h6,
.v-application .text-subtitle-2 {
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Oxygen', 'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue', sans-serif !important;
}

.theme--light .theme--light.v-input--switch .v-input--switch__thumb {
    //This makes the switches in light mode more visible
    color: #5a5a5a;
}
</style>
