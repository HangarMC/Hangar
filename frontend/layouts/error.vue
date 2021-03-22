<template>
    <v-card max-width="400" class="mx-auto">
        <v-card-title v-text="error.statusCode" />
        <v-card-text v-text="text" />
        <v-card-actions class="text-center">
            <v-btn nuxt to="/" color="secondary">
                <v-icon left>mdi-home</v-icon>
                Home
            </v-btn>
        </v-card-actions>
    </v-card>
</template>

<script lang="ts">
import { PropType } from 'vue';
import { Component, Prop, Vue } from 'nuxt-property-decorator';
import { NuxtError } from '@nuxt/types';

@Component({
    layout: 'empty',
})
export default class ErrorPage extends Vue {
    // TODO i18n
    pageNotFound = '404 Not Found';
    unknownError = 'An error occurred';

    @Prop({ type: Object as PropType<NuxtError>, default: () => null })
    error!: NuxtError;

    get text() {
        switch (this.error.statusCode) {
            case 404:
                return this.pageNotFound;
            case 401:
                return 'You must be logged in for this';
            case 403:
                return 'You do not have permission to do that';
            default:
                return this.unknownError;
        }
    }

    head() {
        let title = this.unknownError;
        switch (this.error.statusCode) {
            case 404:
                title = this.pageNotFound;
                break;
            case 401:
                title = this.error.message!;
                break;
        }
        return {
            title,
        };
    }
}
</script>

<style scoped>
h1 {
    font-size: 20px;
}
</style>
