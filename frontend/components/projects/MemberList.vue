<template>
    <v-card>
        <v-card-title>
            {{ $t('project.members') }}
            <template v-if="!alwaysEditing">
                <v-tooltip v-if="!editing" bottom>
                    <template #activator="{ on }">
                        <v-btn v-if="$perms.canEditSubjectSettings" icon color="info" class="flex-right" v-on="on" @click="editing = true">
                            <v-icon>mdi-pencil</v-icon>
                        </v-btn>
                    </template>
                    <span>{{ $t('general.edit') }}</span>
                </v-tooltip>
                <v-btn v-else-if="!isEdited" icon color="error" class="flex-right" @click="editing = false">
                    <v-icon>mdi-close</v-icon>
                </v-btn>
            </template>
            <v-btn
                v-if="(alwaysEditing || isEdited) && !noSaveBtn"
                color="success"
                small
                class="flex-right"
                :loading="loading.save"
                :disabled="!isEdited"
                @click="save"
            >
                <v-icon left>mdi-check</v-icon>
                {{ $t('general.save') }}
            </v-btn>
        </v-card-title>
        <v-card-text>
            <v-list>
                <v-list-item
                    v-for="member in editingMembers"
                    :key="member.name"
                    :class="{ 'to-delete': member.toDelete, editing: member.editing, new: member.new }"
                >
                    <UserAvatar :username="member.name" clazz="user-avatar-xs" />
                    <NuxtLink v-if="!isEditing" :to="'/' + member.name">{{ member.name }}</NuxtLink>
                    <span v-else>{{ member.name }}</span>

                    <template v-if="isEditing && (!member.editing || member.new) && member.roleAssignable">
                        <v-btn v-if="!member.toDelete" icon x-small color="error" class="ml-1" @click="removeMember(member)">
                            <v-icon>mdi-delete</v-icon>
                        </v-btn>
                        <v-btn v-else icon x-small color="error" class="ml-1" @click="member.toDelete = false"><v-icon>mdi-undo</v-icon></v-btn>
                    </template>

                    <span v-if="!member.editing" class="flex-right">
                        <span v-if="member.roleAccepted">{{ member.roleTitle }}</span>
                        <span v-else>{{ $t('form.memberList.invitedAs', [member.roleTitle]) }}</span>
                    </span>
                    <v-select
                        v-else
                        v-model="member.roleId"
                        dense
                        hide-details
                        single-line
                        :items="roles"
                        item-text="title"
                        item-value="roleId"
                        class="flex-right"
                        style="max-width: 120px"
                    />

                    <template v-if="isEditing && !member.toDelete && !member.new && member.roleAssignable">
                        <v-tooltip v-if="!member.editing" bottom>
                            <template #activator="{ on }">
                                <v-btn class="flex-right ml-1" icon x-small color="info" v-on="on" @click="member.editing = true">
                                    <v-icon>mdi-pencil</v-icon>
                                </v-btn>
                            </template>
                            <span>{{ $t('form.memberList.editUser') }}</span>
                        </v-tooltip>
                        <v-btn v-else class="flex-right ml-1" icon x-small color="info" @click="stopEditing(member)">
                            <v-icon>mdi-undo</v-icon>
                        </v-btn>
                    </template>
                </v-list-item>
                <template v-if="isEditing">
                    <v-divider />
                    <v-list-item>
                        <v-autocomplete
                            v-model="selectedUser"
                            hide-details
                            hide-no-data
                            item-text="name"
                            auto-select-first
                            return-object
                            hide-selected
                            dense
                            filled
                            clearable
                            class="mt-2"
                            :search-input.sync="userSearch"
                            :items="users"
                            :loading="loading.search"
                            :label="$t('form.memberList.addUser')"
                        >
                            <template #append-outer>
                                <v-btn fab small class="input-append-btn" color="info" :disabled="!selectedUser" @click="addMember">
                                    <v-icon>mdi-plus</v-icon>
                                </v-btn>
                            </template>
                        </v-autocomplete>
                    </v-list-item>
                </template>
            </v-list>
        </v-card-text>
    </v-card>
</template>

<script lang="ts">
import { Component, Prop, Vue, Watch } from 'nuxt-property-decorator';
import { PropType } from 'vue';
import { JoinableMember } from 'hangar-internal';
import { PaginatedResult, Role, User } from 'hangar-api';
import { UserAvatar } from '~/components/users';

interface EditableMember {
    name: string;
    roleTitle?: string;
    roleId?: number;
    roleAssignable: boolean;
    roleAccepted?: boolean;
    editing: boolean;
    toDelete: boolean;
    new: boolean;
}

@Component({
    components: { UserAvatar },
})
export default class MemberList extends Vue {
    @Prop({ type: Array as PropType<JoinableMember[]>, default: () => [] })
    members!: JoinableMember[];

    @Prop({ type: Boolean, default: false })
    alwaysEditing!: boolean;

    @Prop({ type: Array as PropType<Role[]>, required: true })
    roles!: Role[];

    @Prop({ type: Function as PropType<(name: User) => boolean>, default: () => (_: User) => true })
    searchFilter!: (user: User) => boolean;

    @Prop({ type: Boolean, default: false })
    noSaveBtn!: boolean;

    editing: boolean = false;
    editingMembers: EditableMember[] = [];
    userSearch: string = '';
    users: User[] = [];
    selectedUser: User | null = null;
    loading = {
        save: false,
        search: false,
    };

    get isEditing() {
        return this.alwaysEditing || this.editing;
    }

    get isEdited() {
        return this.editedMembers.length;
    }

    setupEditing() {
        this.editingMembers = [];
        this.editingMembers = this.convertMembers(this.members);
    }

    convertMembers(jms: JoinableMember[]): EditableMember[] {
        return jms.map((jm) => ({
            name: jm.user.name,
            roleTitle: jm.role.role.title,
            roleId: jm.role.role.roleId,
            roleAccepted: jm.role.accepted,
            roleAssignable: jm.role.role.assignable,
            editing: false,
            toDelete: false,
            new: false,
        }));
    }

    get editedMembers(): EditableMember[] {
        return this.editingMembers.filter(
            (em) =>
                em.toDelete ||
                (em.new && em.roleId) ||
                (em.editing && !em.new && em.roleId !== this.members.find((jm) => jm.user.name === em.name)!.role.role.roleId)
        );
    }

    stopEditing(member: EditableMember) {
        const originalMember = this.members.find((jm) => jm.user.name === member.name)!;
        member.roleTitle = originalMember.role.role.title;
        member.roleId = originalMember.role.role.roleId;
        member.editing = false;
    }

    removeMember(member: EditableMember) {
        if (member.new) {
            this.$delete(this.editingMembers, this.editingMembers.indexOf(member));
        } else {
            member.toDelete = true;
        }
    }

    addMember() {
        if (!this.selectedUser) return;
        this.editingMembers.push({
            name: this.selectedUser.name,
            new: true,
            editing: true,
            roleAssignable: true,
            toDelete: false,
        });
        this.selectedUser = null;
    }

    save() {
        const editedMembers = this.editedMembers;
        const deletedMembers = editedMembers.filter((em) => em.toDelete);
        if (deletedMembers.length) {
            // TODO should we confirm the deletion? You are already queuing up the deletion so maybe that's enough
        }
        this.loading.save = true;
        this.$api
            .requestInternal(`projects/project/${this.$route.params.author}/${this.$route.params.slug}/members`, true, 'post', editedMembers)
            .then(() => {
                this.editing = false;
                this.$nuxt.refresh().then(() => {
                    this.setupEditing();
                });
            })
            .catch(this.$util.handleRequestError)
            .finally(() => {
                this.loading.save = false;
            });
    }

    @Watch('userSearch')
    onSearch(val: string) {
        this.users = [];
        if (this.loading.search) {
            return;
        }

        this.loading.search = true;
        this.$api
            .request<PaginatedResult<User>>('users', false, 'get', {
                query: val,
                limit: 25,
                offset: 0,
            })
            .then((users) => {
                this.users = users.result.filter(this.searchFilter).filter((u) => this.editingMembers.findIndex((em) => em.name === u.name) === -1);
            })
            .catch(this.$util.handleRequestError)
            .finally(() => {
                this.loading.search = false;
            });
    }

    @Watch('editing')
    onEditToggle(val: boolean) {
        if (val) {
            this.setupEditing();
        }
    }

    created() {
        this.setupEditing();
    }
}
</script>

<style lang="scss" scoped>
@import '~vuetify/src/styles/styles';

.to-delete {
    background-color: rgba(map-get($red, 'lighten-2'), 0.3);
}

.editing {
    background-color: rgba(map-get($blue, 'lighten-2'), 0.3);
}

.new {
    background-color: rgba(map-get($light-green, 'accent-3'), 0.3);
}
</style>
