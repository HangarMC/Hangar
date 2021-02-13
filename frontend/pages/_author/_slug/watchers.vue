<template>
    <v-card>
        <v-card-title>
            {{ $t('project.watchers') }}
        </v-card-title>
        <v-card-text>
            <v-row>
                <v-col v-for="watcher in watchers" :key="watcher.name" md="4">
                    <UserAvatar clazz="user-avatar-xs" :username="watcher.name" :avatar-url="$util.avatarUrl(watcher.name)"></UserAvatar>
                    {{ watcher.name }}
                </v-col>
            </v-row>
        </v-card-text>
    </v-card>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { User } from 'hangar-api';
import UserAvatar from '~/components/UserAvatar.vue';

@Component({
    components: { UserAvatar },
})
export default class ProjectWatchersPage extends Vue {
    // todo load watchers from server
    watchers: Array<User> = [this.$util.dummyUser(), this.$util.dummyUser(), this.$util.dummyUser(), this.$util.dummyUser(), this.$util.dummyUser()];
}
</script>

<style lang="scss" scoped></style>
