<template>
    <v-card>
        <v-card-subtitle>Sponsored by</v-card-subtitle>
    </v-card>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';

export interface Sponsor {
    img: String;
    title: String;
    link: String;
}

@Component
export default class Card extends Vue {
    // TODO get sponsor from server, randomly
    sponsor: Sponsor = {
        img: '',
        title: '',
        link: '',
    };
}
</script>
