<template>
    <div>
        <v-tabs>
            <v-tab
                v-for="version in versionPlatforms"
                :key="`platform-${version}`"
                nuxt
                :to="{ name: `author-slug-versions-version-platform`, params: { ...$route.params, platform: version.toLowerCase() } }"
            >
                <v-icon left v-text="`$vuetify.icons.${version.toLowerCase()}`"></v-icon>
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
        if (typeof versions === 'undefined' || versions.length < 1) {
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
            let path = route.path;
            if (path.endsWith('/')) {
                path = path.substring(0, path.length - 1);
            }
            redirect(`${path}/${versionMap.keys().next().value.toLowerCase()}`);
        }
        return { versions: versionMap, versionPlatforms };
    }
}
</script>

<style lang="scss" scoped></style>
