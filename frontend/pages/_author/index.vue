<template>
    <UserProfile :user="user">
        <v-col cols="12" md="8">
            <ProjectList :projects="projects" />
        </v-col>
        <v-col cols="12" md="4">
            <v-list dense>
                <v-subheader>{{ $t('author.orgs') }}</v-subheader>
                <!-- todo display orgs -->
            </v-list>
            <v-list dense>
                <v-subheader>{{ $t('author.stars') }}</v-subheader>
                <!-- todo display stars -->
            </v-list>
            <v-list dense>
                <v-subheader>{{ $t('author.watching') }}</v-subheader>
                <!-- todo watching -->
            </v-list>
        </v-col>
    </UserProfile>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { PaginatedResult, Project } from 'hangar-api';
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

    head() {
        return {
            title: this.user.name,
        };
    }

    async asyncData({ $api, route, $util }: Context) {
        const user = await $api.requestInternal<HangarUser>(`users/${route.params.author}`, false).catch($util.handlePageRequestError);
        const projects = await $api
            .request<PaginatedResult<Project>>(`projects`, false, 'get', {
                owner: route.params.author,
            })
            .catch($util.handlePageRequestError);
        return { user, projects };
    }
}
</script>

<style lang="scss" scoped></style>
