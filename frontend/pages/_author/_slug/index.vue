<template>
    <v-row>
        <v-col v-if="page.contents" cols="12" md="8">
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
                        <v-card-title v-text="$t('project.info.title')" />
                        <v-card-text>
                            <DownloadButton
                                v-if="project.recommendedVersions && Object.keys(project.recommendedVersions).length > 0"
                                :project="project"
                                :platform-selection="true"
                                :small="false"
                            />

                            <div v-if="project.settings.donation.enable" style="margin-top: 5px">
                                <DonationModal
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
                                            <v-icon left> mdi-currency-usd </v-icon>
                                            {{ $t('general.donate') }}
                                        </v-btn>
                                    </template>
                                </DonationModal>
                            </div>

                            <div class="project-info">
                                <p>{{ $t('project.category.info', [$store.state.projectCategories.get(project.category).title]) }}</p>
                                <p>{{ $t('project.info.publishDate', [$util.prettyDate(project.createdAt)]) }}</p>
                                <p v-if="project">
                                    <span id="view-count">{{ $tc('project.info.views', project.stats.views, [project.stats.views]) }}</span
                                    >,&nbsp;<span id="download-count"
                                        >{{ $tc('project.info.totalDownloads', project.stats.downloads, [project.stats.downloads]) }}
                                    </span>
                                </p>
                                <p v-if="project">
                                    <v-btn :to="`${$route.params.slug}/stars`" nuxt small>
                                        {{ $tc('project.info.stars', project.stats.stars, [project.stats.stars]) }}
                                    </v-btn>
                                    <v-btn :to="`${$route.params.slug}/watchers`" nuxt small>
                                        {{ $tc('project.info.watchers', project.stats.watchers, [project.stats.watchers]) }}
                                    </v-btn>
                                </p>
                                <p v-if="project && project.settings.license && project.settings.license.name">
                                    {{ $t('project.license.link') }}
                                    <a ref="noopener" :href="project.settings.license.url" target="_blank">{{ project.settings.license.name }}</a>
                                </p>
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

@Component({
    components: { DownloadButton, ProjectPageList, Markdown, MemberList, DonationModal, MarkdownEditor, Tag },
})
export default class DocsPage extends DocPageMixin {
    roles!: Role[];

    get publicHost() {
        return process.env.publicHost;
    }

    async asyncData({ $api, params, $util }: Context) {
        const page = await $api.requestInternal<ProjectPage>(`pages/page/${params.author}/${params.slug}`, false).catch<any>($util.handlePageRequestError);
        const roles = await $api.requestInternal('data/projectRoles', false).catch($util.handlePageRequestError);
        return { page, roles };
    }
}
</script>

<style lang="scss" scoped>
.project-info p {
    margin-bottom: 3px;
}
</style>
