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
                        <v-list-item-title>{{
                            $t('flagReview.line1', [flag.reportedByName, flag.projectNamespace, $util.prettyDateTime(flag.createdAt)])
                        }}</v-list-item-title>
                        <v-list-item-subtitle>{{ $t('flagReview.line2', [flag.reason, flag.comment]) }}</v-list-item-subtitle>
                    </v-list-item-content>
                    <v-list-item-action>
                        <v-btn small :href="$util.forumUrl(flag.reportedByName)"><v-icon>mdi-reply</v-icon>{{ $t('flagReview.msgUser') }}</v-btn>
                        <v-btn small :href="$util.forumUrl(flag.projectOwnerName)"><v-icon>mdi-reply</v-icon>{{ $t('flagReview.msgProjectOwner') }}</v-btn>
                        <v-menu offset-y>
                            <template #activator="{ on, attrs }">
                                <v-btn small v-bind="attrs" v-on="on"><v-icon>mdi-eye</v-icon>{{ $t('flagReview.visibilityActions') }}</v-btn>
                            </template>
                            <v-list>
                                <v-list-item v-for="(v, index) in visibilities" :key="index">
                                    <v-list-item-title @click="visibility(flag, v)">{{ v }}</v-list-item-title>
                                </v-list-item>
                            </v-list>
                        </v-menu>
                        <v-btn small color="primary" @click="resolve(flag)"><v-icon>mdi-check</v-icon>{{ $t('flagReview.markResolved') }}</v-btn>
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
import { NamedPermission, Visibility } from '~/types/enums';
import { GlobalPermission } from '~/utils/perms';

@Component({
    components: { UserAvatar },
})
@GlobalPermission(NamedPermission.MOD_NOTES_AND_FLAGS)
export default class AdminFlagsPage extends Vue {
    flags!: Flag[];

    async asyncData({ $api, $util }: Context) {
        const flags = await $api.requestInternal<Flag[]>(`flags/`, false).catch<any>($util.handlePageRequestError);
        return { flags };
    }

    get visibilities(): Visibility[] {
        return Object.keys(Visibility) as Visibility[];
    }

    // todo send to server
    visibility(flag: Flag, visibility: Visibility) {
        console.log('changing visibility of ', flag, 'to ', visibility);
    }

    resolve(flag: Flag) {
        console.log('resolve ', flag);
    }
}
</script>

<style lang="scss" scoped>
.v-list-item__action--stack {
    flex-direction: row;
}
</style>
