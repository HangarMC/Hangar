<template>
    <v-row>
        <v-col cols="12" md="8">
            <v-card>
                <v-card-title class="sticky">
                    {{ $t('project.settings.title') }}
                    <v-btn @click="save">
                        <v-icon>mdi-check</v-icon>
                        {{ $t('project.settings.save') }}
                    </v-btn>
                </v-card-title>
                <v-card-text>
                    <div>
                        <h2>{{ $t('project.settings.category') }}</h2>
                        <p>{{ $t('project.settings.categorySub') }}</p>
                        <v-select
                            v-model="form.category"
                            :append-icon="categoryIcon"
                            :items="$store.getters.visibleCategories"
                            dense
                            filled
                            item-text="title"
                            item-value="apiName"
                        />
                    </div>
                    <v-divider />
                    <div>
                        <h2>
                            {{ $t('project.settings.keywords') }}&nbsp;<small>{{ $t('project.settings.optional') }}</small>
                        </h2>
                        <p>{{ $t('project.settings.keywordsSub') }}</p>
                        <v-combobox
                            v-model="form.keywords"
                            small-chips
                            deletable-chips
                            multiple
                            dense
                            hide-details
                            filled
                            :delimiters="[' ', ',', '.']"
                            append-icon="mdi-file-word-box"
                        />
                    </div>
                    <v-divider />
                    <div>
                        <h2>
                            {{ $t('project.settings.homepage') }}&nbsp;<small>{{ $t('project.settings.optional') }}</small>
                        </h2>
                        <p>{{ $t('project.settings.homepageSub') }}</p>
                        <v-text-field v-model.trim="form.links.homepage" dense hide-details filled append-icon="mdi-home-search" />
                    </div>
                    <v-divider />
                    <div>
                        <h2>
                            {{ $t('project.settings.issues') }}&nbsp;<small>{{ $t('project.settings.optional') }}</small>
                        </h2>
                        <p>{{ $t('project.settings.issuesSub') }}</p>
                        <v-text-field v-model.trim="form.links.issues" dense hide-details filled append-icon="mdi-bug" />
                    </div>
                    <v-divider />
                    <div>
                        <h2>
                            {{ $t('project.settings.source') }}&nbsp;<small>{{ $t('project.settings.optional') }}</small>
                        </h2>
                        <p>{{ $t('project.settings.sourceSub') }}</p>
                        <v-text-field v-model.trim="form.links.source" dense hide-details filled append-icon="mdi-source-branch" />
                    </div>
                    <v-divider />
                    <div>
                        <h2>
                            {{ $t('project.settings.support') }}&nbsp;<small>{{ $t('project.settings.optional') }}</small>
                        </h2>
                        <p>{{ $t('project.settings.supportSub') }}</p>
                        <v-text-field v-model.trim="form.links.support" dense hide-details filled append-icon="mdi-face-agent" />
                    </div>
                    <v-divider />
                    <div>
                        <h2>
                            {{ $t('project.settings.license') }}&nbsp;<small>{{ $t('project.settings.optional') }}</small>
                        </h2>
                        <p>{{ $t('project.settings.licenceSub') }}</p>
                        <v-row>
                            <v-col cols="12" :md="isCustomLicense ? 4 : 6">
                                <v-select
                                    v-model="form.license.type"
                                    dense
                                    hide-details
                                    filled
                                    clearable
                                    :items="licences"
                                    :label="$t('project.settings.licenceType')"
                                />
                            </v-col>
                            <v-col v-if="isCustomLicense" cols="12" md="8">
                                <v-text-field v-model.trim="form.license.customName" dense hide-details filled :label="$t('project.settings.licenceCustom')" />
                            </v-col>
                            <v-col cols="12" :md="isCustomLicense ? 12 : 6">
                                <v-text-field v-model.trim="form.license.url" dense hide-details filled :label="$t('project.settings.licenceUrl')" />
                            </v-col>
                        </v-row>
                    </div>
                    <v-divider />
                    <div>
                        <h2>{{ $t('project.settings.forum') }}</h2>
                        <v-row>
                            <v-col cols="12" md="8">
                                <p>{{ $t('project.settings.forumSub') }}</p>
                            </v-col>
                            <v-col cols="12" md="4">
                                <v-switch v-model="form.forumPost"></v-switch>
                            </v-col>
                        </v-row>
                    </div>
                    <v-divider />
                    <div>
                        <h2>{{ $t('project.settings.description') }}</h2>
                        <p>{{ $t('project.settings.descriptionSub') }}</p>
                        <v-text-field v-model.trim="form.description" dense filled clearable append-icon="mdi-card-text" />
                    </div>
                    <v-divider />
                    <div>
                        <h2>{{ $t('project.settings.icon') }}</h2>
                        <v-row>
                            <v-col cols="12" md="8">
                                <p>{{ $t('project.settings.iconSub') }}</p>
                                <v-file-input chips show-size></v-file-input>
                            </v-col>
                            <v-col cols="12" md="4">
                                <v-btn @click="uploadIcon">{{ $t('project.settings.iconUpload') }}</v-btn>
                            </v-col>
                        </v-row>
                    </div>
                    <v-divider />
                    <div>
                        <h2>{{ $t('project.settings.apiKey') }}</h2>
                        <v-row>
                            <v-col cols="12" md="8">
                                <p>{{ $t('project.settings.apiKeySub') }}</p>
                                <v-text-field v-model.trim="apiKey" dense hide-details filled />
                            </v-col>
                            <v-col cols="12" md="4">
                                <v-btn @click="generateApiKey">{{ $t('project.settings.apiKeyGenerate') }}</v-btn>
                            </v-col>
                        </v-row>
                    </div>
                    <v-divider />
                    <div>
                        <h2>{{ $t('project.settings.rename') }}</h2>
                        <v-row>
                            <v-col cols="12" md="8">
                                <p>{{ $t('project.settings.renameSub') }}</p>
                                <v-text-field v-model.trim="newName" dense hide-details filled />
                            </v-col>
                            <v-col cols="12" md="4">
                                <v-btn color="warning" @click="rename">{{ $t('project.settings.rename') }}</v-btn>
                            </v-col>
                        </v-row>
                    </div>
                    <v-divider />
                    <div>
                        <h2>{{ $t('project.settings.delete') }}</h2>
                        <v-row>
                            <v-col cols="12" md="8">
                                <p>{{ $t('project.settings.deleteSub') }}</p>
                            </v-col>
                            <v-col cols="12" md="4">
                                <v-btn color="error" @click="softDelete">{{ $t('project.settings.delete') }}</v-btn>
                            </v-col>
                        </v-row>
                    </div>
                    <v-divider />
                    <div>
                        <h2>{{ $t('project.settings.hardDelete') }}</h2>
                        <v-row>
                            <v-col cols="12" md="8">
                                <p>{{ $t('project.settings.hardDeleteSub') }}</p>
                            </v-col>
                            <v-col cols="12" md="4">
                                <v-btn color="error" @click="hardDelete">{{ $t('project.settings.hardDelete') }}</v-btn>
                            </v-col>
                        </v-row>
                    </div>
                    <v-divider />
                </v-card-text>
                <v-card-actions>
                    <v-btn @click="save">
                        <v-icon>mdi-check</v-icon>
                        {{ $t('project.settings.save') }}
                    </v-btn>
                </v-card-actions>
            </v-card>
        </v-col>
        <v-col cols="12" md="4">
            <v-card>
                <v-card-title>
                    {{ $t('project.members') }}
                </v-card-title>
                <v-card-text>
                    <UserSelectionForm :users="project.members" />
                </v-card-text>
            </v-card>
        </v-col>
    </v-row>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { HangarProject } from 'hangar-internal';
import { Prop } from 'vue-property-decorator';
import { ProjectPermission } from '~/utils/perms';
import { NamedPermission, ProjectCategory } from '~/types/enums';
import { RootState } from '~/store';
import MemberList from '~/components/MemberList.vue';
import UserSelectionForm from '~/components/UserSelectionForm.vue';
@Component({
    components: { UserSelectionForm, MemberList },
})
@ProjectPermission(NamedPermission.EDIT_SUBJECT_SETTINGS)
export default class ProjectManagePage extends Vue {
    apiKey = '';
    newName = '';
    form = {
        keywords: '',
        links: {
            homepage: '',
            issues: '',
            source: '',
            support: '',
        },
        description: '',
        license: {
            type: '',
            url: '',
            customName: '',
        },
        category: ProjectCategory.UNDEFINED,
    };

    @Prop()
    project!: HangarProject;

    get categoryIcon() {
        return (this.$store.state as RootState).projectCategories.get(this.form.category)?.icon;
    }

    get isCustomLicense() {
        return this.form.license.type === '(custom)';
    }

    // TODO do we want to get those from the server? Jake: I think so, it'd be nice to admins to be able to configure default licenses, but not needed for MVP
    get licences() {
        return ['MIT', 'Apache 2.0', 'GPL', 'LGPL', '(custom)'];
    }

    // TODO implement
    save() {}

    rename() {}

    softDelete() {}

    hardDelete() {}

    uploadIcon() {}

    generateApiKey() {}
}
</script>

<style lang="scss" scoped>
hr {
    margin-top: 10px;
    margin-bottom: 5px;
}

h2 {
    margin-top: 5px;
    margin-bottom: 5px;

    small {
        font-size: 70%;
        font-style: italic;
    }
}

.sticky {
    position: sticky;
    top: 63px;
    z-index: 1;
}

.theme--dark {
    .sticky {
        background-color: #1e1e1e;
    }
}

.col-12 .v-input--selection-controls {
    margin-top: 0;
}
</style>
