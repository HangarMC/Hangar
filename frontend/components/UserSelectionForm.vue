<template>
    <v-list>
        <v-list-item v-for="member in users" :key="member.user.name">
            <UserAvatar :username="member.user.name" clazz="user-avatar-xs"></UserAvatar>
            <NuxtLink :to="'/' + member.user.name">{{ member.user.name }}</NuxtLink>
            <span class="flex-right">{{ member.role.role.title }}</span>
        </v-list-item>
        <v-divider />
        <v-list-item>
            <!-- todo auto suggest users here -->
            <v-text-field :label="$t('form.userSelection.addUser')"></v-text-field>
        </v-list-item>
        <v-divider />
    </v-list>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { ProjectMember } from 'hangar-internal';
import UserAvatar from '~/components/UserAvatar.vue';
import { RoleCategory } from '~/types/enums';

// TODO v-model for users
@Component({
    components: { UserAvatar },
})
export default class UserSelectionForm extends Vue {
    users: Array<ProjectMember> = [
        {
            user: this.$util.dummyUser(),
            role: {
                accepted: true,
                id: -1,
                createdAt: '',
                role: { title: 'Owner', value: 'owner', roleId: -1, category: RoleCategory.PROJECT, permission: '', color: { hex: '' } },
            },
        },
    ];
}
</script>

<style lang="scss" scoped></style>
