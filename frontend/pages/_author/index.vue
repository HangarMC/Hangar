<template>
    <UserProfile :user="user">
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
    </UserProfile>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { Organization, PaginatedResult, Project } from 'hangar-api';
import { Context } from '@nuxt/types';
import { HangarUser } from 'hangar-internal';
import UserAvatar from '~/components/UserAvatar.vue';
import ProjectList from '~/components/projects/ProjectList.vue';
import UserProfile from '~/components/layouts/UserProfile.vue';

@Component({
    components: {
        UserProfile,
        UserAvatar,
        ProjectList,
    },
})
export default class AuthorPage extends Vue {
    user!: HangarUser;

    projects!: PaginatedResult<Project>;
    // todo load orgs from server
    orgs: PaginatedResult<Organization> = { result: [], pagination: { offset: 0, count: 0, limit: 20 } };
    starred!: PaginatedResult<Project>;
    watching!: PaginatedResult<Project>;

    head() {
        return {
            title: this.user.name,
        };
    }

    async asyncData({ $api, route, $util }: Context) {
        const user = await $api.requestInternal<HangarUser>(`users/${route.params.author}`, false).catch($util.handlePageRequestError);
        const starred = await $api.request<Project>(`users/${route.params.author}/starred`, false).catch($util.handlePageRequestError);
        const watching = await $api.request<Project>(`users/${route.params.author}/watching`, false).catch($util.handlePageRequestError);
        const projects = await $api
            .request<PaginatedResult<Project>>(`projects`, false, 'get', {
                owner: route.params.author,
            })
            .catch($util.handlePageRequestError);
        return { user, projects, starred, watching };
    }
}
</script>

<style lang="scss" scoped>
.v-card {
    margin-bottom: 2em;
}
</style>
