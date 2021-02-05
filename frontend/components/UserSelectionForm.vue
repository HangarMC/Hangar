<template>
    <div>
        <v-list>
            <v-list-item v-for="user in users" :key="user.id">
                <UserAvatar :username="user.name" :avatar-url="$util.avatarUrl(user.name)" clazz="user-avatar-xs" />
                {{ user.name }}
                ROLE HERE
                <!-- todo role -->
            </v-list-item>
            <v-divider />
            <v-list-item>
                <!-- todo auto suggest users here -->
                <v-text-field :label="$t('form.userSelection.addUser')"></v-text-field>
            </v-list-item>
            <v-divider />
        </v-list>
        <v-btn>{{ $t('form.userSelection.create') }}</v-btn>
    </div>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { User } from 'hangar-api';
import UserAvatar from '~/components/UserAvatar.vue';

// TODO v-model for users
@Component({
    components: { UserAvatar },
})
export default class UserSelectionForm extends Vue {
    users: Array<User> = [this.$util.dummyUser()];
}
</script>

<style lang="scss" scoped></style>
