<template>
    <v-card>
        <v-card-title>{{ $t('health.title') }}</v-card-title>
        <v-card-text>
            <v-row>
                <v-list dense class="col col-12 col-md-6">
                    <v-subheader>{{ $t('health.noTopicProject') }}</v-subheader>
                    <v-list-item v-for="project in noTopicProject" :key="project.namespace.slug + project.namespace.owner">
                        <NuxtLink :to="'/' + project.namespace.owner + '/' + project.namespace.slug">
                            <strong>{{ project.namespace.owner + '/' + project.namespace.slug }}</strong>
                        </NuxtLink>
                    </v-list-item>
                    <v-list-item v-if="!noTopicProject || noTopicProject.length === 0">{{ $t('health.empty') }}</v-list-item>
                </v-list>
                <v-list dense class="col col-12 col-md-6">
                    <v-subheader>{{ $t('health.erroredJobs') }}</v-subheader>
                    <v-list-item v-for="job in erroredJobs" :key="job.jobType + job.lastUpdated.toISOString()">
                        {{ $t('health.jobText', [job.jobType, job.lastErrorDescriptor, $util.prettyDate(job.lastUpdated)]) }}
                    </v-list-item>
                    <v-list-item v-if="!erroredJobs || erroredJobs.length === 0">{{ $t('health.empty') }}</v-list-item>
                </v-list>
                <v-list dense class="col col-12 col-md-6">
                    <v-subheader>{{ $t('health.staleProjects') }}</v-subheader>
                    <v-list-item v-for="project in staleProjects" :key="project.namespace.slug + project.namespace.owner">
                        <NuxtLink :to="'/' + project.namespace.owner + '/' + project.namespace.slug">
                            <strong>{{ project.namespace.owner + '/' + project.namespace.slug }}</strong>
                        </NuxtLink>
                    </v-list-item>
                    <v-list-item v-if="!staleProjects || staleProjects.length === 0">{{ $t('health.empty') }}</v-list-item>
                </v-list>
                <v-list dense class="col col-12 col-md-6">
                    <v-subheader>{{ $t('health.notPublicProjects') }}</v-subheader>
                    <v-list-item v-for="project in notPublicProjects" :key="project.namespace.slug + project.namespace.owner">
                        <NuxtLink :to="'/' + project.namespace.owner + '/' + project.namespace.slug">
                            <strong>{{ project.namespace.owner + '/' + project.namespace.slug }}</strong>
                            <small>{{ $t('visibility.name.' + project.visibility) }}</small>
                        </NuxtLink>
                    </v-list-item>
                    <v-list-item v-if="!notPublicProjects || notPublicProjects.length === 0">{{ $t('health.empty') }}</v-list-item>
                </v-list>
                <v-list dense class="col col-12 col-md-6">
                    <v-subheader>{{ $t('health.noPlatform') }}</v-subheader>
                    <v-list-item>ToDo: implement me</v-list-item>
                    <v-list-item v-if="!noPlatform || noPlatform.length === 0">{{ $t('health.empty') }}</v-list-item>
                </v-list>
                <v-list dense class="col col-12 col-md-6">
                    <v-subheader>{{ $t('health.missingFileProjects') }}</v-subheader>
                    <v-list-item v-for="project in missingFileProjects" :key="project.namespace.slug + project.namespace.owner">
                        <NuxtLink :to="'/' + project.namespace.owner + '/' + project.namespace.slug">
                            <strong>{{ project.namespace.owner + '/' + project.namespace.slug }}</strong>
                        </NuxtLink>
                    </v-list-item>
                    <v-list-item v-if="!missingFileProjects || missingFileProjects.length === 0">{{ $t('health.empty') }}</v-list-item>
                </v-list>
            </v-row>
        </v-card-text>
    </v-card>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { Job, Project } from 'hangar-api';

@Component
export default class AdminHealthPage extends Vue {
    // TODO get these from the server
    noTopicProject: Array<Project> = [this.$util.dummyProject()];
    erroredJobs: Array<Job> = [({ jobType: 'Test', lastErrorDescriptor: 'Dum', lastUpdated: new Date() } as unknown) as Job];
    staleProjects: Array<Project> = [this.$util.dummyProject()];
    notPublicProjects: Array<Project> = [this.$util.dummyProject()];
    noPlatform: Array<Project> = [this.$util.dummyProject()];
    missingFileProjects: Array<Project> = [this.$util.dummyProject()];
}
</script>

<style lang="scss" scoped></style>
