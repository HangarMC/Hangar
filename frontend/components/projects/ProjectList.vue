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
                                <v-img :src="project.iconUrl" :alt="project.name" width="60px" height="60px" class="my-2 ml-2"></v-img>
                            </div>
                            <div class="ml-2">
                                <span class="text-h6">{{ project.name }}</span>
                                <br />
                                <span class="text-subtitle-2">{{ project.description }}</span>
                            </div>
                        </v-row>
                    </v-sheet>
                </NuxtLink>
            </v-hover>
        </template>
    </v-data-iterator>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'nuxt-property-decorator';
import { PaginatedResult, Project } from 'hangar-api';
import { PropType } from 'vue';
import { DataOptions } from 'vuetify';

@Component
export default class ProjectList extends Vue {
    @Prop({ type: Object as PropType<PaginatedResult<Project>>, required: true })
    projects!: PaginatedResult<Project>;

    options = {
        page: 1,
        itemsPerPage: 25,
    } as DataOptions;
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
