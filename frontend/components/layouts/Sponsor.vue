<template>
    <v-card>
        <v-card-subtitle v-text="$t('hangar.sponsoredBy')"></v-card-subtitle>
        <v-card-text v-if="sponsor">
            <a :href="sponsor.link">
                <v-img :src="sponsor.image" :alt="sponsor.name" :title="sponsor.name" :height="50" contain />
            </a>
        </v-card-text>
    </v-card>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { Sponsor } from 'hangar-api';
import { Prop } from 'vue-property-decorator';

@Component
export default class HangarSponsor extends Vue {
    @Prop({ required: true })
    sponsor!: Sponsor;
}
</script>

<style lang="scss" scoped></style>
