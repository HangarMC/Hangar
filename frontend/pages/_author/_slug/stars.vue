<template>
    <v-card>
        <v-card-title>
            {{ $t('project.stargazers') }}
        </v-card-title>
        <v-card-text>
            <v-row v-if="stargazers.result && stargazers.result.length > 0">
                <v-col v-for="stargazer in stargazers.result" :key="stargazer.name" md="4">
                    <UserAvatar clazz="user-avatar-xs" :username="stargazer.name" :avatar-url="$util.avatarUrl(stargazer.name)"></UserAvatar>
                    {{ stargazer.name }}
                </v-col>
            </v-row>
            <v-alert v-else type="info" prominent>
                {{ $t('project.noStargazers') }}
            </v-alert>
        </v-card-text>
    </v-card>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { PaginatedResult, User } from 'hangar-api';
import { Context } from '@nuxt/types';
import UserAvatar from '~/components/UserAvatar.vue';

@Component({
    components: { UserAvatar },
})
export default class ProjectStarsPage extends Vue {
    stargazers!: PaginatedResult<User>;

    async asyncData({ $api, params, $util }: Context) {
        const watchers = await $api
            .request<PaginatedResult<User>>(`projects/${params.author}/${params.slug}/watchers`, false)
            .catch($util.handlePageRequestError);
        return { watchers };
    }
}
</script>

<style lang="scss" scoped></style>
