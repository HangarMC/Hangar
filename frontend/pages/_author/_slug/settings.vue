<template>
    <v-row>
        <v-col cols="12" md="9">
            <v-card class="settings-card">
                <v-card-title class="sticky">
                    {{ $t('project.settings.title') }}
                    <VisibilityChangerModal
                        v-if="$perms.canSeeHidden"
                        type="project"
                        :prop-visibility="project.visibility"
                        activator-class="flex-right"
                        :post-url="`projects/visibility/${project.id}`"
                    />
                    <v-btn
                        class="flex-right"
                        :class="{ 'ml-1': $perms.canSeeHidden }"
                        color="success"
                        :loading="loading.save"
                        :disabled="!validForm.settings || settingsEqual"
                        @click="save"
                    >
                        <v-icon left>mdi-check</v-icon>
                        {{ $t('project.settings.save') }}
                    </v-btn>
                </v-card-title>
                <v-card-text>
                    <v-tabs vertical>
                        <!-- TODO i18n & link tabs to a separate url #general, #optional, etc.-->
                        <v-tab>General</v-tab>
                        <v-tab>Optional</v-tab>
                        <v-tab>Management</v-tab>
                        <v-tab>Donation</v-tab>

                        <v-tab-item>
                            <v-form v-model="validForm.settings">
                                <div>
                                    <h2>{{ $t('project.settings.category') }}</h2>
                                    <p>{{ $t('project.settings.categorySub') }}</p>
                                    <v-select
                                        v-model="form.category"
                                        :prepend-inner-icon="categoryIcon"
                                        :items="$store.getters.visibleCategories"
                                        dense
                                        hide-details
                                        filled
                                        item-text="title"
                                        item-value="apiName"
                                        :rules="[$util.$vc.require()]"
                                    />
                                </div>
                                <v-divider />
                                <div>
                                    <h2>{{ $t('project.settings.description') }}</h2>
                                    <p>{{ $t('project.settings.descriptionSub') }}</p>
                                    <v-text-field
                                        v-model.trim="form.description"
                                        dense
                                        filled
                                        clearable
                                        :counter="validations.project.desc.max"
                                        :rules="[$util.$vc.maxLength(validations.project.desc.max)]"
                                        prepend-inner-icon="mdi-card-text"
                                    />
                                </div>
                                <v-divider />
                                <div>
                                    <h2>{{ $t('project.settings.forum') }}</h2>
                                    <v-row>
                                        <v-col cols="12" md="8">
                                            <p>{{ $t('project.settings.forumSub') }}</p>
                                        </v-col>
                                        <v-col cols="12" md="4">
                                            <v-switch v-model="form.settings.forumSync" hide-details dense />
                                        </v-col>
                                    </v-row>
                                </div>
                                <v-divider />
                            </v-form>
                            <div>
                                <h2>{{ $t('project.settings.icon') }}</h2>
                                <v-row>
                                    <v-col>
                                        <p>{{ $t('project.settings.iconSub') }}</p>
                                        <v-file-input
                                            v-model="projectIcon"
                                            chips
                                            show-size
                                            dense
                                            filled
                                            prepend-inner-icon="$file"
                                            accept="image/png, image/jpeg"
                                            prepend-icon=""
                                            @change="onFileChange"
                                        />
                                        <v-btn color="info" :disabled="!projectIcon" :loading="loading.uploadIcon" @click="uploadIcon">
                                            <v-icon left>mdi-upload</v-icon>
                                            {{ $t('project.settings.iconUpload') }}
                                        </v-btn>
                                        <v-btn color="warning" :loading="loading.resetIcon" @click="resetIcon">
                                            <v-icon left>mdi-upload</v-icon>
                                            {{ $t('project.settings.iconReset') }}
                                        </v-btn>
                                    </v-col>
                                    <v-col>
                                        <img
                                            id="project-icon-preview"
                                            :src="$util.projectUrl(project.namespace.owner, project.namespace.slug)"
                                            alt="Project Icon"
                                            width="150"
                                            height="150"
                                        />
                                    </v-col>
                                </v-row>
                            </div>
                        </v-tab-item>

                        <v-tab-item>
                            <v-form v-model="validForm.settings">
                                <div>
                                    <h2>
                                        {{ $t('project.settings.keywords') }}&nbsp;<small>{{ $t('project.settings.optional') }}</small>
                                    </h2>
                                    <p>{{ $t('project.settings.keywordsSub') }}</p>
                                    <v-combobox
                                        v-model="form.settings.keywords"
                                        small-chips
                                        deletable-chips
                                        multiple
                                        dense
                                        filled
                                        clearable
                                        :counter="validations.project.keywords.max"
                                        :rules="[$util.$vc.maxLength(validations.project.keywords.max)]"
                                        :delimiters="[' ', ',', '.']"
                                        :label="$t('project.new.step3.keywords')"
                                        prepend-inner-icon="mdi-file-word-box"
                                    />
                                </div>
                                <v-divider />
                                <div>
                                    <h2>
                                        {{ $t('project.settings.homepage') }}&nbsp;<small>{{ $t('project.settings.optional') }}</small>
                                    </h2>
                                    <p>{{ $t('project.settings.homepageSub') }}</p>
                                    <v-text-field
                                        v-model.trim="form.settings.homepage"
                                        dense
                                        filled
                                        prepend-inner-icon="mdi-home-search"
                                        :label="$t('project.new.step3.homepage')"
                                        :rules="[$util.$vc.url]"
                                    />
                                </div>
                                <v-divider />
                                <div>
                                    <h2>
                                        {{ $t('project.settings.issues') }}&nbsp;<small>{{ $t('project.settings.optional') }}</small>
                                    </h2>
                                    <p>{{ $t('project.settings.issuesSub') }}</p>
                                    <v-text-field
                                        v-model.trim="form.settings.issues"
                                        dense
                                        filled
                                        prepend-inner-icon="mdi-bug"
                                        :label="$t('project.new.step3.issues')"
                                        :rules="[$util.$vc.url]"
                                    />
                                </div>
                                <v-divider />
                                <div>
                                    <h2>
                                        {{ $t('project.settings.source') }}&nbsp;<small>{{ $t('project.settings.optional') }}</small>
                                    </h2>
                                    <p>{{ $t('project.settings.sourceSub') }}</p>
                                    <v-text-field
                                        v-model.trim="form.settings.source"
                                        dense
                                        filled
                                        prepend-inner-icon="mdi-source-branch"
                                        :label="$t('project.new.step3.source')"
                                        :rules="[$util.$vc.url]"
                                    />
                                </div>
                                <v-divider />
                                <div>
                                    <h2>
                                        {{ $t('project.settings.support') }}&nbsp;<small>{{ $t('project.settings.optional') }}</small>
                                    </h2>
                                    <p>{{ $t('project.settings.supportSub') }}</p>
                                    <v-text-field
                                        v-model.trim="form.settings.support"
                                        dense
                                        filled
                                        prepend-inner-icon="mdi-face-agent"
                                        :label="$t('project.new.step3.support')"
                                        :rules="[$util.$vc.url]"
                                    />
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
                                                v-model="form.settings.license.type"
                                                dense
                                                hide-details
                                                filled
                                                :items="licences"
                                                :label="$t('project.settings.licenceType')"
                                            />
                                        </v-col>
                                        <v-col v-if="isCustomLicense" cols="12" md="8">
                                            <v-text-field
                                                v-model.trim="form.settings.license.name"
                                                dense
                                                hide-details
                                                filled
                                                :label="$t('project.settings.licenceCustom')"
                                            />
                                        </v-col>
                                        <v-col cols="12" :md="isCustomLicense ? 12 : 6">
                                            <v-text-field
                                                v-model.trim="form.settings.license.url"
                                                dense
                                                clearable
                                                filled
                                                :rules="[$util.$vc.url]"
                                                :label="$t('project.settings.licenceUrl')"
                                            />
                                        </v-col>
                                    </v-row>
                                </div>
                            </v-form>
                        </v-tab-item>

                        <v-tab-item>
                            <div>
                                <h2>{{ $t('project.settings.apiKey') }}</h2>
                                <v-row>
                                    <v-col cols="12">
                                        <p>{{ $t('project.settings.apiKeySub') }}</p>
                                        <v-text-field v-model.trim="apiKey" dense hide-details filled>
                                            <template #append-outer>
                                                <v-btn class="input-append-btn" @click="generateApiKey">{{ $t('project.settings.apiKeyGenerate') }}</v-btn>
                                            </template>
                                        </v-text-field>
                                    </v-col>
                                </v-row>
                            </div>
                            <v-divider />
                            <div>
                                <h2>{{ $t('project.settings.rename') }}</h2>
                                <v-row>
                                    <v-col cols="12">
                                        <p>{{ $t('project.settings.renameSub') }}</p>
                                        <v-form v-model="nameForm">
                                            <v-text-field v-model.trim="newName" dense filled :error-messages="nameErrors">
                                                <template #append-outer>
                                                    <v-btn
                                                        class="input-append-btn"
                                                        color="warning"
                                                        :disabled="!newName || !nameForm"
                                                        :loading="loading.rename"
                                                        @click="rename"
                                                    >
                                                        <v-icon left>mdi-rename-box</v-icon>
                                                        {{ $t('project.settings.rename') }}
                                                    </v-btn>
                                                </template>
                                            </v-text-field>
                                        </v-form>
                                    </v-col>
                                </v-row>
                            </div>
                            <v-divider />
                            <div v-if="$perms.canDeleteProject">
                                <h2>{{ $t('project.settings.delete') }}</h2>
                                <v-row>
                                    <v-col cols="12" md="8">
                                        <p>{{ $t('project.settings.deleteSub') }}</p>
                                    </v-col>
                                    <v-col cols="12" md="4">
                                        <TextareaModal :title="$t('project.settings.delete')" :label="$t('general.comment')" :submit="softDelete">
                                            <template #activator="{ on, attrs }">
                                                <v-btn color="error" v-bind="attrs" v-on="on">{{ $t('project.settings.delete') }}</v-btn>
                                            </template>
                                        </TextareaModal>
                                    </v-col>
                                </v-row>
                            </div>
                            <v-divider />
                            <div v-if="$perms.canHardDeleteProject" class="error darken-4">
                                <!-- TODO striped background to separate from normal delete-->
                                <h2>{{ $t('project.settings.hardDelete') }}</h2>
                                <v-row>
                                    <v-col cols="12" md="8">
                                        <p>{{ $t('project.settings.hardDeleteSub') }}</p>
                                    </v-col>
                                    <v-col cols="12" md="4">
                                        <TextareaModal :title="$t('project.settings.hardDelete')" :label="$t('general.comment')" :submit="hardDelete">
                                            <template #activator="{ on, attrs }">
                                                <v-btn color="error" v-bind="attrs" v-on="on">{{ $t('project.settings.hardDelete') }}</v-btn>
                                            </template>
                                        </TextareaModal>
                                    </v-col>
                                </v-row>
                            </div>
                        </v-tab-item>

                        <v-tab-item>
                            <v-form v-model="validForm.settings">
                                <div>
                                    <h2>{{ $t('project.settings.donation.enable') }}</h2>
                                    <p>{{ $t('project.settings.donation.enableSub') }}</p>
                                    <v-switch v-model="form.settings.donation.enable" hide-details dense />
                                </div>
                                <v-divider />
                                <div>
                                    <h2>{{ $t('project.settings.donation.email') }}</h2>
                                    <p>{{ $t('project.settings.donation.emailSub') }}</p>
                                    <v-text-field v-model="form.settings.donation.email" hide-details dense />
                                </div>
                                <v-divider />
                                <div>
                                    <h2>{{ $t('project.settings.donation.defaultAmount') }}</h2>
                                    <p>{{ $t('project.settings.donation.defaultAmountSub') }}</p>
                                    <v-text-field v-model.number="form.settings.donation.defaultAmount" type="number" hide-details dense />
                                </div>
                                <v-divider />
                                <div>
                                    <h2>{{ $t('project.settings.donation.oneTimeAmounts') }}</h2>
                                    <p>{{ $t('project.settings.donation.oneTimeAmountsSub') }}</p>
                                    <v-combobox
                                        v-model="form.settings.donation.oneTimeAmounts"
                                        small-chips
                                        deletable-chips
                                        multiple
                                        dense
                                        filled
                                        clearable
                                        :rules="[$util.$vc.requireNumberArray()]"
                                        :delimiters="[' ', ',', '.']"
                                        prepend-inner-icon="mdi-file-word-box"
                                    />
                                </div>
                                <v-divider />
                                <div>
                                    <h2>{{ $t('project.settings.donation.monthlyAmounts') }}</h2>
                                    <p>{{ $t('project.settings.donation.monthlyAmountsSub') }}</p>
                                    <v-combobox
                                        v-model="form.settings.donation.monthlyAmounts"
                                        small-chips
                                        deletable-chips
                                        multiple
                                        dense
                                        filled
                                        clearable
                                        :rules="[$util.$vc.requireNumberArray()]"
                                        :delimiters="[' ', ',', '.']"
                                        prepend-inner-icon="mdi-file-word-box"
                                    />
                                </div>
                                <v-divider />
                            </v-form>
                        </v-tab-item>
                    </v-tabs>
                </v-card-text>
            </v-card>
        </v-col>
        <v-col cols="12" md="3">
            <MemberList :members="project.members" :roles="roles" always-editing />
        </v-col>
    </v-row>
</template>

<script lang="ts">
import { Component, Watch } from 'nuxt-property-decorator';
import { TranslateResult } from 'vue-i18n';
import { AxiosError } from 'axios';
import { Context } from '@nuxt/types';
import { Role } from 'hangar-api';
import { cloneDeep, isEqual } from 'lodash-es';
import { ProjectPermission } from '~/utils/perms';
import { NamedPermission } from '~/types/enums';
import { RootState } from '~/store';
import { HangarProjectMixin } from '~/components/mixins';
import { MemberList } from '~/components/projects';
import VisibilityChangerModal from '~/components/modals/VisibilityChangerModal.vue';
import TextareaModal from '~/components/modals/TextareaModal.vue';

@Component({
    components: { TextareaModal, VisibilityChangerModal, MemberList },
})
@ProjectPermission(NamedPermission.EDIT_SUBJECT_SETTINGS)
export default class ProjectManagePage extends HangarProjectMixin {
    roles!: Role[];
    licences!: string[];
    apiKey = '';
    newName = '';
    nameErrors: TranslateResult[] = [];
    nameForm = false;
    projectIcon: File | null = null;
    form = {
        settings: cloneDeep(this.project.settings),
        description: this.project.description,
        category: this.project.category,
    };

    validForm = {
        settings: false,
    };

    loading = {
        save: false,
        uploadIcon: false,
        resetIcon: false,
        rename: false,
    };

    get settingsEqual() {
        return (
            isEqual(this.form.settings, this.project.settings) &&
            this.form.category === this.project.category &&
            this.form.description === this.project.description
        );
    }

    get categoryIcon() {
        return (this.$store.state as RootState).projectCategories.get(this.form.category)?.icon;
    }

    get isCustomLicense() {
        return this.form.settings.license.type === '(custom)';
    }

    onFileChange() {
        if (this.projectIcon) {
            const reader = new FileReader();
            reader.onload = (ev) => {
                document.getElementById('project-icon-preview')!.setAttribute('src', ev.target!.result as string);
            };
            reader.readAsDataURL(this.projectIcon);
        } else {
            document
                .getElementById('project-icon-preview')!
                .setAttribute('src', this.$util.projectUrl(this.project.namespace.owner, this.project.namespace.slug));
        }
    }

    save() {
        this.loading.save = true;
        this.$api
            .requestInternal(`projects/project/${this.$route.params.author}/${this.$route.params.slug}/settings`, true, 'post', {
                ...this.form,
            })
            .then(() => {
                this.$nuxt.refresh();
            })
            .catch(this.$util.handleRequestError)
            .finally(() => {
                this.loading.save = false;
            });
    }

    @Watch('newName')
    onNewNameChange(val: string) {
        if (!val) {
            this.nameErrors = [];
        } else {
            this.$api
                .requestInternal('projects/validateName', false, 'get', {
                    userId: this.project.owner.userId,
                    value: val,
                })
                .then(() => {
                    this.nameErrors = [];
                })
                .catch((err: AxiosError) => {
                    this.nameErrors = [];
                    if (!err.response?.data.isHangarApiException) {
                        return;
                    }
                    this.nameErrors.push(this.$t(err.response.data.message));
                });
        }
    }

    rename() {
        this.loading.rename = true;
        this.$api
            .requestInternal<string>(`projects/project/${this.$route.params.author}/${this.$route.params.slug}/rename`, true, 'post', {
                content: this.newName,
            })
            .then((newSlug) => {
                this.$util.success(this.$t('project.settings.success.rename', [this.newName]));
                this.$router.push({
                    name: 'author-slug',
                    params: {
                        author: this.$route.params.author,
                        slug: newSlug,
                    },
                });
            })
            .catch(this.$util.handleRequestError)
            .finally(() => {
                this.loading.rename = false;
            });
    }

    softDelete(comment: string) {
        return this.$api
            .requestInternal(`projects/project/${this.project.id}/manage/delete`, true, 'post', {
                content: comment,
            })
            .then(() => {
                this.$util.success(this.$t('project.settings.success.softDelete'));
                this.$nuxt.refresh();
            })
            .catch(this.$util.handleRequestError);
    }

    hardDelete(comment: string) {
        return this.$api
            .requestInternal(`projects/project/${this.project.id}/manage/hardDelete`, true, 'post', {
                content: comment,
            })
            .then(() => {
                this.$util.success(this.$t('project.settings.success.hardDelete'));
                this.$router.push('/');
            })
            .catch(this.$util.handleRequestError);
    }

    uploadIcon() {
        const data = new FormData();
        data.append('projectIcon', this.projectIcon!);
        this.loading.uploadIcon = true;
        this.$api
            .requestInternal(`projects/project/${this.$route.params.author}/${this.$route.params.slug}/saveIcon`, true, 'post', data)
            .then(() => {
                this.projectIcon = null;
                this.$util.success(this.$t('project.settings.success.changedIcon'));
                this.$nuxt.refresh();
            })
            .catch(this.$util.handleRequestError)
            .finally(() => {
                this.loading.uploadIcon = false;
            });
    }

    resetIcon() {
        // TODO some way of disabling the reset icon button if not using a custom image
        this.loading.resetIcon = true;
        this.$api
            .requestInternal(`projects/project/${this.$route.params.author}/${this.$route.params.slug}/resetIcon`, true, 'post')
            .then(() => {
                this.$util.success(this.$t('project.settings.success.resetIcon'));
                document
                    .getElementById('project-icon-preview')!
                    .setAttribute('src', `${this.$util.projectUrl(this.project.namespace.owner, this.project.namespace.slug)}?noCache=${Math.random()}`);
                this.$nuxt.refresh();
            })
            .catch(this.$util.handleRequestError)
            .finally(() => {
                this.loading.resetIcon = false;
            });
    }

    // TODO implement
    generateApiKey() {}

    async asyncData({ $api, $util }: Context) {
        const data = await Promise.all([$api.requestInternal('data/projectRoles', false), $api.requestInternal('data/licences', false)]).catch(
            $util.handlePageRequestError
        );
        if (typeof data === 'undefined') return;
        return { roles: data[0], licences: data[1] };
    }
}
</script>
<style lang="scss">
.settings-card {
    .v-text-field .v-text-field__details {
        margin-bottom: 0;
    }
}
</style>

<style lang="scss" scoped>
hr {
    margin-top: 6px;
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

.v-window-item {
    padding-left: 10px;
}
</style>
