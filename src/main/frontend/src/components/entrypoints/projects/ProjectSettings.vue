<template>
    <div class="row">
        <div class="col-md-8">
            <div class="card card-settings">
                <div class="card-header sticky">
                    <h3 class="card-title float-left" v-text="$t('project.settings._')"></h3>
                    <template v-if="permissions.seeHidden">
                        <BtnHide :namespace="project.namespace" :project-visibility="project.visibility"></BtnHide>
                    </template>
                    <button type="button" class="btn btn-success float-right" @click="save">
                        <i class="fas fa-check"></i>
                        Save changes
                    </button>
                </div>
                <div class="card-body">
                    <Setting :name="$t('project.settings.category._')" :desc="$t('project.settings.category.info', [categories.length])">
                        <template v-slot:content>
                            <label for="category" class="sr-only">{{ $t('project.settings.category._') }}</label>
                            <select aria-labelledby="category-desc" v-model="form.category" class="form-control" id="category" name="category">
                                <option v-for="category in categories" :key="category.id" :value="category.id">
                                    {{ category.name }}
                                </option>
                            </select>
                        </template>
                    </Setting>
                    <Setting optional :name="$t('project.settings.keywords._')" :desc="$t('project.settings.keywords.info')">
                        <label for="keywords-input" class="sr-only">{{ $t('project.settings.keywords._') }}</label>
                        <input
                            v-model.trim="form.keywords"
                            aria-labelledby="keywords-desc"
                            type="text"
                            id="keywords-input"
                            class="form-control"
                            placeholder="sponge server plugins mods"
                        />
                    </Setting>
                    <Setting optional :name="$t('project.settings.homepage._')" :desc="$t('project.settings.homepage.info')">
                        <label for="homepage-input" class="sr-only">{{ $t('project.settings.homepage._') }}</label>
                        <input
                            v-model.trim="form.links.homepage"
                            aria-labelledby="homepage-desc"
                            type="url"
                            class="form-control"
                            id="homepage-input"
                            placeholder="https://papermc.io"
                        />
                    </Setting>
                    <Setting optional :name="$t('project.settings.issues._')" :desc="$t('project.settings.issues.info')">
                        <label for="issues-input" class="sr-only">{{ $t('project.settings.issues._') }}</label>
                        <input
                            v-model.trim="form.links.issues"
                            aria-labelledby="issues-desc"
                            type="url"
                            class="form-control"
                            id="issues-input"
                            placeholder="https://github.com/MiniDigger/Hangar/issues"
                        />
                        <div class="clearfix"></div>
                    </Setting>
                    <Setting optional :name="$t('project.settings.source._')" :desc="$t('project.settings.source.info')">
                        <label for="source-input" class="sr-only">{{ $t('project.settings.source._') }}</label>
                        <input
                            v-model.trim="form.links.source"
                            aria-labelledby="source-desc"
                            type="url"
                            class="form-control"
                            id="source-input"
                            placeholder="https://github.com/MiniDigger/Hangar"
                        />
                    </Setting>
                    <Setting optional :name="$t('project.settings.externalSupport._')" :desc="$t('project.settings.externalSupport.info')">
                        <label for="external-input" class="sr-only">{{ $t('project.settings.externalSupport._') }}</label>
                        <input
                            v-model.trim="form.links.support"
                            aria-labelledby="external-support-desc"
                            type="url"
                            class="form-control"
                            id="external-input"
                            placeholder="https://discord.gg/papermc"
                        />
                    </Setting>
                    <Setting optional :name="$t('project.settings.license._')" :desc="$t('project.settings.license.info')">
                        <div class="input-group pb-2 float-left">
                            <div class="input-group-prepend">
                                <label for="license-type-input" class="input-group-text">Type</label>
                            </div>
                            <select v-model="form.license.type" name="licenseType" id="license-type-input" class="custom-select">
                                <option v-text="$t('licenses.mit')"></option>
                                <option v-text="$t('licenses.apache2.0')"></option>
                                <option v-text="$t('licenses.gpl')"></option>
                                <option v-text="$t('licenses.lgpl')"></option>
                                <option v-text="$t('licenses.custom')"></option>
                            </select>
                        </div>
                        <div class="input-group">
                            <div class="input-group-prepend">
                                <label for="license-url-input" class="input-group-text">
                                    <i class="fas fa-gavel"></i> {{ form.licenseType === $t('licenses.custom') ? 'Name/' : '' }}URL
                                </label>
                            </div>
                            <div v-show="form.license.type === $t('licenses.custom')" class="input-group-prepend">
                                <input
                                    v-model.trim.trim="form.license.name"
                                    type="text"
                                    id="license-name-input"
                                    name="licenseName"
                                    class="form-control"
                                    placeholder="Custom Name"
                                    aria-label="License Name"
                                />
                            </div>
                            <input
                                v-model.trim="form.license.url"
                                id="license-url-input"
                                name="licenseUrl"
                                class="form-control"
                                type="text"
                                placeholder="URL"
                            />
                        </div>
                    </Setting>
                    <Setting :name="$t('project.settings.forumSync._')" :desc="$t('project.settings.forumSync.info')">
                        <template v-slot:content>
                            <label>
                                <input v-model="form.forumSync" type="checkbox" id="forum-sync" />
                            </label>
                        </template>
                    </Setting>
                    <Setting :name="$t('project.settings.description._')" :desc="$t('project.settings.description.info')">
                        <label for="description-input" class="sr-only">{{ $t('project.settings.description._') }}</label>
                        <input
                            v-model.trim="form.description"
                            type="text"
                            class="form-control"
                            id="description-input"
                            maxlength="120"
                            :placeholder="$t('version.create.noDescription')"
                        />
                    </Setting>
                    <transition name="slide-down">
                        <div v-show="showUploadMsg" class="alert alert-info">Don't forget to save changes!</div>
                    </transition>
                    <Setting :name="$t('project.settings.icon._')">
                        <template v-slot:description>
                            <h4 v-text="$t('project.settings.icon._')"></h4>
                            <UserAvatar :img-src="icon.previewSrc" :user-name="project.project.ownerName" clazz="user-avatar-md"></UserAvatar>
                            <input type="file" id="icon-upload-input" class="form-control-static mt-2" @change="handleFileChange($event.target)" />
                        </template>
                        <template v-slot:content>
                            <div class="icon-description">
                                <p v-text="$t('project.settings.icon.info')"></p>
                                <div class="btn-group float-right">
                                    <button class="btn btn-default" @click.prevent="resetIcon">Reset</button>
                                    <button class="btn btn-info float-right" :disabled="!icon.file" @click.prevent="uploadIcon">
                                        <i v-show="loading.upload" class="fas fa-spinner fa-spin"></i>
                                        <i v-show="!loading.upload" class="fas fa-upload"></i> Upload
                                    </button>
                                </div>
                            </div>
                        </template>
                    </Setting>
                    <Setting v-if="true" :name="$t('project.settings.donation._')" :desc="$t('project.settings.donation.info')">
                        <label for="donation-email-input" class="sr-only">{{ $t('project.settings.donation._') }}</label>
                        <input
                            v-model.trim="form.donation.donationEmail"
                            type="email"
                            class="form-control"
                            id="donation-email-input"
                            maxlength="120"
                            placeholder="Paypal Email"
                        />
                        <label for="description-input" class="sr-only">{{ $t('project.settings.donation._') }}</label>
                        <TagInput v-model="form.donation.tierTag" />
                    </Setting>
                    <Setting v-if="permissions.editApiKeys" :name="$t('project.settings.deployKey._')" :desc="$t('project.settings.deployKey.info')">
                        <template v-slot:description="props">
                            <h4 v-text="props['setting-name']"></h4>
                            <p>
                                {{ props.desc }}
                                <!-- TODO I think this link is supposed to show some info or something -->
                                <a href="#" @click.prevent class="ml-1"><i class="fas fa-question-circle"></i></a>
                            </p>
                            <input
                                v-if="deploymentKey"
                                aria-label="Deployment Key"
                                class="form-control input-key"
                                type="text"
                                :value="deploymentKey.value"
                                readonly
                            />
                            <input v-else aria-label="Deployment Key" class="form-control input-key" type="text" value="" readonly />
                        </template>
                        <template v-slot:content>
                            <button v-if="deploymentKey" class="btn btn-danger btn-block" @click="genKey">
                                <span v-show="loading.genKey" class="spinner" style="display: none"><i class="fas fa-spinner fa-spin"></i></span>
                                <span class="text" v-text="$t('project.settings.revokeKey')"></span>
                            </button>
                            <button v-else class="btn btn-info btn-block" @click="revokeKey">
                                <span v-show="loading.revokeKey" class="spinner"><i class="fas fa-spinner fa-spin"></i></span>
                                <span class="text" v-text="$t('project.settings.genKey')"></span>
                            </button>
                        </template>
                    </Setting>
                    <HangarForm id="rename-form" method="post" :action="ROUTES.parse('PROJECTS_RENAME', project.project.ownerName, project.project.slug)">
                        <Setting :name="$t('project.rename._')" :desc="$t('project.rename.info')">
                            <template v-slot:content>
                                <input
                                    v-model.trim="renameForm.value"
                                    name="name"
                                    class="form-control mb-2"
                                    type="text"
                                    maxlength="25"
                                    @keydown.enter.prevent
                                    @keyup.enter.prevent="$refs.renameButton.click()"
                                />
                                <button
                                    ref="renameButton"
                                    type="button"
                                    id="btn-rename"
                                    data-toggle="modal"
                                    data-target="#modal-rename"
                                    class="btn btn-warning"
                                    :disabled="renameForm.value === project.project.name"
                                >
                                    {{ $t('project.rename._') }}
                                </button>
                            </template>
                        </Setting>
                        <HangarModal target-id="modal-rename" label-id="label-rename">
                            <template v-slot:modal-content>
                                <div class="modal-header">
                                    <h4 class="modal-title" id="label-rename" v-text="$t('project.rename.title')"></h4>
                                    <button type="button" class="close" data-dismiss="modal" :aria-label="$t('general.cancel')">
                                        <span aria-hidden="true">&times;</span>
                                    </button>
                                </div>
                                <div class="modal-body" v-text="$t('project.rename.info')"></div>
                                <div class="modal-footer">
                                    <div class="form-inline">
                                        <div class="btn-group">
                                            <button type="button" class="btn btn-default" data-dismiss="modal" v-text="$t('channel.edit.close')"></button>
                                            <button type="submit" form="rename-form" class="btn btn-warning" v-text="$t('project.rename._')" />
                                        </div>
                                    </div>
                                </div>
                            </template>
                        </HangarModal>
                    </HangarForm>
                    <HangarForm
                        v-if="permissions.deleteProject"
                        id="delete-form"
                        method="post"
                        :action="ROUTES.parse('PROJECTS_SOFT_DELETE', project.project.ownerName, project.project.slug)"
                    >
                        <Setting name="Danger" desc="Once you delete a project, it cannot be recovered." danger>
                            <template v-slot:content>
                                <button type="button" class="btn btn-delete btn-danger" data-toggle="modal" data-target="#modal-delete">Delete</button>
                            </template>
                        </Setting>
                        <HangarModal target-id="modal-delete" label-id="label-delete">
                            <template v-slot:modal-content>
                                <div class="modal-header">
                                    <h4 class="modal-title" id="label-delete" v-text="$t('project.delete.title')"></h4>
                                    <button type="button" class="close" data-dismiss="modal" aria-label="Cancel">
                                        <span aria-hidden="true">&times;</span>
                                    </button>
                                </div>
                                <div class="modal-body">
                                    {{ $t('project.delete.info._') }}
                                    <br />
                                    <textarea
                                        v-model.trim="deleteForm.value"
                                        form="delete-form"
                                        name="comment"
                                        class="textarea-delete-comment form-control"
                                        rows="3"
                                    ></textarea>
                                    <br />
                                    <div class="alert alert-warning" v-text="$t('project.delete.info.uniqueid', [project.project.name])"></div>
                                </div>
                                <div class="modal-footer">
                                    <div class="form-inline">
                                        <div class="btn-group">
                                            <button type="button" class="btn btn-default" data-dismiss="modal" v-text="$t('channel.edit.close')"></button>
                                            <button
                                                type="submit"
                                                form="delete-form"
                                                class="btn btn-danger"
                                                v-text="$t('general.delete')"
                                                :disabled="!deleteForm.value"
                                            />
                                        </div>
                                    </div>
                                </div>
                            </template>
                        </HangarModal>
                    </HangarForm>
                    <HangarForm
                        id="hard-delete-form"
                        v-if="permissions.hardDeleteProject"
                        method="post"
                        :action="ROUTES.parse('PROJECTS_DELETE', project.project.ownerName, project.project.slug)"
                    >
                        <Setting name="Hard Delete" desc="Once you delete a project, it cannot be recovered." class="striped" danger>
                            <template v-slot:content>
                                <button type="button" class="btn btn-delete btn-danger" data-toggle="modal" data-target="#hard-delete-modal">
                                    <strong>Hard Delete</strong>
                                </button>
                            </template>
                        </Setting>
                        <HangarModal target-id="hard-delete-modal" label-id="label-hard-delete">
                            <template v-slot:modal-content>
                                <div class="modal-header">
                                    <h4 class="modal-title" id="label-hard-delete">Hard Delete Project?</h4>
                                    <button type="button" class="close" data-dismiss="modal" aria-label="Cancel">
                                        <span aria-hidden="true">&times;</span>
                                    </button>
                                </div>
                                <div class="modal-body">Are you sure you want to permanently delete this project?</div>
                                <div class="modal-footer">
                                    <div class="btn-group">
                                        <button type="button" class="btn btn-default" data-dismiss="modal" v-text="$t('channel.edit.close')"></button>
                                        <button type="submit" form="hard-delete-form" class="btn btn-danger" v-text="$t('general.delete')" />
                                    </div>
                                </div>
                            </template>
                        </HangarModal>
                    </HangarForm>
                </div>
                <div class="card-footer">
                    <transition name="slide-down">
                        <div v-show="error" id="project-settings-error" class="alert alert-danger" v-text="error"></div>
                    </transition>
                    <button type="button" class="btn btn-success float-right" @click="save">
                        <i class="fas fa-check"></i>
                        Save changes
                    </button>
                </div>
            </div>
        </div>
        <div class="col-md-4">
            <MemberList
                ref="memberList"
                :filtered-members-prop="memberList.filteredMembers"
                :can-manage-members="permissions.manageMembers"
                :roles="memberList.roles"
                editable
                :remove-call="ROUTES.parse('PROJECTS_REMOVE_MEMBER', project.project.ownerName, project.project.slug)"
                :settings-call="ROUTES.parse('PROJECTS_SHOW_SETTINGS', project.project.ownerName, project.project.slug)"
            ></MemberList>
        </div>
    </div>
</template>

<script>
import axios from 'axios';

import { Category } from '@/enums';
import BtnHide from '@/components/BtnHide';
import MemberList from '@/components/MemberList';
import HangarForm from '@/components/HangarForm';
import UserAvatar from '@/components/UserAvatar';
import Setting from '@/components/Setting';
import HangarModal from '@/components/HangarModal';
import { VueTagsInput } from '@johmun/vue-tags-input/vue-tags-input/publish';
import TagInput from '@/components/TagInput';

const LICENSES = ['MIT', 'Apache 2.0', 'GNU General Public License (GPL)', 'GNU Lesser General Public License (LGPL)'];
const VALIDATIONS = [
    { name: 'category', el: 'category' },
    { name: 'description', el: 'description-input' },
];

export default {
    name: 'ProjectSettings',
    components: { TagInput, HangarModal, Setting, UserAvatar, HangarForm, MemberList, BtnHide, VueTagsInput },
    data() {
        return {
            ROUTES: window.ROUTES,
            error: null,
            loading: {
                upload: false,
                genKey: false,
                revokeKey: false,
            },
            showUploadMsg: false,
            project: window.PROJECT,
            deploymentKey: window.DEPLOYMENT_KEY,
            permissions: {
                seeHidden: window.PERMISSIONS.SEE_HIDDEN,
                manageMembers: window.PERMISSIONS.MANAGE_MEMBERS,
                editApiKeys: window.PERMISSIONS.EDIT_API_KEYS,
                deleteProject: window.PERMISSIONS.DELETE_PROJECT,
                hardDeleteProject: window.PERMISSIONS.HARD_DELETE_PROJECT,
            },
            memberList: {
                filteredMembers: window.FILTERED_MEMBERS,
                roles: window.POSSIBLE_ROLES,
            },
            categories: Category.values,
            form: {
                category: window.PROJECT.project.category,
                keywords: window.PROJECT.settings.keywords.length ? window.PROJECT.settings.keywords.join(' ') : null,
                links: {
                    homepage: window.PROJECT.settings.homepage,
                    issues: window.PROJECT.settings.issues,
                    source: window.PROJECT.settings.source,
                    support: window.PROJECT.settings.support,
                },
                license: {
                    type: LICENSES.indexOf(window.PROJECT.settings.licenseName) > -1 ? window.PROJECT.settings.licenseName : 'Custom',
                    name: LICENSES.indexOf(window.PROJECT.settings.licenseName) > -1 ? null : window.PROJECT.settings.licenseName,
                    url: window.PROJECT.settings.licenseUrl,
                },
                forumSync: window.PROJECT.settings.forumSync,
                description: window.PROJECT.project.description,
                donation: {
                    donationEmail: window.PROJECT.settings.donationEmail,
                    tierTag: '',
                    tiers: window.PROJECT.settings.donationTiers || [],
                },
            },
            renameForm: {
                value: window.PROJECT.project.name,
            },
            deleteForm: {
                value: null,
            },
            icon: {
                file: null,
                previewSrc: window.PROJECT.iconUrl,
                hasChanged: false,
            },
            donationTierValidation: [
                {
                    classes: 'valid-amount',
                    rule: /^([0-9]{1,3}(\.[0-9]{1,2})?)$/,
                },
            ],
        };
    },
    methods: {
        showError(id) {
            document.getElementById(id).classList.add('invalid-input');
            document.getElementById(id).scrollIntoView({
                behavior: 'smooth',
                block: 'center',
                inline: 'center',
            });
        },
        save() {
            Array.from(document.getElementsByClassName('invalid-input')).forEach((el) => {
                el.classList.remove('invalid-input');
            });
            const data = {
                category: this.form.category,
                keywords: typeof this.form.keywords === 'string' ? this.form.keywords.split(' ') : !Array.isArray(this.form.keywords) ? [] : this.form.keywords,
                links: { ...this.form.links },
                license: {
                    name: this.form.license.type !== 'Custom' ? this.form.license.type : this.form.license.name,
                    url: this.form.license.url,
                },
                forumSync: this.form.forumSync,
                description: this.form.description,
                iconChange: this.icon.hasChanged,
                members: this.$refs.memberList.getForm(),
            };

            for (const validation of VALIDATIONS) {
                if (!validation.name.split('.').reduce((o, i) => o[i], data)) {
                    this.showError(validation.el);
                    return;
                }
            }
            if (data.license.url && !data.license.name) {
                this.showError('license-name-input');
                return;
            }
            const self = this;
            axios
                .post(this.ROUTES.parse('PROJECTS_SAVE', this.project.project.ownerName, this.project.project.slug), data, window.ajaxSettings)
                .then(() => {
                    location.href = this.ROUTES.parse('PROJECTS_SHOW', this.project.project.ownerName, this.project.project.slug);
                })
                .catch((err) => {
                    if (err.response.headers['content-type'] === 'application/json') {
                        this.error = this.$t(err.response.data.messageKey, err.response.data.messageArgs);
                    } else {
                        this.error = 'Error while saving, ' + err.message; // TODO move to i18n
                    }
                    document.getElementById('project-settings-error').scrollIntoView({
                        inline: 'nearest',
                        block: 'nearest',
                        behavior: 'smooth',
                    });
                    setTimeout(
                        () => {
                            self.error = null;
                        },
                        5000,
                        this
                    );
                });
        },
        handleFileChange(target) {
            if (target.files.length) {
                this.icon.file = target.files[0];
            } else {
                this.icon.file = null;
            }
        },
        uploadIcon() {
            if (!this.icon.file) return;
            this.loading.upload = true;
            const formData = new FormData();
            formData.append('icon', this.icon.file);
            axios
                .post(this.ROUTES.parse('PROJECTS_UPLOAD_ICON', this.project.project.ownerName, this.project.project.slug), formData, {
                    headers: {
                        [window.csrfInfo.headerName]: window.csrfInfo.token,
                        'content-type': 'multipart/form-data',
                    },
                })
                .then(() => {
                    this.icon.previewSrc =
                        this.ROUTES.parse('PROJECTS_SHOW_PENDING_ICON', this.project.project.ownerName, this.project.project.slug) + '?' + performance.now();
                    this.loading.upload = false;
                    this.icon.file = null;
                    document.getElementById('icon-upload-input').value = null;
                    this.showUploadMsg = true;
                    this.icon.hasChanged = true;
                });
        },
        resetIcon() {
            document.getElementById('icon-upload-input').value = null;
            axios
                .post(this.ROUTES.parse('PROJECTS_RESET_ICON', this.project.project.ownerName, this.project.project.slug), {}, window.ajaxSettings)
                .then(() => {
                    this.icon.previewSrc =
                        this.ROUTES.parse('PROJECTS_SHOW_ICON', this.project.project.ownerName, this.project.project.slug) + '?' + performance.now();
                    this.icon.file = null;
                    this.showUploadMsg = false;
                    this.icon.hasChanged = false;
                });
        },
        genKey() {
            this.loading.genKey = true;
            axios
                .post(`/api/old/v1/projects/${this.project.namespace}/keys/new`, { 'key-type': 0 }, window.ajaxSettings)
                .then(({ data }) => {
                    this.deploymentKey = data;
                })
                .finally(() => {
                    this.loading.genKey = false;
                });
        },
        revokeKey() {
            this.loading.revokeKey = true;
            axios
                .post(`/api/old/v1/projects/${this.project.namespace}/keys/revoke`, { id: this.deploymentKey.id }, window.ajaxSettings)
                .then(() => {
                    this.deploymentKey = null;
                })
                .finally(() => {
                    this.loading.revokeKey = false;
                });
        },
    },
};
</script>
<style lang="scss" scoped>
.slide-down-enter-active,
.slide-down-leave-active {
    transition: all 0.5s ease;
}

.slide-down-enter-from,
.slide-down-leave-to {
    margin-top: -60px;
    transform: scaleY(0);
    transform-origin: top;
}

.alert {
    width: 100%;
}
</style>
