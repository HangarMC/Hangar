<template>
    <v-card max-width="400" class="mx-auto">
        <v-card-title v-text="error.statusCode" />
        <v-card-text v-text="text" />
        <v-card-actions class="text-center">
            <v-btn nuxt to="/" color="secondary">
                <v-icon left>mdi-home</v-icon>
                {{ $t('general.home') }}
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
    @Prop({ type: Object as PropType<NuxtError>, default: () => null })
    error!: NuxtError;

    get text() {
        switch (this.error.statusCode) {
            case 404:
                return this.$t('error.404');
            case 401:
                return this.$t('error.401');
            case 403:
                return this.$t('error.403');
            default:
                return this.$t('error.unknown');
        }
    }

    head() {
        let title = this.$t('error.unknown');
        switch (this.error.statusCode) {
            case 404:
                title = this.$t('error.404');
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
