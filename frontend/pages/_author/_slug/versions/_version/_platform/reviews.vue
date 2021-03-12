<template>
    <v-container>
        <v-row class="mb-1">
            <v-col>{{ $t('reviews.headline', [projectVersion.author, $util.prettyDate(projectVersion.createdAt)]) }}</v-col>
            <v-col class="text-right">
                <template v-if="!isReviewStateChecked">
                    <v-btn small color="secondary" @click="removeFromQueue"><v-icon left>mdi-layers-remove</v-icon>{{ $t('reviews.removeFromQueue') }}</v-btn>
                    <v-btn small color="info" :to="{ name: 'author-slug', params: $route.params }" nuxt exact>{{ $t('reviews.projectPage') }}</v-btn>
                    <v-btn small color="info" :to="'/' + $route.params.author + '/' + $route.params.slug + '/versions/' + $route.params.version + '/jar'" nuxt>
                        <v-icon left>mdi-download</v-icon>
                        {{ $t('reviews.downloadFile') }}
                    </v-btn>
                    <v-btn v-if="!hasReviewStarted" small color="success" :loading="loading.start" @click="startReview">
                        <v-icon left>mdi-play</v-icon>
                        {{ $t('reviews.startReview') }}
                    </v-btn>
                    <v-btn v-if="hasReviewStarted" small color="error" @click="stopReview">{{ $t('reviews.stopReview') }}</v-btn>
                    <v-btn v-if="hasReviewStarted" small color="primary" @click="approve">{{ $t('reviews.approve') }}</v-btn>
                    <v-btn v-if="hasReviewStarted" small color="primary" @click="approve">{{ $t('reviews.approvePartial') }}</v-btn>
                </template>
                <v-btn v-else small color="error"><v-icon left>mdi-undo</v-icon>{{ $t('reviews.undoApproval') }}</v-btn>
            </v-col>
        </v-row>
        <v-divider />
        <v-row>
            <v-col>
                <v-text-field
                    v-if="hasReviewStarted"
                    v-model="message"
                    :label="$t('reviews.reviewMessage')"
                    append-outer-icon="mdi-clipboard"
                    @click="sendMessage"
                />

                <h2>{{ $t('reviews.title') }}</h2>

                <v-alert v-if="!hasReviewStarted" type="info">{{ $t('reviews.notUnderReview') }}</v-alert>

                <v-list v-for="(review, index) in reviews" :key="`review-${index}`">
                    <v-list-item v-for="(msg, mIndex) in review.messages" :key="`review-${index}-msg-${mIndex}`">
                        <v-list-item-action-text>
                            {{ msg.message }}
                        </v-list-item-action-text>
                    </v-list-item>
                </v-list>
            </v-col>
        </v-row>
    </v-container>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { HangarReview } from 'hangar-internal';
import { HangarProjectVersionMixin } from '~/components/mixins';
import { GlobalPermission } from '~/utils/perms';
import { NamedPermission, ReviewAction } from '~/types/enums';

// TODO implement ProjectVersionsVersionReviewPage
@Component
@GlobalPermission(NamedPermission.REVIEWER)
export default class ProjectVersionsVersionReviewPage extends HangarProjectVersionMixin {
    hasReviewStarted: Boolean = false;
    message: String = '';
    reviews: HangarReview[] = [];
    loading = {
        start: false,
    };

    async fetch() {
        this.reviews = await this.$api
            .requestInternal<HangarReview[]>(`reviews/${this.projectVersion.id}/reviews`)
            .catch<any>(this.$util.handlePageRequestError);
        if (this.reviews.length > 0 && this.reviews[this.reviews.length - 1].endedAt === null) {
            this.hasReviewStarted = true;
        }
    }

    // TODO send all these to server
    startReview() {
        const msg = `${this.$store.state.auth.user.name} started a review`;
        this.$api
            .requestInternal(`reviews/${this.projectVersion.id}/reviews/start`, true, 'post', {
                content: msg,
            })
            .then(() => {
                this.hasReviewStarted = true;
                this.reviews.push({
                    userName: this.$store.state.auth.user.name,
                    createdAt: new Date().toISOString(),
                    endedAt: null,
                    messages: [
                        {
                            message: msg,
                            action: ReviewAction.START,
                            createdAt: new Date().toISOString(),
                        },
                    ],
                });
            })
            .catch(this.$util.handleRequestError);
        this.hasReviewStarted = true;
        // this.reviewEvents.push({ date: this.$util.prettyDate(new Date()), message: this.$util.getCurrentUser()?.name + ' started a review' });
    }

    stopReview() {
        this.hasReviewStarted = false;
    }

    sendMessage() {
        // this.reviewEvents.push({
        //     date: this.$util.prettyDate(new Date()),
        //     message: this.$util.getCurrentUser()?.name + ' added a message: <br><i>' + this.message + '</i>',
        // });
        // this.message = '';
    }

    approve() {}

    removeFromQueue() {}
}
</script>

<style lang="scss" scoped></style>
