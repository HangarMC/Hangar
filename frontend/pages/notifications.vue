<template>
    <v-row>
        <v-col cols="12" md="7">
            <h1>{{ $t('notifications.title') }}</h1>
            <v-select :items="filter"></v-select>
            <v-btn>{{ $t('notifications.readAll') }}</v-btn>
            <v-list>
                <v-list-item v-for="notification in filteredNotifications" :key="notification.id">
                    {{ notification.text }}
                    <v-list-item-action>
                        <v-icon @click="markNotificationRead(notification)">mdi-check</v-icon>
                    </v-list-item-action>
                </v-list-item>
            </v-list>
        </v-col>
        <v-col cols="12" md="5">
            <h1>{{ $t('notifications.invites') }}</h1>
            <v-select :items="inviteFilter"></v-select>
            <v-list>
                <v-list-item v-for="invite in invites" :key="invite.id">
                    {{ invite.text }}
                    <v-list-item-action>
                        <v-icon @click="markNotificationRead(invite)">mdi-check</v-icon>
                    </v-list-item-action>
                </v-list-item>
            </v-list>
        </v-col>
    </v-row>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';

// TODO implement NotificationsPage

@Component
export default class NotificationsPage extends Vue {
    notifications = [{ text: 'test', id: 1 }]; // TODO get from server
    invites = [{ text: 'test', id: 1 }]; // TODO get from server

    get filteredNotifications() {
        return this.notifications; // TODO filter
    }

    get filteredInvites() {
        return this.invites; // TODO filter
    }

    get filter() {
        return [
            { text: this.$t('notifications.filterUnread'), value: 'unread' },
            { text: this.$t('notifications.filterRead'), value: 'read' },
            { text: this.$t('notifications.filterAll'), value: 'all' },
        ];
    }

    get inviteFilter() {
        return [
            { text: this.$t('notifications.filterInviteOrg'), value: 'org' },
            { text: this.$t('notifications.filterInviteProject'), value: 'project' },
            { text: this.$t('notifications.filterInviteAll'), value: 'all' },
        ];
    }

    markNotificationRead(notification) {
        // TODO mark as read and remove out of array
    }

    markInviteRead(invite) {
        // TODO mark as read and remove out of array
    }
}
</script>

<style lang="scss" scoped></style>
