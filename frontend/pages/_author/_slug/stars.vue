<template>
    <v-card>
        <v-card-title>
            {{ $t('project.stargazers') }}
        </v-card-title>
        <v-card-text>
            <v-row>
                <v-col v-for="stargazer in stargazers" :key="stargazer.name" md="4">
                    <UserAvatar clazz="user-avatar-xs" :username="stargazer.name" :avatar-url="$util.avatarUrl(stargazer.name)"></UserAvatar>
                    {{ stargazer.name }}
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
export default class ProjectStarsPage extends Vue {
    // todo load stargazers from server
    stargazers: Array<User> = [this.$util.dummyUser(), this.$util.dummyUser(), this.$util.dummyUser(), this.$util.dummyUser(), this.$util.dummyUser()];
}
</script>

<style lang="scss" scoped></style>
