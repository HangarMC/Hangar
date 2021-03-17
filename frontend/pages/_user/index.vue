<template>
    <v-row>
        <v-col cols="12" md="8">
            <ProjectList :projects="projects" />
        </v-col>
        <v-col cols="12" md="4">
            <v-card>
                <v-card-title>{{ $t('author.orgs') }}</v-card-title>
                <v-card-text>
                    <v-list dense>
                        <v-list-item v-for="org in orgs.result" :key="org.name">
                            <NuxtLink :to="'/' + org.name">{{ org.name }}</NuxtLink>
                        </v-list-item>
                    </v-list>
                    <span v-if="!orgs || orgs.result.length === 0">
                        {{ $t('author.noOrgs', [user.name]) }}
                    </span>
                </v-card-text>
            </v-card>
            <v-card>
                <v-card-title>{{ $t('author.stars') }}</v-card-title>
                <v-card-text>
                    <v-list dense>
                        <v-list-item v-for="star in starred.result" :key="star.name">
                            <NuxtLink :to="{ name: `author-slug`, params: { author: star.namespace.owner, slug: star.namespace.slug } }">
                                {{ star.namespace.owner }}/<b>{{ star.name }}</b>
                            </NuxtLink>
                        </v-list-item>
                    </v-list>
                    <span v-if="!starred || starred.result.length === 0">
                        {{ $t('author.noStarred', [user.name]) }}
                    </span>
                </v-card-text>
            </v-card>
            <v-card class="mb-1">
                <v-card-title>{{ $t('author.watching') }}</v-card-title>
                <v-card-text>
                    <v-list dense>
                        <v-list-item v-for="watch in watching.result" :key="watch.name">
                            <NuxtLink :to="{ name: `author-slug`, params: { author: watch.namespace.owner, slug: watch.namespace.slug } }">
                                {{ watch.namespace.owner }}/<b>{{ watch.name }}</b>
                            </NuxtLink>
                        </v-list-item>
                    </v-list>
                    <span v-if="!watching || watching.result.length === 0">
                        {{ $t('author.noWatching', [user.name]) }}
                    </span>
                </v-card-text>
            </v-card>
        </v-col>
    </v-row>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { Organization, PaginatedResult, Project, ProjectCompact } from 'hangar-api';
import { Context } from '@nuxt/types';
import UserAvatar from '~/components/UserAvatar.vue';
import ProjectList from '~/components/projects/ProjectList.vue';
import { HangarUserMixin } from '~/components/mixins';

@Component({
    components: {
        UserAvatar,
        ProjectList,
    },
})
export default class AuthorPage extends HangarUserMixin {
    projects!: PaginatedResult<Project>;
    // todo load orgs from server
    orgs: PaginatedResult<Organization> = { result: [], pagination: { offset: 0, count: 0, limit: 20 } };
    starred!: PaginatedResult<ProjectCompact>;
    watching!: PaginatedResult<ProjectCompact>;

    head() {
        return {
            title: this.user.name,
        };
    }

    async asyncData({ $api, route, $util }: Context) {
        const data = await Promise.all([
            $api.request<PaginatedResult<ProjectCompact>>(`users/${route.params.user}/starred`, false),
            $api.request<PaginatedResult<ProjectCompact>>(`users/${route.params.user}/watching`, false),
            $api.request<PaginatedResult<Project>>(`projects`, false, 'get', {
                owner: route.params.user,
            }),
        ]).catch($util.handlePageRequestError);
        if (typeof data === 'undefined') return;
        return { starred: data[1], watching: data[0], projects: data[2] };
    }
}
</script>

<style lang="scss" scoped>
.v-card {
    margin-bottom: 2em;
}
</style>
