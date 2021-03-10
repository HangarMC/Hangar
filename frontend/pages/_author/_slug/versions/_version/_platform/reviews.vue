<template>
    <div>
        <v-row>
            <v-col>{{ $t('reviews.headline', [version.author, $util.prettyDate(version.createdAt)]) }}</v-col>
            <v-col>
                <v-btn color="secondary" @click="removeFromQueue">{{ $t('reviews.removeFromQueue') }}</v-btn>
                <v-btn color="secondary" :to="'/' + $route.params.author + '/' + $route.params.slug">{{ $t('reviews.projectPage') }}</v-btn>
                <v-btn color="secondary" :to="'/' + $route.params.author + '/' + $route.params.slug + '/versions/' + $route.params.version + '/jar'">
                    {{ $t('reviews.downloadFile') }}
                </v-btn>
                <v-btn v-if="!hasReviewStarted" color="primary" @click="startReview">{{ $t('reviews.startReview') }}</v-btn>
                <v-btn v-if="hasReviewStarted" color="error" @click="stopReview">{{ $t('reviews.stopReview') }}</v-btn>
                <v-btn v-if="hasReviewStarted" color="primary" @click="approve">{{ $t('reviews.approve') }}</v-btn>
                <v-btn v-if="hasReviewStarted" color="primary" @click="approve">{{ $t('reviews.approvePartial') }}</v-btn>
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
                    @click:append-outer="sendMessage"
                ></v-text-field>

                <h2>{{ $t('reviews.title') }}</h2>

                <v-alert v-if="!hasReviewStarted" type="info">{{ $t('reviews.notUnderReview') }}</v-alert>

                <v-list v-if="hasReviewStarted">
                    <v-list-item v-for="(event, idx) in reviewEvents" :key="idx">
                        <v-row>
                            <v-col cols="2">{{ event.date }}</v-col>
                            <!-- eslint-disable-next-line vue/no-v-html -->
                            <v-col cols="10" v-html="event.message"></v-col>
                        </v-row>
                    </v-list-item>
                </v-list>
            </v-col>
        </v-row>
    </div>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { ReviewEvent } from 'hangar-internal';
import { HangarProjectVersionMixin } from '~/components/mixins';

// TODO implement ProjectVersionsVersionReviewPage
@Component
export default class ProjectVersionsVersionReviewPage extends HangarProjectVersionMixin {
    hasReviewStarted: Boolean = false;
    message: String = '';

    reviewEvents: ReviewEvent[] = [];

    // TODO send all these to server
    startReview() {
        this.hasReviewStarted = true;
        this.reviewEvents.push({ date: this.$util.prettyDate(new Date()), message: this.$util.getCurrentUser()?.name + ' started a review' });
    }

    stopReview() {
        this.hasReviewStarted = false;
    }

    sendMessage() {
        this.reviewEvents.push({
            date: this.$util.prettyDate(new Date()),
            message: this.$util.getCurrentUser()?.name + ' added a message: <br><i>' + this.message + '</i>',
        });
        this.message = '';
    }

    approve() {}

    removeFromQueue() {}
}
</script>

<style lang="scss" scoped></style>
