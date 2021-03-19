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
import { Component, Prop, Vue } from 'nuxt-property-decorator';
import { ProjectMember } from 'hangar-internal';
import { PropType } from 'vue';
import UserAvatar from '~/components/UserAvatar.vue';

// TODO v-model for users
@Component({
    components: { UserAvatar },
})
export default class UserSelectionForm extends Vue {
    @Prop({ type: Array as PropType<ProjectMember[]>, required: true })
    users!: ProjectMember[];
}
</script>

<style lang="scss" scoped></style>
