<template>
    <HangarForm :action="ROUTES.parse('PROJECTS_CREATE_PROJECT')" method="post" no-autocomplete>
        <div class="row">
            <div class="col-12 col-xl-5">
                <div class="input-group">
                    <div class="input-group-prepend">
                        <label for="create-as-selector" class="input-group-text"> Create as... </label>
                    </div>
                    <select v-model="form.createAs" id="create-as-selector" name="owner" class="custom-select">
                        <option :value="currentUser.id" v-text="currentUser.name"></option>
                        <option v-for="org in organizations" :key="org.id" :value="org.id" v-text="org.name"></option>
                    </select>
                </div>
            </div>
            <div class="col-12 col-xl-7">
                <div class="input-group">
                    <div class="input-group-prepend">
                        <label for="project-name" class="input-group-text" v-text="$t('project.create.input.name')"></label>
                    </div>
                    <input v-model.trim="form.projectName" type="text" id="project-name" name="name" class="form-control" required @input="projectNameInput" />
                    <div class="input-group-append">
                        <div v-show="success.projectName" class="input-group-text text-success">
                            <i class="fas fa-check-circle fa-lg"></i>
                        </div>
                        <div v-show="!success.projectName" class="input-group-text text-danger">
                            <i class="fas fa-times-circle fa-lg"></i>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-12">
                <div class="input-group">
                    <div class="input-group-prepend">
                        <label for="project-description" class="input-group-text" v-text="$t('project.create.input.description')"></label>
                    </div>
                    <input v-model.trim="form.description" type="text" name="description" class="form-control" id="project-description" required />
                </div>
            </div>
            <div class="col-12">
                <div class="input-group">
                    <div class="input-group-prepend">
                        <label for="category-input" class="input-group-text" v-text="$t('project.create.input.category')"></label>
                    </div>
                    <select id="category-input" name="category" class="custom-select" required>
                        <option v-for="cat in categories" :key="cat.id" :value="cat.name" v-text="cat.name"></option>
                    </select>
                </div>
            </div>
        </div>
        <div class="row collapse" id="additional-settings">
            <div class="col-12 text-center"><span class="input-divider">Links</span></div>
            <div class="col-12">
                <div class="input-group">
                    <div class="input-group-prepend">
                        <label for="homepage-input" class="input-group-text"> <i class="fas fa-home"></i> Homepage </label>
                    </div>
                    <input id="homepage-input" name="homepageUrl" type="text" class="form-control" />
                </div>
            </div>
            <div class="col-12">
                <div class="input-group">
                    <div class="input-group-prepend">
                        <label for="issue-tracker-input" class="input-group-text"> <i class="fas fa-bug"></i> Issue Tracker </label>
                    </div>
                    <input id="issue-tracker-input" name="issueTrackerUrl" type="text" class="form-control" />
                </div>
            </div>
            <div class="col-12">
                <div class="input-group">
                    <div class="input-group-prepend">
                        <label for="source-input" class="input-group-text"> <i class="fas fa-code"></i> Source Code </label>
                    </div>
                    <input id="source-input" name="sourceUrl" type="text" class="form-control" />
                </div>
            </div>
            <div class="col-12">
                <div class="input-group">
                    <div class="input-group-prepend">
                        <label for="external-support-input" class="input-group-text"> <i class="fas fa-question"></i> External Support </label>
                    </div>
                    <input id="external-support-input" name="externalSupportUrl" type="text" class="form-control" />
                </div>
            </div>
            <div class="col-12 text-center"><span class="input-divider">License</span></div>
            <div class="col-12">
                <div class="input-group">
                    <div class="input-group-prepend">
                        <label for="license-type-input" class="input-group-text">Type</label>
                    </div>
                    <select v-model="form.licenseType" name="licenseType" id="license-type-input" class="custom-select">
                        <option v-text="$t('licenses.mit')"></option>
                        <option v-text="$t('licenses.apache2.0')"></option>
                        <option v-text="$t('licenses.gpl')"></option>
                        <option v-text="$t('licenses.lgpl')"></option>
                        <option v-text="$t('licenses.custom')"></option>
                    </select>
                </div>
            </div>
            <div class="col-12">
                <div class="input-group">
                    <div class="input-group-prepend">
                        <label for="license-url-input" class="input-group-text">
                            <i class="fas fa-gavel"></i> {{ form.licenseType === $t('licenses.custom') ? 'Name/' : '' }}URL
                        </label>
                    </div>
                    <div v-show="form.licenseType === $t('licenses.custom')" class="input-group-prepend">
                        <input
                            v-model.trim="form.customName"
                            type="text"
                            id="license-name-input"
                            name="licenseName"
                            class="form-control"
                            placeholder="Custom Name"
                            aria-label="License Name"
                        />
                    </div>
                    <input id="license-url-input" name="licenseUrl" class="form-control" type="text" placeholder="URL" />
                </div>
            </div>
            <div class="col-12 text-center"><span class="input-divider">SEO</span></div>
            <div class="col-12">
                <div class="input-group">
                    <div class="input-group-prepend">
                        <label for="keywords-input" class="input-group-text"> <i class="fas fa-key"></i> Keywords </label>
                    </div>
                    <input id="keywords-input" name="keywords" type="text" class="form-control" />
                </div>
            </div>
            <div class="col-12 text-center"><span class="input-divider">BBCode</span></div>
            <div class="col-12">
                <input type="hidden" name="pageContent" v-model="form.pageContent" />
                <BBCodeConverter v-model:proj-page-content="form.pageContent"></BBCodeConverter>
            </div>
        </div>
        <button
            type="button"
            class="btn btn-info float-left"
            data-toggle="collapse"
            data-target="#additional-settings"
            aria-expanded="false"
            aria-controls="additional-settings"
        >
            Additional Settings (optional)
        </button>
        <button
            type="submit"
            class="btn btn-primary float-right"
            :disabled="!success.projectName || !form.description || (form.licenseType === $t('licenses.custom') && !form.customName)"
        >
            Create project
        </button>
    </HangarForm>
</template>

<script>
import axios from 'axios';
import HangarForm from '@/components/HangarForm';
import { Category } from '@/enums';
import BBCodeConverter from '@/components/BBCodeConverter';

export default {
    name: 'CreateProject',
    components: {
        BBCodeConverter,
        HangarForm,
    },
    data() {
        return {
            ROUTES: window.ROUTES,
            organizations: window.ORGANIZAITONS,
            currentUser: window.CURRENT_USER,
            success: {
                projectName: false,
            },
            form: {
                createAs: window.CURRENT_USER.id,
                projectName: '',
                description: '',
                licenseType: '',
                customName: '',
                pageContent: '',
            },
            categories: Category.values,
        };
    },
    methods: {
        projectNameInput() {
            if (!this.form.createAs) {
                this.success.projectName = false;
            } else {
                axios
                    .post(
                        window.ROUTES.parse('PROJECTS_VALIDATE_NAME'),
                        {
                            user: this.form.createAs,
                            name: this.form.projectName,
                        },
                        window.ajaxSettings
                    )
                    .then(() => {
                        this.success.projectName = true;
                    })
                    .catch(() => {
                        this.success.projectName = false;
                    });
            }
        },
    },
};
</script>
<style lang="scss" scoped>
.row > * {
    margin-bottom: 10px;
}

label > svg {
    margin-right: 5px;
}

#license-name-input {
    border-radius: 0;
}

.input-divider {
    position: relative;
}

.input-divider::before,
.input-divider::after {
    content: '';
    position: absolute;
    width: 10vw;
    top: 0.35rem;
    height: 2px;
    overflow: hidden;
    background-color: #00000082;
}

.input-divider::before {
    left: -11vw;
}

.input-divider::after {
    right: -11vw;
}
</style>
