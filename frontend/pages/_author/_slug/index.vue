<template>
    <v-row>
        <v-col v-if="page.contents" cols="12" md="8">
            <MarkdownEditor v-if="canEdit" ref="editor" :raw="page.contents" :editing.sync="editingPage" :deletable="page.deletable" @save="savePage" />
            <Markdown v-else :raw="page.contents" />
        </v-col>
        <v-col v-else>
            <v-progress-circular indeterminate color="primary" />
        </v-col>
        <v-col cols="12" md="4">
            <v-row>
                <v-col cols="12">
                    <v-card>
                        <v-card-title v-text="$t('project.info')"></v-card-title>
                        <v-card-text>
                            <!-- todo where do we get this from? -->
                            <v-btn-toggle v-if="project.recommendedVersionId">
                                <v-btn>
                                    <v-icon>mdi-download-outline</v-icon>
                                    {{ $t('general.download') }}
                                </v-btn>

                                <v-btn>
                                    <v-icon>mdi-content-copy</v-icon>
                                </v-btn>
                            </v-btn-toggle>

                            <!-- todo: make donation button toggleable in settings, get email and stuff into modal, translate -->
                            <div v-if="true">
                                <DonationModal
                                    donation-email="minidigger-author@hangar.minidigger.me"
                                    donation-target="paper/Test"
                                    return-url="http://localhost:8080/paper/Test?donation=success"
                                    cancel-return-url="http://localhost:8080/paper/Test?donation=failure"
                                >
                                    <template #activator="{ on, attrs }">
                                        <v-btn v-bind="attrs" v-on="on">
                                            <v-icon left>mdi-currency-usd</v-icon>
                                            {{ $t('general.donate') }}
                                        </v-btn>
                                    </template>
                                </DonationModal>
                            </div>

                            <div>
                                <p>{{ $t('project.category.info', [$store.state.projectCategories.get(project.category).title]) }}</p>
                                <p>{{ $t('project.publishDate', [$util.prettyDate(project.createdAt)]) }}</p>
                                <p v-if="project">
                                    <span id="view-count">{{ $t('project.views', [project.stats.views]) }}</span>
                                </p>
                                <p v-if="project">
                                    <span id="star-count">{{ project.stats.stars }}</span>
                                    <NuxtLink :to="project.namespace.slug + '/stars'">
                                        {{ project.stats.views !== 1 ? ' stars' : ' star' }}
                                    </NuxtLink>
                                </p>
                                <p v-if="project">
                                    <span id="watcher-count">{{ project.stats.watchers }}</span>
                                    <NuxtLink :to="project.namespace.slug + '/watchers'">
                                        {{ project.stats.views !== 1 ? ' watchers' : ' watcher' }}
                                    </NuxtLink>
                                </p>
                                <p v-if="project">
                                    <span id="download-count">{{ project.stats.downloads }} total download{{ project.stats.downloads !== 1 ? 's' : '' }}</span>
                                </p>
                                <p v-if="project && project.settings.license">
                                    {{ $t('project.license.link') }}
                                    <a ref="noopener" :href="project.settings.license.url" target="_blank">{{ project.settings.license.name }}</a>
                                </p>
                            </div>
                        </v-card-text>
                    </v-card>
                </v-col>
                <v-col cols="12">
                    <v-card>
                        <v-card-title v-text="$t('project.promotedVersions')"></v-card-title>
                        <v-card-text>
                            <v-list v-if="project">
                                <v-list-item v-for="(version, index) in project.promotedVersions" :key="`${index}-${version.version}`">
                                    {{ version.version.substring(0, version.version.lastIndexOf('.')) }}
                                    <Tag v-for="(tag, idx) in version.tags" :key="idx" :color="tag.color" :data="tag.displayData" :name="tag.name"></Tag>
                                </v-list-item>
                            </v-list>
                            <div v-else class="text-center py-4">
                                <v-progress-circular indeterminate />
                            </div>
                        </v-card-text>
                    </v-card>
                </v-col>
                <v-col cols="12">
                    <ProjectPageList :project="project" />
                </v-col>
                <!-- todo member list -->
                <v-col cols="12">
                    <MemberList />
                    <!--                    <MemberList-->
                    <!--                        :filtered-members-prop="filteredMembers"-->
                    <!--                        :can-manage-members="canManageMembers"-->
                    <!--                        :settings-call="ROUTES.parse('PROJECTS_SHOW_SETTINGS', project.ownerName, project.slug)"-->
                    <!--                    ></MemberList>-->
                </v-col>
            </v-row>
        </v-col>
    </v-row>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { ProjectPage } from 'hangar-internal';
import { Context } from '@nuxt/types';
import MarkdownEditor from '~/components/MarkdownEditor.vue';
import Tag from '~/components/Tag.vue';
import DonationModal from '~/components/donation/DonationModal.vue';
import MemberList from '~/components/MemberList.vue';
import Markdown from '~/components/Markdown.vue';
import NewPageModal from '~/components/modals/pages/NewPageModal.vue';
import { DocPageMixin } from '~/components/mixins';
import ProjectPageList from '~/components/projects/ProjectPageList.vue';

@Component({
    components: { ProjectPageList, NewPageModal, Markdown, MemberList, DonationModal, MarkdownEditor, Tag },
})
export default class DocsPage extends DocPageMixin {
    async asyncData({ $api, params, $util }: Context) {
        const page = await $api.requestInternal<ProjectPage>(`pages/page/${params.author}/${params.slug}`, false).catch<any>($util.handlePageRequestError);
        return { page };
    }
}
</script>
