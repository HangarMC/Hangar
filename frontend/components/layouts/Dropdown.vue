<template>
    <v-list dense>
        <slot name="pre" />
        <template v-for="(control, index) in controls">
            <v-list-item
                v-if="!control.isDivider"
                :key="control.title"
                link
                :to="isRouterLink(control) ? control.link : undefined"
                :nuxt="isRouterLink(control)"
                :href="isRouterLink(control) ? undefined : control.link"
                exact
                @click="control.action ? control.action() : undefined"
            >
                <v-list-item-icon>
                    <v-badge v-if="control.badge" offset-y="7" offset-x="7" left :content="control.badgeContent" :value="control.badgeContent">
                        <v-icon color="white">{{ control.icon }}</v-icon>
                    </v-badge>
                    <v-icon v-else color="white">{{ control.icon }}</v-icon>
                </v-list-item-icon>
                <v-list-item-content>
                    <v-list-item-title>{{ control.title }}</v-list-item-title>
                </v-list-item-content>
            </v-list-item>
            <v-divider v-else :key="index" class="my-2" />
        </template>
        <slot name="post" />
    </v-list>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'nuxt-property-decorator';
import { PropType } from 'vue';
import { TranslateResult } from 'vue-i18n';

export interface Control {
    icon?: string;
    title?: TranslateResult;
    badge?: boolean;
    badgeContent?: any;
    link?: string;
    action?: Function;
    isDivider?: boolean;
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
