<template>
    <div>
        <div class="text-h4 my-2">{{ $t('userActivity.title', [$route.params.user]) }}</div>
        <v-row>
            <v-col>
                <v-card>
                    <v-card-title>{{ $t('userActivity.reviews') }}</v-card-title>
                    <v-card-text>
                        <v-simple-table v-if="reviewActivities.length">
                            <tbody>
                                <tr v-for="(activity, idx) in reviewActivities" :key="`review-${idx}`">
                                    <td>{{ $t('userActivity.reviewApproved') }}</td>
                                    <td>{{ activity.endedAt ? $util.prettyDateTime(activity.endedAt) : '' }}</td>
                                    <td>
                                        {{
                                            `${activity.namespace.owner}/${activity.namespace.slug}/${
                                                activity.versionString
                                            }: ${activity.platforms[0].toLowerCase()}`
                                        }}
                                    </td>
                                    <td>
                                        <v-btn
                                            color="warning"
                                            :to="{
                                                name: 'author-slug-versions-version-platform-reviews',
                                                params: getRouteParams(activity),
                                            }"
                                            nuxt
                                        >
                                            <v-icon left>mdi-list-status</v-icon>
                                            {{ $t('version.page.reviewLogs') }}
                                        </v-btn>
                                    </td>
                                </tr>
                            </tbody>
                        </v-simple-table>
                        <v-alert v-else type="info" text>
                            {{ $t('health.empty') }}
                        </v-alert>
                    </v-card-text>
                </v-card>
            </v-col>
            <v-col>
                <v-card>
                    <v-card-title>{{ $t('userActivity.flags') }}</v-card-title>
                    <v-card-text>
                        <v-simple-table v-if="flagActivities.length">
                            <tbody>
                                <tr v-for="(activity, idx) in flagActivities" :key="`flag-${idx}`">
                                    <td>{{ $t('userActivity.flagResolved') }}</td>
                                    <td>{{ activity.resolvedAt ? $util.prettyDateTime(activity.resolvedAt) : '' }}</td>
                                    <td>
                                        <NuxtLink :to="`/${activity.namespace.owner}/${activity.namespace.slug}`">
                                            {{ `${activity.namespace.owner}/${activity.namespace.slug}` }}
                                        </NuxtLink>
                                    </td>
                                </tr>
                            </tbody>
                        </v-simple-table>
                        <v-alert v-else type="info" text>
                            {{ $t('health.empty') }}
                        </v-alert>
                    </v-card-text>
                </v-card>
            </v-col>
        </v-row>
    </div>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { Context } from '@nuxt/types';
import { FlagActivity, ReviewActivity } from 'hangar-internal';
import { GlobalPermission } from '~/utils/perms';
import { NamedPermission } from '~/types/enums';
import { HangarComponent } from '~/components/mixins';

@Component
@GlobalPermission(NamedPermission.REVIEWER)
export default class AdminActivitiesUserPage extends HangarComponent {
    flagActivities!: FlagActivity[];
    reviewActivities!: ReviewActivity[];

    head() {
        return this.$seo.head(this.$t('userActivity.title', [this.$route.params.user]), null, this.$route, this.$util.avatarUrl(this.$route.params.user));
    }

    getRouteParams(activity: ReviewActivity) {
        return {
            author: activity.namespace.owner,
            slug: activity.namespace.slug,
            version: activity.versionString,
            platform: activity.platforms[0].toLowerCase(),
        };
    }

    async asyncData({ $api, $util, params }: Context) {
        const data = await Promise.all([
            $api.requestInternal<FlagActivity[]>(`admin/activity/${params.user}/flags`),
            $api.requestInternal<ReviewActivity[]>(`admin/activity/${params.user}/reviews`),
        ]).catch($util.handlePageRequestError);
        if (!data) return;
        return { flagActivities: data[0], reviewActivities: data[1] };
    }
}
</script>

<style lang="scss" scoped></style>
