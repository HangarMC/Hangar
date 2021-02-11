<template>
    <div>
        <v-row>
            <v-col cols="1">
                <UserAvatar :username="user.name" :avatar-url="$util.avatarUrl(user.name)" :clazz="avatarClazz"></UserAvatar>
            </v-col>
            <v-col cols="auto">
                <h1 class="d-inline">{{ user.name }}</h1>
                <v-list dense flat class="d-inline-flex">
                    <v-list-item v-for="btn in buttons" :key="btn.name">
                        <v-list-item-content>
                            <v-btn icon
                                ><v-icon>{{ btn.icon }}</v-icon></v-btn
                            >
                        </v-list-item-content>
                    </v-list-item>
                </v-list>
                <div>
                    <v-subheader>
                        <template v-if="user.tagline">{{ user.tagline }}</template>
                        <!-- TODO tagline edit -->
                        <!--<template v-else-if="u.isCurrent() || canEditOrgSettings(u, o, so)">{{ $t('author.addTagline') }}</template>-->
                        <v-btn icon>
                            <v-icon>mdi-pencil</v-icon>
                        </v-btn>
                    </v-subheader>
                </div>
            </v-col>
            <v-spacer />
            <v-col cols="2">
                <v-subheader>{{ $t('author.numProjects', [user.projectCount]) }}</v-subheader>
                <v-subheader>{{ $t('author.memberSince', [$util.prettyDate(user.joinDate)]) }}</v-subheader>
                <a :href="$util.forumUrl(user.name)">{{ $t('author.viewForums') }}<v-icon>mdi-open-in-new</v-icon></a>
            </v-col>
        </v-row>
        <v-divider />
        <v-row>
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
        </v-row>
    </div>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { PaginatedResult, Project } from 'hangar-api';
import { Context } from '@nuxt/types';
import { HangarUser } from 'hangar-internal';
import UserAvatar from '~/components/UserAvatar.vue';
import ProjectList from '~/components/projects/ProjectList.vue';

interface Button {
    icon: String;
    action?: Function;
    url: String;
    name: String;
}

@Component({
    components: {
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

    get avatarClazz(): String {
        return 'user-avatar-md';
        // todo check org an add 'organization-avatar'
    }

    get buttons(): Button[] {
        const buttons = [] as Button[];
        // TODO user admin
        buttons.push({ icon: 'mdi-cog', url: '', name: 'Settings' });
        buttons.push({ icon: 'mdi-lock-open-outline', url: '', name: 'Lock Account' });
        buttons.push({ icon: 'mdi-lock-outline', url: '', name: 'Unlock Account' });
        buttons.push({ icon: 'mdi-key', url: '', name: 'API Keys' });
        buttons.push({ icon: 'mdi-calendar', url: '', name: 'Activity' });
        buttons.push({ icon: 'mdi-wrench', url: '', name: 'User Admin' });
        return buttons;
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
