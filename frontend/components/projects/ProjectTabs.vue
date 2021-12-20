<template>
    <v-tabs>
        <v-tab v-for="tab in tabs" :key="tab.title" :exact-path="!!tab.exact" :to="tab.external ? '/linkout?remoteUrl=' + tab.link : tab.link" nuxt>
            <v-icon left>
                {{ tab.icon }}
            </v-icon>
            {{ tab.title }}
            <v-icon v-if="tab.external" small class="mb-1 ml-1" color="primary">mdi-open-in-new</v-icon>
        </v-tab>
    </v-tabs>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { TranslateResult } from 'vue-i18n';
import { HangarProjectMixin } from '~/components/mixins';
import VisibilityChangerModal from '~/components/modals/VisibilityChangerModal.vue';
import Markdown from '~/components/markdown/Markdown.vue';

interface Tab {
    title: string | TranslateResult;
    icon: string;
    link: string;
    external: boolean;
    exact?: boolean;
}

@Component({
    components: { Markdown, VisibilityChangerModal },
})
export default class ProjectTabs extends HangarProjectMixin {
    get tabs(): Tab[] {
        const tabs = [] as Tab[];
        tabs.push({ title: this.$t('project.tabs.docs'), icon: 'mdi-book', link: this.slug, external: false, exact: true });
        tabs.push({
            title: this.$t('project.tabs.versions'),
            icon: 'mdi-download',
            link: this.slug + '/versions',
            external: false,
        });
        if ((this.project.settings.forumSync && this.project.postId) || this.$perms.canEditSubjectSettings) {
            tabs.push({
                title: this.$t('project.tabs.discuss'),
                icon: 'mdi-account-group',
                link: this.slug + '/discuss',
                external: false,
            });
        }
        if (this.$perms.canEditSubjectSettings) {
            tabs.push({
                title: this.$t('project.tabs.settings'),
                icon: 'mdi-cog',
                link: this.slug + '/settings',
                external: false,
            });
        }

        if (this.project.settings.homepage) {
            tabs.push({
                title: this.$t('project.tabs.homepage'),
                icon: 'mdi-home',
                link: this.project.settings.homepage,
                external: true,
            });
        }
        if (this.project.settings.issues) {
            tabs.push({
                title: this.$t('project.tabs.issues'),
                icon: 'mdi-bug',
                link: this.project.settings.issues,
                external: true,
            });
        }
        if (this.project.settings.source) {
            tabs.push({
                title: this.$t('project.tabs.source'),
                icon: 'mdi-code-tags',
                link: this.project.settings.source,
                external: true,
            });
        }
        if (this.project.settings.support) {
            tabs.push({
                title: this.$t('project.tabs.support'),
                icon: 'mdi-chat-question',
                link: this.project.settings.support,
                external: true,
            });
        }
        return tabs;
    }

    get slug(): string {
        return `/${this.project.namespace.owner}/${this.project.namespace.slug}`;
    }
}
</script>

<style lang="scss">
.v-tabs {
    margin-top: 12px;
}
.v-tabs-bar {
    background-color: #272727 !important;

    a:not(.v-tab--active) {
        & .v-icon--left {
            color: white !important;
        }
        color: white !important;
    }
}
.v-slide-group__prev--disabled,
.v-slide-group__next--disabled {
    display: none !important;
}
</style>
