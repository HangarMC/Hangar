<template>
    <div class="row">
        <Card v-if="!isOrg && roles.orgs" title="Organizations">
            <table class="table">
                <tr>
                    <th>Organization</th>
                    <th>Owner</th>
                    <th>Role</th>
                    <th>Accepted</th>
                </tr>
                <tr v-for="{ key: org, value: role } in roles.orgs" :key="org.org.id">
                    <td>
                        <a :href="ROUTES.parse('USER_ADMIN', org.org.name)" v-text="org.org.name"></a>
                    </td>
                    <td>
                        <a :href="ROUTES.parse('USER_ADMIN', org.ownerName)" v-text="org.ownerName"></a>
                    </td>
                    <template v-if="!role.role.assignable">
                        <td v-text="role.role.title"></td>
                        <td colspan="2">
                            <input type="checkbox" :checked="role.accepted" disabled @change="roleAcceptedChange($event, 'orgRole', role.userRole.id)" />
                        </td>
                    </template>
                    <template v-else>
                        <td>
                            <label for="org-role-select" class="sr-only">Role Select</label>
                            <select class="select-role" id="org-role-select" @change="selectRoleChange($event, 'orgRole', role.userRole.id)">
                                <option
                                    v-for="(type, index) in roleTypes.org"
                                    :key="index"
                                    :value="type.roleId"
                                    v-text="type.title"
                                    :selected="type.roleId === role.role.roleId"
                                ></option>
                            </select>
                        </td>
                        <td>
                            <input
                                class="role-accepted"
                                type="checkbox"
                                :checked="role.accepted"
                                @change="roleAcceptedChange($event, 'orgRole', role.userRole.id)"
                            />
                        </td>
                        <td>
                            <a href="#" @click.prevent="deleteRoleClick($event, 'orgRole', role.userRole.id)"><i class="fas fa-trash text-danger"></i></a>
                        </td>
                    </template>
                </tr>
            </table>
        </Card>
        <Card v-else-if="isOrg && roles.members" title="Members">
            <table class="table">
                <tr>
                    <th>User</th>
                    <th>Role</th>
                    <th>Accepted</th>
                </tr>
                <tr v-for="{ key: user, value: role } in roles.members" :key="user.id">
                    <td>
                        <a :href="ROUTES.parse('USER_ADMIN', user.name)" v-text="user.name"></a>
                    </td>
                    <template v-if="!role.role.assignable">
                        <td v-text="role.role.title"></td>
                        <td colspan="2">
                            <input type="checkbox" :checked="role.accepted" disabled @change="roleAcceptedChange($event, 'memberRole', role.userRole.id)" />
                        </td>
                    </template>
                    <template v-else>
                        <td>
                            <label for="member-select-role" class="sr-only">Select Role</label>
                            <select class="select-role" id="member-select-role" @change="selectRoleChange($event, 'memberRole', role.userRole.id)">
                                <option
                                    v-for="(type, index) in roleTypes.org"
                                    :key="index"
                                    :value="type.roleId"
                                    v-text="type.title"
                                    :selected="type.roleId === role.role.roleId"
                                ></option>
                            </select>
                        </td>
                        <td>
                            <input
                                class="role-accepted"
                                type="checkbox"
                                :checked="role.accepted"
                                @change="roleAcceptedChange($event, 'memberRole', role.userRole.id)"
                            />
                        </td>
                        <td>
                            <a href="#" @click.prevent="deleteRoleClick($event, 'memberRole', role.userRole.id)"><i class="fas fa-trash text-danger"></i></a>
                        </td>
                    </template>
                </tr>
            </table>
        </Card>
    </div>
    <div v-if="!isOrg && roles.projects" class="row">
        <Card title="Projects">
            <table class="table">
                <tr>
                    <th>Project</th>
                    <th>Owner</th>
                    <th>Role</th>
                    <th>Accepted</th>
                </tr>
                <tr v-for="{ key: project, value: role } in roles.projects" :key="project.project.id">
                    <td>
                        <a :href="ROUTES.parse('USER_ADMIN', project.project.name)" v-text="project.project.name"></a>
                    </td>
                    <td>
                        <a :href="ROUTES.parse('USER_ADMIN', project.ownerName)" v-text="project.ownerName"></a>
                    </td>
                    <template v-if="!role.role.assignable">
                        <td v-text="role.role.title"></td>
                        <td colspan="2">
                            <input
                                class="role-accepted"
                                type="checkbox"
                                :checked="role.accepted"
                                disabled
                                @change="roleAcceptedChange($event, 'projectRole', role.userRole.id)"
                            />
                        </td>
                    </template>
                    <template v-else>
                        <td>
                            <label for="project-role-select" class="sr-only">Role Select</label>
                            <select class="select-role" id="project-role-select" @change="selectRoleChange($event, 'projectRole', role.userRole.id)">
                                <option
                                    v-for="(type, index) in roleTypes.project"
                                    :key="index"
                                    :value="type.roleId"
                                    v-text="type.title"
                                    :selected="type.roleId === role.role.roleId"
                                ></option>
                            </select>
                        </td>
                        <td>
                            <input
                                class="role-accepted"
                                type="checkbox"
                                :checked="role.accepted"
                                @change="roleAcceptedChange($event, 'projectRole', role.userRole.id)"
                            />
                        </td>
                        <td>
                            <a href="#" @click.prevent="deleteRoleClick($event, 'projectRole', role.userRole.id)"><i class="fas fa-trash text-danger"></i></a>
                        </td>
                    </template>
                </tr>
            </table>
        </Card>
    </div>
</template>

<script>
import axios from 'axios';

import Card from '@/components/Card';

export default {
    name: 'UserAdmin',
    components: { Card },
    data() {
        return {
            ROUTES: window.ROUTES,
            isOrg: window.IS_ORGANIZATION,
            roles: {
                orgs: window.ROLES.ORGANIZATIONS,
                members: window.ROLES.MEMBERS,
                projects: window.ROLES.PROJECTS,
            },
            roleTypes: {
                project: window.ROLE_TYPES.PROJECT,
                org: window.ROLE_TYPES.ORGANIZATION,
            },
        };
    },
    methods: {
        selectRoleChange(event, type, id) {
            this.rowChange('setRole', { role: parseInt(event.target.value) }, event.target, type, id)
                .catch(() => {
                    event.target.selectedIndex = event.target.defaultValue;
                })
                .finally(() => {
                    event.target.defaultValue = event.target.selectedIndex;
                    history.go(0);
                });
        },
        roleAcceptedChange(event, type, id) {
            this.rowChange('setAccepted', { accepted: event.target.checked }, event.target, type, id).catch(() => {
                event.target.checked = !event.target.checked;
            });
        },
        deleteRoleClick(event, type, id) {
            this.rowChange('deleteRole', {}, event.target, type, id).then(() => {
                event.target.closest('tr').remove();
            });
        },
        rowChange(action, data, element, type, id) {
            element.disabled = true;
            return this.update(type, action, { ...data, id: id }).finally(() => {
                element.disabled = false;
            });
        },
        update(thing, action, data) {
            return axios.post(
                window.location.pathname + '/update',
                {
                    thing: thing,
                    action: action,
                    data,
                },
                window.ajaxSettings
            );
        },
    },
    mounted() {
        Array.from(document.getElementsByClassName('select-role')).forEach((el) => {
            el.defaultValue = el.selectedIndex;
        });
    },
};
</script>
