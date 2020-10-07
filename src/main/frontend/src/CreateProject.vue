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
                    <input v-model="form.projectName" type="text" id="project-name" name="name" class="form-control" required @input="projectNameInput" />
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
                    <input v-model="form.description" type="text" name="description" class="form-control" id="project-description" required />
                </div>
            </div>
            <div class="col-12">
                <div class="input-group">
                    <div class="input-group-prepend">
                        <label for="category-input" class="input-group-text" v-text="$t('project.create.input.category')"></label>
                    </div>
                    <select v-model="form.category" id="category-input" name="category" class="custom-select" required>
                        <option v-for="cat in categories" :key="cat.id" :value="cat.name" v-text="cat.name"></option>
                    </select>
                </div>
            </div>
        </div>
        <button type="submit" class="btn btn-primary float-right" :disabled="!success.projectName">Create project</button>
    </HangarForm>
</template>

<script>
import axios from 'axios';
import HangarForm from '@/components/HangarForm';
import { Category } from '@/enums';

export default {
    name: 'CreateProject',
    components: {
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
                category: Category.values[0].name,
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
</style>
