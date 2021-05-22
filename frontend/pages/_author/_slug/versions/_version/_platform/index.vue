<template>
    <div>
        <v-row>
            <v-col>
                <h1>{{ projectVersion.name }}</h1>
                <TagComponent :tag="channel" :short-form="true" />
                <v-subheader>{{ $t('version.page.subheader', [projectVersion.author, $util.prettyDate(projectVersion.createdAt)]) }}</v-subheader>
            </v-col>
            <v-col class="text-right">
                <v-subheader style="justify-content: end">
                    <i v-if="$perms.isReviewer && projectVersion.approvedBy" class="mr-1">{{
                        $t('version.page.adminMsg', [projectVersion.approvedBy, $util.prettyDate(projectVersion.createdAt)])
                    }}</i>
                    <v-icon v-if="projectVersion.recommended.includes(platform.enumName)" color="info" :title="$t('version.page.recommended')"
                        >mdi-diamond-stone</v-icon
                    >
                    <v-icon v-if="isReviewStateChecked" :title="approvalTooltip" color="success">mdi-check-circle-outline</v-icon>
                </v-subheader>

                <template v-if="$perms.isReviewer">
                    <v-btn v-if="isReviewStateChecked" small color="success" :to="$route.path + '/reviews'" nuxt>
                        <v-icon left>mdi-list-status</v-icon>
                        {{ $t('version.page.reviewLogs') }}
                    </v-btn>
                    <v-btn v-else-if="isUnderReview" small color="info" :to="$route.path + '/reviews'" nuxt>
                        <v-icon left>mdi-list-status</v-icon>
                        {{ $t('version.page.reviewLogs') }}
                    </v-btn>
                    <v-btn v-else color="success" small :to="$route.path + '/reviews'" nuxt>
                        <v-icon left>mdi-play</v-icon>
                        {{ $t('version.page.reviewStart') }}
                    </v-btn>
                </template>

                <v-tooltip
                    v-if="$perms.canEditVersion && projectVersion.visibility !== 'softDelete' && !projectVersion.recommended.includes(platform.enumName)"
                    bottom
                >
                    <template #activator="{ on }">
                        <v-btn color="info" small :loading="loading.recommend" @click.stop="setRecommended" v-on="on">
                            <v-icon left>mdi-diamond</v-icon>
                            {{ $t('version.page.setRecommended') }}
                        </v-btn>
                    </template>
                    <span>{{ $t('version.page.setRecommendedTooltip', [platform.name]) }}</span>
                </v-tooltip>

                <TextareaModal :title="$t('version.page.delete')" :label="$t('general.comment')" :submit="deleteVersion">
                    <template #activator="{ on, attrs }">
                        <!--TODO this button doesn't seem to show up when the version is restored, you have to reload the page. Unsure why-->
                        <v-btn v-if="$perms.canDeleteVersion && projectVersion.visibility !== 'softDelete'" small color="error" v-bind="attrs" v-on="on">{{
                            $t('version.page.delete')
                        }}</v-btn>
                    </template>
                </TextareaModal>

                <DownloadButton :small="true" :version="projectVersion" :project="project" :platform="platform"></DownloadButton>

                <v-menu v-if="$perms.canViewLogs || ($perms.isReviewer && projectVersion.visibility === 'softDelete') || $perms.canHardDeleteVersion" offset-y>
                    <template #activator="{ on, attrs }">
                        <v-btn v-ripple="false" small plain v-bind="attrs" v-on="on">
                            {{ $t('version.page.adminActions') }}
                            <v-icon right>mdi-chevron-down</v-icon>
                        </v-btn>
                    </template>
                    <v-list>
                        <!--todo route for user action log, with filtering-->
                        <v-list-item v-if="$perms.canViewLogs" nuxt :to="`/admin/log`">
                            <v-list-item-title>
                                {{ $t('version.page.userAdminLogs') }}
                            </v-list-item-title>
                        </v-list-item>
                        <v-list-item v-if="$perms.isReviewer && projectVersion.visibility === 'softDelete'" @click.stop="restoreVersion">
                            <v-list-item-title class="success--text">{{ $t('version.page.restore') }}</v-list-item-title>
                        </v-list-item>
                        <TextareaModal :title="$t('version.page.hardDelete')" :submit="hardDeleteVersion" :label="$t('general.comment')">
                            <template #activator="{ on, attrs }">
                                <v-list-item v-if="$perms.canHardDeleteVersion" v-bind="attrs" v-on="on">
                                    <v-list-item-title class="error--text">{{ $t('version.page.hardDelete') }}</v-list-item-title>
                                </v-list-item>
                            </template>
                        </TextareaModal>
                    </v-list>
                </v-menu>
            </v-col>
        </v-row>
        <v-row>
            <v-col cols="12" md="8">
                <v-alert v-if="requiresConfirmation" type="info" class="mb-8">{{ $t('version.page.unsafeWarning') }}</v-alert>
                <MarkdownEditor
                    v-if="$perms.canEditVersion"
                    ref="editor"
                    :raw="projectVersion.description"
                    :editing.sync="editingPage"
                    :deletable="false"
                    @save="savePage"
                />
                <Markdown v-else :raw="projectVersion.description" />
            </v-col>
            <v-col cols="12" md="4">
                <v-card>
                    <v-card-subtitle>{{ $t('version.page.platform') }}<PlatformVersionEditModal :project="project" :versions="versions" /></v-card-subtitle>
                    <v-card-text>
                        <v-icon v-text="`$vuetify.icons.${platform.name.toLowerCase()}`"></v-icon>
                        {{ $store.state.platforms.get($route.params.platform.toUpperCase()).name }}:
                        {{ platformTag.data }}
                    </v-card-text>
                    <v-divider />
                    <v-card-subtitle class="pb-0">
                        {{ $t('version.page.dependencies') }}
                        <DependencyEditModal :project="project" :versions="versions" />
                    </v-card-subtitle>
                    <v-list class="pa-2">
                        <v-list-item
                            v-for="dep in projectVersion.pluginDependencies[platform.name.toUpperCase()]"
                            :key="dep.name"
                            :href="dep.externalUrl || undefined"
                            :target="dep.externalUrl ? '_blank' : undefined"
                            :nuxt="!!dep.namespace"
                            :to="!!dep.namespace ? { name: 'author-slug', params: { author: dep.namespace.owner, slug: dep.namespace.slug } } : undefined"
                        >
                            <v-list-item-title>
                                {{ dep.name }}
                                <small v-if="dep.required">({{ $t('general.required') }})</small>
                            </v-list-item-title>
                        </v-list-item>
                    </v-list>
                </v-card>
            </v-col>
        </v-row>
    </div>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { Tag } from 'hangar-api';
import { TranslateResult } from 'vue-i18n';
import { HangarProjectVersionMixin } from '~/components/mixins';
import { ReviewState } from '~/types/enums';
import TagComponent from '~/components/Tag.vue';
import { Markdown, MarkdownEditor } from '~/components/markdown';
import PlatformVersionEditModal from '~/components/modals/versions/PlatformVersionEditModal.vue';
import DependencyEditModal from '~/components/modals/versions/DependencyEditModal.vue';
import TextareaModal from '~/components/modals/TextareaModal.vue';
import { DownloadButton } from '~/components/projects';

@Component({
    components: { DownloadButton, TextareaModal, DependencyEditModal, PlatformVersionEditModal, TagComponent, Markdown, MarkdownEditor },
})
export default class ProjectVersionPage extends HangarProjectVersionMixin {
    editingPage: boolean = false;
    loading = {
        recommend: false,
    };

    head() {
        return this.$seo.head(
            this.project.name + ' ' + this.projectVersion.name,
            null,
            this.$route,
            this.$util.projectUrl(this.project.namespace.owner, this.project.namespace.slug)
        );
    }

    get channel(): Tag | null {
        return this.projectVersion.tags?.find((t) => t.name === 'Channel') || null;
    }

    get approvalTooltip(): TranslateResult {
        return this.projectVersion.reviewState === ReviewState.PARTIALLY_REVIEWED
            ? this.$t('version.page.partiallyApproved')
            : this.$t('version.page.approved');
    }

    get platformTag(): Tag {
        return this.projectVersion.tags.find((t) => t.name === this.platform.name)!;
    }

    get requiresConfirmation(): boolean {
        return this.projectVersion.externalUrl !== null || this.projectVersion.reviewState !== ReviewState.REVIEWED;
    }

    $refs!: {
        editor: MarkdownEditor;
    };

    savePage(content: string) {
        this.$api
            .requestInternal(`versions/version/${this.project.id}/${this.projectVersion.id}/saveDescription`, true, 'post', {
                content,
            })
            .then(() => {
                this.projectVersion.description = content;
                this.editingPage = false;
            })
            .catch((err) => {
                this.$refs.editor.loading.save = false;
                this.$util.handleRequestError(err, 'page.new.error.save');
            });
    }

    setRecommended() {
        this.loading.recommend = true;
        this.$api
            .requestInternal(`versions/version/${this.project.id}/${this.projectVersion.id}/${this.platform.enumName}/recommend`, true, 'post')
            .then(() => {
                this.$util.success(this.$t('version.success.recommended', [this.platform.name]));
                this.$nuxt.refresh();
            })
            .catch(this.$util.handleRequestError)
            .finally(() => {
                this.loading.recommend = false;
            });
    }

    deleteVersion(comment: string) {
        return this.$api
            .requestInternal(`versions/version/${this.project.id}/${this.projectVersion.id}/delete`, true, 'post', {
                content: comment,
            })
            .then(() => {
                this.$util.success(this.$t('version.success.softDelete'));
                this.$nuxt.refresh();
            })
            .catch(this.$util.handleRequestError);
    }

    hardDeleteVersion(comment: string) {
        return this.$api
            .requestInternal(`versions/version/${this.project.id}/${this.projectVersion.id}/hardDelete`, true, 'post', {
                content: comment,
            })
            .then(() => {
                this.$util.success(this.$t('version.success.hardDelete'));
                this.$router.push({
                    name: 'author-slug-versions',
                    params: {
                        ...this.$route.params,
                    },
                });
            })
            .catch(this.$util.handleRequestError);
    }

    restoreVersion() {
        this.$api
            .requestInternal(`versions/version/${this.project.id}/${this.projectVersion.id}/restore`, true, 'post')
            .then(() => {
                this.$util.success(this.$t('version.success.restore'));
                this.$nuxt.refresh();
            })
            .catch(this.$util.handleRequestError);
    }
}
</script>

<style lang="scss" scoped></style>
