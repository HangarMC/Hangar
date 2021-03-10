<template>
    <div>
        <v-tabs>
            <v-tab v-for="version in versionPlatforms" :key="`platform-${version}`" nuxt :to="version.toLowerCase()">
                {{ version }}
            </v-tab>
        </v-tabs>
        <NuxtChild :project="project" :versions="versions" />
    </div>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { HangarVersion } from 'hangar-internal';
import { Context } from '@nuxt/types';
import { HangarProjectMixin } from '~/components/mixins';
import { Platform } from '~/types/enums';

@Component
export default class ProjectVersionPageParent extends HangarProjectMixin {
    versions!: Map<Platform, HangarVersion>;
    versionPlatforms!: Set<Platform>;

    async asyncData({ $api, $util, params, error, redirect, route }: Context) {
        const versions = await $api
            .requestInternal<HangarVersion[]>(`versions/version/${params.author}/${params.slug}/versions/${params.version}`)
            .catch<any>($util.handlePageRequestError);
        if (versions.length < 1) {
            return error({
                statusCode: 404,
            });
        }
        const versionMap: Map<Platform, HangarVersion> = new Map<Platform, HangarVersion>();
        const versionPlatforms: Set<Platform> = new Set<Platform>();
        for (const version of versions) {
            for (const platformKey in version.platformDependencies) {
                versionPlatforms.add(platformKey as Platform);
                versionMap.set(platformKey as Platform, version);
            }
        }
        if (!params.platform) {
            redirect(`${route.path}/${versionMap.keys().next().value.toLowerCase()}`);
        }
        return { versions: versionMap, versionPlatforms };
    }
}
</script>

<style lang="scss" scoped></style>
