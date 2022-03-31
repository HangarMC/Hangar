<template>
    <div class="card-user-info card">
        <div class="card-header">
            <h3 class="card-title" v-text="$t('project.manager')"></h3>
        </div>
        <table class="table card-body">
            <tbody>
                <tr v-for="{ key: role, value: project } in projectRoles" :key="role.id">
                    <td>
                        <a :href="ROUTES.parse('PROJECTS_SHOW', project.ownerName, project.slug)" v-text="`${project.ownerName}/${project.slug}`"></a>
                        <span class="minor ml-2" v-text="role.role.title" style="font-size: 12px"></span>
                    </td>
                    <td>
                        <template v-if="role.role.value !== 'Project_Owner' && !role.justAccepted">
                            <button
                                class="btn btn-sm float-right"
                                :class="role.isAccepted || role.error ? 'btn-danger' : 'btn-info'"
                                v-text="role.error || (role.isAccepted ? 'Leave' : 'Join')"
                                :disabled="role.error || role.loading"
                                @click="changeInviteStatus(role, organizationName, role.isAccepted ? 'decline' : 'accept')"
                            ></button>
                        </template>
                        <transition name="fade">
                            <template v-if="role.role.value !== 'Project_Owner' && role.justAccepted">
                                <span class="text-success text-bold"> Joined! </span>
                            </template>
                        </transition>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>
</template>

<script>
import axios from 'axios';
import { remove } from 'lodash-es';

export default {
    name: 'OrgProjectManager',
    data() {
        return {
            ROUTES: window.ROUTES,
            projectRoles: window.PROJECT_ROLES,
            organizationName: window.ORG_NAME,
        };
    },
    methods: {
        replyToInvite(id, behalf, reply, success, error, final) {
            axios.post(`/invite/${id}/${reply}/${behalf}`, {}, window.ajaxSettings).then(success).catch(error).finally(final);
        },
        changeInviteStatus(role, behalf, reply) {
            role.loading = true;
            this.replyToInvite(
                role.id,
                behalf,
                reply,
                () => {
                    if (reply === 'decline') {
                        remove(this.projectRoles, (r) => r.key.id === role.id);
                    } else {
                        role.justAccepted = true;
                        setTimeout(() => {
                            role.justAccepted = false;
                            role.isAccepted = true;
                        }, 5000);
                    }
                },
                () => {
                    role.error = 'Failed to update';
                },
                () => {
                    role.loading = false;
                }
            );
        },
    },
};
</script>
<style lang="scss" scoped>
.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.5s ease;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
}
</style>
