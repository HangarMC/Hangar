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
                                        :label="$t('project.new.step2.userselect')"
                                        :rules="[$util.$vc.require()]"
                                        :append-icon="createAsIcon"
                                    />
                                </v-col>
                                <v-col cols="12" md="6">
                                    <!-- todo custom rule to check if a name exist already -->
                                    <v-text-field
                                        v-model.trim="form.name"
                                        autofocus
                                        dense
                                        filled
                                        :label="$t('project.new.step2.projectname')"
                                        :rules="[$util.$vc.require('Name')]"
                                        append-icon="mdi-form-textbox"
                                    />
                                </v-col>
                                <v-col cols="12" md="8">
                                    <v-text-field
                                        v-model.trim="form.description"
                                        dense
                                        filled
                                        clearable
                                        :label="$t('project.new.step2.projectsummary')"
                                        :rules="[$util.$vc.require('Description')]"
                                        append-icon="mdi-card-text"
                                    />
                                </v-col>
                                <v-col cols="12" md="4">
                                    <v-select
                                        v-model="form.category"
                                        :append-icon="categoryIcon"
                                        :items="$store.getters.visibleCategories"
                                        dense
                                        filled
                                        :label="$t('project.new.step2.projectcategory')"
                                        item-text="title"
                                        item-value="apiName"
                                        :rules="[$util.$vc.require('Category')]"
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
                                    v-model.trim="form.links.homepage"
                                    dense
                                    hide-details
                                    filled
                                    :label="$t('project.new.step3.homepage')"
                                    append-icon="mdi-home-search"
                                />
                            </v-col>
                            <v-col cols="12">
                                <v-text-field
                                    v-model.trim="form.links.issues"
                                    dense
                                    hide-details
                                    filled
                                    :label="$t('project.new.step3.issues')"
                                    append-icon="mdi-bug"
                                />
                            </v-col>
                            <v-col cols="12">
                                <v-text-field
                                    v-model.trim="form.links.source"
                                    dense
                                    hide-details
                                    filled
                                    :label="$t('project.new.step3.source')"
                                    append-icon="mdi-source-branch"
                                />
                            </v-col>
                            <v-col cols="12">
                                <v-text-field
                                    v-model.trim="form.links.support"
                                    dense
                                    hide-details
                                    filled
                                    :label="$t('project.new.step3.support')"
                                    append-icon="mdi-face-agent"
                                />
                            </v-col>
                        </v-row>
                        <div class="text-h6 pt-5">
                            <v-icon color="info" large class="mb-1">mdi-license</v-icon>
                            {{ $t('project.new.step3.licence') }}
                        </div>
                        <v-divider class="mb-2" />
                        <v-row>
                            <v-col cols="12" :md="isCustomLicense ? 4 : 6">
                                <v-select
                                    v-model="form.license.type"
                                    dense
                                    hide-details
                                    filled
                                    clearable
                                    :items="licences"
                                    :label="$t('project.new.step3.type')"
                                />
                            </v-col>
                            <v-col v-if="isCustomLicense" cols="12" md="8">
                                <v-text-field v-model.trim="form.license.customName" dense hide-details filled :label="$t('project.new.step3.customName')" />
                            </v-col>
                            <v-col cols="12" :md="isCustomLicense ? 12 : 6">
                                <v-text-field v-model.trim="form.license.url" dense hide-details filled :label="$t('project.new.step3.url')" />
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
                                    v-model="form.keywords"
                                    small-chips
                                    deletable-chips
                                    multiple
                                    dense
                                    hide-details
                                    filled
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
                    <!-- todo spigot bbcode converter thingy -->
                    <v-tab-item>1 </v-tab-item>
                    <v-tab-item>2 </v-tab-item>
                    <v-tab-item>3 </v-tab-item>
                </v-tabs-items>
            </StepperStepContent>
            <StepperStepContent :step="5" hide-buttons>
                <v-card>
                    <v-card-text class="text-center">
                        <v-progress-circular v-if="projectLoading" indeterminate color="red" size="50"></v-progress-circular>
                        <div v-if="!projectError" class="text-h5 mt-2">{{ $t('project.new.step5.text') }}</div>
                        <template v-else>
                            <div class="text-h5 mt-2">{{ $t('project.new.error') }}</div>
                            <v-btn
                                @click="
                                    step = 1;
                                    projectLoading = true;
                                    projectError = false;
                                "
                                >Retry</v-btn
                            >
                        </template>
                    </v-card-text>
                </v-card>
            </StepperStepContent>
        </v-stepper-items>
    </v-stepper>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { Context } from '@nuxt/types';
import { ProjectOwner } from 'hangar-internal';
import StepperStepContent from '~/components/steppers/StepperStepContent.vue';
import { RootState } from '~/store';
import { ProjectCategory } from '~/types/enums';

interface NewProjectForm {
    ownerId: ProjectOwner['userId'];
    name: string;
    description: string;
    category: ProjectCategory;
    pageContent: string | null;
    links: {
        homepage: string | null;
        issues: string | null;
        source: string | null;
        support: string | null;
    };
    license: {
        type: string | null;
        url: string | null;
        customName: string | null;
    };
    keywords: string[];
}

@Component({
    components: {
        StepperStepContent,
    },
    head: {
        title: 'New Project',
    },
})
export default class NewPage extends Vue {
    step = 1;
    spigotConvertTab = 0;
    projectLoading = true;
    projectError = false;
    projectOwners!: ProjectOwner[];
    error = null as string | null;
    form = ({
        category: ProjectCategory.ADMIN_TOOLS,
        links: {},
        license: {},
        keywords: [],
    } as unknown) as NewProjectForm;

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
        return this.form.license.type === '(custom)';
    }

    get noBasicSettingsError() {
        return this.step !== 2 || this.forms.step2;
    }

    // TODO do we want to get those from the server? Jake: I think so, it'd be nice to admins to be able to configure default licenses, but not needed for MVP
    get licences() {
        return ['MIT', 'Apache 2.0', 'GPL', 'LGPL', '(custom)'];
    }

    async asyncData({ $api }: Context) {
        return {
            projectOwners: await $api.requestInternal<ProjectOwner[]>('projects/possibleOwners'),
        };
    }

    created() {
        this.form.ownerId = this.projectOwners.find((po) => !po.isOrganization)?.userId!;
    }

    createProject() {
        console.log(this.form);
        this.$api
            .requestInternal<string>('projects/create', true, 'post', this.form)
            .then((url) => {
                this.$router.push(url);
            })
            .catch((err) => {
                this.projectError = true;
                this.$util.handleRequestError(err, 'Unable to create project');
            })
            .finally(() => {
                this.projectLoading = false;
            });
    }
}
</script>
