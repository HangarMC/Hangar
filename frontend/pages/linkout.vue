<template>
    <v-col cols="12" md="6" offset-md="3">
        <v-card>
            <v-card-title>{{ $t('linkout.title') }}</v-card-title>
            <v-card-text v-text="$t('linkout.text', [target])" />
            <v-card-actions>
                <v-btn color="primary" @click="$router.back()">
                    {{ $t('linkout.abort') }}
                </v-btn>
                <v-btn :href="target" class="linkoutContinueButton">
                    {{ $t('linkout.continue') }}
                </v-btn>
            </v-card-actions>
        </v-card>
    </v-col>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { HangarComponent } from '~/components/mixins';

@Component({})
export default class LinkoutPage extends HangarComponent {
    head() {
        return this.$seo.head('Linkout', null, this.$route, null);
    }

    get target() {
        return this.$route.query.remoteUrl;
    }
}
</script>

<style lang="scss" scoped>
.theme--light .v-btn.v-btn--has-bg.linkoutContinueButton {
    background-color: transparent !important;
    color: black !important;
}
</style>
