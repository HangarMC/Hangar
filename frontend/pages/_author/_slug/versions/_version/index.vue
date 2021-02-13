<template>
    <div>{{ $nuxt.$route.name }}</div>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { Context } from '@nuxt/types';
import { Version } from 'hangar-api';

// TODO implement ProjectVersionsVersionPage
@Component
export default class ProjectVersionsVersionPage extends Vue {
    versions!: Version[];

    async asyncData({ $api, $util, params }: Context) {
        const versions = await $api
            .request<Version[]>(`projects/${params.author}/${params.slug}/versions/${params.version}`)
            .catch($util.handlePageRequestError);
        console.log(versions);
        return { versions };
    }
}
</script>

<style lang="scss" scoped></style>
