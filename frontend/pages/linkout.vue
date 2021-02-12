<template>
    <v-col cols="12" md="6" offset-md="3">
        <v-card>
            <v-card-title>{{ $t('linkout.title') }}</v-card-title>
            <v-card-text v-text="$t('linkout.text', [target])" />
            <v-card-actions>
                <v-btn @click="goBack">{{ $t('linkout.abort') }}</v-btn>
                <v-btn :href="target" replace>{{ $t('linkout.continue') }}</v-btn>
            </v-card-actions>
        </v-card>
    </v-col>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';

@Component
export default class LinkoutPage extends Vue {
    get target() {
        return this.$route.query.remoteUrl;
    }

    goBack() {
        window.history.back();
    }
}
</script>

<style lang="scss" scoped></style>
