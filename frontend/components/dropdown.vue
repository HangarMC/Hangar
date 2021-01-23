<template>
    <v-list dense>
        <v-list-item
            v-for="control in controls"
            :key="control.title"
            link
            :to="isRouterLink(control) ? control.link : null"
            :nuxt="isRouterLink(control)"
            :href="isRouterLink(control) ? null : control.link"
            @click="control.action ? control.action() : null"
        >
            <v-list-item-icon>
                <v-icon color="white">{{ control.icon }}</v-icon>
            </v-list-item-icon>
            <v-list-item-content>
                <v-list-item-title>{{ control.title }}</v-list-item-title>
            </v-list-item-content>
        </v-list-item>
    </v-list>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { Prop } from 'vue-property-decorator';
import { PropType } from 'vue';
import { TranslateResult } from 'vue-i18n';

export interface Control {
    icon: String;
    title: String | TranslateResult;
    link?: String;
    action?: Function;
}

@Component
export default class Dropdown extends Vue {
    @Prop({ type: Array as PropType<Control[]>, required: true })
    controls!: Control[];

    isRouterLink(control: Control): Boolean {
        return !!control.link && control.link.startsWith('/');
    }
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
