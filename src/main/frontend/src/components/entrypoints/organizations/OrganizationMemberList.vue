<template>
    <MemberList
        :filtered-members-prop="filteredMembers"
        editable
        :can-manage-members="canManageMembers"
        :save-call="ROUTES.parse('ORG_UPDATE_MEMBERS', joinableName)"
        :remove-call="ROUTES.parse('ORG_REMOVE_MEMBER', joinableName)"
        :roles="roles"
    ></MemberList>
</template>

<script>
import MemberList from '@/components/MemberList';

export default {
    name: 'OrgMemberList',
    components: {
        MemberList,
    },
    data() {
        return {
            ROUTES: window.ROUTES,
            joinableName: window.JOINABLE_NAME,
            canManageMembers: window.CAN_MANAGE_MEMBERS,
            filteredMembers: window.FILTERED_MEMBERS,
            roles: window.POSSIBLE_ROLES || [],
        };
    },
};
</script>
