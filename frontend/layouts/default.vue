<template>
    <v-app>
        <Header />
        <v-main>
            <v-container>
                <Announcement v-for="(announcement, idx) in announcements" :key="idx" :announcement="announcement" />

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

@Component({
    components: {
        Header,
        Footer,
        Announcement,
        HangarSnackbar,
    },
})
export default class DefaultLayout extends Vue {
    title = 'Hangar';
    // TODO fetch from server
    announcements = [
        {
            text:
                'This is a staging server for testing purposes. Data could be deleted at any time. email confirmations are disabled. If you wanna help test, sneak into #hangar-dev',
            color: 'red lighten-1',
        },
    ];
}
</script>
