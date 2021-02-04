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
                        <ProjectList :projects="projects.result"></ProjectList>
                    </v-col>
                </v-row>
            </v-col>

            <v-col cols="12" sm="2" md="2">
                <HangarSponsor />

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
                        <v-list-item v-for="(cat, i) in platforms" :key="i">
                            <v-list-item-icon>
                                <v-icon v-text="cat.icon" />
                            </v-list-item-icon>
                            <v-list-item-content>
                                <v-list-item-title v-text="cat.text"></v-list-item-title>
                            </v-list-item-content> </v-list-item
                    ></v-list-item-group>
                </v-list>
            </v-col>
        </v-row>
    </div>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { PaginatedResult, Project } from 'hangar-api';
import ProjectList from '~/components/ProjectList.vue';
import HangarSponsor from '~/components/layouts/Sponsor.vue';

// TODO move somewhere else
interface Platform {
    icon: string;
    text: string;
}

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

    // TODO get platforms from server
    platforms: Platform[] = [
        {
            icon: 'mdi-home',
            text: 'Test',
        },
    ];

    head() {
        return {
            title: 'Home',
        };
    }

    asyncData() {
        // async asyncData({ $api }: Context): Promise<{ projects: [] }> {
        return { projects: { result: [] } };
        // return { projects: await $api.request<PaginatedResult<Project>>('projects', 'get', { limit: 25, offset: 0 }) };
    }
}
</script>
