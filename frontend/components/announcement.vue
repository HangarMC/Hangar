<template>
    <v-card class="announcement">
        <v-card-text class="announcement-text" :style="'background-color: ' + announcement.color">{{ announcement.text }}</v-card-text>
    </v-card>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { Prop } from 'vue-property-decorator';
import { Announcement } from 'hangar-api';
import { PropType } from 'vue';

@Component
export default class Card extends Vue {
    @Prop({ type: Object as PropType<Announcement>, required: true })
    announcement: Announcement;
}
</script>

<style lang="scss" scoped>
.announcement {
    text-align: center;
    margin-bottom: 1rem;
}

.announcement-text {
    padding: 0.25rem 0;
}
</style>
