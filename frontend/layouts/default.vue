<template>
    <v-app>
        <Header />
        <v-main>
            <v-container>
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
}
</script>
