<template>
    <div>
        <Wrapper clazz="header">
            <template v-if="!isPublic">
                <v-alert v-if="needsChanges" type="error" text>
                    <v-row>
                        <v-col class="grow">
                            <strong>{{ $t('visibility.notice.' + project.visibility) }}</strong>
                        </v-col>
                        <v-col v-if="$perms.canEditPage" class="shrink">
                            <v-btn v-if="$perms.canEditPage" :loading="loading.approval" color="warning" @click.stop="sendForApproval">
                                {{ $t('project.sendForApproval') }}
                            </v-btn>
                        </v-col>
                    </v-row>
                    <Markdown :raw="project.lastVisibilityChangeComment || 'Unknown'" style="z-index: 1; position: relative" class="mt-2" />
                </v-alert>
                <v-alert v-else-if="isSoftDeleted" type="error">
                    {{ $t('visibility.notice.' + project.visibility, [project.lastVisibilityChangeUserName]) }}
                </v-alert>
                <v-alert v-else type="error">
                    {{ $t('visibility.notice.' + project.visibility) }}
                    <Markdown v-if="project.lastVisibilityChangeComment" :raw="project.lastVisibilityChangeComment" />
                </v-alert>
            </template>
            <v-row>
                <v-col cols="1">
                    <UserAvatar
                        :username="project.name"
                        :href="'/' + project.namespace.owner + '/' + project.namespace.slug"
                        :avatar-url="$util.projectUrl(project.namespace.owner, project.namespace.slug)"
                        clazz="user-avatar-md"
                    ></UserAvatar>
                </v-col>
                <v-col cols="auto">
                    <h1 class="d-inline project-link">
                        <NuxtLink :to="'/' + project.namespace.owner">
                            {{ project.namespace.owner }}
                        </NuxtLink>
                        /
                        <NuxtLink :to="slug">
                            {{ project.name }}
                        </NuxtLink>
                    </h1>
                    <div>
                        <v-subheader>{{ project.description }}</v-subheader>
                    </div>
                </v-col>
                <v-spacer />
                <v-col v-if="isLoggedIn" cols="3">
                    <v-row no-gutters justify="end">
                        <v-tooltip v-if="!$util.isCurrentUser(project.owner.id)" bottom>
                            <template #activator="{ on }">
                                <v-btn icon @click="toggleStar" v-on="on">
                                    <v-icon v-if="project.userActions.starred" color="amber"> mdi-star </v-icon>
                                    <v-icon v-else color="amber"> mdi-star-outline </v-icon>
                                </v-btn>
                            </template>
                            <span v-if="project.userActions.starred">{{ $t('project.actions.unstar') }}</span>
                            <span v-else>{{ $t('project.actions.star') }}</span>
                        </v-tooltip>

                        <v-tooltip v-if="!$util.isCurrentUser(project.owner.id)" bottom>
                            <template #activator="{ on }">
                                <v-btn icon @click="toggleWatch" v-on="on">
                                    <v-icon v-if="project.userActions.watching"> mdi-eye-off </v-icon>
                                    <v-icon v-else> mdi-eye </v-icon>
                                </v-btn>
                            </template>
                            <span v-if="project.userActions.watching">{{ $t('project.actions.unwatch') }}</span>
                            <span v-else>{{ $t('project.actions.watch') }}</span>
                        </v-tooltip>
                        <FlagModal v-if="isLoggedIn && !$util.isCurrentUser(project.owner.id)" :project="project" activator-class="ml-1" />
                        <v-menu v-if="$perms.isStaff" bottom offset-y open-on-hover>
                            <template #activator="{ on, attrs }">
                                <v-btn v-bind="attrs" class="ml-1" color="info" v-on="on">
                                    {{ $t('project.actions.adminActions') }}
                                </v-btn>
                            </template>
                            <v-list>
                                <v-list-item :to="slug + '/flags'" nuxt>
                                    <v-list-item-title>
                                        {{ $t('project.actions.flagHistory', [project.info.flagCount]) }}
                                    </v-list-item-title>
                                </v-list-item>
                                <v-list-item :to="slug + '/notes'" nuxt>
                                    <v-list-item-title>
                                        {{ $t('project.actions.staffNotes', [project.info.noteCount]) }}
                                    </v-list-item-title>
                                </v-list-item>
                                <v-list-item :to="'/admin/log/?projectFilter=' + slug" nuxt>
                                    <v-list-item-title>
                                        {{ $t('project.actions.userActionLogs') }}
                                    </v-list-item-title>
                                </v-list-item>
                                <v-list-item :href="$util.forumUrl(project.namespace.owner)">
                                    <v-list-item-title>
                                        {{ $t('project.actions.forum') }}
                                        <v-icon>mdi-open-in-new</v-icon>
                                    </v-list-item-title>
                                </v-list-item>
                            </v-list>
                        </v-menu>
                    </v-row>
                </v-col>
            </v-row>
            <v-row>
                <v-tabs>
                    <v-tab
                        v-for="tab in tabs"
                        :key="tab.title"
                        :exact-path="!!tab.exactPath"
                        :to="tab.external ? '/linkout?remoteUrl=' + tab.link : tab.link"
                        nuxt
                    >
                        <v-icon left>
                            {{ tab.icon }}
                        </v-icon>
                        {{ tab.title }}
                        <v-icon v-if="tab.external" small class="mb-1 ml-1" color="primary"> mdi-open-in-new </v-icon>
                    </v-tab>
                </v-tabs>
            </v-row>
        </Wrapper>
        <Wrapper clazz="main-content">
            <NuxtChild class="mt-5 main-content" :project="project">
                <v-tab-item>
                    {{ $route.name }}
                </v-tab-item>
            </NuxtChild>
        </Wrapper>
    </div>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { Context } from '@nuxt/types';
import { HangarProject } from 'hangar-internal';
import { NavigationGuardNext, Route } from 'vue-router';
import { TranslateResult } from 'vue-i18n';
import Wrapper from '~/components/layouts/helpers/Wrapper';
import { Markdown } from '~/components/markdown';
import FlagModal from '~/components/modals/projects/FlagModal.vue';
import { UserAvatar } from '~/components/users';
import { Visibility } from '~/types/enums';
import { HangarComponent } from '~/components/mixins';

interface Tab {
    title: string | TranslateResult;
    icon: string;
    link: string;
    external: boolean;
    exactPath?: string;
}

@Component({
    components: {
        FlagModal,
        Wrapper,
        Markdown,
        UserAvatar,
    },
})
export default class ProjectPage extends HangarComponent {
    project!: HangarProject;
    loading = {
        approval: false,
    };

    head() {
        return this.$seo.head(
            this.project?.name,
            this.project?.description,
            this.$route,
            this.$util.projectUrl(this.project?.namespace.owner, this.project?.namespace.slug)
        );
    }

    async asyncData({ $api, params, $util }: Context) {
        const project = await $api
            .requestInternal<HangarProject>(`projects/project/${params.author}/${params.slug}`, false)
            .catch($util.handlePageRequestError);
        return { project };
    }

    get tabs(): Tab[] {
        const tabs = [] as Tab[];
        tabs.push({ title: this.$t('project.tabs.docs'), icon: 'mdi-book', link: this.slug, external: false, exactPath: this.slug });
        tabs.push({ title: this.$t('project.tabs.versions'), icon: 'mdi-download', link: this.slug + '/versions', external: false });
        if ((this.project.settings.forumSync && this.project.postId) || this.$perms.canEditSubjectSettings) {
            tabs.push({ title: this.$t('project.tabs.discuss'), icon: 'mdi-account-group', link: this.slug + '/discuss', external: false });
        }
        if (this.$perms.canEditSubjectSettings) {
            tabs.push({ title: this.$t('project.tabs.settings'), icon: 'mdi-cog', link: this.slug + '/settings', external: false });
        }

        if (this.project.settings.homepage) {
            tabs.push({ title: this.$t('project.tabs.homepage'), icon: 'mdi-home', link: this.project.settings.homepage, external: true });
        }
        if (this.project.settings.issues) {
            tabs.push({ title: this.$t('project.tabs.issues'), icon: 'mdi-bug', link: this.project.settings.issues, external: true });
        }
        if (this.project.settings.source) {
            tabs.push({ title: this.$t('project.tabs.source'), icon: 'mdi-code-tags', link: this.project.settings.source, external: true });
        }
        if (this.project.settings.support) {
            tabs.push({ title: this.$t('project.tabs.support'), icon: 'mdi-chat-question', link: this.project.settings.support, external: true });
        }
        return tabs;
    }

    get isPublic(): Boolean {
        return this.project.visibility === Visibility.PUBLIC;
    }

    get needsChanges(): Boolean {
        return this.project.visibility === Visibility.NEEDS_CHANGES;
    }

    get isSoftDeleted(): Boolean {
        return this.project.visibility === Visibility.SOFT_DELETE;
    }

    get slug(): string {
        return `/${this.project.namespace.owner}/${this.project.namespace.slug}`;
    }

    toggleStar() {
        this.$api
            .requestInternal<void>(`projects/project/${this.project.id}/star/${!this.project.userActions.starred}`, true, 'post')
            .then(() => {
                this.project.userActions.starred = !this.project.userActions.starred;
            })
            .catch((err) => this.$util.handleRequestError(err, 'project.error.star'));
    }

    toggleWatch() {
        this.$api
            .requestInternal(`projects/project/${this.project.id}/watch/${!this.project.userActions.watching}`, true, 'post')
            .then(() => {
                this.project.userActions.watching = !this.project.userActions.watching;
            })
            .catch((err) => this.$util.handleRequestError(err, 'project.error.watch'));
    }

    sendForApproval() {
        this.loading.approval = true;
        this.$api
            .requestInternal(`projects/visibility/${this.project.id}/sendforapproval`, true, 'post')
            .then(() => {
                this.$util.success(this.$t('projectApproval.sendForApproval'));
                this.$nuxt.refresh();
            })
            .catch(this.$util.handleRequestError)
            .finally(() => {
                this.loading.approval = false;
            });
    }

    // Need to refresh the project if anything has changed. idk if this is the best way to do this
    async beforeRouteUpdate(to: Route, _from: Route, next: NavigationGuardNext) {
        this.project = await this.$api
            .requestInternal<HangarProject>(`projects/project/${to.params.author}/${to.params.slug}`, false)
            .catch<any>(this.$util.handlePageRequestError);
        next();
    }
}
</script>
