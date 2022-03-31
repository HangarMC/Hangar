<template>
    <button
        class="btn btn-primary btn-block"
        type="button"
        data-toggle="collapse"
        data-target="#dep-collapse"
        aria-expanded="false"
        aria-controls="dep-collapse"
    >
        Manage Platforms/Dependencies
    </button>
    <div id="dep-collapse" class="collapse">
        <div v-if="!loading" class="container-fluid">
            <template v-for="(platform, platformKey) in platformInfo" :key="platformKey">
                <div
                    :id="`${platformKey}-row`"
                    class="row platform-row align-items-center"
                    :style="{
                        backgroundColor: !platforms[platformKey] ? '#00000022' : platform.tag.background + '45',
                    }"
                >
                    <div class="platform-row-overlay" :style="{ zIndex: !platforms[platformKey] ? 5 : -10 }"></div>
                    <div class="col-1 d-flex align-items-center" style="z-index: 10">
                        <input
                            :id="`${platformKey}-is-enabled`"
                            type="checkbox"
                            :checked="platforms[platformKey]"
                            :disabled="freezePlatforms"
                            class="mr-2"
                            @change="selectPlatform(platformKey)"
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
                        <div>
                            <div class="row no-gutters">
                                <!--is there a better way of making two columns?-->
                                <div
                                    v-for="(versionList, index) in [
                                        platform.possibleVersions.slice(0, Math.ceil(platform.possibleVersions.length / 2)),
                                        platform.possibleVersions.slice(Math.ceil(platform.possibleVersions.length / 2)),
                                    ]"
                                    :key="index"
                                    class="col-6 text-right d-flex flex-column justify-content-start"
                                >
                                    <div v-for="version in versionList" :key="version">
                                        <label :for="`${platformKey}-version-${version}`">
                                            {{ version }}
                                        </label>
                                        <input
                                            :id="`${platformKey}-version-${version}`"
                                            type="checkbox"
                                            :value="version"
                                            v-model="platforms[platformKey]"
                                            class="mr-2"
                                            :disabled="!platforms[platformKey]"
                                        />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col-8 text-center">
                        <form :ref="`${platformKey.toLowerCase()}DepForm`" autocomplete="off">
                            <table
                                v-if="(dependencies[platformKey] && dependencies[platformKey].length) || !freezePlatforms"
                                class="table table-bordered table-dark dependency-table"
                            >
                                <tr>
                                    <th>Name</th>
                                    <th>Required</th>
                                    <th>Link</th>
                                </tr>
                                <tr v-for="dep in dependencies[platformKey]" :key="dep.name">
                                    <td class="text-center">
                                        <div class="w-100">
                                            {{ dep.name }}
                                        </div>

                                        <button v-if="!freezePlatforms" class="btn btn-danger" @click="removeDepFromTable(platformKey, dep.name)">
                                            <i class="fas fa-trash"></i>
                                        </button>
                                    </td>
                                    <td class="align-middle">
                                        <span :style="{ fontSize: '3rem', color: dep.required ? 'green' : 'red' }">
                                            <i class="fas" :class="`${dep.required ? 'fa-check-circle' : 'fa-times-circle'}`"></i>
                                        </span>
                                    </td>
                                    <td :id="`${platformKey}-${dep.name}-link-cell`">
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
                                            <ul :id="`${platformKey}-${dep.name}-project-dropdown`" class="search-dropdown list-group" style="display: none">
                                                <li
                                                    v-for="project in searchResults"
                                                    :key="`${project.namespace.owner}/${project.namespace.slug}`"
                                                    class="list-group-item list-group-item-dark"
                                                    @mousedown.prevent=""
                                                    @click="selectProject(platformKey, dep.name, project)"
                                                >
                                                    {{ project.namespace.owner }} /
                                                    {{ project.namespace.slug }}
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
                                                :id="`${platformKey}-${dep.name}-external-input`"
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
                                <tr v-if="!freezePlatforms">
                                    <td colspan="3">
                                        <div class="input-group">
                                            <input
                                                :id="`${platformKey}-new-dep`"
                                                type="text"
                                                placeholder="Dependency Name"
                                                class="form-control"
                                                aria-label="New Dependency Name"
                                                v-model="addDependency[platformKey].name"
                                            />
                                            <div class="input-group-append">
                                                <div class="input-group-text">
                                                    <input
                                                        :id="`${platformKey}-new-dep-required`"
                                                        class="mr-2"
                                                        type="checkbox"
                                                        aria-label="Dependency is required?"
                                                        v-model="addDependency[platformKey].required"
                                                    />
                                                    <label :for="`${platformKey}-new-dep-required`" style="position: relative; top: 1px"> Required? </label>
                                                </div>
                                                <button
                                                    :disabled="!addDependency[platformKey].name || addDependency[platformKey].name.trim().length === 0"
                                                    class="btn btn-info"
                                                    type="button"
                                                    @click="addDepToTable(platformKey)"
                                                >
                                                    Add
                                                </button>
                                            </div>
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
</template>
<script>
import { nextTick } from 'vue';
import $ from 'jquery';
import 'bootstrap/js/dist/collapse';
import { isEmpty, remove, union } from 'lodash-es';

import { API } from '@/api';

export default {
    name: 'DependencySelection',
    emits: ['update:platformsProp', 'update:dependenciesProp'],
    props: {
        platformsProp: Object,
        dependenciesProp: Object,
        prevVersion: Object,
    },
    data() {
        return {
            freezePlatforms: false,
            loading: true,
            dependencyLinking: {},
            searchResults: [],
            platformInfo: {},
            addDependency: {},
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
    async created() {
        const data = await API.request('platforms');
        for (const platformObj of data) {
            this.platformInfo[platformObj.name.toUpperCase()] = platformObj;
            this.dependencyLinking[platformObj.name.toUpperCase()] = {};
            this.addDependency[platformObj.name.toUpperCase()] = {
                name: '',
                required: false,
            };
        }
        this.loading = false;
        await nextTick();

        if (!isEmpty(this.platforms)) {
            this.freezePlatforms = true;
            for (const platformName in this.platforms) {
                if (this.prevVersion && this.prevVersion.platforms.find((plat) => plat.name === platformName)) {
                    this.platforms[platformName] = union(
                        this.platforms[platformName],
                        this.prevVersion.platforms.find((plat) => plat.name === platformName).versions
                    );
                }

                if (this.prevVersion && this.prevVersion.dependencies[platformName] && this.prevVersion.dependencies[platformName].length) {
                    this.setDependencyLinks(this.prevVersion.dependencies[platformName], platformName);
                }
            }
        } else {
            $('#dep-collapse').collapse('show');
            if (this.prevVersion) {
                for (const platformObj of this.prevVersion.platforms) {
                    const platformName = platformObj.name.toUpperCase();
                    this.platforms[platformName] = platformObj.versions;
                }
                for (const platform in this.prevVersion.dependencies) {
                    this.dependencies[platform.toUpperCase()] = this.prevVersion.dependencies[platform];
                }
                await nextTick();
                for (const platform in this.dependencies) {
                    this.setDependencyLinks(this.dependencies[platform], platform);
                }
            }
        }
    },
    methods: {
        setDependencyLinks(deps, platformName) {
            for (const dep of deps) {
                if (dep.project_id) {
                    this.dependencyLinking[platformName][dep.name] = 'Hangar';
                    API.request(`projects/${dep.project_id}`).then((res) => {
                        this.selectProject(platformName, dep.name, res);
                    });
                } else if (dep.external_url) {
                    $(`#${platformName}-${dep.name}-external-input`).val(dep.external_url);
                    this.dependencyLinking[platformName][dep.name] = 'External';
                    this.setExternalUrl(dep.external_url, platformName, dep.name);
                }
            }
        },
        isEmpty,
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
            let namespace = '';
            if (project.namespace) {
                namespace = `${project.namespace.owner} / ${project.namespace.slug}`;
            } else {
                namespace = `${project.author} / ${project.slug}`;
            }
            input.data('id', project.id);
            input.data('namespace', namespace);
            this.dependencies[platformKey].find((dep) => dep.name === depName).project_id = project.id;
            input.val(namespace);
        },
        setExternalUrl(value, platformKey, depName) {
            this.dependencies[platformKey].find((dep) => dep.name === depName).external_url = value;
        },
        selectPlatform(platformKey) {
            if (!this.platforms[platformKey]) {
                this.platforms[platformKey] = [];
                this.dependencies[platformKey] = [];
            } else {
                delete this.platforms[platformKey];
                delete this.dependencies[platformKey];
            }
        },
        addDepToTable(platformKey) {
            if (!this.dependencies[platformKey]) {
                this.dependencies[platformKey] = [];
            }
            this.dependencies[platformKey].push({
                name: this.addDependency[platformKey].name,
                required: this.addDependency[platformKey].required,
                project_id: null,
                external_url: null,
            });
            this.addDependency[platformKey].name = '';
            this.addDependency[platformKey].required = false;
        },
        removeDepFromTable(platformKey, depName) {
            remove(this.dependencies[platformKey], (dep) => dep.name === depName);
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

.platform-row-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.6);
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

.input-group input[type='text'] {
    width: unset;
}
</style>
