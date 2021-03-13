<template>
    <v-app>
        <Header />
        <v-main>
            <v-container>
                <template v-if="announcements">
                    <Announcement v-for="(announcement, idx) in announcements" :key="idx" :announcement="announcement" />
                </template>

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
    announcements: Announcement[] = [];

    async fetch() {
        this.announcements = await this.$api.requestInternal<Announcement[]>('data/announcements', false).catch<any>(this.$util.handlePageRequestError);
    }
}
</script>
