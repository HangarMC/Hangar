<template>
    <div class="version-list">
        <div class="row text-center">
            <div class="col-12">
                <a v-if="canUpload" class="btn yellow" :href="ROUTES.parse('VERSIONS_SHOW_CREATOR', projectOwner, projectSlug)"
                    >Upload a New Version</a
                >
            </div>
        </div>
        <div v-show="loading">
            <i class="fas fa-spinner fa-spin"></i>
            <span>Loading versions for you...</span>
        </div>
        <div v-show="!loading">
            <div class="list-group">
                <a
                    v-for="(version, index) in versions"
                    :href="ROUTES.parse('VERSIONS_SHOW', htmlDecode(projectOwner), htmlDecode(projectSlug), version.name)"
                    class="list-group-item list-group-item-action"
                    :class="[classForVisibility(version.visibility)]"
                    :key="index"
                >
                    <div class="container-fluid">
                        <div class="row">
                            <div
                                class="col-4 col-md-2 col-lg-2"
                                :set="(channel = version.tags.find((filterTag) => filterTag.name === 'Channel'))"
                            >
                                <div class="row">
                                    <div class="col-12">
                                        <span class="text-bold">{{ version.name }}</span>
                                    </div>
                                    <div class="col-12">
                                        <span v-if="channel" class="channel" v-bind:style="{ background: channel.color.background }">{{
                                            channel.data
                                        }}</span>
                                    </div>
                                </div>
                            </div>
                            <div class="col-8 col-md-6 col-lg-4">
                                <Tag
                                    v-for="tag in version.tags.filter((filterTag) => filterTag.name !== 'Channel')"
                                    v-bind:key="tag.name + ':' + tag.data"
                                    v-bind="tag"
                                ></Tag>
                            </div>
                            <div class="col-md-4 col-lg-3 d-none d-md-block">
                                <div class="row">
                                    <div class="col-12">
                                        <i class="fas fa-fw fa-calendar"></i>
                                        {{ formatDate(version.created_at) }}
                                    </div>
                                    <div class="col-12">
                                        <i class="far fa-fw fa-file"></i>
                                        <span v-if="version.file_info.size_bytes">
                                            {{ formatSize(version.file_info.size_bytes) }}
                                        </span>
                                        <span v-else> (external) </span>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-3 col-lg-3 d-none d-lg-block">
                                <div class="row">
                                    <div class="col-12">
                                        <i class="fas fa-fw fa-user-tag"></i>
                                        {{ version.author }}
                                    </div>
                                    <div class="col-12">
                                        <i class="fas fa-fw fa-download"></i>
                                        {{ version.stats.downloads }} Downloads
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </a>
            </div>
            <Pagination :current="current" :total="total" @prev="page--" @next="page++" @jumpTo="page = $event"></Pagination>
        </div>
    </div>
</template>

<script>
import fileSize from 'filesize';
import moment from 'moment';

import Tag from './components/Tag';
import Pagination from './components/Pagination';
import { Visibility } from './enums';
import { apiV2Request } from '@/js/apiRequests';

export default {
    components: {
        Tag,
        Pagination,
    },
    data() {
        return {
            page: 1,
            limit: 10,
            projectOwner: window.PROJECT_OWNER,
            projectSlug: window.PROJECT_SLUG,
            versions: [],
            totalVersions: 0,
            canUpload: false,
            loading: true,
            ROUTES: window.ROUTES,
        };
    },
    created() {
        this.update();
        apiV2Request('permissions', 'GET', { author: window.PROJECT_OWNER, slug: window.PROJECT_SLUG }).then((response) => {
            this.canUpload = response.permissions.includes('create_version');
        });
        this.$watch(
            () => this.page,
            () => {
                this.update();
                window.scrollTo(0, 0);
            }
        );
    },
    methods: {
        update() {
            apiV2Request('projects/' + this.projectOwner + '/' + this.projectSlug + '/versions', 'GET', {
                limit: this.limit,
                offset: this.offset,
            }).then((response) => {
                this.versions = response.result;
                this.totalVersions = response.pagination.count;
                this.loading = false;
            });
        },
        formatSize(size) {
            return fileSize(size);
        },
        formatDate(date) {
            return moment(date).format('MMM D, YYYY');
        },
        classForVisibility(visibility) {
            return Visibility.fromName(visibility).class;
        },
        htmlDecode(htmlEncoded) {
            const parser = new DOMParser();
            const dom = parser.parseFromString('<!doctype html><body>' + htmlEncoded, 'text/html');
            return dom.body.textContent;
        },
    },
    computed: {
        routes() {
            return window.jsRoutes.controllers.project;
        },
        offset() {
            return (this.page - 1) * this.limit;
        },
        current() {
            return Math.ceil(this.offset / this.limit) + 1;
        },
        total() {
            return Math.ceil(this.totalVersions / this.limit);
        },
    },
};
</script>

<style lang="scss">
.version-list {
    .list-group > .list-group-item > .container-fluid > .row {
        display: flex;
        align-items: center;
    }
    .btn {
        margin-bottom: 1rem;
    }
}
</style>
