<template>
    <v-row>
        <v-col md="6" cols="12">
            <v-card>
                <v-card-title>{{ $t('projectApproval.awaitingChanges') }}</v-card-title>
                <AdminList :projects="waitingProjects" />
            </v-card>
        </v-col>
        <v-col md="6" cols="12">
            <v-card>
                <v-card-title>{{ $t('projectApproval.needsApproval') }}</v-card-title>
                <AdminList :projects="needsApproval" />
            </v-card>
        </v-col>
    </v-row>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { Context } from '@nuxt/types';
import { ProjectApproval } from 'hangar-internal';
import { GlobalPermission } from '~/utils/perms';
import { NamedPermission } from '~/types/enums';
import { HangarComponent } from '~/components/mixins';
import AdminList from '~/components/projects/AdminList.vue';

@Component({
    components: { AdminList },
})
@GlobalPermission(NamedPermission.REVIEWER)
export default class AdminApprovalProjectsPage extends HangarComponent {
    needsApproval!: ProjectApproval[];
    waitingProjects!: ProjectApproval[];

    head() {
        return {
            title: this.$t('projectApproval.title'),
        };
    }

    async asyncData({ $api, $util }: Context) {
        const data = await $api
            .requestInternal<{ needsApproval: ProjectApproval[]; waitingProjects: ProjectApproval[] }>('projects/admin/approval')
            .catch<any>($util.handlePageRequestError);
        return { needsApproval: data.needsApproval, waitingProjects: data.waitingProjects };
    }
}
</script>

<style lang="scss" scoped></style>
