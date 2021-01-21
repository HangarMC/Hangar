<template>
    <v-app>
        <Header />

        <v-main>
            <v-container>
                <Announcement v-for="(announcement, idx) in announcements" :key="idx" :announcement="announcement" />

                <nuxt />
            </v-container>
        </v-main>

        <Footer />
    </v-app>
</template>

<script>
import { Component, Vue } from 'nuxt-property-decorator';

@Component
export default class DefaultLayout extends Vue {
    title = 'Hangar';
    // TODO fetch from server
    announcements = [
        {
            text:
                'This is a staging server for testing purposes. Data could be deleted at any time. email confirmations are disabled. If you wanna help test, sneak into #hangar-dev',
            color: '#ff544b',
        },
    ];
}
</script>
