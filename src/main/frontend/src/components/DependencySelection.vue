<template>
    <tr>
        <td colspan="2">
            <div id="dependencies-accordion">
                <div class="card bg-light">
                    <div class="card-header" id="dep-heading">
                        <button
                            class="btn btn-lg btn-block btn-primary"
                            data-toggle="collapse"
                            data-target="#dep-collapse"
                            aria-expanded="true"
                            aria-controls="dep-collapse"
                        >
                            Manage Platforms/Dependencies
                        </button>
                    </div>

                    <div id="dep-collapse" class="collapse" aria-labelledby="dep-heading" data-parent="#dependencies-accordion">
                        <div v-if="!loading" class="card-body">
                            <template v-for="(platform, platformKey) in platforms" :key="platformKey">
                                <!--<div
                                    class="row platform-header-row"
                                    :style="{ backgroundColor: `${platform.tag.background}22` }"
                                >
                                    <div class="col-12 text-center">
                                        &lt;!&ndash;<div v-if="platform.tag" class="tags">
                                            <span
                                                :style="{
                                                    color: platform.tag.foreground,
                                                    backgroundColor: platform.tag.background,
                                                    borderColor: platform.tag.background,
                                                }"
                                                class="tag"
                                            >
                                                {{ platform.name }}
                                            </span>
                                        </div>&ndash;&gt;
                                    </div>
                                </div>-->
                                <div
                                    class="row platform-row align-items-center"
                                    :style="{ backgroundColor: `${platform.tag.background}22` }"
                                >
                                    <div class="col-1 d-flex align-items-center">
                                        <input
                                            :id="`${platformKey}-is-enabled`"
                                            type="checkbox"
                                            v-model="platformsSelected"
                                            :value="platformKey"
                                            :disabled="freezePlatforms"
                                            class="mr-2"
                                        />
                                        <label :for="`${platformKey}-is-enabled`">
                                            <span
                                                class="platform-header p-1 rounded"
                                                :style="{
                                                    color: platform.tag.foreground,
                                                    backgroundColor: platform.tag.background,
                                                    borderColor: platform.tag.background,
                                                }"
                                            >
                                                {{ platform.name }}
                                            </span>
                                        </label>
                                    </div>
                                    <div class="col-3">
                                        <div class="clearfix"></div>
                                        <div v-if="!isEmpty(platforms)">
                                            <div class="row no-gutters">
                                                <div class="col-6 text-right">
                                                    <div
                                                        v-for="version in platform.possibleVersions.slice(
                                                            0,
                                                            Math.ceil(platform.possibleVersions.length / 2)
                                                        )"
                                                        :key="version"
                                                    >
                                                        <label :for="`${platformKey}-version-${version}`">
                                                            {{ version }}
                                                        </label>
                                                        <input
                                                            :id="`${platformKey}-version-${version}`"
                                                            type="checkbox"
                                                            :value="version"
                                                            v-model="versions[platformKey]"
                                                            class="mr-2"
                                                            :disabled="platformsSelected.indexOf(platformKey) === -1"
                                                            @change="versionClick(platformKey)"
                                                        />
                                                        <!--                    <template v-if="(index + 1) % 5 === 0" v-html="</div><div>"> </template>-->
                                                    </div>
                                                </div>
                                                <div class="col-6 text-right">
                                                    <div
                                                        v-for="version in platform.possibleVersions.slice(
                                                            Math.ceil(platform.possibleVersions.length / 2)
                                                        )"
                                                        :key="version"
                                                    >
                                                        <label :for="`${platformKey}-version-${version}`">
                                                            {{ version }}
                                                        </label>
                                                        <input
                                                            :id="`${platformKey}-version-${version}`"
                                                            type="checkbox"
                                                            :value="version"
                                                            v-model="versions[platformKey]"
                                                            class="mr-2"
                                                            :disabled="platformsSelected.indexOf(platformKey) === -1"
                                                            @change="versionClick(platformKey)"
                                                        />
                                                        <!--                    <template v-if="(index + 1) % 5 === 0" v-html="</div><div>"> </template>-->
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-8 text-center">
                                        <form :ref="`${platformKey.toLowerCase()}DepForm`" autocomplete="off">
                                            <table
                                                v-if="dependencies[platformKey] && dependencies[platformKey].length"
                                                class="table table-bordered table-dark dependency-table"
                                            >
                                                <tr>
                                                    <th>Name</th>
                                                    <th>Required</th>
                                                    <th>Link</th>
                                                </tr>
                                                <tr v-for="dep in dependencies[platformKey]" :key="dep.name">
                                                    <td class="text-left">{{ dep.name }}</td>
                                                    <td class="align-middle">
                                                        <span :style="{ fontSize: '3rem', color: dep.required ? 'green' : 'red' }">
                                                            <i
                                                                class="fas"
                                                                :class="`${dep.required ? 'fa-check-circle' : 'fa-times-circle'}`"
                                                            ></i>
                                                        </span>
                                                    </td>
                                                    <td>
                                                        <div class="input-group">
                                                            <div class="input-group-prepend">
                                                                <div class="input-group-text">
                                                                    <input
                                                                        type="radio"
                                                                        value="Hangar"
                                                                        aria-label="Select Hangar Project"
                                                                        v-model="dependencyLinking[platformKey][dep.name]"
                                                                        @change="linkingClick('external_url', platformKey, dep.name)"
                                                                    />
                                                                </div>
                                                            </div>
                                                            <input
                                                                :id="`${platformKey}-${dep.name}-project-input`"
                                                                type="text"
                                                                class="form-control"
                                                                aria-label="Hangar Project Name"
                                                                placeholder="Search for project..."
                                                                :required="dependencyLinking[platformKey][dep.name] !== 'External'"
                                                                :disabled="dependencyLinking[platformKey][dep.name] !== 'Hangar'"
                                                                @focus="toggleFocus(platformKey, dep.name, true)"
                                                                @blur="toggleFocus(platformKey, dep.name, false)"
                                                                @input="projectSearch($event.target, platformKey, dep.name)"
                                                            />
                                                            <ul
                                                                :id="`${platformKey}-${dep.name}-project-dropdown`"
                                                                class="search-dropdown list-group"
                                                                style="display: none"
                                                            >
                                                                <li
                                                                    v-for="project in searchResults"
                                                                    :key="`${project.namespace.owner}/${project.namespace.slug}`"
                                                                    class="list-group-item list-group-item-dark"
                                                                    @mousedown.prevent=""
                                                                    @click="selectProject(platformKey, dep.name, project)"
                                                                >
                                                                    {{ project.namespace.owner }} / {{ project.namespace.slug }}
                                                                </li>
                                                            </ul>
                                                        </div>
                                                        <div class="input-group mt-1">
                                                            <div class="input-group-prepend">
                                                                <div class="input-group-text">
                                                                    <input
                                                                        type="radio"
                                                                        value="External"
                                                                        aria-label="External Link"
                                                                        v-model="dependencyLinking[platformKey][dep.name]"
                                                                        @change="linkingClick('project_id', platformKey, dep.name)"
                                                                    />
                                                                </div>
                                                            </div>
                                                            <input
                                                                type="text"
                                                                class="form-control"
                                                                aria-label="Hangar Project Name"
                                                                placeholder="Paste an external link"
                                                                :required="dependencyLinking[platformKey][dep.name] !== 'Hangar'"
                                                                :disabled="dependencyLinking[platformKey][dep.name] !== 'External'"
                                                                @change="setExternalUrl($event.target.value, platformKey, dep.name)"
                                                            />
                                                        </div>
                                                    </td>
                                                </tr>
                                            </table>
                                            <i v-else class="dark-gray">No dependencies</i>
                                        </form>
                                    </div>
                                </div>
                            </template>
                        </div>
                    </div>
                </div>
            </div>
        </td>
    </tr>
</template>
<script>
import axios from 'axios';
import $ from 'jquery';
import { isEmpty } from 'lodash-es';

import { API } from '@/api';

export default {
    name: 'DependencySelection',
    emits: ['update:platformsProp', 'update:dependenciesProp'],
    props: {
        platformsProp: Object,
        dependenciesProp: Object,
        tags: Array,
    },
    data() {
        return {
            platformsSelected: [],
            freezePlatforms: false,
            versions: {},
            loading: true,
            dependencyLinking: {},
            searchResults: [],
        };
    },
    computed: {
        platforms: {
            get() {
                return this.platformsProp;
            },
            set(val) {
                this.$emit('update:platformsProp', val);
            },
        },
        dependencies: {
            get() {
                return this.dependenciesProp;
            },
            set() {
                this.$emit('update:dependenciesProp');
            },
        },
    },
    created() {
        if (this.platforms) {
            this.platformsSelected = Object.keys(this.platforms);
            this.freezePlatforms = true;
        }
        axios.get('/api/v1/platforms').then((res) => {
            for (const platform of res.data) {
                const versions = this.platforms[platform.name.toUpperCase()];
                this.platforms[platform.name.toUpperCase()] = { ...platform, versions };
                this.versions[platform.name.toUpperCase()] = versions;
                this.dependencyLinking[platform.name.toUpperCase()] = {};
            }
            this.loading = false;
        });
    },
    methods: {
        isEmpty,
        versionClick(platformKey) {
            this.platforms[platformKey].versions = this.versions[platformKey];
        },
        linkingClick(toReset, platformKey, depName) {
            this.dependencies[platformKey].find((dep) => dep.name === depName)[toReset] = null;
        },
        toggleFocus(platformKey, depName, showDropdown) {
            if (showDropdown) {
                $(`#${platformKey}-${depName}-project-dropdown`).show();
            } else {
                $(`#${platformKey}-${depName}-project-dropdown`).hide();
            }
        },
        projectSearch(target, platformKey, depName) {
            if (target.value) {
                const inputVal = target.value.trim();
                const input = $(target);
                if (input.data('namespace') !== inputVal) {
                    // reset on changing
                    input.data('namespace', '');
                    input.data('id', '');
                    this.dependencies[platformKey].find((dep) => dep.name === depName).project_id = null;
                }

                API.request(`projects?relevance=true&limit=25&offset=0&q=${inputVal}`).then((res) => {
                    if (res.result.length) {
                        $(`#${platformKey}-${depName}-project-dropdown`).show();
                        this.searchResults = res.result;
                    } else {
                        $(`#${platformKey}-${depName}-project-dropdown`).hide();
                        this.searchResults = [];
                    }
                });
            } else {
                $(target).parent().find('.search-dropdown').hide();
            }
        },
        selectProject(platformKey, depName, project) {
            $(`#${platformKey}-${depName}-project-dropdown`).hide();
            const input = $(`#${platformKey}-${depName}-project-input`);
            const namespace = `${project.namespace.owner} / ${project.namespace.slug}`;
            input.data('id', project.id);
            input.data('namespace', namespace);
            this.dependencies[platformKey].find((dep) => dep.name === depName).project_id = project.id;
            input.val(namespace);
        },
        setExternalUrl(value, platformKey, depName) {
            this.dependencies[platformKey].find((dep) => dep.name === depName).external_url = value;
        },
    },
};
</script>
<style lang="scss" scoped>
.search-dropdown {
    position: absolute;
    top: 100%;
    max-height: 300px;
    z-index: 100;
    width: 100%;

    li {
        cursor: pointer;
    }
}

label {
    margin-bottom: 0;
}

.platform-row:not(:first-of-type) {
    margin-top: 10px;
}

.platform-row {
    position: relative;
    padding-top: 10px;
    padding-bottom: 10px;
}

.platform-select-div {
    position: absolute;
    left: 0;
}

.platform-header {
    border: #aaa 1px solid;
    box-shadow: #2a2a2a;
    font-size: 1.1em;
}

.dependency-table {
    margin-bottom: 0;
}

.input-group > input[type='text'].form-control {
    width: 1%;
}
</style>
