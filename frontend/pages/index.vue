<template>
    <div>
        <v-row>
            <v-col cols="12" sm="8" md="5" offset-md="3">
                <v-row justify="center" align="center">
                    <h1>Hangar</h1>
                    <v-subheader v-text="$t('hangar.subtitle')"></v-subheader>
                </v-row>
                <v-row justify="center" align="center">
                    <v-col cols="12">
                        <v-text-field v-model="projectFilter" :label="$t('hangar.projectSearch.query', [totalProjects])" clearable></v-text-field>
                    </v-col>
                </v-row>
                <v-row justify="center" align="center">
                    <v-col cols="12">
                        <ProjectList :projects="projects"></ProjectList>
                    </v-col>
                </v-row>
            </v-col>

            <v-col cols="12" sm="2" md="2">
                <HangarSponsor :sponsor="sponsor" />

                <v-select></v-select>

                <v-checkbox :label="$t('hangar.projectSearch.relevanceSort')"></v-checkbox>

                <v-list dense>
                    <v-subheader>Categories</v-subheader>
                    <v-list-item-group>
                        <v-list-item v-for="cat in $store.getters.visibleCategories" :key="cat.apiName">
                            <v-list-item-icon>
                                <v-icon v-text="cat.icon" />
                            </v-list-item-icon>
                            <v-list-item-content>
                                <v-list-item-title v-text="$t(`project.category.${cat.apiName}`)"></v-list-item-title>
                            </v-list-item-content>
                        </v-list-item>
                    </v-list-item-group>
                </v-list>

                <v-list dense>
                    <v-subheader>Platforms</v-subheader>
                    <v-list-item-group>
                        <v-list-item v-for="(platform, i) in platforms" :key="i">
                            <v-list-item-icon>
                                <v-icon v-text="`$vuetify.icons.${platform.name.toLowerCase()}`" />
                            </v-list-item-icon>
                            <v-list-item-content>
                                <v-list-item-title v-text="platform.name"></v-list-item-title>
                            </v-list-item-content>
                        </v-list-item>
                    </v-list-item-group>
                </v-list>
            </v-col>
        </v-row>
    </div>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { PaginatedResult, Project, Sponsor } from 'hangar-api';
import { IPlatform, ProjectPage } from 'hangar-internal';
import { Context } from '@nuxt/types';
import ProjectList from '~/components/projects/ProjectList.vue';
import HangarSponsor from '~/components/layouts/Sponsor.vue';
import { RootState } from '~/store';

@Component({
    components: {
        ProjectList,
        HangarSponsor,
    },
})
export default class Home extends Vue {
    // TODO implement filtering
    projects?: PaginatedResult<Project>;
    totalProjects: Number = 1337;
    projectFilter: String | null = null;
    sponsor!: Sponsor;

    get platforms(): IPlatform[] {
        return Array.from((this.$store.state as RootState).platforms.values());
    }

    head() {
        return {
            title: 'Home',
        };
    }

    async asyncData({ $api, $util }: Context) {
        const sponsor = await $api.requestInternal<ProjectPage>(`data/sponsor`, false).catch<any>($util.handleRequestError);
        const projects = await $api
            .request<PaginatedResult<Project>>('projects', false, 'get', { limit: 25, offset: 0 })
            .catch<any>($util.handlePageRequestError);
        return { projects, sponsor };
    }
}
</script>
