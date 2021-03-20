<template>
    <v-card>
        <v-card-title>{{ $t('flagReview.title') }}</v-card-title>
        <v-card-text>
            <v-list v-if="flags.length > 0">
                <v-list-item v-for="flag in flags" :key="flag.id">
                    <v-list-item-avatar>
                        <UserAvatar :username="flag.user.name" clazz="user-avatar-xs"></UserAvatar>
                    </v-list-item-avatar>
                    <!-- todo flag content -->
                    <v-list-item-content>
                        <v-list-item-title>dddddddd</v-list-item-title>
                        <v-list-item-title>dddddddd</v-list-item-title>
                    </v-list-item-content>
                    <v-list-item-action>
                        <v-btn small :to="$util.forumUrl(flag.user.name)"><v-icon>mdi-reply</v-icon>{{ $t('flagReview.msgUser') }}</v-btn>
                        <!-- todo the subject isnt part of the model? -->
                        <v-btn small :to="$util.forumUrl(flag.user.name)"><v-icon>mdi-reply</v-icon>{{ $t('flagReview.msgProjectOwner') }}</v-btn>
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
import UserAvatar from '~/components/users/UserAvatar.vue';
import { Visibility } from '~/types/enums';
@Component({
    components: { UserAvatar },
})
export default class AdminFlagsPage extends Vue {
    // todo load from server
    flags: Flag[] = [
        {
            createdAt: new Date().toString(),
            id: 1,
            resolvedBy: this.$util.dummyUser(),
            resolved: false,
            resolvedAt: '',
            user: this.$util.dummyUser(),
            reason: { type: 'TEST', title: 'TEST' },
            comment: 'Test',
        },
    ];

    get visibilities(): Visibility[] {
        return Object.keys(Visibility);
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
