<template>
    <div class="mt-4">
        <div class="float-left">
            {{ $t('reviews.headline', [projectVersion.author, $util.prettyDate(projectVersion.createdAt)]) }}
        </div>
        <div class="float-right">
            <template v-if="!isReviewStateChecked">
                <v-btn x-small color="info" :to="{ name: 'author-slug', params: $route.params }" nuxt exact>
                    <v-icon left> mdi-home </v-icon>
                    {{ $t('reviews.projectPage') }}
                </v-btn>
                <v-btn
                    x-small
                    color="info"
                    :to="'/' + $route.params.author + '/' + $route.params.slug + '/versions/' + $route.params.version + '/jar'"
                    nuxt
                    class="mr-4"
                >
                    <v-icon left> mdi-download </v-icon>
                    {{ $t('reviews.downloadFile') }}
                </v-btn>
            </template>
        </div>
        <div style="clear: both" class="mb-4" />
        <v-divider />

        <h2 class="mt-3">
            {{ $t('reviews.title') }}
        </h2>
        <v-row class="my-1" dense>
            <v-col class="flex-grow-0">
                <v-btn class="primary" @click="$fetch">
                    <v-icon left> mdi-refresh </v-icon>
                    {{ $t('general.refresh') }}
                </v-btn>
            </v-col>
            <v-col v-if="!currentUserReview" class="flex-grow-0">
                <v-btn color="success" :loading="loadingValues.start" @click="startReview">
                    <v-icon left> mdi-play </v-icon>
                    {{ $t('reviews.startReview') }}
                </v-btn>
            </v-col>
            <v-col>
                <v-checkbox v-model="hideClosed" dense hide-details :label="$t('reviews.hideClosed')" class="mt-0" />
            </v-col>
        </v-row>

        <v-list v-for="(review, index) in filteredReviews" :key="`review-${index}`">
            <v-list-group :value="!review.endedAt">
                <template #activator>
                    <v-list-item-title class="review-message review-message-title">
                        <v-row no-gutters dense>
                            <v-col>
                                {{ $t('reviews.presets.reviewTitle', { name: review.userName }) }}
                                <span class="review-message-state" :class="`review-message-state-${getReviewStateString(review)}`">{{
                                    $t(`reviews.state.${getReviewStateString(review)}`)
                                }}</span>
                                <span class="review-message-datetime ml-1">
                                    {{ $t('reviews.state.lastUpdate', [getLastUpdateDate(review)]) }}
                                </span>
                            </v-col>
                            <v-col v-if="isCurrentReviewOpen && currentUserReview === review" class="text-right">
                                <TextareaModal :title="$t('reviews.stopReview')" :label="$t('general.message')" :submit="stopReview">
                                    <template #activator="props">
                                        <v-btn x-small color="error" v-bind="props.attrs" v-on="props.on">
                                            <v-icon left> mdi-stop </v-icon>
                                            {{ $t('reviews.stopReview') }}
                                        </v-btn>
                                    </template>
                                </TextareaModal>

                                <v-btn x-small color="success lighten-1" :loading="loadingValues.approvePartial" @click.stop="approvePartial">
                                    <v-icon left> mdi-check-decagram-outline </v-icon>
                                    {{ $t('reviews.approvePartial') }}
                                </v-btn>
                                <v-btn x-small color="success" :loading="loadingValues.approve" @click.stop="approve">
                                    <v-icon left> mdi-check-decagram </v-icon>
                                    {{ $t('reviews.approve') }}
                                </v-btn>
                            </v-col>
                            <v-col v-else-if="currentUserReview === review" class="text-right">
                                <v-btn
                                    v-if="currentReviewLastAction === 'STOP'"
                                    color="warning"
                                    :loading="loadingValues.reopen"
                                    x-small
                                    @click.stop="reopenReview"
                                >
                                    <v-icon left> mdi-refresh </v-icon>
                                    {{ $t('reviews.reopenReview') }}
                                </v-btn>
                                <v-btn
                                    v-else-if="currentReviewLastAction === 'APPROVE' || currentReviewLastAction === 'PARTIALLY_APPROVE'"
                                    x-small
                                    color="error"
                                    :loading="loadingValues.undoApproval"
                                    @click.stop="undoApproval"
                                >
                                    <v-icon left> mdi-undo </v-icon>
                                    {{ $t('reviews.undoApproval') }}
                                </v-btn>
                            </v-col>
                        </v-row>
                    </v-list-item-title>
                </template>
                <v-list-item v-for="(msg, mIndex) in review.messages" :key="`review-${index}-msg-${mIndex}`">
                    <div class="review-message" :class="`review-message-${msg.action.toLowerCase()}`">
                        <span>{{ $t(msg.message, msg.args) }}</span>
                        <span class="review-message-datetime"> {{ $util.prettyDateTime(msg.createdAt) }}</span>
                    </div>
                </v-list-item>
                <v-list-item v-if="isCurrentReviewOpen && currentUserReview === review">
                    <v-form ref="messageForm" v-model="validForm" style="width: 100%" class="mx-5">
                        <v-textarea
                            v-model.trim="message"
                            class="mt-2"
                            dense
                            full-width
                            auto-grow
                            autofocus
                            hide-details
                            filled
                            :label="$t('reviews.reviewMessage')"
                            :rows="3"
                            :rules="[$util.$vc.required($t('general.message'))]"
                            @keydown.enter.prevent=""
                        />
                        <v-btn block color="primary" :loading="loadingValues.send" class="mt-2" :disabled="!validForm" @click="sendMessage">
                            <v-icon left> mdi-send </v-icon>
                            {{ $t('general.send') }}
                        </v-btn>
                    </v-form>
                </v-list-item>
            </v-list-group>
        </v-list>
        <v-alert v-if="!reviews.length" type="info" class="mt-2">
            {{ $t('reviews.notUnderReview') }}
        </v-alert>
    </div>
</template>

<script lang="ts">
import { Component, mixins } from 'nuxt-property-decorator';
import { HangarReview, HangarReviewMessage } from 'hangar-internal';
import { Authed, HangarForm, HangarProjectVersionMixin } from '~/components/mixins';
import { GlobalPermission } from '~/utils/perms';
import { NamedPermission, ReviewAction } from '~/types/enums';
import TextareaModal from '~/components/modals/TextareaModal.vue';

@Component({
    components: { TextareaModal },
})
@GlobalPermission(NamedPermission.REVIEWER)
export default class ProjectVersionsVersionReviewPage extends mixins(Authed, HangarProjectVersionMixin, HangarForm) {
    message: string = '';
    hideClosed: boolean = false;
    reviews: HangarReview[] = [];
    loadingValues = {
        start: false,
        send: false,
        reopen: false,
        approve: false,
        approvePartial: false,
        undoApproval: false,
    };

    head() {
        return this.$seo.head(
            'Reviews | ' + this.project.name,
            null,
            this.$route,
            this.$util.projectUrl(this.project.namespace.owner, this.project.namespace.slug)
        );
    }

    async fetch() {
        this.reviews = await this.$api
            .requestInternal<HangarReview[]>(`reviews/${this.projectVersion.id}/reviews`)
            .catch<any>(this.$util.handlePageRequestError);
    }

    get currentUserReview(): HangarReview | undefined {
        return this.reviews.find((r) => r.userId === this.currentUser.id);
    }

    get isCurrentReviewOpen(): boolean {
        return !this.currentUserReview?.endedAt;
    }

    get currentReviewLastAction(): ReviewAction {
        const lastMsg: HangarReviewMessage = this.currentUserReview!.messages[this.currentUserReview!.messages.length - 1];
        return lastMsg.action;
    }

    get filteredReviews() {
        if (this.hideClosed) {
            return this.reviews.filter((r) => !r.endedAt);
        }
        return this.reviews;
    }

    getReviewStateString(review: HangarReview): string {
        const lastMsg: HangarReviewMessage = review.messages[review.messages.length - 1];
        switch (lastMsg.action) {
            case ReviewAction.START:
            case ReviewAction.MESSAGE:
            case ReviewAction.REOPEN:
            case ReviewAction.UNDO_APPROVAL:
                return 'ongoing';
            case ReviewAction.STOP:
                return 'stopped';
            case ReviewAction.APPROVE:
                return 'approved';
            case ReviewAction.PARTIALLY_APPROVE:
                return 'partiallyApproved';
        }
    }

    getLastUpdateDate(review: HangarReview): string {
        const lastMsg: HangarReviewMessage = review.messages[review.messages.length - 1];
        return this.$util.prettyDateTime(lastMsg.createdAt);
    }

    $refs!: {
        messageForm: any[];
    };

    startReview() {
        const message = 'reviews.presets.start';
        const args = {
            name: this.currentUser.name,
        };
        this.loadingValues.start = true;
        this.sendReviewRequest(
            'start',
            { name: this.currentUser.name },
            ReviewAction.START,
            () => {
                this.reviews.push({
                    userName: this.currentUser.name,
                    userId: this.currentUser.id,
                    createdAt: new Date().toISOString(),
                    endedAt: null,
                    messages: [
                        {
                            message,
                            args,
                            action: ReviewAction.START,
                            createdAt: new Date().toISOString(),
                        },
                    ],
                });
            },
            () => {
                this.loadingValues.start = false;
            }
        );
    }

    sendMessage() {
        if (!this.isCurrentReviewOpen) return;
        this.loadingValues.send = true;
        this.sendReviewRequest(
            'message',
            { msg: this.message },
            ReviewAction.MESSAGE,
            () => {
                if (document.activeElement instanceof HTMLElement) {
                    document.activeElement.blur();
                }
                this.$refs.messageForm[0].reset();
            },
            () => {
                this.loadingValues.send = false;
            }
        );
    }

    stopReview(userMsg: string) {
        if (!this.isCurrentReviewOpen) return;
        const args = {
            name: this.currentUserReview!.userName,
            msg: userMsg,
        };
        return this.sendReviewRequest('stop', args, ReviewAction.STOP, () => {
            this.currentUserReview!.endedAt = new Date().toISOString();
        });
    }

    reopenReview() {
        if (this.isCurrentReviewOpen) return;
        this.loadingValues.reopen = true;
        this.sendReviewRequest(
            'reopen',
            { name: this.currentUserReview!.userName },
            ReviewAction.REOPEN,
            () => {
                this.currentUserReview!.endedAt = null;
            },
            () => {
                this.loadingValues.reopen = false;
            }
        );
    }

    approve() {
        if (!this.isCurrentReviewOpen) return;
        this.loadingValues.approve = true;
        this.sendReviewRequest(
            'approve',
            { name: this.currentUserReview!.userName },
            ReviewAction.APPROVE,
            () => {
                this.$auth.refreshUser();
                this.currentUserReview!.endedAt = new Date().toISOString();
            },
            () => {
                this.loadingValues.approve = false;
            }
        );
    }

    approvePartial() {
        if (!this.isCurrentReviewOpen) return;
        this.loadingValues.approvePartial = true;
        this.sendReviewRequest(
            'approvePartial',
            { name: this.currentUserReview!.userName },
            ReviewAction.PARTIALLY_APPROVE,
            () => {
                this.$auth.refreshUser();
                this.currentUserReview!.endedAt = new Date().toISOString();
            },
            () => {
                this.loadingValues.approvePartial = false;
            }
        );
    }

    undoApproval() {
        if (this.isCurrentReviewOpen) return;
        this.loadingValues.undoApproval = true;
        this.sendReviewRequest(
            'undoApproval',
            { name: this.currentUser.name },
            ReviewAction.UNDO_APPROVAL,
            () => {
                this.reviews.find((r) => r.userId === this.currentUser.id)!.endedAt = null;
            },
            () => {
                this.loadingValues.undoApproval = false;
            }
        );
    }

    sendReviewRequest(urlPath: string, args: Record<string, string>, action: ReviewAction, then: () => void, final: () => void = () => {}): Promise<void> {
        const message = `reviews.presets.${urlPath}`;
        return this.$api
            .requestInternal(`reviews/${this.projectVersion.id}/reviews/${urlPath}`, true, 'post', { message, args })
            .then(() => {
                if (this.currentUserReview) {
                    this.currentUserReview.messages.push({
                        action,
                        createdAt: new Date().toISOString(),
                        message,
                        args,
                    });
                }
                then();
                this.$nuxt.refresh();
            })
            .catch(this.$util.handleRequestError)
            .finally(final);
    }
}
</script>

<style lang="scss" scoped>
@import '~vuetify/src/styles/styles';

.review-message {
    color: map-deep-get($material-dark, 'text', 'primary');
    width: 100%;

    &.review-message-title {
        .review-message-state {
            font-size: 70%;
            margin-left: 4px;
            border-radius: 3px;
            padding: 0 3px;

            &.review-message-state-stopped {
                background-color: map-get($red, 'accent-4');
            }

            &.review-message-state-ongoing {
                color: map-deep-get($material-light, 'text', 'secondary');
                background-color: map-get($yellow, 'accent-4');
            }

            &.review-message-state-approved {
                color: map-deep-get($material-light, 'text', 'primary');
                background-color: map-deep-get($green, 'accent-2');
            }

            &.review-message-state-partiallyApproved {
                color: map-deep-get($material-light, 'text', 'secondary');
                background-color: map-deep-get($green, 'base');
            }
        }
    }

    &.review-message-start {
        color: map-get($yellow, 'accent-4');
    }

    &.review-message-stop {
        color: map-get($red, 'accent-2');
    }

    &.review-message-reopen {
        color: map-get($yellow, 'accent-1');
    }

    &.review-message-approve,
    &.review-message-partially_approve {
        color: map-get($green, 'accent-2');
    }

    &.review-message-undo_approval {
        color: map-get($orange, 'accent-3');
    }

    &.review-message-message .review-message-datetime {
        margin-left: 8px;
    }

    .review-message-datetime {
        font-size: 70%;
        color: map-deep-get($material-dark, 'text', 'secondary');
    }

    &.review-message-message {
        margin-left: 24px;
    }
}

.v-btn.v-size--x-small .v-icon.v-icon--left {
    margin-right: 2px;
}
</style>
