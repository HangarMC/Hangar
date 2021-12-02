<template>
    <div>
        <v-row>
            <v-col cols="12" sm="8" md="5" offset-md="3">
                <br />
                <br />
                <v-row justify="center" align="center">
                    <h1>Hangar</h1>
                    <v-subheader v-text="$t('hangar.subtitle')" style="position: relative; top: 3px" />
                </v-row>
                <v-row justify="center" align="center">
                    <v-col cols="12">
                        <v-text-field v-model="filters.search" :label="$t('hangar.projectSearch.query', [projects.pagination.count])" clearable />
                    </v-col>
                </v-row>
                <v-row justify="center" align="center">
                    <v-col cols="12">
                        <ProjectList :projects="projects" :list-options.sync="options" />
                    </v-col>
                </v-row>
            </v-col>

            <v-col cols="12" sm="2" md="2">
                <br />
                <HangarSponsor :sponsor="sponsor" />

                <v-select />

                <v-checkbox :label="$t('hangar.projectSearch.relevanceSort')" />

                <v-list dense style="border-radius: 5px">
                    <v-row justify="center" align="center" style="position: relative; top: 5px; padding-bottom: 10px">
                        <v-subheader>Categories</v-subheader>
                    </v-row>
                    <v-list-item-group v-model="filters.categories" multiple>
                        <v-list-item v-for="cat in $store.getters.visibleCategories" :key="cat.apiName" :value="cat.apiName">
                            <v-list-item-icon>
                                <v-icon v-text="cat.icon" />
                            </v-list-item-icon>
                            <v-list-item-content>
                                <v-list-item-title v-text="$t(`project.category.${cat.apiName}`)" />
                            </v-list-item-content>
                        </v-list-item>
                    </v-list-item-group>
                </v-list>
                <br />
                <v-list dense style="border-radius: 5px">
                    <v-row justify="center" align="center" style="position: relative; top: 5px; padding-bottom: 10px">
                        <v-subheader>Platforms</v-subheader>
                    </v-row>
                    <v-list-item-group v-model="filters.platforms" multiple>
                        <v-list-item v-for="platform in $store.getters.visiblePlatforms" :key="platform.enumName" :value="platform.name">
                            <v-list-item-icon>
                                <v-icon v-text="`$vuetify.icons.${platform.name.toLowerCase()}`" />
                            </v-list-item-icon>
                            <v-list-item-content>
                                <v-list-item-title v-text="platform.name" />
                            </v-list-item-content>
                        </v-list-item>
                    </v-list-item-group>
                </v-list>
            </v-col>
        </v-row>
        <br />
    </div>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { PaginatedResult, Project, Sponsor } from 'hangar-api';
import { IPlatform } from 'hangar-internal';
import { Context } from '@nuxt/types';
import { DataOptions } from 'vuetify';
import { ProjectList } from '~/components/projects';
import HangarSponsor from '~/components/layouts/Sponsor.vue';
import { RootState } from '~/store';
import { HangarComponent } from '~/components/mixins';
import { Platform, ProjectCategory } from '~/types/enums';

@Component({
    components: {
        ProjectList,
        HangarSponsor,
    },
})
export default class Home extends HangarComponent {
    // TODO implement filtering
    projects!: PaginatedResult<Project>;
    sponsor!: Sponsor;
    filters = {
        search: null as string | null,
        platforms: [] as Platform[],
        categories: [] as ProjectCategory[],
    };

    options = {
        itemsPerPage: 25,
        page: 1,
    } as DataOptions;

    head() {
        const meta = this.$seo.head('Home', null, this.$route, null);
        meta.script = meta.script ? meta.script : [];
        meta.script.push({
            type: 'application/ld+json',
            json: {
                '@context': 'https://schema.org',
                '@type': 'WebSite',
                url: this.$seo.baseUrl(),
                potentialAction: {
                    '@type': 'SearchAction',
                    target: this.$seo.baseUrl() + '/?q={search_term_string}', // todo fix once search actually works
                    'query-input': 'required name=search_term_string',
                },
            },
        });
        return meta;
    }

    mounted() {
        this.$watch('options', this.reloadProjectList, { deep: true });
        this.$watch('filters', this.reloadProjectList, { deep: true });
    }

    get platforms(): IPlatform[] {
        return Array.from((this.$store.state as RootState).platforms.values());
    }

    reloadProjectList() {
        this.$api
            .request<PaginatedResult<Project>>('projects', false, 'get', this.requestOptions)
            .then((result) => {
                this.projects = result;
            })
            .catch(this.$util.handleRequestError);
    }

    get requestOptions() {
        const requestOptions: { [key: string]: any } = {
            limit: this.options.itemsPerPage,
            offset: (this.options.page - 1) * this.options.itemsPerPage,
            category: this.filters.categories,
            platform: this.filters.platforms,
        };
        if (this.filters.search != null && this.filters.search.length > 0) {
            requestOptions.q = this.filters.search;
        }
        return requestOptions;
    }

    async asyncData({ $api, $util }: Context) {
        const res = await Promise.all<Sponsor, PaginatedResult<Project>>([
            $api.requestInternal<Sponsor>(`data/sponsor`, false),
            $api.request<PaginatedResult<Project>>('projects', false, 'get', { limit: 25, offset: 0 }),
        ]).catch($util.handlePageRequestError);
        if (typeof res === 'undefined') {
            return;
        }
        return { sponsor: res[0], projects: res[1] };
    }
}
</script>
