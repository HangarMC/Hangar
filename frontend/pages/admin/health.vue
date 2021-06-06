<template>
    <v-row>
        <h2 class="col-12 text-h4">
            {{ $t('health.title') }}
        </h2>
        <v-col cols="12" md="6">
            <v-card height="100%">
                <v-card-title>{{ $t('health.noTopicProject') }}</v-card-title>
                <v-card-text>
                    <v-list dense>
                        <v-list-item v-for="project in noTopicProjects" :key="project.namespace.slug + project.namespace.owner">
                            <NuxtLink :to="'/' + project.namespace.owner + '/' + project.namespace.slug">
                                <strong>{{ project.namespace.owner + '/' + project.namespace.slug }}</strong>
                            </NuxtLink>
                        </v-list-item>
                        <v-list-item v-if="!noTopicProjects || noTopicProjects.length === 0">
                            <v-alert type="success" width="100%" dense>
                                {{ $t('health.empty') }}
                            </v-alert>
                        </v-list-item>
                    </v-list>
                </v-card-text>
            </v-card>
        </v-col>
        <v-col cols="12" md="6">
            <v-card height="100%">
                <v-card-title>{{ $t('health.erroredJobs') }}</v-card-title>
                <v-card-text>
                    <v-list dense>
                        <v-list-item v-for="job in erroredJobs" :key="job.jobType + new Date(job.lastUpdated).toISOString()">
                            {{ $t('health.jobText', [job.jobType, job.lastErrorDescriptor, $util.prettyDateTime(job.lastUpdated)]) }}
                        </v-list-item>
                        <v-list-item v-if="!erroredJobs || erroredJobs.length === 0">
                            <v-alert type="success" width="100%" dense>
                                {{ $t('health.empty') }}
                            </v-alert>
                        </v-list-item>
                    </v-list>
                </v-card-text>
            </v-card>
        </v-col>
        <v-col cols="12" md="6">
            <v-card height="100%">
                <v-card-title>{{ $t('health.staleProjects') }}</v-card-title>
                <v-card-text>
                    <v-list dense>
                        <v-list-item v-for="project in staleProjects" :key="project.namespace.slug + project.namespace.owner">
                            <NuxtLink :to="'/' + project.namespace.owner + '/' + project.namespace.slug">
                                <strong>{{ project.namespace.owner + '/' + project.namespace.slug }}</strong>
                            </NuxtLink>
                        </v-list-item>
                        <v-list-item v-if="!staleProjects || staleProjects.length === 0">
                            <v-alert type="success" width="100%" dense>
                                {{ $t('health.empty') }}
                            </v-alert>
                        </v-list-item>
                    </v-list>
                </v-card-text>
            </v-card>
        </v-col>
        <v-col cols="12" md="6">
            <v-card height="100%">
                <v-card-title>{{ $t('health.notPublicProjects') }}</v-card-title>
                <v-card-text>
                    <v-list dense>
                        <v-list-item v-for="project in nonPublicProjects" :key="project.namespace.slug + project.namespace.owner">
                            <NuxtLink :to="'/' + project.namespace.owner + '/' + project.namespace.slug">
                                <strong>{{ project.namespace.owner + '/' + project.namespace.slug }}</strong>
                                <small>{{ $t('visibility.name.' + project.visibility) }}</small>
                            </NuxtLink>
                        </v-list-item>
                        <v-list-item v-if="!nonPublicProjects || nonPublicProjects.length === 0">
                            <v-alert type="success" width="100%" dense>
                                {{ $t('health.empty') }}
                            </v-alert>
                        </v-list-item>
                    </v-list>
                </v-card-text>
            </v-card>
        </v-col>
        <v-col cols="12" md="6">
            <v-card height="100%">
                <v-card-title>{{ $t('health.noPlatform') }}</v-card-title>
                <v-card-text />
                <v-list dense>
                    <v-list-item>
                        <v-alert type="warning" width="100%" dense> TODO: Implementation </v-alert>
                    </v-list-item>
                    <!--TODO idek what this is for?-->
                    <!--<v-list-item v-if="!noPlatform || noPlatform.length === 0">{{ $t('health.empty') }}</v-list-item>-->
                </v-list>
            </v-card>
        </v-col>
        <v-col cols="12" md="6">
            <v-card height="100%">
                <v-card-title>{{ $t('health.missingFileProjects') }}</v-card-title>
                <v-card-text>
                    <v-list dense>
                        <v-list-item v-for="project in missingFiles" :key="project.namespace.slug + project.namespace.owner">
                            <NuxtLink :to="'/' + project.namespace.owner + '/' + project.namespace.slug">
                                <strong>{{ project.namespace.owner + '/' + project.namespace.slug }}</strong>
                            </NuxtLink>
                        </v-list-item>
                        <v-list-item v-if="!missingFiles || missingFiles.length === 0">
                            <v-alert type="success" width="100%" dense>
                                {{ $t('health.empty') }}
                            </v-alert>
                        </v-list-item>
                    </v-list>
                </v-card-text>
            </v-card>
        </v-col>
    </v-row>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { Job, MissingFile, UnhealthyProject } from 'hangar-internal';
import { Context } from '@nuxt/types';
import { GlobalPermission } from '~/utils/perms';
import { NamedPermission } from '~/types/enums';

interface HealthReport {
    noTopicProjects: UnhealthyProject[];
    staleProjects: UnhealthyProject[];
    nonPublicProjects: UnhealthyProject[];
    missingFiles: MissingFile[];
    erroredJobs: Job[];
}

@Component
@GlobalPermission(NamedPermission.VIEW_HEALTH)
export default class AdminHealthPage extends Vue {
    noTopicProjects!: UnhealthyProject[];
    staleProjects!: UnhealthyProject[];
    nonPublicProjects!: UnhealthyProject[];
    missingFiles!: MissingFile[];
    erroredJobs!: Job[];
    // noPlatform: Array<Project> = [this.$util.dummyProject()];

    head() {
        return this.$seo.head(this.$t('health.title'), null, this.$route, null);
    }

    async asyncData({ $api, $util }: Context) {
        const data: HealthReport = await $api.requestInternal<HealthReport>('admin/health').catch<any>($util.handlePageRequestError);
        return { ...data };
    }
}
</script>

<style lang="scss" scoped></style>
