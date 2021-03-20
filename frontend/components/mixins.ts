import { Component, mixins, Prop, Vue, Watch } from 'nuxt-property-decorator';
import { PropType } from 'vue';
import { HangarProject, HangarUser, HangarVersion, IPlatform, ProjectPage } from 'hangar-internal';
import MarkdownEditor from '~/components/markdown/MarkdownEditor.vue';
import { Platform, ReviewState } from '~/types/enums';
import { RootState } from '~/store';

@Component
export class HangarUserMixin extends Vue {
    @Prop({ type: Object as PropType<HangarUser>, required: true })
    user!: HangarUser;
}

@Component
export class HangarProjectMixin extends Vue {
    @Prop({ type: Object as PropType<HangarProject>, required: true })
    project!: HangarProject;
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

@Component
export class HangarModal extends Vue {
    dialog: boolean = false;

    @Prop({ type: String, default: '' })
    activatorClass!: string;

    @Watch('dialog')
    onToggleView(val: boolean) {
        if (!val) {
            this.$nextTick(() => {
                if (document.activeElement instanceof HTMLElement) {
                    document.activeElement.blur();
                }
                if (typeof this.$refs.modalForm !== 'undefined') {
                    // @ts-ignore
                    this.$refs.modalForm.reset();
                }
            });
        }
    }
}

@Component
export class HangarForm extends Vue {
    loading: boolean = false;
    validForm: boolean = false;
}

@Component
export class HangarFormModal extends mixins(HangarModal, HangarForm) {}
