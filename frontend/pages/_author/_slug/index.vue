<template>
    <v-row>
        <v-col v-if="page.contents" cols="12" md="8" class="main-page-content">
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
                    <ProjectInfo :project="project" />
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
import { Markdown, MarkdownEditor } from '~/components/markdown';
import { DocPageMixin } from '~/components/mixins';
import { DownloadButton, MemberList, ProjectPageList } from '~/components/projects';
import ProjectInfo from '~/components/projects/ProjectInfo.vue';

@Component({
    components: {
        ProjectInfo,
        DownloadButton,
        ProjectPageList,
        Markdown,
        MemberList,
        MarkdownEditor,
        Tag,
    },
})
export default class DocsPage extends DocPageMixin {
    roles!: Role[];

    async asyncData({ $api, params, $util }: Context) {
        const page = await $api.requestInternal<ProjectPage>(`pages/page/${params.author}/${params.slug}`, false).catch<any>($util.handlePageRequestError);
        const roles = await $api.requestInternal('data/projectRoles', false).catch($util.handlePageRequestError);
        return { page, roles };
    }
}
</script>

<style lang="scss">
.main-page-content {
    padding-left: 25px;
}
</style>
