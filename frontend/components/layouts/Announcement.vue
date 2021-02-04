<template>
    <v-alert :color="announcement.color" dense>
        {{ announcement.text }}
    </v-alert>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { Prop } from 'vue-property-decorator';
import { Announcement as AnnouncementObject } from 'hangar-api';
import { PropType } from 'vue';

@Component
export default class Announcement extends Vue {
    @Prop({ type: Object as PropType<AnnouncementObject>, required: true })
    announcement!: AnnouncementObject;
}
</script>

<style lang="scss" scoped>
.v-alert {
    text-align: center;
    margin-bottom: 1rem;
    flex: 0 0 100%;
    max-width: 100%;
}
</style>
