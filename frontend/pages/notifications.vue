<template>
    <v-row>
        <v-col cols="12" md="7">
            <h1>{{ $t('notifications.title') }}</h1>
            <v-select v-model="filters.notification" :items="filter"></v-select>
            <v-btn v-if="filteredNotifications.length && filters.notification === 'unread'" color="info" text @click.stop="markAllAsRead">{{
                $t('notifications.readAll')
            }}</v-btn>
            <v-list v-if="filteredNotifications.length" class="mt-2">
                <v-list-item v-for="notification in filteredNotifications" :key="notification.id">
                    <v-list-item-title>
                        {{ $t(notification.message[0], notification.message.slice(1)) }}
                    </v-list-item-title>
                    <v-list-item-action v-if="!notification.read">
                        <v-btn icon class="success" @click.stop="markNotificationRead(notification)">
                            <v-icon>mdi-check</v-icon>
                        </v-btn>
                    </v-list-item-action>
                </v-list-item>
            </v-list>
            <div v-else>
                {{ $t(`notifications.empty.${filters.notification}`) }}
            </div>
        </v-col>
        <v-col v-if="!$fetchState.pending" cols="12" md="5">
            <h1>{{ $t('notifications.invites') }}</h1>
            <v-select v-model="filters.invite" :items="inviteFilter"></v-select>
            <v-list v-if="filteredInvites.length">
                <v-list-item v-for="(invite, index) in filteredInvites" :key="index">
                    <v-list-item-title>
                        {{ $t('notifications.invited', [invite.name]) }}
                    </v-list-item-title>
                    <v-list-item-action>
                        <v-btn icon class="success" @click="updateInvite(invite, 'accept')">
                            <v-icon>mdi-check</v-icon>
                        </v-btn>
                    </v-list-item-action>
                    <v-list-item-action>
                        <v-btn icon color="error" @click="updateInvite(invite, 'decline')">
                            <v-icon>mdi-close</v-icon>
                        </v-btn>
                    </v-list-item-action>
                </v-list-item>
            </v-list>
            <div v-else>
                {{ $t('notifications.empty.invites') }}
            </div>
        </v-col>
    </v-row>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { HangarNotification, Invite, Invites } from 'hangar-internal';
import { LoggedIn } from '~/utils/perms';
import { HangarComponent } from '~/components/mixins';

@Component({
    head: {
        title: 'Notifications',
    },
})
@LoggedIn
export default class NotificationsPage extends HangarComponent {
    notifications: HangarNotification[] = [];
    invites = {} as Invites;
    filters = {
        notification: 'unread' as 'unread' | 'read' | 'all',
        invite: 'all' as 'organizations' | 'projects' | 'all',
    };

    get filteredNotifications(): HangarNotification[] {
        switch (this.filters.notification) {
            case 'unread':
                return this.notifications.filter((n) => !n.read);
            case 'read':
                return this.notifications.filter((n) => n.read);
            case 'all':
                return this.notifications;
        }
    }

    get filteredInvites(): Invite[] {
        switch (this.filters.invite) {
            case 'projects':
                return this.invites.projects;
            case 'organizations':
                return this.invites.organizations;
            case 'all':
                return [...this.invites.projects, ...this.invites.organizations];
        }
    }

    get filter() {
        return [
            { text: this.$t('notifications.unread'), value: 'unread' },
            { text: this.$t('notifications.read'), value: 'read' },
            { text: this.$t('notifications.all'), value: 'all' },
        ];
    }

    get inviteFilter() {
        return [
            { text: this.$t('notifications.invite.organizations'), value: 'organizations' },
            { text: this.$t('notifications.invite.projects'), value: 'projects' },
            { text: this.$t('notifications.invite.all'), value: 'all' },
        ];
    }

    markAllAsRead() {
        this.notifications
            .filter((n) => !n.read)
            .forEach((n) => {
                this.markNotificationRead(n, false);
            });
    }

    markNotificationRead(notification: HangarNotification, router: boolean = true) {
        this.$api
            .requestInternal(`notifications/${notification.id}`, true, 'post')
            .then(() => {
                this.$delete(
                    this.notifications,
                    this.notifications.findIndex((n) => n.id === notification.id)
                );
                if (notification.action && router) {
                    this.$router.push(notification.action);
                }
                this.$auth.refreshUser();
            })
            .catch(this.$util.handleRequestError);
    }

    updateInvite(invite: Invite, status: 'accept' | 'decline' | 'unaccept') {
        this.$api
            .requestInternal(`invites/${invite.type}/${invite.roleTableId}/${status}`)
            .then(() => {
                console.log('successful');
                // TODO actions on save
            })
            .catch(this.$util.handleRequestError);
    }

    async fetch() {
        this.notifications = await this.$api.requestInternal<HangarNotification[]>('notifications');
        this.invites = await this.$api.requestInternal<Invites>('invites');
        // TODO remove this test article
        this.invites.projects.push({
            name: 'TestProject',
            roleTableId: 4,
            type: 'project',
            url: '/Machine_Maker/TestProject',
        } as Invite);
    }
}
</script>

<style lang="scss" scoped></style>
