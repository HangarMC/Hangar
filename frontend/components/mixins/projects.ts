import { Component, Prop } from 'nuxt-property-decorator';
import { PropType } from 'vue';
import { HangarProject, HangarVersion, IPlatform, ProjectPage } from 'hangar-internal';
import { HangarComponent } from './base';
import { Platform, ReviewState } from '~/types/enums';
import { RootState } from '~/store';
import MarkdownEditor from '~/components/markdown/MarkdownEditor.vue';

@Component
export class HangarProjectMixin extends HangarComponent {
    @Prop({ type: Object as PropType<HangarProject>, required: true })
    project!: HangarProject;

    get slug(): string {
        return `/${this.project.namespace.owner}/${this.project.namespace.slug}`;
    }
}

@Component
export class HangarProjectVersionMixin extends HangarProjectMixin {
    @Prop({ type: Map as PropType<Map<Platform, HangarVersion>>, required: true })
    versions!: Map<Platform, HangarVersion>;

    get projectVersion(): HangarVersion {
        return this.versions.get(<Platform>this.$route.params.platform.toUpperCase())!;
    }

    get platform(): IPlatform {
        return (this.$store.state as RootState).platforms.get(this.$route.params.platform.toUpperCase() as Platform)!;
    }

    get isReviewStateChecked(): boolean {
        return this.projectVersion.reviewState === ReviewState.PARTIALLY_REVIEWED || this.projectVersion.reviewState === ReviewState.REVIEWED;
    }

    get isUnderReview(): boolean {
        return this.projectVersion.reviewState === ReviewState.UNDER_REVIEW;
    }
}

@Component
export class DocPageMixin extends HangarProjectMixin {
    editingPage: boolean = false;
    page = {
        contents: '',
        deletable: false,
    } as ProjectPage;

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
                this.$util.handleRequestError(err, 'page.new.error.save');
            });
    }

    deletePage() {
        this.$api
            .requestInternal(`pages/delete/${this.project.id}/${this.page.id}`, true, 'post')
            .then(() => {
                this.$refs.editor.loading.delete = false;
                this.$router.replace(`/${this.$route.params.author}/${this.$route.params.slug}`);
            })
            .catch(this.$util.handleRequestError);
    }
}
