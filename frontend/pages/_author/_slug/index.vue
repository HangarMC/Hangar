<template>
    <v-row class="mt-5">
        <v-col v-if="!$fetchState.pending" cols="12" md="8">
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
                                            <v-icon>mdi-currency-usd</v-icon>
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
                    <v-card>
                        <v-card-title>
                            {{ $t('page.plural') }}
                            <!-- todo new page modal -->
                            <NewPageModal :pages="project.pages" :project-id="project.id" />
                        </v-card-title>
                        <v-card-text>
                            <!--TODO page tree view-->
                            <v-list v-if="rootPages">
                                <v-list-item v-for="page in rootPages" :key="page.id"> </v-list-item>
                            </v-list>
                            <div v-else class="text-center py-4">
                                <v-progress-circular indeterminate />
                            </div>
                        </v-card-text>
                    </v-card>
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
import { Component, Prop, Vue } from 'nuxt-property-decorator';
import { Page } from 'hangar-api';
import { HangarProject, ProjectPage } from 'hangar-internal';
import { PropType } from 'vue';
import MarkdownEditor from '~/components/MarkdownEditor.vue';
import Tag from '~/components/Tag.vue';
import DonationModal from '~/components/donation/DonationModal.vue';
import MemberList from '~/components/MemberList.vue';
import { NamedPermission } from '~/types/enums';
import Markdown from '~/components/Markdown.vue';
import NewPageModal from '~/components/modals/NewPageModal.vue';

@Component({
    components: { NewPageModal, Markdown, MemberList, DonationModal, MarkdownEditor, Tag },
})
export default class DocsPage extends Vue {
    editingPage: boolean = false;
    page = {} as ProjectPage;

    @Prop({ type: Object as PropType<HangarProject>, required: true })
    project!: HangarProject;

    rootPages: Array<Page> = [];

    async fetch() {
        const page = await this.$api
            .requestInternal<ProjectPage>(`pages/page/${this.$route.params.author}/${this.$route.params.slug}`, false)
            .catch(this.$util.handleRequestError);
        this.page = page || ({} as ProjectPage);
    }

    get canEdit(): boolean {
        return this.$util.hasPerms(NamedPermission.EDIT_PAGE);
    }

    $refs!: {
        editor: MarkdownEditor;
    };

    savePage(content: string) {
        this.$api
            .requestInternal(`pages/save/${this.project.id}/${this.page.id}`, true, 'post', {
                content,
            })
            .then(() => {
                this.page.contents = content;
                this.editingPage = false;
            })
            .catch((err) => {
                this.$refs.editor.loading.save = false;
                this.$util.handleRequestError(err, 'Unable to save page');
            });
    }

    // Jake: Project categories are stored in the global store, so just get the title from there
    // formatCategory(apiName: string) {
    //     const formatted = apiName.replace('_', ' ');
    //     return this.capitalize(formatted);
    // }
    //
    // capitalize(input: string) {
    //     return input
    //         .toLowerCase()
    //         .split(' ')
    //         .map((s: string) => s.charAt(0).toUpperCase() + s.substring(1))
    //         .join(' ');
    // }
}
</script>

<style lang="scss" scoped></style>
