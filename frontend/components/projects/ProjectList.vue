<template>
    <v-data-iterator
        :items="projects.result"
        :footer-props="{ itemsPerPageOptions: [5, 15, 25] }"
        :options.sync="options"
        :server-items-length="projects.pagination.count"
    >
        <template #default="props">
            <v-hover v-for="project in props.items" :key="project.id" v-slot="{ hover }" style="width: 100%; height: 78px" class="d-block mb-3">
                <NuxtLink :to="`/${project.namespace.owner}/${project.namespace.slug}`">
                    <v-sheet :elevation="hover ? 24 : 0" height="100%" width="100%" tile color="accent" class="transition-swing grow-on-hover mb-3">
                        <v-row no-gutters>
                            <div class="flex-shrink-0">
                                <v-img
                                    :src="$util.projectUrl(project.namespace.owner, project.namespace.slug)"
                                    :alt="project.name"
                                    width="60px"
                                    height="60px"
                                    class="my-2 ml-2"
                                ></v-img>
                            </div>
                            <div class="ml-2">
                                <span class="text-h6">{{ project.name }}</span>
                                <span v-if="displayAuthor" class="text-h7">by {{ project.namespace.owner }}</span>
                                <br />
                                <span class="text-subtitle-2">{{ project.description }}</span>
                            </div>
                            <div class="flex-grow-1 mr-2 mt-1">
                                <div class="text--secondary text-right body-1">
                                    <span :title="$tc('project.info.views', project.stats.views, [project.stats.views])">
                                        <v-icon small>mdi-eye</v-icon>
                                        {{ project.stats.views }}
                                    </span>
                                    <span :title="$tc('project.info.totalDownloads', project.stats.downloads, [project.stats.downloads])">
                                        <v-icon small class="ml-1">mdi-download</v-icon>
                                        {{ project.stats.downloads }}
                                    </span>
                                    <span :title="$tc('project.info.stars', project.stats.stars, [project.stats.stars])">
                                        <v-icon small class="ml-1">mdi-star</v-icon>
                                        {{ project.stats.stars }}
                                    </span>
                                    <v-icon small class="ml-3" :title="$store.state.projectCategories.get(project.category).title">{{
                                        $store.state.projectCategories.get(project.category).icon
                                    }}</v-icon>
                                </div>
                            </div>
                        </v-row>
                    </v-sheet>
                </NuxtLink>
            </v-hover>
        </template>
        <template #no-data>
            <div>{{ $t('hangar.projectSearch.noProjects') }}</div>
        </template>
        <template #no-results>
            <div>{{ $t('hangar.projectSearch.noProjectsFound') }}</div>
        </template>
    </v-data-iterator>
</template>

<script lang="ts">
import { Component, Prop, PropSync, Vue } from 'nuxt-property-decorator';
import { PaginatedResult, Project } from 'hangar-api';
import { PropType } from 'vue';
import { DataOptions } from 'vuetify';

@Component
export default class ProjectList extends Vue {
    @Prop({ type: Object as PropType<PaginatedResult<Project>>, required: true })
    projects!: PaginatedResult<Project>;

    @Prop({ type: Boolean, default: () => false })
    displayAuthor!: boolean;

    @PropSync('listOptions', {
        type: Object as PropType<DataOptions>,
        default: () => ({
            page: 1,
            itemsPerPage: 25,
        }),
    })
    options!: DataOptions;
}
</script>
<style lang="scss" scoped>
.grow-on-hover {
    transition: all 0.2s ease-in-out;
}

.grow-on-hover:hover {
    transform: scale(1.015);
}

a {
    text-decoration: none;
}
</style>
