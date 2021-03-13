<template>
    <v-card>
        <v-card-title>
            {{ $t('project.watchers') }}
        </v-card-title>
        <v-card-text>
            <v-row v-if="watchers.result && watchers.result.length > 0">
                <v-col v-for="watcher in watchers.result" :key="watcher.name" md="4">
                    <UserAvatar clazz="user-avatar-xs" :username="watcher.name" :avatar-url="$util.avatarUrl(watcher.name)"></UserAvatar>
                    {{ watcher.name }}
                </v-col>
            </v-row>
            <v-alert v-else type="info" prominent>
                {{ $t('project.noWatchers') }}
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
export default class ProjectWatchersPage extends Vue {
    watchers!: PaginatedResult<User>;

    async asyncData({ $api, params, $util }: Context) {
        const watchers = await $api
            .request<PaginatedResult<User>>(`projects/${params.author}/${params.slug}/watchers`, false)
            .catch($util.handlePageRequestError);
        return { watchers };
    }
}
</script>

<style lang="scss" scoped></style>
