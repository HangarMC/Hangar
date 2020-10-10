<template>
    <MemberList
        :joinable="joinable"
        editable
        :can-manage-members="canManageMembers"
        :save-call="ROUTES.parse('ORG_UPDATE_MEMBERS', joinable.org.name)"
        :remove-call="ROUTES.parse('ORG_REMOVE_MEMBER', joinable.org.name)"
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
            joinable: window.JOINABLE,
            canManageMembers: window.CAN_MANAGE_MEMBERS,
        };
    },
};
</script>
