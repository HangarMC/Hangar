<template>
    <v-row>
        <v-col v-if="page.contents" cols="12" md="8" class="main-page-content">
            <v-divider />
            <MarkdownEditor
                v-if="$perms.canEditPage"
                ref="editor"
                :raw="page.contents"
                :editing.sync="editingPage"
                :deletable="page.deletable"
                @save="savePage"
            />
            <Markdown v-else :raw="page.contents" />
        </v-col>
        <v-col v-else>
            <v-progress-circular indeterminate color="primary" />
        </v-col>
        <v-col cols="12" md="4">
            <v-row>
                <v-col cols="12">
                    <v-card>
                        <v-card-title>
                            <v-col cols="auto">
                                {{ $t('project.info.title') }}
                            </v-col>
                            <v-col v-if="isLoggedIn && !$util.isCurrentUser(project.owner.id)" cols="auto">
                                <FlagModal :project="project" activator-class="ml-1" />
                            </v-col>
                            <v-col v-if="$perms.isStaff" cols="auto">
                                <v-menu bottom offset-y open-on-hover>
                                    <template #activator="{ on, attrs }">
                                        <v-btn v-bind="attrs" small class="ml-1" color="info" v-on="on">
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
                            </v-col>
                            <v-col cols="auto">
                                <DonationModal
                                    v-if="project.settings.donation.enable"
                                    :donation-email="project.settings.donation.email"
                                    :default-amount="project.settings.donation.defaultAmount"
                                    :one-time-amounts="project.settings.donation.oneTimeAmounts"
                                    :monthly-amounts="project.settings.donation.monthlyAmounts"
                                    :donation-target="project.namespace.owner + '/' + project.name"
                                    :return-url="publicHost + '/' + project.namespace.owner + '/' + project.name + '?donation=success'"
                                    :cancel-return-url="publicHost + '/' + project.namespace.owner + '/' + project.name + '?donation=failure'"
                                >
                                    <template #activator="{ on, attrs }">
                                        <v-btn v-bind="attrs" v-on="on">
                                            <v-icon left> mdi-currency-usd</v-icon>
                                            {{ $t('general.donate') }}
                                        </v-btn>
                                    </template>
                                </DonationModal></v-col
                            >

                            <v-col cols="auto">
                                <v-btn v-if="!$util.isCurrentUser(project.owner.id)" @click="toggleStar">
                                    <v-icon v-if="project.userActions.starred" left color="amber"> mdi-star</v-icon>
                                    <v-icon v-else lef> mdi-star-outline</v-icon>
                                    <span v-if="project.userActions.starred">{{ $t('project.actions.unstar') }}</span>
                                    <span v-else>{{ $t('project.actions.star') }}</span>
                                </v-btn>
                            </v-col>

                            <v-col cols="auto">
                                <v-btn v-if="!$util.isCurrentUser(project.owner.id)" @click="toggleWatch">
                                    <v-icon v-if="project.userActions.watching" left> mdi-eye-off</v-icon>
                                    <v-icon v-else left> mdi-eye</v-icon>
                                    <span v-if="project.userActions.watching">{{ $t('project.actions.unwatch') }}</span>
                                    <span v-else>{{ $t('project.actions.watch') }}</span>
                                </v-btn>
                            </v-col>
                        </v-card-title>

                        <v-card-text>
                            <v-divider class="mb-5" />

                            <div class="project-info">
                                <div class="float-left">
                                    <p>{{ $t('project.category.info') }}</p>
                                    <p>{{ $t('project.info.publishDate') }}</p>
                                    <p>{{ $tc('project.info.views', project.stats.views) }}</p>
                                    <p>{{ $tc('project.info.totalDownloads', project.stats.downloads) }}</p>
                                    <NuxtLink :to="`${$route.params.slug}/stars`" nuxt small class="d-block">
                                        {{ $tc('project.info.stars', project.stats.stars) }}
                                    </NuxtLink>
                                    <NuxtLink :to="`${$route.params.slug}/watchers`" nuxt small class="d-block">
                                        {{ $tc('project.info.watchers', project.stats.watchers) }}
                                    </NuxtLink>
                                    <p v-if="project && project.settings.license && project.settings.license.name">
                                        {{ $t('project.license.link') }}
                                    </p>
                                </div>
                                <div class="float-right">
                                    <p>{{ $store.state.projectCategories.get(project.category).title }}</p>
                                    <p>{{ $util.prettyDate(project.createdAt) }}</p>
                                    <p>{{ project.stats.views }}</p>
                                    <p>{{ project.stats.downloads }}</p>
                                    <p>{{ project.stats.stars }}</p>
                                    <p>{{ project.stats.watchers }}</p>
                                    <p v-if="project && project.settings.license && project.settings.license.name">
                                        <a ref="noopener" :href="project.settings.license.url" target="_blank">{{ project.settings.license.name }}</a>
                                    </p>
                                </div>
                            </div>
                        </v-card-text>
                    </v-card>
                </v-col>
                <v-col cols="12">
                    <v-card>
                        <v-card-title v-text="$t('project.promotedVersions')" />
                        <v-card-text>
                            <v-list>
                                <v-list-item
                                    v-for="(version, index) in project.promotedVersions"
                                    :key="`${index}-${version.version}`"
                                    :to="{
                                        name: 'author-slug-versions-version',
                                        params: { author: project.namespace.owner, slug: project.namespace.slug, version: version.version },
                                    }"
                                >
                                    {{ version.version }}
                                    <Tag v-for="(tag, idx) in version.tags" :key="idx" :color="tag.color" :data="tag.displayData" :name="tag.name" />
                                </v-list-item>
                            </v-list>
                        </v-card-text>
                    </v-card>
                </v-col>
                <v-col cols="12">
                    <ProjectPageList :project="project" />
                </v-col>
                <v-col cols="12">
                    <MemberList :members="project.members" :roles="roles" />
                </v-col>
            </v-row>
        </v-col>
    </v-row>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { ProjectPage } from 'hangar-internal';
import { Context } from '@nuxt/types';
import { Role } from 'hangar-api';
import Tag from '~/components/Tag.vue';
import DonationModal from '~/components/donation/DonationModal.vue';
import { Markdown, MarkdownEditor } from '~/components/markdown';
import { DocPageMixin } from '~/components/mixins';
import { DownloadButton, MemberList, ProjectPageList } from '~/components/projects';
import FlagModal from '~/components/modals/projects/FlagModal.vue';

@Component({
    components: {
        FlagModal,
        DownloadButton,
        ProjectPageList,
        Markdown,
        MemberList,
        DonationModal,
        MarkdownEditor,
        Tag,
    },
})
export default class DocsPage extends DocPageMixin {
    roles!: Role[];

    get publicHost() {
        return process.env.publicHost;
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

    get slug(): string {
        return `/${this.project.namespace.owner}/${this.project.namespace.slug}`;
    }

    async asyncData({ $api, params, $util }: Context) {
        const page = await $api.requestInternal<ProjectPage>(`pages/page/${params.author}/${params.slug}`, false).catch<any>($util.handlePageRequestError);
        const roles = await $api.requestInternal('data/projectRoles', false).catch($util.handlePageRequestError);
        return { page, roles };
    }
}
</script>

<style lang="scss">
.project-info {
    height: 170px; // no idea why I have to set a height...
    p {
        margin-bottom: 3px;
    }
    .float-right p {
        text-align: right;
    }
}

.flag-modal-trigger {
    display: inline;
}

.main-page-content {
    padding-left: 25px;
}
</style>
