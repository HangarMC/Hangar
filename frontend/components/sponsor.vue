<template>
    <v-card>
        <v-card-subtitle v-text="$t('hangar.sponsoredBy')"></v-card-subtitle>
    </v-card>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { Sponsor } from 'hangar-api';

@Component
export default class HangarSponsor extends Vue {
    // TODO get sponsor from server, randomly
    sponsor: Sponsor = {
        img: '',
        title: '',
        link: '',
    };
}
</script>
