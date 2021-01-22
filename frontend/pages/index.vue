<template>
    <div>
        <v-row>
            <v-col cols="12" sm="8" md="5" offset-md="3">
                <v-row justify="center" align="center">
                    <h1>Hangar</h1>
                    <v-subheader>A Minecraft package repository</v-subheader>
                </v-row>
                <v-row justify="center" align="center">
                    <v-col cols="12">
                        <v-text-field
                            v-model="projectFilter"
                            :label="'Search in ' + totalProjects + ' projects, proudly made by the community...'"
                            clearable
                        ></v-text-field>
                    </v-col>
                </v-row>
                <v-row justify="center" align="center">
                    <v-col cols="12">
                        <ProjectList :projects="projects.result"></ProjectList>
                    </v-col>
                </v-row>
            </v-col>

            <v-col cols="12" sm="2" md="2">
                <sponsor />

                <v-select></v-select>

                <v-checkbox label="Sort with relevance"></v-checkbox>

                <v-list dense>
                    <v-subheader>Categories</v-subheader>
                    <v-list-item-group>
                        <v-list-item v-for="(cat, i) in categories" :key="i">
                            <v-list-item-icon>
                                <v-icon v-text="cat.icon" />
                            </v-list-item-icon>
                            <v-list-item-content>
                                <v-list-item-title v-text="cat.text"></v-list-item-title>
                            </v-list-item-content> </v-list-item
                    ></v-list-item-group>
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
import { Context } from '@nuxt/types';
import { PaginatedResult, Project } from 'hangar-api';

// TODO move somewhere else
interface Category {
    icon: string;
    text: string;
}

interface Platform {
    icon: string;
    text: string;
}

@Component
export default class Home extends Vue {
    projects?: PaginatedResult<Project>;
    totalProjects: Number = 1337;
    projectFilter: String | null = null;
    // TODO get categories from server
    categories: Category[] = [
        {
            icon: 'mdi-home',
            text: 'Test',
        },
    ];

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

    async asyncData({ $api }: Context): Promise<{ projects: PaginatedProjectList }> {
        return { projects: await $api.request<PaginatedProjectList>('projects', 'get', { limit: 25, offset: 0 }) };
    }
}
</script>
