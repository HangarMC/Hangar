<template>
    <template v-if="filteredMembers.length">
        <div class="card" style="z-index: 2">
            <div class="card-header">
                <h3 class="float-left card-title" v-text="$t('project.settings.members')"></h3>
                <div v-if="canManageMembers" class="float-right">
                    <a v-if="!editable && settingsCall" :href="settingsCall" class="btn bg-warning btn-sm">
                        <i class="fas fa-pencil-alt"></i>
                    </a>
                    <button
                        v-if="saveCall && (Object.keys(form.updates).length || Object.keys(form.additions).length)"
                        class="btn-members-save btn btn-card btn-sm"
                        data-toggle="tooltip"
                        data-placement="top"
                        :data-title="$t('org.users.save')"
                        @click.prevent="save"
                    >
                        <i class="fas fa-save"></i>
                    </button>
                </div>
            </div>
            <ul class="list-members list-group">
                <transition name="slide-down">
                    <li v-show="error" class="list-group-item" style="z-index: -1">
                        <div class="alert alert-danger" role="alert" v-text="error"></div>
                    </li>
                </transition>
                <li v-for="({ key: role, value: user }, index) in filteredMembers" :key="index" class="list-group-item">
                    <UserAvatar :user-name="user.name" :avatar-url="avatarUrl(user.name)" clazz="user-avatar-xs"></UserAvatar>
                    <a :href="ROUTES.parse('USERS_SHOW_PROJECTS', user.name)" class="username" v-text="user.name"></a>
                    <template v-if="editable && canManageMembers && (!role.role || (role.role.permissions & isOrgOwnerPermission) !== isOrgOwnerPermission)">
                        <a v-if="!role.isEditing" href="#" @click.prevent title="Remove Member">
                            <i
                                class="ml-1 fas fa-trash fa-xs text-danger"
                                data-toggle="modal"
                                data-target="#modal-user-delete"
                                @click="userToDelete = user.name"
                            ></i>
                        </a>
                        <a v-if="!role.isEditing" href="#" @click.prevent="edit(role, user)" title="Change Role"><i class="ml-1 fas fa-edit fa-xs"></i></a>
                        <a v-else href="#" @click.prevent="cancel(role, user)" :title="`Cancel ${role.isNew ? 'New' : 'Edit'}`">
                            <i class="ml-1 fas fa-times fa-sm"></i>
                        </a>
                    </template>
                    <span v-if="!role.isEditing" class="minor float-right">
                        <template v-if="!role.isAccepted">
                            <span class="minor">(Invited as {{ role.role.title }})</span>
                        </template>
                        <template v-else>
                            {{ role.role.title }}
                        </template>
                    </span>
                    <select
                        v-if="
                            editable &&
                            canManageMembers &&
                            (!role.role || (role.role.permissions & isOrgOwnerPermission) !== isOrgOwnerPermission) &&
                            role.isEditing &&
                            !role.isNew
                        "
                        aria-label="Role Selection"
                        v-model="form.updates[user.name].role"
                    >
                        <option v-for="role in roles" :key="role.value" :value="role.value">{{ role.title }}</option>
                    </select>
                    <select
                        v-if="
                            editable &&
                            canManageMembers &&
                            (!role.role || (role.role.permissions & isOrgOwnerPermission) !== isOrgOwnerPermission) &&
                            role.isEditing &&
                            role.isNew
                        "
                        aria-label="Role Selection"
                        v-model="form.additions[user.name].role"
                    >
                        <option v-for="role in roles" :key="role.value" :value="role.value">{{ role.title }}</option>
                    </select>
                </li>
                <li v-if="editable && canManageMembers" class="list-group-item">
                    <UserSearch @add-user="addUser"></UserSearch>
                </li>
            </ul>
        </div>
        <teleport to="body">
            <div class="modal fade" id="modal-user-delete" tabindex="-1" role="dialog" aria-labelledby="label-user-delete">
                <div class="modal-dialog" role="document">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h4 class="modal-title" id="label-user-delete" v-text="$t('project.removeMember._')"></h4>
                            <button type="button" class="close" data-dismiss="modal" :aria-label="$t('general.close')">
                                <span aria-hidden="true">&times;</span>
                            </button>
                        </div>
                        <div class="modal-body" v-text="$t('project.removeMember.confirm')"></div>
                        <div class="modal-footer">
                            <HangarForm :action="removeCall" method="POST" clazz="form-inline">
                                <input v-model="userToDelete" type="hidden" name="username" />
                                <div class="btn-group" role="group">
                                    <button type="button" class="btn btn-default" data-dismiss="modal" v-text="$t('general.close')"></button>
                                    <button type="submit" class="btn btn-danger" v-text="$t('general.remove')"></button>
                                </div>
                            </HangarForm>
                        </div>
                    </div>
                </div>
            </div>
        </teleport>
    </template>
</template>

<script>
import axios from 'axios';
import { remove } from 'lodash-es';

import UserAvatar from '@/components/UserAvatar';
import UserSearch from '@/components/UserSearch';
import HangarForm from '@/components/HangarForm';

export default {
    name: 'MemberList',
    components: {
        HangarForm,
        UserSearch,
        UserAvatar,
    },
    props: {
        filteredMembersProp: Array,
        canManageMembers: Boolean,
        editable: Boolean,
        removeCall: String,
        settingsCall: String,
        saveCall: String,
        roles: Array,
    },
    data() {
        return {
            ROUTES: window.ROUTES,
            filteredMembers: [],
            isOrgOwnerPermission: window.ORG_OWNER_PERM,
            form: {
                updates: {},
                additions: {},
            },
            error: null,
            userToDelete: null,
        };
    },
    created() {
        this.filteredMembers = [...this.filteredMembersProp];
    },
    methods: {
        avatarUrl(username) {
            return window.AVATAR_URL.replace('%s', username);
        },
        edit(role, user) {
            role.isEditing = true;
            if (role.isNew) {
                this.form.additions[user.name] = { role: null, id: user.id };
            } else {
                this.form.updates[user.name] = { role: role.role.value, id: user.id };
            }
        },
        save() {
            const data = {
                updates: [],
                additions: [],
            };
            for (const name in this.form.updates) {
                data.updates.push({
                    role: this.form.updates[name].role,
                    id: this.form.updates[name].id,
                });
            }
            for (const name in this.form.additions) {
                data.additions.push({
                    role: this.form.additions[name].role,
                    id: this.form.additions[name].id,
                });
            }
            axios
                .post(this.saveCall, data, window.ajaxSettings)
                .then(() => {
                    location.reload();
                })
                .catch(() => {
                    console.error('Error updating roles');
                    this.error = 'Error updating roles';
                    setTimeout(
                        (self) => {
                            self.error = null;
                        },
                        5000,
                        this
                    );
                    this.filteredMembers = [...this.filteredMembersProp];
                    this.resetForm();
                });
        },
        resetForm() {
            this.form = {
                updates: {},
                additions: {},
            };
        },
        cancel(role, user) {
            if (role.isNew) {
                remove(this.filteredMembers, (mem) => mem.value.id === user.id);
                delete this.form.additions[user.name];
            } else {
                role.isEditing = false;
                delete this.form.updates[user.name];
            }
        },
        addUser(user) {
            const role = { isNew: true };
            const newUser = { ...user };
            this.filteredMembers.push({
                key: role,
                value: newUser,
            });
            this.edit(role, newUser);
        },
    },
};
</script>
<style lang="scss" scoped>
.slide-down-enter-active,
.slide-down-leave-active {
    transition: all 0.5s ease;
}

.slide-down-enter-from,
.slide-down-leave-to {
    margin-top: -60px;
    transform: scaleY(0);
    transform-origin: top;
}

.alert {
    margin-bottom: 0;
    width: 100%;
}
</style>
