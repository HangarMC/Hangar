<template>
    <div>
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
        <div class="project-header">
            <v-container>
                <v-row>
                    <v-col cols="auto" align-self="center">
                        <UserAvatar
                            :username="project.name"
                            :href="'/' + project.namespace.owner + '/' + project.namespace.slug"
                            :avatar-url="$util.projectUrl(project.namespace.owner, project.namespace.slug)"
                            clazz="user-avatar-md"
                        ></UserAvatar>
                    </v-col>
                    <v-col cols="auto" align-self="center">
                        <h1 class="d-inline">
                            <NuxtLink :to="'/' + project.namespace.owner">
                                {{ project.namespace.owner }}
                            </NuxtLink>
                            /
                            <NuxtLink :to="slug">
                                {{ project.name }}
                            </NuxtLink>
                        </h1>
                        <v-subheader>{{ project.description }}</v-subheader>
                    </v-col>
                    <v-spacer />
                    <v-col cols="auto" align-self="center">
                        <DownloadButton
                            v-if="project.recommendedVersions && Object.keys(project.recommendedVersions).length > 0"
                            :project="project"
                            :platform-selection="true"
                            :small="false"
                        />
                    </v-col>
                    <ProjectTabs :project="project"></ProjectTabs>
                </v-row>
            </v-container>
        </div>
        <v-container>
            <NuxtChild class="mt-5" :project="project">
                <v-tab-item>
                    {{ $route.name }}
                </v-tab-item>
            </NuxtChild>
        </v-container>
    </div>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { Context } from '@nuxt/types';
import { HangarProject } from 'hangar-internal';
import { NavigationGuardNext, Route } from 'vue-router';
import { Markdown } from '~/components/markdown';
import { UserAvatar } from '~/components/users';
import { Visibility } from '~/types/enums';
import { HangarComponent } from '~/components/mixins';
import ProjectTabs from '~/components/projects/ProjectTabs.vue';
import DownloadButton from '~/components/projects/DownloadButton.vue';

@Component({
    components: {
        DownloadButton,
        ProjectTabs,
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

<style lang="scss">
div.project-header {
    background-color: #1d1d1d;
    min-height: 125px;

    .container {
        padding: 15px 0 0 0;
        .row {
            margin: 0;
        }
    }

    .col:first-child {
        margin-left: 50px;
        padding: 0;
    }

    .user-avatar-wrap,
    .user-avatar {
        height: 95px;
        width: 95px;
    }

    a {
        text-decoration: none;
        color: white;
        font-weight: normal;
    }

    .v-subheader {
        padding: 0;
        height: 18px;
    }
}
</style>
