<template>
    <div class="row">
        <div class="col-12">
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title" v-text="$t('user.queue.progress')"></h3>
                </div>
                <table class="table table-hover mb-0">
                    <thead>
                        <tr>
                            <th>Project version</th>
                            <th>Queued by</th>
                            <th style="text-align: right; max-width: 40px"></th>
                            <th>Status</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-if="!underReview.length">
                            <th rowspan="5">
                                <h4 v-text="$t('queue.review.none')"></h4>
                            </th>
                        </tr>
                        <tr
                            v-for="entry in underReview"
                            :key="entry.namespace + '/' + entry.versionStringUrl"
                            :class="{ warning: entry.unfinished && entry.reviewerId === currentUser.id }"
                        >
                            <td>
                                <a :href="ROUTES.parse('VERSIONS_SHOW', entry.author, entry.slug, entry.versionStringUrl)" v-text="entry.namespace"></a>
                                <br />
                                {{ entry.versionString }}
                                <span class="channel" :style="{ backgroundColor: entry.channelColor.hex }" v-text="entry.channelName"></span>
                            </td>
                            <td>
                                <a v-if="entry.versionAuthor" :href="`https://papermc.io/forums/users/${entry.versionAuthor}`" v-text="entry.versionAuthor"></a>
                                <span v-else>Unknown</span>
                                <br />
                                <span class="faint" v-text="new Date(entry.versionCreatedAt).toLocaleDateString()"></span>
                            </td>
                            <td v-if="entry.unfinished" style="text-align: right; max-width: 40px">
                                <i v-if="currentUser.id === entry.reviewerId" class="status fas fa-fw fa-play-circle fa-2x" style="color: green"></i>
                                <i v-else class="status fas fa-fw fa-cog fa-2x" style="color: black"></i>
                            </td>
                            <td v-else style="text-align: right; max-width: 40px">
                                <i class="status fas fa-fw fa-pause-circle fa-2x" style="color: orange"></i>
                            </td>
                            <td v-if="entry.unfinished" style="color: darkred">
                                {{ entry.reviewerName }}
                                <br />
                                <span v-if="Date.now() - new Date(entry.reviewStarted) >= maxReviewTime">pastdue </span>
                                <span v-else>started </span>
                                <span>{{ ((Date.now() - new Date(entry.reviewStarted)) / 1000 / 60 / 60).toFixed(2) }} hours ago</span>
                            </td>
                            <td v-else>
                                <span v-text="entry.reviewerName" style="text-decoration: line-through"></span>
                                <br />
                                <span v-if="Date.now() - new Date(entry.reviewStarted) >= maxReviewTime">pastdue </span>
                                <span>abandoned </span>
                                <span>{{ ((Date.now() - new Date(entry.reviewStarted)) / 1000 / 60 / 60).toFixed(2) }} hours ago</span>
                            </td>
                            <td style="vertical-align: middle; text-align: right; padding-right: 15px">
                                <a :href="ROUTES.parse('REVIEWS_SHOW_REVIEWS', entry.author, entry.slug, entry.versionStringUrl)">
                                    <i class="fas fa-2x fa-fw fa-info"></i>
                                </a>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
    <div class="row">
        <div class="col-12">
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title" v-text="$t('user.queue.open')"></h3>
                </div>
                <table class="table table-hover mb-0">
                    <thead>
                        <tr>
                            <th></th>
                            <th>Project</th>
                            <th>Version</th>
                            <th>Queued by</th>
                            <th style="text-align: right">Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-if="!notStarted.length">
                            <th rowspan="5">
                                <h4><i class="fas fa-thumbs-o-up"></i> {{ $t('user.queue.none') }}</h4>
                            </th>
                        </tr>
                        <tr v-for="entry in notStarted" :key="entry.namespace + '/' + entry.versionStringUrl">
                            <td>
                                <UserAvatar :user-name="entry.author" clazz="user-avatar-xs" />
                            </td>
                            <td>
                                <a :href="ROUTES.parse('VERSIONS_SHOW', entry.author, entry.slug, entry.versionStringUrl)" v-text="entry.namespace"></a>
                            </td>
                            <td>
                                <span class="faint">{{ new Date(entry.versionCreatedAt).toLocaleDateString() }}&nbsp;</span>
                                <span class="minor">{{ entry.versionString }}&nbsp;</span>
                                <span class="channel" :style="{ backgroundColor: entry.channelColor.hex }">{{ entry.channelName }}</span>
                            </td>
                            <td>
                                <a v-if="entry.versionAuthor" :href="`https://papermc.io/forums/users/${entry.versionAuthor}`" v-text="entry.versionAuthor"></a>
                            </td>
                            <td>
                                <a
                                    class="btn btn-success float-right"
                                    :href="ROUTES.parse('REVIEWS_SHOW_REVIEWS', entry.author, entry.slug, entry.versionStringUrl)"
                                >
                                    Start Review
                                </a>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</template>

<script>
import UserAvatar from '@/components/UserAvatar';

export default {
    name: 'VersionQueue',
    components: { UserAvatar },
    data() {
        return {
            ROUTES: window.ROUTES,
            maxReviewTime: window.MAX_REVIEW_TIME,
            currentUser: window.CURRENT_USER,
            notStarted: window.NOT_STARTED.sort((a, b) => new Date(a.versionCreatedAt) - new Date(b.versionCreatedAt)),
            underReview: window.UNDER_REVIEW,
        };
    },
    created() {
        console.log(this.maxReviewTime);
    },
};
</script>
