<template>
    <div class="version-list">
        <div class="row text-center">
            <div class="col-xs-12">
                <a v-if="canUpload" class="btn yellow" :href="routes.Versions.showCreator(htmlDecode(projectOwner), htmlDecode(projectSlug)).absoluteURL()">Upload a New Version</a>
            </div>
        </div>
        <div v-show="loading">
            <i class="fas fa-spinner fa-spin"></i>
            <span>Loading versions for you...</span>
        </div>
        <div v-show="!loading">
            <div class="list-group">
                <a v-for="version in versions" :href="routes.Versions.show(htmlDecode(projectOwner), htmlDecode(projectSlug), version.name).absoluteURL()" class="list-group-item"
                   :class="[classForVisibility(version.visibility)]">
                    <div class="container-fluid">
                        <div class="row">
                            <div class="col-xs-6 col-sm-3" :set="channel = version.tags.find(filterTag => filterTag.name === 'Channel')">
                                <div class="row">
                                    <div class="col-xs-12">
                                        <span class="text-bold">{{ version.name }}</span>
                                    </div>
                                    <div class="col-xs-12">
                                        <span class="channel" v-bind:style="{ background: channel.color.background }">{{ channel.data }}</span>
                                    </div>
                                </div>
                            </div>
                            <div class="col-xs-6 col-sm-3">
                                <Tag v-for="tag in version.tags.filter(filterTag => filterTag.name !== 'Channel')"
                                     v-bind:key="tag.name + ':' + tag.data" v-bind="tag"></Tag>
                            </div>
                            <div class="col-xs-3 hidden-xs">
                                <div class="row">
                                    <div class="col-xs-12">
                                        <i class="fas fa-fw fa-calendar"></i>
                                        {{ formatDate(version.created_at) }}
                                    </div>
                                    <div class="col-xs-12">
                                        <i class="far fa-fw fa-file"></i>
                                        {{ formatSize(version.file_info.size_bytes) }}
                                    </div>
                                </div>
                            </div>
                            <div class="col-xs-3 hidden-xs">
                                <div class="row">
                                    <div class="col-xs-12">
                                        <i class="fas fa-fw fa-user-tag"></i>
                                        {{ version.author }}
                                    </div>
                                    <div class="col-xs-12">
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
    import Tag from "./components/Tag";
    import Pagination from "./components/Pagination";
    import {Visibility} from "./enums";

    export default {
        components: {
            Tag,
            Pagination
        },
        data() {
            return {
                page: 1,
                limit: 10,
                pluginId: window.PLUGIN_ID,
                projectOwner: window.PROJECT_OWNER,
                projectSlug: window.PROJECT_SLUG,
                versions: [],
                totalVersions: 0,
                canUpload: false,
                loading: true
            }
        },
        created() {
            this.update();
            apiV2Request("permissions", "GET", {pluginId: window.PLUGIN_ID}).then((response) => {
               this.canUpload = response.permissions.includes("create_version")
            });
            this.$watch(vm => vm.page, () => {
                this.update();
                window.scrollTo(0,0);
            });
        },
        methods: {
            update() {
                apiV2Request("projects/" + this.pluginId + "/versions", "GET", { limit: this.limit, offset: this.offset}).then((response) => {
                    this.versions = response.result;
                    this.totalVersions = response.pagination.count;
                    this.loading = false;
                });
            },
            formatSize(size) {
                return window.filesize(size);
            },
            formatDate(date) {
                return window.moment(date).format("MMM D, YYYY")
            },
            classForVisibility(visibility) {
                return Visibility.fromName(visibility).class;
            },
            htmlDecode(htmlEncoded) {
              const parser = new DOMParser;
              const dom = parser.parseFromString(
                '<!doctype html><body>' + htmlEncoded,
                'text/html');
              return dom.body.textContent;
            }
        },
        computed: {
            routes() {
                return jsRoutes.controllers.project;
            },
            offset() {
                return (this.page - 1) * this.limit
            },
            current() {
                return Math.ceil(this.offset / this.limit) + 1;
            },
            total() {
                return Math.ceil(this.totalVersions / this.limit)
            }
        }
    }
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