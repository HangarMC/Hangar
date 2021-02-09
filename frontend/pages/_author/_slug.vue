<template>
    <div>
        <template v-if="!isPublic">
            <!-- todo alert for visibility stuff -->
            <v-alert v-if="needsChanges" type="error">
                <!-- todo remove is no EditPage perm -->
                <v-btn type="primary" :to="'/' + slug + '/manage/sendforapproval'">{{ $t('project.sendForApproval') }} </v-btn>
                <strong>{{ $t('visibility.notice.' + project.visibility) }}</strong>
                <br />
                <Markdown :input="project.lastVisibilityChangeComment || 'Unknown'" />
            </v-alert>
            <v-alert v-else-if="isSoftDeleted" type="error">
                {{ $t('visibility.notice.' + project.visibility, [project.lastVisibilityChangeUserName]) }}
            </v-alert>
            <v-alert v-else type="error">
                {{ $t('visibility.notice.' + project.visibility) }}
                <Markdown v-if="project.lastVisibilityChangeComment" :input="project.lastVisibilityChangeComment" />
            </v-alert>
        </template>
        <v-row>
            <v-col cols="1">
                <UserAvatar :username="project.namespace.owner" :avatar-url="project.iconUrl" clazz="user-avatar-sm"></UserAvatar>
            </v-col>
            <v-col cols="auto">
                <h1 class="d-inline">
                    <NuxtLink :to="'/' + project.namespace.owner">{{ project.namespace.owner }}</NuxtLink> /
                    <NuxtLink :to="slug">{{ project.name }}</NuxtLink>
                </h1>
                <div>
                    <v-subheader>{{ project.description }}</v-subheader>
                </div>
            </v-col>
            <v-spacer />
            <v-col v-if="$util.isLoggedIn()" cols="3">
                <v-row no-gutters justify="end">
                    <v-tooltip v-if="!$util.isCurrentUser(project.owner.id)" bottom>
                        <template #activator="{ on }">
                            <v-btn icon @click="toggleStar" v-on="on">
                                <v-icon v-if="project.userActions.starred" color="amber">mdi-star</v-icon>
                                <v-icon v-else color="amber">mdi-star-outline</v-icon>
                            </v-btn>
                        </template>
                        <span v-if="project.userActions.starred">{{ $t('project.actions.unstar') }}</span>
                        <span v-else>{{ $t('project.actions.star') }}</span>
                    </v-tooltip>

                    <v-tooltip v-if="!$util.isCurrentUser(project.owner.id)" bottom>
                        <template #activator="{ on }">
                            <v-btn icon @click="toggleWatch" v-on="on">
                                <v-icon v-if="project.userActions.watching">mdi-eye-off</v-icon>
                                <v-icon v-else>mdi-eye</v-icon>
                            </v-btn>
                        </template>
                        <span v-if="project.userActions.watching">{{ $t('project.actions.unwatch') }}</span>
                        <span v-else>{{ $t('project.actions.watch') }}</span>
                    </v-tooltip>
                    <!-- todo if not logged in or author, remove both -->
                    <FlagModal :project="project" />
                    <v-menu v-if="isStaff" bottom offset-y>
                        <template #activator="{ on, attrs }">
                            <v-btn v-bind="attrs" v-on="on">
                                {{ $t('project.actions.adminActions') }}
                            </v-btn>
                        </template>
                        <v-list-item :to="'/' + slug + '/flags'">
                            <v-list-item-title>
                                {{ $t('project.actions.flagHistory', []) }}
                            </v-list-item-title>
                        </v-list-item>
                        <v-list-item :to="'/' + slug + '/notes'">
                            <v-list-item-title>
                                {{ $t('project.actions.staffNotes', []) }}
                            </v-list-item-title>
                        </v-list-item>
                        <v-list-item :to="'/admin/log/?projectFilter=' + slug">
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
                    </v-menu>
                </v-row>
            </v-col>
        </v-row>
        <v-row>
            <v-tabs>
                <v-tab
                    v-for="tab in tabs"
                    :key="tab.title"
                    :exact="!tab.external"
                    :to="tab.external ? undefined : tab.link"
                    :href="tab.external ? tab.link : undefined"
                    :nuxt="!tab.external"
                >
                    <v-icon>{{ tab.icon }}</v-icon>
                    {{ tab.title }}
                    <v-icon v-if="tab.external">mdi-open-in-new</v-icon>
                </v-tab>
            </v-tabs>
        </v-row>
        <NuxtChild class="mt-4" :project="project">
            <v-tab-item>
                {{ $route.name }}
            </v-tab-item>
        </NuxtChild>
    </div>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { Context } from '@nuxt/types';
import { HangarProject } from 'hangar-internal';
import Markdown from '~/components/Markdown.vue';
import FlagModal from '~/components/modals/FlagModal.vue';
import UserAvatar from '~/components/UserAvatar.vue';
import { NamedPermission, Visibility } from '~/types/enums';

interface Tab {
    title: String;
    icon: String;
    link: String;
    external: Boolean;
}

@Component({
    components: {
        FlagModal,
        Markdown,
        UserAvatar,
    },
})
export default class ProjectPage extends Vue {
    project!: HangarProject;

    head() {
        return {
            title: this.project?.name,
        };
    }

    async asyncData({ $api, params, $util }: Context) {
        const project = await $api
            .requestInternal<HangarProject>(`projects/project/${params.author}/${params.slug}`, false)
            .catch($util.handlePageRequestError);
        return { project };
    }

    get tabs(): Tab[] {
        const tabs = [] as Tab[];
        tabs.push({ title: this.$t('project.tabs.docs') as String, icon: 'mdi-book', link: this.slug, external: false });
        tabs.push({ title: this.$t('project.tabs.versions') as String, icon: 'mdi-download', link: this.slug + '/versions', external: false });
        if (this.project.settings.forumSync) {
            tabs.push({ title: this.$t('project.tabs.discuss') as String, icon: 'mdi-account-group', link: this.slug + '/discuss', external: false });
        }
        if (this.$util.hasPerms(NamedPermission.EDIT_SUBJECT_SETTINGS)) {
            tabs.push({ title: this.$t('project.tabs.settings') as String, icon: 'mdi-cog', link: this.slug + '/settings', external: false });
        }

        if (this.project.settings.homepage) {
            tabs.push({ title: this.$t('project.tabs.homepage') as String, icon: 'mdi-home', link: this.project.settings.homepage, external: true });
        }
        if (this.project.settings.issues) {
            tabs.push({ title: this.$t('project.tabs.issues') as String, icon: 'mdi-bug', link: this.project.settings.issues, external: true });
        }
        if (this.project.settings.sources) {
            tabs.push({ title: this.$t('project.tabs.source') as String, icon: 'mdi-code-tags', link: this.project.settings.sources, external: true });
        }
        if (this.project.settings.support) {
            tabs.push({ title: this.$t('project.tabs.support') as String, icon: 'mdi-chat-question', link: this.project.settings.support, external: true });
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

    get visibilityComment() {
        // todo get last visibility change comment
        return null;
    }

    get slug(): String {
        return `/${this.project.namespace.owner}/${this.project.namespace.slug}`;
    }

    get isStaff(): boolean {
        return this.$util.hasPerms(NamedPermission.IS_STAFF);
    }

    toggleStar() {
        this.$api
            .requestInternal<void>(`projects/project/${this.project.id}/star/${!this.project.userActions.starred}`, true, 'post')
            .then(() => {
                this.project.userActions.starred = !this.project.userActions.starred;
            })
            .catch((err) => this.$util.handleRequestError(err, 'Could not toggle starred'));
    }

    toggleWatch() {
        this.$api
            .requestInternal(`projects/project/${this.project.id}/watch/${!this.project.userActions.watching}`, true, 'post')
            .then(() => {
                this.project.userActions.watching = !this.project.userActions.watching;
            })
            .catch((err) => this.$util.handleRequestError(err, 'Could not toggle watched'));
    }
}
</script>
