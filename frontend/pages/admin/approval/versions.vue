<template>
    <div>
        <v-card>
            <v-card-title>{{ $t('versionApproval.inReview') }}</v-card-title>
            <v-card-text>
                <v-simple-table>
                    <thead>
                        <tr>
                            <th class="text-left">{{ $t('versionApproval.projectVersion') }}</th>
                            <th class="text-left">{{ $t('versionApproval.queuedBy') }}</th>
                            <th class="text-left">{{ $t('versionApproval.status') }}</th>
                        </tr>
                    </thead>
                    <tbody>
                        <!-- todo loop + proper data -->
                        <tr>
                            <td>
                                <NuxtLink to="/Narnimm/Prism">Narnimm/Prism</NuxtLink><br />
                                2.1.8-SNAPSHOT <Tag color="someObject" data="Release" short-form></Tag>
                            </td>
                            <td>
                                <NuxtLink to="/Narnimm">Narnimm</NuxtLink>
                                10/29/2020
                            </td>
                            <td>
                                <NuxtLink to="/MiniDigger">MiniDigger</NuxtLink><br />
                                bla hours ago
                                <v-icon large class="float-right" style="line-height: 0">mdi-information-outline</v-icon>
                            </td>
                        </tr>
                    </tbody>
                </v-simple-table>
            </v-card-text>
        </v-card>
        <v-card class="mt-4">
            <v-card-title>{{ $t('versionApproval.approvalQueue') }}</v-card-title>
            <v-card-text>
                <v-simple-table>
                    <thead>
                        <tr>
                            <th></th>
                            <th class="text-left">{{ $t('versionApproval.project') }}</th>
                            <th class="text-left">{{ $t('versionApproval.date') }}</th>
                            <th class="text-left">{{ $t('versionApproval.version') }}</th>
                            <th class="text-left">{{ $t('versionApproval.queuedBy') }}</th>
                            <th class="text-left">{{ $t('versionApproval.status') }}</th>
                        </tr>
                    </thead>
                    <tbody>
                        <!-- todo loop + proper data -->
                        <tr>
                            <td><UserAvatar username="Narnimm" :avatar-url="$util.avatarUrl('Narnimm')" clazz="user-avatar-sm"></UserAvatar></td>
                            <td><NuxtLink to="/Narnimm/Prism">Narnimm/Prism</NuxtLink></td>
                            <td>10/29/2020</td>
                            <td>2.1.8-SNAPSHOT <Tag color="someObject" data="Release" short-form></Tag></td>
                            <td><NuxtLink to="/Narnimm">Narnimm</NuxtLink></td>
                            <td><v-btn color="primary">Start Review</v-btn></td>
                        </tr>
                    </tbody>
                </v-simple-table>
            </v-card-text>
        </v-card>
    </div>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { GlobalPermission } from '~/utils/perms';
import { NamedPermission } from '~/types/enums';
import Tag from '~/components/Tag.vue';
import UserAvatar from '~/components/users/UserAvatar.vue';

@Component({
    components: { UserAvatar, Tag },
})
@GlobalPermission(NamedPermission.REVIEWER)
export default class AdminApprovalVersionsPage extends Vue {}
</script>

<style lang="scss" scoped></style>
