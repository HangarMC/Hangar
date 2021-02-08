<template>
    <v-row>
        <v-col cols="12" md="8">
            <Editor />
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
                                <p>{{ $t('project.category.info', [formatCategory(project.category)]) }}</p>
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
                            <v-btn icon><v-icon>mdi-plus</v-icon></v-btn>
                        </v-card-title>
                        <v-card-text>
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
import { Component, Vue } from 'nuxt-property-decorator';
import { Page, Project } from 'hangar-api';
import { HangarProject } from 'hangar-internal';
import { Context } from '@nuxt/types';
import Editor from '~/components/Editor.vue';
import Tag from '~/components/Tag.vue';
import DonationModal from '~/components/donation/DonationModal.vue';
import MemberList from '~/components/MemberList.vue';
@Component({
    components: { MemberList, DonationModal, Editor, Tag },
})
export default class DocsPage extends Vue {
    project!: Project;
    rootPages: Array<Page> = [];

    async asyncData({ $api, params, $util }: Context) {
        const project = await $api
            .requestInternal<HangarProject>(`projects/project/${params.author}/${params.slug}`, false)
            .catch($util.handlePageRequestError);
        // todo add new route
        // const rootPages = await $api
        //     .requestInternal<HangarProject>(`projects/project/${params.author}/${params.slug}`, false)
        //     .catch($util.handlePageRequestError);
        return {
            project,
            // rootPages,
        };
    }

    formatCategory(apiName: string) {
        const formatted = apiName.replace('_', ' ');
        return this.capitalize(formatted);
    }

    capitalize(input: string) {
        return input
            .toLowerCase()
            .split(' ')
            .map((s: string) => s.charAt(0).toUpperCase() + s.substring(1))
            .join(' ');
    }
}
</script>

<style lang="scss" scoped></style>
