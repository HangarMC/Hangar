<template>
    <v-stepper v-model="step">
        <v-stepper-header>
            <v-stepper-step step="1" :complete="step > 1">{{ $t('project.new.step1.title') }}</v-stepper-step>
            <v-divider />
            <v-stepper-step step="2" :complete="step > 2 && forms.step2" :rules="[() => noBasicSettingsError]"
                >{{ $t('project.new.step2.title') }}<small v-show="!noBasicSettingsError">Missing Information</small></v-stepper-step
            >
            <v-divider />
            <v-stepper-step step="3" :complete="step > 3"
                >{{ $t('project.new.step3.title') }}<small>{{ $t('project.new.step3.optional') }}</small></v-stepper-step
            >
            <v-divider />
            <v-stepper-step step="4" :complete="step > 4"
                >{{ $t('project.new.step4.title') }}<small>{{ $t('project.new.step4.optional') }}</small></v-stepper-step
            >
            <v-divider />
            <v-stepper-step step="5" :complete="!projectLoading">{{ $t('project.new.step5.title') }}</v-stepper-step>
        </v-stepper-header>
        <v-stepper-items>
            <StepperStepContent :step="1" @back="$router.push('/')" @continue="step = 2">
                <v-card class="mb-12 pa-1" min-height="200px">
                    <v-card-title v-if="$vuetify.breakpoint.smAndDown">
                        {{ $t('project.new.step1.title') }}
                    </v-card-title>
                    <!-- eslint-disable-next-line vue/no-v-html -->
                    <v-card-text v-html="$t('project.new.step1.text')"></v-card-text>
                </v-card>
            </StepperStepContent>
            <StepperStepContent :step="2" :allow-continue="noBasicSettingsError" @back="step = 1" @continue="step = 3">
                <v-card max-width="800" class="mx-auto">
                    <v-card-title v-if="$vuetify.breakpoint.smAndDown">
                        {{ $t('project.new.step2.title') }}
                    </v-card-title>
                    <v-form v-model="forms.step2">
                        <v-container>
                            <v-row justify="space-around">
                                <v-col cols="12" md="6">
                                    <v-select
                                        v-model="form.ownerId"
                                        :items="projectOwners"
                                        dense
                                        filled
                                        item-text="name"
                                        item-value="userId"
                                        :label="$t('project.new.step2.userSelect')"
                                        :rules="[$util.$vc.require($t('project.new.step2.userSelect'))]"
                                        :append-icon="createAsIcon"
                                    />
                                </v-col>
                                <v-col cols="12" md="6">
                                    <v-text-field
                                        v-model.trim="form.name"
                                        autofocus
                                        dense
                                        filled
                                        :error-messages="nameErrors"
                                        :label="$t('project.new.step2.projectName')"
                                        :rules="[$util.$vc.require($t('project.new.step2.projectName'))]"
                                        append-icon="mdi-form-textbox"
                                    />
                                </v-col>
                                <v-col cols="12" md="8">
                                    <v-text-field
                                        v-model.trim="form.description"
                                        dense
                                        filled
                                        clearable
                                        :label="$t('project.new.step2.projectSummary')"
                                        :rules="[$util.$vc.require($t('project.new.step2.projectSummary')), $util.$vc.maxLength(validations.project.desc.max)]"
                                        append-icon="mdi-card-text"
                                    />
                                </v-col>
                                <v-col cols="12" md="4">
                                    <v-select
                                        v-model="form.category"
                                        :prepend-inner-icon="categoryIcon"
                                        :items="$store.getters.visibleCategories"
                                        dense
                                        filled
                                        :label="$t('project.new.step2.projectCategory')"
                                        item-text="title"
                                        item-value="apiName"
                                        :rules="[$util.$vc.require($t('project.new.step2.projectCategory'))]"
                                    />
                                </v-col>
                            </v-row>
                        </v-container>
                    </v-form>
                </v-card>
            </StepperStepContent>
            <StepperStepContent :step="3" @back="step = 2" @continue="step = 4">
                <v-card max-width="800" class="mx-auto">
                    <v-card-title v-if="$vuetify.breakpoint.smAndDown">
                        {{ $t('project.new.step3.title') }}
                    </v-card-title>
                    <v-container>
                        <div class="text-h6 pt-1">
                            <v-icon color="info" large style="transform: rotate(-45deg)" class="mb-1">mdi-link</v-icon>
                            {{ $t('project.new.step3.links') }}
                        </div>
                        <v-divider class="mb-2" />
                        <v-row>
                            <v-col cols="12">
                                <v-text-field
                                    v-model.trim="form.settings.homepage"
                                    dense
                                    hide-details
                                    filled
                                    :label="$t('project.new.step3.homepage')"
                                    :rules="[$util.$vc.url]"
                                    append-icon="mdi-home-search"
                                />
                            </v-col>
                            <v-col cols="12">
                                <v-text-field
                                    v-model.trim="form.settings.issues"
                                    dense
                                    hide-details
                                    filled
                                    :label="$t('project.new.step3.issues')"
                                    :rules="[$util.$vc.url]"
                                    append-icon="mdi-bug"
                                />
                            </v-col>
                            <v-col cols="12">
                                <v-text-field
                                    v-model.trim="form.settings.source"
                                    dense
                                    hide-details
                                    filled
                                    :label="$t('project.new.step3.source')"
                                    :rules="[$util.$vc.url]"
                                    append-icon="mdi-source-branch"
                                />
                            </v-col>
                            <v-col cols="12">
                                <v-text-field
                                    v-model.trim="form.settings.support"
                                    dense
                                    hide-details
                                    filled
                                    :label="$t('project.new.step3.support')"
                                    :rules="[$util.$vc.url]"
                                    append-icon="mdi-face-agent"
                                />
                            </v-col>
                        </v-row>
                        <div class="text-h6 pt-5">
                            <v-icon color="info" large class="mb-1">mdi-license</v-icon>
                            {{ $t('project.new.step3.license') }}
                        </div>
                        <v-divider class="mb-2" />
                        <v-row>
                            <v-col cols="12" :md="isCustomLicense ? 4 : 6">
                                <v-select
                                    v-model="form.settings.license.type"
                                    dense
                                    hide-details
                                    filled
                                    clearable
                                    :items="licenses"
                                    :label="$t('project.new.step3.type')"
                                />
                            </v-col>
                            <v-col v-if="isCustomLicense" cols="12" md="8">
                                <v-text-field v-model.trim="form.settings.license.name" dense hide-details filled :label="$t('project.new.step3.customName')" />
                            </v-col>
                            <v-col cols="12" :md="isCustomLicense ? 12 : 6">
                                <v-text-field
                                    v-model.trim="form.settings.license.url"
                                    dense
                                    hide-details
                                    filled
                                    :label="$t('project.new.step3.url')"
                                    :rules="[$util.$vc.url]"
                                />
                            </v-col>
                        </v-row>
                        <div class="text-h6 pt-5">
                            <v-icon color="info" large class="mb-1">mdi-cloud-search</v-icon>
                            {{ $t('project.new.step3.seo') }}
                        </div>
                        <v-divider class="mb-2" />
                        <v-row>
                            <v-col cols="12">
                                <v-combobox
                                    v-model="form.settings.keywords"
                                    small-chips
                                    deletable-chips
                                    multiple
                                    dense
                                    hide-details
                                    filled
                                    clearable
                                    :delimiters="[' ', ',', '.']"
                                    :label="$t('project.new.step3.keywords')"
                                    append-icon="mdi-file-word-box"
                                />
                            </v-col>
                        </v-row>
                    </v-container>
                </v-card>
            </StepperStepContent>
            <StepperStepContent
                :step="4"
                @back="step = 3"
                @continue="
                    step = 5;
                    createProject();
                "
            >
                <v-tabs v-model="spigotConvertTab" fixed-tabs>
                    <v-tab v-text="$t('project.new.step4.convert')"></v-tab>
                    <v-tab v-text="$t('project.new.step4.preview')"></v-tab>
                    <v-tab v-text="$t('project.new.step4.tutorial')"></v-tab>
                </v-tabs>
                <v-tabs-items v-model="spigotConvertTab">
                    <v-tab-item>
                        <v-card-text>
                            <v-textarea v-model="converter.bbCode" hide-details dense :rows="6" filled :label="$t('project.new.step4.convertLabels.bbCode')" />
                            <div>
                                <v-btn block color="primary" class="my-3" :loading="converter.loading" @click="convertBBCode">
                                    <v-icon left large>mdi-chevron-double-down</v-icon>
                                    {{ $t('project.new.step4.convert') }}
                                    <v-icon right large>mdi-chevron-double-down</v-icon>
                                </v-btn>
                            </div>
                            <v-textarea
                                v-model="converter.markdown"
                                hide-details
                                dense
                                :rows="6"
                                filled
                                :label="$t('project.new.step4.convertLabels.output')"
                            />
                        </v-card-text>
                    </v-tab-item>
                    <v-tab-item>
                        <Markdown :raw="converter.markdown"></Markdown>
                    </v-tab-item>
                    <v-tab-item>
                        <v-card-text class="text-center">
                            {{ $t('project.new.step4.tutorialInstructions.line1') }}<br />
                            {{ $t('project.new.step4.tutorialInstructions.line2') }}<br />
                            <img src="https://i.imgur.com/8CyLMf3.png" alt="Edit Project" /><br />
                            {{ $t('project.new.step4.tutorialInstructions.line3') }}<br />
                            <img src="https://i.imgur.com/FLVIuQK.png" width="425" height="198" alt="Show BBCode" /><br />
                            {{ $t('project.new.step4.tutorialInstructions.line4') }}<br />
                        </v-card-text>
                    </v-tab-item>
                </v-tabs-items>
            </StepperStepContent>
            <StepperStepContent :step="5" hide-buttons>
                <v-card>
                    <v-card-text class="text-center">
                        <v-progress-circular v-if="projectLoading" indeterminate color="red" size="50"></v-progress-circular>
                        <div v-if="!projectError" class="text-h5 mt-2">{{ $t('project.new.step5.text') }}</div>
                        <template v-else>
                            <div class="text-h5 mt-2">{{ $t('project.new.error.create') }}</div>
                            <v-btn @click="retry">Retry</v-btn>
                        </template>
                    </v-card-text>
                </v-card>
            </StepperStepContent>
        </v-stepper-items>
    </v-stepper>
</template>

<script lang="ts">
import { Component, Watch } from 'nuxt-property-decorator';
import { Context } from '@nuxt/types';
import { ProjectOwner, ProjectSettingsForm } from 'hangar-internal';
import { AxiosError } from 'axios';
import { TranslateResult } from 'vue-i18n';
import StepperStepContent from '~/components/steppers/StepperStepContent.vue';
import { RootState } from '~/store';
import { ProjectCategory } from '~/types/enums';
import { HangarComponent } from '~/components/mixins';
import { LoggedIn } from '~/utils/perms';
import Markdown from '~/components/markdown/Markdown.vue';

interface NewProjectForm extends ProjectSettingsForm {
    ownerId: ProjectOwner['userId'];
    name: string;
    pageContent: string | null;
}

@Component({
    components: {
        Markdown,
        StepperStepContent,
    },
    head: {
        title: 'New Project',
    },
})
@LoggedIn
export default class NewProjectPage extends HangarComponent {
    step = 1;
    spigotConvertTab = 0;
    projectLoading = true;
    projectError = false;
    projectOwners!: ProjectOwner[];
    licenses!: string[];
    error = null as string | null;
    form: NewProjectForm = {
        category: ProjectCategory.ADMIN_TOOLS,
        settings: ({
            license: {} as ProjectSettingsForm['settings']['license'],
            donation: {} as ProjectSettingsForm['settings']['donation'],
            keywords: [],
        } as unknown) as ProjectSettingsForm['settings'],
    } as NewProjectForm;

    nameErrors: TranslateResult[] = [];

    converter = {
        bbCode: '',
        markdown: '',
        loading: false,
    };

    forms = {
        step2: false,
    };

    get categoryIcon() {
        return (this.$store.state as RootState).projectCategories.get(this.form.category)?.icon;
    }

    get createAsIcon() {
        return this.projectOwners.find((po) => po.userId === this.form.ownerId)?.isOrganization ? 'mdi-account-multiple' : 'mdi-account';
    }

    get isCustomLicense() {
        return this.form.settings.license.type === '(custom)';
    }

    get noBasicSettingsError() {
        return this.step !== 2 || this.forms.step2;
    }

    async asyncData({ $api, $util }: Context) {
        const data = await Promise.all([$api.requestInternal('projects/possibleOwners'), $api.requestInternal('data/licenses', false)]).catch(
            $util.handlePageRequestError
        );
        if (typeof data === 'undefined') return;
        return {
            projectOwners: data[0],
            licenses: data[1],
        };
    }

    created() {
        this.form.ownerId = this.projectOwners.find((po) => !po.isOrganization)?.userId!;
    }

    createProject() {
        this.$api
            .requestInternal<string>('projects/create', true, 'post', this.form)
            .then((url) => {
                this.$router.push(url);
            })
            .catch((err) => {
                this.projectError = true;
                this.$util.handleRequestError(err, 'project.new.error.create');
            })
            .finally(() => {
                this.projectLoading = false;
            });
    }

    convertBBCode() {
        this.converter.loading = true;
        this.$api
            .requestInternal<string>('pages/convert-bbcode', false, 'post', {
                content: this.converter.bbCode,
            })
            .then((markdown) => {
                this.converter.markdown = markdown;
            })
            .catch(this.$util.handleRequestError)
            .finally(() => {
                this.converter.loading = false;
            });
    }

    retry() {
        this.step = 1;
        this.projectLoading = true;
        this.projectError = false;
    }

    @Watch('form.name')
    onProjectNameChange(val: string) {
        if (!val) {
            this.nameErrors = [];
            return;
        }
        this.$api
            .requestInternal('projects/validateName', false, 'get', {
                userId: this.form.ownerId,
                value: val,
            })
            .then(() => {
                this.nameErrors = [];
            })
            .catch((err: AxiosError) => {
                this.nameErrors = [];
                if (!err.response?.data.isHangarApiException) {
                    return this.$util.handleRequestError(err);
                }
                this.nameErrors.push(this.$t(err.response.data.message));
            });
    }
}
</script>
