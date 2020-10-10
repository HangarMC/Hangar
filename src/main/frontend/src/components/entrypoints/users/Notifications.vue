<template>
    <div class="row">
        <div class="col-12 col-md-8 header-flags">
            <div class="row">
                <div class="col-12 header-flags">
                    <div class="float-left">
                        <h2 v-text="$t('notification.plural')"></h2>
                    </div>
                    <div class="float-right form-inline notification-controls">
                        <select
                            class="form-control mr-3"
                            v-model="currentFilter.notification"
                            aria-label="Notification Filter"
                            @change="changeNotificationFilter"
                        >
                            <option
                                v-for="(filter, index) in filter.notifications"
                                :key="`notif-filter-${index}`"
                                :value="filter"
                                v-text="$t(filter.title)"
                            ></option>
                        </select>
                        <button
                            v-if="notifications.length > 0 && currentFilter.notification.name === 'unread'"
                            class="btn btn-primary"
                            v-text="$t('notification.markAllRead')"
                            @click="readAll"
                        ></button>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-12">
                    <div v-show="notifications.length === 0" class="list-group-item no-notifications">
                        <h3 class="minor"><i class="far fa-thumbs-up"></i> {{ $t(currentFilter.notification.emptyMessage) }}</h3>
                    </div>
                    <ul class="list-group">
                        <transition v-for="{ key: notification, value: origin } in notifications" :key="notification.id" name="fade">
                            <li v-show="!notification.toHide" class="list-group-item notification" @click="markRead(notification)">
                                <UserAvatar v-if="origin" :user-name="origin.name" :avatar-url="avatarUrl(origin.name)" clazz="user-avatar-s"></UserAvatar>
                                {{ $t(notification.messageArgs[0], [...notification.messageArgs.slice(1)]) }}
                                <span v-if="!notification.read" class="btn-mark-read">
                                    <i class="minor float-right fas fa-check sm"></i>
                                </span>
                            </li>
                        </transition>
                    </ul>
                </div>
            </div>
        </div>
        <div class="col-12 col-md-4">
            <div class="row">
                <div class="col-12">
                    <h2 class="float-left" v-text="$t('notification.invites')"></h2>
                    <div class="float-right form-inline">
                        <select
                            class="form-control notification-controls select-invites"
                            v-model="currentFilter.invite"
                            aria-label="Invite Filter"
                            @change="changeInviteFilter"
                        >
                            <option
                                v-for="(filter, index) in filter.invites"
                                :key="`invite-filter-${index}`"
                                :value="filter"
                                v-text="$t(filter.title)"
                            ></option>
                        </select>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-12">
                    <div v-for="(chunk, index) in invites" :key="`invite-chunk-${index}`" class="row">
                        <div v-for="({ key: invite, value: subject }, index) in chunk" :key="`invite-${index}`" class="invite col-12 col-md-6">
                            <div class="invite-content">
                                <span class="minor">
                                    <span class="float-right"
                                        ><i class="fas fa-tag"></i> {{ subject.type.slice(0, 1).toUpperCase() + subject.type.slice(1) }}</span
                                    >
                                </span>
                                <br />
                                <transition name="slide-fade">
                                    <div v-show="inviteVisibility.choice.indexOf(invite.userRole.id) > -1" class="invite-message invite-choice">
                                        <p>
                                            {{ $t('notification.invite._', [subject.type]) }}
                                            <a :href="subject.url" v-text="subject.name"></a>
                                        </p>
                                        <button
                                            class="btn btn-invite btn-accept btn-sm btn-success mr-2"
                                            v-text="$t('notification.invite.accept')"
                                            @click="replyToInvite('accept', invite, subject)"
                                        ></button>
                                        <button
                                            class="btn btn-invite btn-decline btn-sm btn-danger"
                                            v-text="$t('notification.invite.decline')"
                                            @click="replyToInvite('decline', invite, subject)"
                                        ></button>
                                    </div>
                                </transition>

                                <div v-show="inviteVisibility.accepted.indexOf(invite.userRole.id) > -1" class="invite-message invite-accepted">
                                    <i class="minor fas fa-3x fa-birthday-cake"></i> <br />
                                    <span v-html="$t('notification.invite.joined', [subject.name])"></span> <br />
                                    <a :href="subject.url" class="btn btn-sm btn-primary mr-2" v-text="$t('notification.invite.visit')"></a>
                                    <button
                                        class="btn btn-undo btn-sm btn-info"
                                        v-text="$t('notification.invite.undo')"
                                        @click="undoAccept(invite, subject)"
                                    ></button>
                                </div>

                                <div v-show="inviteVisibility.declined.indexOf(invite.userRole.id) > -1" class="invite-message invite-declined">
                                    <i class="minor fas fa-3x fa-times"></i>
                                    <p v-html="$t('notification.invite.declined', [subject.name])"></p>
                                </div>
                                <i
                                    v-show="inviteVisibility.loading.indexOf(invite.userRole.id) > -1"
                                    class="minor invite-loading fas fa-5x fa-spinner fa-spin"
                                ></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import $ from 'jquery';
import axios from 'axios';
import { chunk, remove } from 'lodash-es';
import UserAvatar from '@/components/UserAvatar';

export default {
    name: 'Notifications',
    components: {
        UserAvatar,
    },
    data() {
        return {
            filter: {
                notifications: window.FILTERS.NOTIFICATIONS,
                invites: window.FILTERS.INVITES,
            },
            currentFilter: {
                notification: window.NOTIFICATION_FILTER,
                invite: window.INVITE_FILTER,
            },
            notifications: window.NOTIFICATIONS,
            inviteVisibility: {
                choice: window.INVITES.map((inv) => inv.key.userRole.id),
                accepted: [],
                declined: [],
                loading: [],
            },
            invites: chunk(window.INVITES, 2),
        };
    },
    mounted() {
        const invites = document.querySelector('.invite-content');
        invites.style.height = invites.offsetWidth;
    },
    methods: {
        avatarUrl(username) {
            return window.AVATAR_URL.replace('%s', username);
        },
        readAll() {
            for (const notification of this.notifications) {
                this.markRead(notification.key);
            }
        },
        markRead(notification) {
            if (notification.read) {
                return;
            }
            axios.post(window.ROUTES.parse('USERS_MARK_NOTIFICATION_READ', notification.id), {}, window.ajaxSettings).then(() => {
                notification.toHide = true;
                this.fadeOut(300, (self) => remove(self.notifications, (notif) => notif.key.id === notification.id), this);
                if (notification.action) {
                    window.location.href = notification.action;
                }
            });
        },
        changeNotificationFilter() {
            window.location = `/notifications?notificationFilter=${this.currentFilter.notification.value}`;
        },
        changeInviteFilter() {
            window.location = `/notifications?inviteFilter=${this.currentFilter.invite.value}`;
        },
        fadeOut(timeout, postFade, ...postFadeArgs) {
            setTimeout(postFade, timeout, ...postFadeArgs);
        },
        replyToInvite(reply, invite, subject) {
            this.inviteVisibility.loading.push(invite.userRole.id);
            const url = `${subject.type === 'project' ? '' : 'organizations'}/invite/${invite.userRole.id}/${reply}`;
            axios
                .post(url, {}, window.ajaxSettings)
                .then(() => {
                    remove(this.inviteVisibility.loading, (o) => o === invite.userRole.id);
                    remove(this.inviteVisibility.choice, (o) => o === invite.userRole.id);
                    if (reply === 'accept') {
                        this.fadeOut(300, (self) => self.inviteVisibility.accepted.push(invite.userRole.id), this);
                    } else if (reply === 'decline') {
                        this.fadeOut(300, (self) => self.inviteVisibility.declined.push(invite.userRole.id), this);
                    }
                })
                .catch((error) => {
                    remove(this.inviteVisibility.loading, (o) => o === invite.userRole.id);
                    console.error(error);
                    // TODO better error handling
                });
        },
        undoAccept(invite, subject) {
            this.inviteVisibility.loading.push(invite.userRole.id);
            const url = `${subject.type === 'project' ? '' : 'organizations'}/invite/${invite.userRole.id}/unaccept`;
            axios
                .post(url, {}, window.ajaxSettings)
                .then(() => {
                    remove(this.inviteVisibility.loading, (o) => o === invite.userRole.id);
                    remove(this.inviteVisibility.accepted, (o) => o === invite.userRole.id);
                    this.fadeOut(300, (self) => self.inviteVisibility.choice.push(invite.userRole.id), this);
                })
                .catch((error) => {
                    remove(this.inviteVisibility.loading, (o) => o === invite.userRole.id);
                    console.error(error);
                });
        },
    },
};
</script>
<style lang="scss" scoped>
.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.3s ease;
}

.fade-enter-to .fade-leave-from {
    opacity: 1;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
}

.slide-fade-enter-active,
.slide-fade-leave-active {
    transition: all 0.3s ease-out;
}

.slide-fade-enter-from,
.slide-fade-leave-to {
    transform: translateX(200px);
    opacity: 0;
}
</style>
