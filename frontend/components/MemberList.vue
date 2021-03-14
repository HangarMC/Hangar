<template>
    <v-card>
        <v-card-title>
            {{ $t('project.members') }}
            <v-btn v-if="canEdit" icon class="flex-right" :to="manageUrl">
                <v-icon> mdi-pencil </v-icon>
            </v-btn>
        </v-card-title>
        <v-card-text>
            <v-list v-if="members">
                <v-list-item v-for="member in members" :key="member.user.name">
                    <UserAvatar :username="member.user.name" clazz="user-avatar-xs"></UserAvatar>
                    <NuxtLink :to="'/' + member.user.name">{{ member.user.name }}</NuxtLink>
                    <span class="flex-right">{{ member.role.role.title }}</span>
                </v-list-item>
            </v-list>
            <div v-else class="text-center py-4">
                <v-progress-circular indeterminate />
            </div>
        </v-card-text>
    </v-card>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'nuxt-property-decorator';
import { ProjectMember } from 'hangar-internal';
import UserAvatar from '~/components/UserAvatar.vue';

@Component({
    components: { UserAvatar },
})
export default class MemberList extends Vue {
    @Prop()
    members!: ProjectMember[];

    @Prop()
    canEdit!: Boolean;

    @Prop()
    manageUrl!: String;
}
</script>

<style lang="scss" scoped></style>
