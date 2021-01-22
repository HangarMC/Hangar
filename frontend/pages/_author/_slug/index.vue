<template>
    <div>{{ project.name }}</div>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { Context } from '@nuxt/types';
import { ApiError, Project } from 'hangar-api';
import { AxiosError } from 'axios';

@Component
export default class ProjectPage extends Vue {
    project?: Project;

    head() {
        return {
            title: this.project?.name,
        };
    }

    async asyncData({ $api, params, error }: Context): Promise<{ project: Project }> {
        return await $api
            .request<Project>(`projects/${params.author}/${params.slug}`)
            .then((project) => {
                return { project };
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
