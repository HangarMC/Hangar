<template>
    <div class="row">
        <div class="col-md-9">
            <div class="project-search" :class="{'input-group': q.length > 0}">
                <input type="text" class="form-control" v-model="q" @keydown="resetPage" :placeholder="queryPlaceholder" />
                <span class="input-group-btn" v-if="q.length > 0">
                    <button class="btn btn-default" type="button" @click="q = ''"><i class="fas fa-times"></i></button>
                </span>
            </div>
            <div v-if="!isDefault" class="clearSelection">
                <a @click="reset"><i class="fa fa-window-close"></i> Clear current search query, categories, platform, and sort</a>
            </div>
            <project-list v-bind="listBinding" ref="list" @prevPage="page--"
                          @nextPage="page++" @jumpToPage="page = $event" v-bind:projectCount.sync="projectCount"></project-list>
        </div>
        <div class="col-md-3">
            <select class="form-control select-sort" v-model="sort" @change="resetPage">
                <option v-for="option in availableOptions.sort" :value="option.id">{{ option.name }}</option>
            </select>

            <div>
                <input type="checkbox" id="relevanceBox" v-model="relevance">
                <label for="relevanceBox">Sort with relevance</label>
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h3 class="panel-title">Categories</h3>
                        <a class="category-reset" @click="categories = []" v-if="categories.length > 0">
                            <i class="fas fa-times white"></i>
                        </a>
                    </div>

                    <div class="list-group category-list">
                        <a v-for="category in availableOptions.category" class="list-group-item" @click="changeCategory(category)"
                           v-bind:class="{ active: categories.includes(category.id) }">
                            <i class="fas fa-fw" :class="'fa-' + category.icon"></i>
                            <strong>{{ category.name }}</strong>
                        </a>
                    </div>
                </div>
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h3 class="panel-title">Platforms</h3>
                    </div>

                    <div class="list-group platform-list">
                        <a class="list-group-item" @click="tags = []" v-bind:class="{ active: tags.length === 0 }">
                            <span class="parent">Any</span>
                        </a>
                        <a v-for="platform in availableOptions.platform" class="list-group-item" @click="tags = [platform.id]"
                           v-bind:class="{ active: tags.includes(platform.id) }">
                            <span :class="{parent: platform.parent}">{{ platform.name }}</span>
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    import ProjectList from "./components/ProjectList"
    import queryString from "query-string"
    import {clearFromDefaults} from "./utils"
    import {Category, Platform, SortOptions} from "./enums";
    import debounce from "lodash/debounce"

    function defaultData() {
        return {
            q: "",
            sort: "updated",
            relevance: true,
            categories: [],
            tags: [],
            page: 1,
            offset: 0,
            limit: 25,
            projectCount: null,
            availableOptions: {
                category: Category.values,
                platform: Platform.values,
                sort: SortOptions
            }
        };
    }

    export default {
        components: {
            ProjectList
        },
        data: defaultData,
        computed: {
            isDefault: function() {
                return Object.keys(clearFromDefaults(this.baseBinding, defaultData())).length === 0;
            },
            baseBinding: function () {
                return {
                    q: this.q,
                    sort: this.sort,
                    relevance: this.relevance,
                    categories: this.categories,
                    tags: this.tags
                }
            },
            listBinding: function () {
                return clearFromDefaults(Object.assign({}, this.baseBinding, {offset: (this.page - 1) * this.limit, limit: this.limit}), defaultData())
            },
            urlBinding: function () {
                return clearFromDefaults(Object.assign({}, this.baseBinding, {page: this.page}), defaultData())
            },
            queryPlaceholder: function () {
                return `Search in ${this.projectCount === null ? "all" : this.projectCount} projects` +
                    `${!this.isDefault ? " matching your filters" : ""}` +
                    ", proudly made by the community...";
            }
        },
        methods: {
            reset: function() {
                Object.entries(defaultData()).forEach(([key, value]) => this.$data[key] = value)
            },
            resetPage: function() {
                this.page = 1;
            },
            changeCategory: function(category) {
                if(this.categories.includes(category.id)) {
                    this.categories.splice(this.categories.indexOf(category.id), 1);
                } else if(this.categories.length + 1 === Category.values.length) {
                    this.categories = [];
                } else {
                    this.categories.push(category.id);
                }
            },
            updateQuery(newQuery) {
                window.history.pushState(null, null, newQuery !== "" ? "?" + newQuery : "/");
            },
            updateData() {
                Object.entries(queryString.parse(location.search, {arrayFormat: 'bracket', parseBooleans: true}))
                    .filter(([key, value]) => defaultData().hasOwnProperty(key))
                    .forEach(([key, value]) => this.$data[key] = value);
            }
        },
        created() {
            this.updateData();
            window.addEventListener('popstate', this.updateData);

            this.debouncedUpdateProps = debounce(this.updateQuery, 500);
            this.$watch(vm => [vm.q, vm.sort, vm.relevance, vm.categories, vm.tags, vm.page].join(), () => {
                const query = queryString.stringify(this.urlBinding, {arrayFormat: 'bracket'});
                this.debouncedUpdateProps(query);
            });
            this.$watch(vm => [vm.q, vm.sort, vm.relevance, vm.categories, vm.tags].join(), () => {
                this.resetPage();
            });
        },
        watch: {
            page: function () {
                window.scrollTo(0, 0);
            }
        }
    }
</script>

<style lang="scss">
    @import "./scss/variables";

    .select-sort {
        margin-bottom: 10px;
    }
    .category-reset {
        display: flex;
        cursor: pointer;
    }
    .category-list {
        a.list-group-item {
            svg {
                margin-right: 0.5rem;
            }

            &:hover {
                cursor: pointer;
                background-color: $mainBackground;
            }

            &.active {
                background: #FFFFFF;
                border-bottom: 1px solid #dddddd;
                border-top: 1px solid #dddddd;
                box-shadow: inset -10px 0px 0px 0px $sponge_yellow;
                color: unset;
            }
        }
    }
    .platform-list {
        .list-group-item {
            cursor: pointer;
        }
        .parent {
            font-weight: bold;
        }
    }
    .clearSelection {
        margin-bottom: 1rem;
        a {
            cursor: pointer;
            color: #586069;
        }
    }
</style>
