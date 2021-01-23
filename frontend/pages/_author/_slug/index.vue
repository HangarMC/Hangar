<template>
    <div>
        <ProjectLayout :project="project" :author="user">
            <v-row>
                <v-col cols="12" md="8"> Description </v-col>
                <v-col cols="12" md="4"> Download </v-col>
            </v-row>
        </ProjectLayout>
    </div>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { Context } from '@nuxt/types';
import { ApiError, Project, User } from 'hangar-api';
import { AxiosError } from 'axios';
import ProjectLayout from '~/components/ProjectLayout.vue';

@Component({
    components: { ProjectLayout },
})
export default class ProjectPage extends Vue {
    project?: Project;
    user?: User = { name: 'test' }; // todo load user

    head() {
        return {
            title: this.project?.name,
        };
    }

    async asyncData({ $api, params, error }: Context): Promise<{ project: Project } | void> {
        return await $api
            .request<Project>(`projects/${params.author}/${params.slug}`)
            .then((project) => {
                return Promise.resolve({ project });
            })
            .catch((err: AxiosError) => {
                const hangarError: ApiError = err.response?.data as ApiError;
                error({
                    message: hangarError.message,
                    statusCode: hangarError.error.code,
                });
            });
    }
}
</script>

<style lang="scss" scoped></style>
