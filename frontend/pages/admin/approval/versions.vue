<template>
    <div>
        <v-card>
            <v-card-title>{{ $t('versionApproval.approvalQueue') }}</v-card-title>
            <v-card-text>
                <v-data-table :headers="notStartedHeaders" :items="notStarted" :custom-sort="sorter" disable-pagination disable-filtering hide-default-footer>
                    <template #item.project="{ item }">
                        <NuxtLink :to="`/${item.namespace.owner}/${item.namespace.slug}`">{{ `${item.namespace.owner}/${item.namespace.slug}` }}</NuxtLink>
                    </template>
                    <template #item.date="{ item }">
                        <span class="start-date">{{ $util.prettyDateTime(item.versionCreatedAt) }}</span>
                    </template>
                    <template #item.version="{ item }">
                        <NuxtLink :to="{ name: 'author-slug-versions-version-platform', params: getRouteParams(item) }">
                            <Tag :color="{ background: item.channelColor }" :name="item.channelName" :data="item.versionString" />
                        </NuxtLink>
                    </template>
                    <template #item.queuedBy="{ item }">
                        <NuxtLink :to="`/${item.versionAuthor}`">
                            {{ item.versionAuthor }}
                        </NuxtLink>
                    </template>
                    <template #item.startBtn="{ item }">
                        <v-btn color="primary" :to="{ name: 'author-slug-versions-version-platform-reviews', params: getRouteParams(item) }" nuxt>
                            <v-icon left>mdi-play</v-icon>
                            {{ $t('version.page.reviewStart') }}
                        </v-btn>
                    </template>
                </v-data-table>
            </v-card-text>
        </v-card>
        <v-card class="mt-4">
            <v-card-title>{{ $t('versionApproval.inReview') }}</v-card-title>
            <v-card-text>
                <v-data-table
                    :headers="underReviewHeaders"
                    :items="underReview"
                    :expanded.sync="underReviewExpanded"
                    :custom-sort="sorter"
                    disable-filtering
                    item-key="versionId"
                    hide-default-footer
                    disable-pagination
                    single-expand
                    @click:row="clickRow"
                >
                    <template #item.project="{ item }">
                        <NuxtLink :to="`/${item.namespace.owner}/${item.namespace.slug}`">
                            {{ `${item.namespace.owner}/${item.namespace.slug}` }}
                        </NuxtLink>
                    </template>
                    <template #item.version="{ item }">
                        <NuxtLink :to="{ name: 'author-slug-versions-version-platform', params: getRouteParams(item) }">
                            <Tag :color="{ background: item.channelColor }" :name="item.channelName" :data="item.versionString" />
                        </NuxtLink>
                    </template>
                    <template #item.queuedBy="{ item }">
                        <NuxtLink :to="`/${item.versionAuthor}`">
                            {{ item.versionAuthor }}
                        </NuxtLink>
                        <br />
                        <small class="start-date">{{ $util.prettyDateTime(item.versionCreatedAt) }}</small>
                    </template>
                    <template #item.status="{ item }">
                        <span class="status-count status-colored ongoing">{{ $t('versionApproval.statuses.ongoing', [getOngoingCount(item)]) }}</span>
                        <br />
                        <span class="status-count status-colored stopped">{{ $t('versionApproval.statuses.stopped', [getStoppedCount(item)]) }}</span>
                        <br />
                        <span class="status-count status-colored approved">{{ $t('versionApproval.statuses.approved', [getApprovedCount(item)]) }}</span>
                    </template>
                    <template #item.reviewLogs="{ item }">
                        <v-btn
                            color="warning"
                            :to="{
                                name: 'author-slug-versions-version-platform-reviews',
                                params: getRouteParams(item),
                            }"
                            nuxt
                            @click.stop=""
                        >
                            <v-icon left>mdi-list-status</v-icon>
                            {{ $t('version.page.reviewLogs') }}
                        </v-btn>
                    </template>
                    <template #expanded-item="{ item, headers }">
                        <td :colspan="headers.length">
                            <v-list dense>
                                <v-list-item v-for="entry in item.reviews" :key="entry.reviewerName" class="review-list-entry">
                                    <span
                                        class="reviewer-name status-colored"
                                        :class="{ ongoing: isOngoing(entry), stopped: isStopped(entry), approved: isApproved(entry) }"
                                        >{{ entry.reviewerName }}</span
                                    >
                                    <span class="review-started">{{ $t('versionApproval.started', [$util.prettyDateTime(entry.reviewStarted)]) }}</span>
                                    <span
                                        v-if="entry.reviewEnded"
                                        class="review-ended status-colored"
                                        :class="{ stopped: isStopped(entry), approved: isApproved(entry) }"
                                        >{{ $t('versionApproval.ended', [$util.prettyDateTime(entry.reviewEnded)]) }}</span
                                    >
                                </v-list-item>
                            </v-list>
                        </td>
                    </template>
                </v-data-table>
            </v-card-text>
        </v-card>
    </div>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { Context } from '@nuxt/types';
import { Review, ReviewQueueEntry } from 'hangar-internal';
import { DataTableHeader, DataTableItemProps } from 'vuetify';
import { GlobalPermission } from '~/utils/perms';
import { NamedPermission, ReviewAction } from '~/types/enums';
import Tag from '~/components/Tag.vue';
import UserAvatar from '~/components/users/UserAvatar.vue';
import { HangarComponent } from '~/components/mixins';

@Component({
    components: { UserAvatar, Tag },
})
@GlobalPermission(NamedPermission.REVIEWER)
export default class AdminApprovalVersionsPage extends HangarComponent {
    underReviewExpanded = [];
    underReview!: ReviewQueueEntry[];
    notStarted!: ReviewQueueEntry[];
    actions = {
        ongoing: [ReviewAction.START, ReviewAction.MESSAGE, ReviewAction.UNDO_APPROVAL, ReviewAction.REOPEN],
        stopped: [ReviewAction.STOP],
        approved: [ReviewAction.APPROVE, ReviewAction.PARTIALLY_APPROVE],
    };

    underReviewHeaders: DataTableHeader[] = [
        { text: this.$t('versionApproval.project') as string, value: 'project', sortable: false },
        { text: this.$t('versionApproval.version') as string, value: 'version', sortable: false },
        { text: this.$t('versionApproval.queuedBy') as string, value: 'queuedBy' },
        { text: this.$t('versionApproval.status') as string, value: 'status' },
        { text: '', value: 'reviewLogs', sortable: false, align: 'end' },
    ];

    notStartedHeaders: DataTableHeader[] = [
        { text: this.$t('versionApproval.project') as string, value: 'project', sortable: false },
        { text: this.$t('versionApproval.date') as string, value: 'date' },
        { text: this.$t('versionApproval.version') as string, value: 'version', sortable: false },
        { text: this.$t('versionApproval.queuedBy') as string, value: 'queuedBy' },
        { text: '', value: 'startBtn', sortable: false, align: 'end' },
    ];

    head() {
        return this.$seo.head(this.$t('versionApproval.title'), null, this.$route, null);
    }

    sorter(entries: ReviewQueueEntry[], sortBy: string[], sortDesc: boolean[]) {
        if (sortBy[0] === 'queuedBy') {
            entries.sort((a, b) => {
                if (new Date(a.versionCreatedAt) < new Date(b.versionCreatedAt)) {
                    return sortDesc[0] ? 1 : -1;
                }
                return sortDesc[0] ? -1 : 1;
            });
        } else if (sortBy[0] === 'status') {
            entries.sort((a, b) => {
                const aCount = this.getOngoingCount(a);
                const bCount = this.getOngoingCount(b);
                if (aCount < bCount) {
                    return sortDesc[0] ? 1 : -1;
                } else if (aCount === bCount) {
                    if (this.getStoppedCount(a) < this.getStoppedCount(b)) {
                        return sortDesc[0] ? 1 : -1;
                    }
                }
                return sortDesc[0] ? -1 : 1;
            });
        }
        return entries;
    }

    queueSort(a: ReviewQueueEntry, b: ReviewQueueEntry): number {
        if (new Date(a.versionCreatedAt) > new Date(b.versionCreatedAt)) {
            return 1;
        }
        return -1;
    }

    clickRow(_: ReviewQueueEntry, event: DataTableItemProps) {
        event.expand(!event.isExpanded);
    }

    getRouteParams(entry: ReviewQueueEntry) {
        return {
            author: entry.namespace.owner,
            slug: entry.namespace.slug,
            version: entry.versionString,
            platform: entry.platforms[0].toLowerCase(),
        };
    }

    async asyncData({ $api, $util }: Context) {
        const data = await $api
            .requestInternal<{ underReview: ReviewQueueEntry[]; notStarted: ReviewQueueEntry[] }>('admin/approval/versions')
            .catch<any>($util.handlePageRequestError);
        if (!data) return;
        return { underReview: data.underReview, notStarted: data.notStarted };
    }

    isOngoing(review: Review) {
        return this.actions.ongoing.includes(review.lastAction);
    }

    isStopped(review: Review) {
        return this.actions.stopped.includes(review.lastAction);
    }

    isApproved(review: Review) {
        return this.actions.approved.includes(review.lastAction);
    }

    getOngoingCount(entry: ReviewQueueEntry) {
        return this.getCount(entry, ...this.actions.ongoing);
    }

    getStoppedCount(entry: ReviewQueueEntry) {
        return this.getCount(entry, ...this.actions.stopped);
    }

    getApprovedCount(entry: ReviewQueueEntry) {
        return this.getCount(entry, ...this.actions.approved);
    }

    getCount(entry: ReviewQueueEntry, ...actions: ReviewAction[]) {
        let count = 0;
        for (const review of entry.reviews) {
            if (actions.includes(review.lastAction)) {
                count++;
            }
        }
        return count;
    }
}
</script>

<style lang="scss" scoped>
@import '~vuetify/src/styles/styles';

.status-count {
    font-size: 12px;
}

.status-colored {
    &.ongoing {
        color: map-get($yellow, 'accent-4');
    }

    &.stopped {
        color: map-get($red, 'accent-2');
    }

    &.approved {
        color: map-get($green, 'accent-2');
    }
}

.start-date {
    color: map-deep-get($material-dark, 'text', 'secondary');
    font-size: 12px;
}

.review-list-entry {
    .reviewer-name {
        font-weight: bold;
        margin-right: 6px;
    }

    .review-started {
        @extend .start-date;
    }

    .review-ended {
        margin-left: 16px;
        font-size: 12px;
    }
}
</style>
