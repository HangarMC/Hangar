<template>
    <v-row>
        <v-col cols="12" md="7">
            <h1>{{ $t('notifications.title') }}</h1>
            <v-select v-model="filters.notification" dense filled :items="filter">
                <template #append-outer>
                    <v-btn
                        v-if="filteredNotifications.length && filters.notification === 'unread'"
                        color="info"
                        text
                        class="input-append-btn"
                        @click.stop="markAllAsRead"
                    >
                        {{ $t('notifications.readAll') }}
                    </v-btn>
                </template>
            </v-select>
            <v-list v-if="filteredNotifications.length">
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
            <div v-else class="red--text text--lighten-2">
                {{ $t(`notifications.empty.${filters.notification}`) }}
            </div>
        </v-col>
        <v-divider vertical class="mt-2" />
        <v-col v-if="!$fetchState.pending" cols="12" md="5">
            <h1>{{ $t('notifications.invites') }}</h1>
            <v-select v-model="filters.invite" dense filled :items="inviteFilter"></v-select>
            <v-list v-if="filteredInvites.length">
                <v-list-item v-for="(invite, index) in filteredInvites" :key="index">
                    <v-list-item-title>
                        {{ $t(!invite.accepted ? 'notifications.invited' : 'notifications.inviteAccepted', [invite.type]) }}:
                        <NuxtLink :to="invite.url" exact>{{ invite.name }}</NuxtLink>
                    </v-list-item-title>
                    <template v-if="!invite.accepted">
                        <v-list-item-action>
                            <v-tooltip bottom>
                                <template #activator="{ on }">
                                    <v-btn icon class="success" v-on="on" @click="updateInvite(invite, 'accept')">
                                        <v-icon>mdi-check</v-icon>
                                    </v-btn>
                                </template>
                                <span>{{ $t('notifications.invite.btns.accept') }}</span>
                            </v-tooltip>
                        </v-list-item-action>
                        <v-list-item-action>
                            <v-tooltip bottom>
                                <template #activator="{ on }">
                                    <v-btn icon color="error" v-on="on" @click="updateInvite(invite, 'decline')">
                                        <v-icon>mdi-close</v-icon>
                                    </v-btn>
                                </template>
                                <span>{{ $t('notifications.invite.btns.decline') }}</span>
                            </v-tooltip>
                        </v-list-item-action>
                    </template>
                    <v-list-item-action v-else>
                        <v-tooltip bottom>
                            <template #activator="{ on }">
                                <v-btn icon class="warning" v-on="on" @click="updateInvite(invite, 'unaccept')">
                                    <v-icon>mdi-undo</v-icon>
                                </v-btn>
                            </template>
                            <span>{{ $t('notifications.invite.btns.unaccept') }}</span>
                        </v-tooltip>
                    </v-list-item-action>
                </v-list-item>
            </v-list>
            <div v-else class="red--text text--lighten-2">
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
                return this.invites.project;
            case 'organizations':
                return this.invites.organization;
            case 'all':
                return [...this.invites.project, ...this.invites.organization];
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
            .requestInternal(`invites/${invite.type}/${invite.roleTableId}/${status}`, true, 'post')
            .then(() => {
                if (status === 'accept') {
                    this.$set(invite, 'accepted', true);
                } else if (status === 'unaccept') {
                    this.$set(invite, 'accepted', false);
                } else {
                    this.$delete(this.invites[invite.type], this.invites[invite.type].indexOf(invite));
                }
                this.$auth.refreshUser();
                this.$util.success(this.$t(`notifications.invite.msgs.${status}`, [invite.name]));
            })
            .catch(this.$util.handleRequestError);
    }

    async fetch() {
        this.notifications = (await this.$api.requestInternal<HangarNotification[]>('notifications').catch(this.$util.handleRequestError)) || [];
        this.invites = (await this.$api.requestInternal<Invites>('invites').catch(this.$util.handleRequestError)) || { project: [], organization: [] };
    }
}
</script>

<style lang="scss" scoped></style>
