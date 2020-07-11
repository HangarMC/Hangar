<template>
    <div>
        <div v-show="loading">
            <i class="fas fa-spinner fa-spin"></i>
            <span>Loading projects for you...</span>
        </div>
        <div v-show="!loading">
            <div v-if="projects.length > 0">
                <ul class="list-group project-list">
                    <li class="list-group-item project" v-for="project in projects" :class="visibilityFromName(project.visibility).class">
                        <div class="container-fluid">
                            <div class="row">
                                <div class="col-xs-12 col-sm-1">
                                    <Icon :name="project.namespace.owner" :src="project.icon_url"
                                          extra-classes="user-avatar-sm"></Icon>
                                </div>
                                <div class="col-xs-12 col-sm-11">
                                    <div class="row">
                                        <div class="col-sm-6">
                                            <a :href="routes.Projects.show(project.namespace.owner, project.namespace.slug).absoluteURL()"
                                               class="title">
                                                {{ project.name }}
                                            </a>
                                        </div>
                                        <div class="col-sm-6 hidden-xs">
                                            <div class="info minor">
                                                <span class="stat recommended-version" title="Recommended version" v-if="project.recommended_version">
                                                    <i class="far fa-gem"></i>
                                                    <a :href="routes.Versions.show(project.namespace.owner, project.namespace.slug, project.recommended_version.version).absoluteURL()">
                                                        {{ project.recommended_version.version }}
                                                    </a>
                                                </span>

                                                <span class="stat" title="Views"><i class="fas fa-eye"></i> {{ formatStats(project.stats.views) }}</span>
                                                <span class="stat" title="Download"><i class="fas fa-download"></i> {{ formatStats(project.stats.downloads) }}</span>
                                                <span class="stat" title="Stars"><i class="fas fa-star"></i> {{ formatStats(project.stats.stars) }}</span>

                                                <span :title="categoryFromId(project.category).name" class="stat">
                                                    <i :class="'fa-' + categoryFromId(project.category).icon" class="fas"></i>
                                                </span>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <div class="col-sm-7 description-column">
                                            <div class="description">{{ project.description }}</div>
                                        </div>
                                        <div class="col-xs-12 col-sm-5 tags-line" v-if="project.promoted_versions">
                                            <Tag v-bind:name="tag.name" v-bind:data="tag.versions.join(' | ')" v-bind:color="tag.color"
                                                 v-bind:key="project.name + '-' + tag.name" v-for="tag in tagsFromPromoted(project.promoted_versions)"></Tag>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </li>
                </ul>
                <Pagination :current="current" :total="total" @jumpTo="$emit('jumpToPage', $event)" @next="$emit('nextPage')"
                            @prev="$emit('prevPage')"></Pagination>
            </div>
            <div v-else class="list-group-item empty-project-list">
                <i class="far fa-2x fa-sad-tear"></i>
                <span>Oops! No projects found...</span>
            </div>
        </div>
    </div>
</template>

<script>
    import Tag from "./Tag"
    import {clearFromEmpty} from "./../utils"
    import {Category, Platform, Visibility} from "../enums";
    import Pagination from "./Pagination";
    import Icon from "./Icon"
    import debounce from "lodash/debounce"
    import {API} from "../api";

    export default {
        components: {
            Tag,
            Pagination,
            Icon
        },
        props: {
            q: String,
            categories: {
                type: Array
            },
            tags: Array,
            owner: String,
            sort: String,
            relevance: {
                type: Boolean,
                default: true
            },
            limit: {
                type: Number,
                default: 25
            },
            offset: {
                type: Number,
                default: 0
            }
        },
        data() {
            return {
                projects: [],
                totalProjects: 0,
                loading: true
            }
        },
        computed: {
            current: function () {
                return Math.ceil(this.offset / this.limit) + 1;
            },
            total: function () {
                return Math.ceil(this.totalProjects / this.limit)
            },
            routes: function () {
                return jsRoutes.controllers.project;
            }
        },
        created() {
            this.update();
            this.debouncedUpdateProps = debounce(this.update, 500);
            this.$watch(vm => [vm.q, vm.categories, vm.tags, vm.owner, vm.sort, vm.relevance, vm.limit, vm.offset].join(), () => {
                this.debouncedUpdateProps();
            });
        },
        methods: {
            update() {
                API.request("projects", "GET", clearFromEmpty(this.$props)).then((response) => {
                    this.projects = response.result;
                    this.totalProjects = response.pagination.count;
                    this.loading = false;
                    this.$emit('update:projectCount', this.totalProjects);
                });
            },
            categoryFromId(id) {
                return Category.fromId(id);
            },
            visibilityFromName(name) {
                return Visibility.fromName(name);
            },
            tagsFromPromoted(promotedVersions) {
                let tagsArray = [];
                promotedVersions
                    .map(version => version.tags)
                    .forEach(tags => tagsArray = tags.filter(tag => Platform.isPlatformTag(tag)).concat(tagsArray));

                const reducedTags = [];

                Platform.values.forEach(platform => {
                    let versions = [];
                    tagsArray.filter(tag => tag.name === platform.id).reverse().forEach(tag => {
                        versions.push(tag.display_data || tag.data);
                    });

                    if(versions.length > 0) {
                        reducedTags.push({
                            name: platform.id,
                            versions: versions,
                            color: platform.color
                        });
                    }
                });

                return reducedTags;
            },
            formatStats(number) {
                return numberWithCommas(number);
            }
        }
    }
</script>

<style lang="scss">
    @import "./../scss/variables";

    .empty-project-list {
        display: flex;
        align-items: center;

        span {
            margin-left: 0.5rem;
        }
    }
    .project-list {
        margin-bottom: 0;

        .row {
            display: flex;
            flex-wrap: nowrap;
        }

        @media (max-width: 767px) {
            .row {
                display: flex;
                flex-wrap: wrap;
            }
        }

        .project {
            padding: 10px 0;
            margin-bottom: 0.25rem;

            &:last-child {
                margin-bottom: 0;
            }
        }

        .title {
            font-size: 2rem;
            color: $sponge_grey;
            font-weight: bold;
        }

        .description-column {
            overflow: hidden;

            .description {
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
            }
        }

        .tags-line {
            display: flex;
            justify-content: flex-end;

            @media (max-width: 480px) {
                justify-content: flex-start;
                margin-top: 0.5rem;
            }

            .tags {
                margin-right: 0.5rem;
            }

            :last-child {
                margin-right: 0;
            }

            .tag {
                margin: 0;
            }
        }

        .info {
            display: flex;
            justify-content: flex-end;

            span {
                margin-right: 1.5rem;

                &:last-child {
                    margin-right: 0;
                }

                &.recommended-version a {
                    font-weight: bold;
                    color: #636363;
                }
            }
        }
    }
</style>
