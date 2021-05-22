<template>
    <v-card>
        <v-card-title>{{ $t('flagReview.title') }}</v-card-title>
        <v-card-text>
            <v-list v-if="flags.length > 0">
                <v-list-item v-for="flag in flags" :key="flag.id">
                    <v-list-item-avatar>
                        <UserAvatar :username="flag.reportedByName" clazz="user-avatar-xs"></UserAvatar>
                    </v-list-item-avatar>
                    <v-list-item-content>
                        <v-list-item-title>
                            {{
                                $t('flagReview.line1', [
                                    flag.reportedByName,
                                    `${flag.projectNamespace.owner}/${flag.projectNamespace.slug}`,
                                    $util.prettyDateTime(flag.createdAt),
                                ])
                            }}
                            <v-btn small icon color="primary" :to="`/${flag.projectNamespace.owner}/${flag.projectNamespace.slug}`" nuxt target="_blank">
                                <v-icon small>mdi-open-in-new</v-icon>
                            </v-btn>
                        </v-list-item-title>
                        <v-list-item-subtitle>{{ $t('flagReview.line2', [$t(flag.reason)]) }}</v-list-item-subtitle>
                        <v-list-item-subtitle>{{ $t('flagReview.line3', [flag.comment]) }}</v-list-item-subtitle>
                    </v-list-item-content>
                    <v-list-item-action>
                        <v-btn small :href="$util.forumUrl(flag.reportedByName)" class="mr-1">
                            <v-icon small left>mdi-reply</v-icon>
                            {{ $t('flagReview.msgUser') }}
                        </v-btn>
                        <v-btn small :href="$util.forumUrl(flag.projectNamespace.owner)" class="mr-1">
                            <v-icon small left>mdi-reply</v-icon>
                            {{ $t('flagReview.msgProjectOwner') }}
                        </v-btn>
                        <VisibilityChangerModal
                            :prop-visibility="flag.projectVisibility"
                            type="project"
                            :post-url="`projects/visibility/${flag.projectId}`"
                            small-btn
                        />
                        <v-btn small color="success" :loading="loading[flag.id]" @click="resolve(flag)">
                            <v-icon small left>mdi-check</v-icon>
                            {{ $t('flagReview.markResolved') }}
                        </v-btn>
                    </v-list-item-action>
                </v-list-item>
            </v-list>
            <v-alert v-else icon="mdi-thumb-up">{{ $t('flagReview.noFlags') }}</v-alert>
        </v-card-text>
    </v-card>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { Flag } from 'hangar-internal';
import { Context } from '@nuxt/types';
import UserAvatar from '~/components/users/UserAvatar.vue';
import { NamedPermission } from '~/types/enums';
import { GlobalPermission } from '~/utils/perms';
import VisibilityChangerModal from '~/components/modals/VisibilityChangerModal.vue';

@Component({
    components: { VisibilityChangerModal, UserAvatar },
})
@GlobalPermission(NamedPermission.MOD_NOTES_AND_FLAGS)
export default class AdminFlagsPage extends Vue {
    flags!: Flag[];
    loading: { [key: number]: boolean } = {};

    head() {
        return this.$seo.head(this.$t('flagReview.title'), null, this.$route, null);
    }

    resolve(flag: Flag) {
        this.loading[flag.id] = true;
        this.$api
            .requestInternal<Flag[]>(`flags/${flag.id}/resolve/true`, false, 'POST')
            .catch<any>(this.$util.handleRequestError)
            .then(() => {
                this.$nuxt.refresh();
                this.$auth.refreshUser();
            })
            .finally(() => {
                this.loading[flag.id] = false;
            });
    }

    created() {
        for (const flag of this.flags) {
            this.loading[flag.id] = false;
        }
    }

    async asyncData({ $api, $util }: Context) {
        const flags = await $api.requestInternal<Flag[]>(`flags/`, false).catch<any>($util.handlePageRequestError);
        return { flags };
    }
}
</script>

<style lang="scss" scoped>
.v-list-item__action--stack {
    flex-direction: row;
}
</style>
