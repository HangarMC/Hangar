<template>
    <div>
        <table class="table table-bordered">
            <thead class="thead-dark">
                <tr>
                    <th scope="col">Platform</th>
                    <th scope="col"><i class="fas fa-tags"></i>Versions</th>
                    <th scope="col"><i class="fas fa-plus"></i>Add Version</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="{ platform, versions } in data" :key="`${platform}-row`">
                    <td>{{ platform }}</td>
                    <td>
                        <div class="platform-version" v-for="(v, index) in versions" :key="index">
                            <span @click="removeVersion(versions, v)" style="cursor:pointer;">
                                <i class="fas fa-times" style="color: #bb0400"></i>
                            </span>
                            {{ v }}
                        </div>
                    </td>
                    <td>
                        <div class="input-group float-left">
                            <div class="input-group-prepend">
                                <span class="input-group-text">Version Identifier</span>
                            </div>
                            <label for="add-version-input" class="sr-only">Add Version</label>
                            <input type="text" id="add-version-input" class="form-control" v-model="inputs[platform]" />
                            <div class="input-group-append">
                                <button
                                    type="button"
                                    class="btn btn-primary"
                                    @click="addVersion(platform)"
                                    :disabled="!inputs[platform]"
                                >
                                    <i class="fas fa-plus"></i>
                                </button>
                            </div>
                        </div>
                    </td>
                </tr>
            </tbody>
        </table>
        <button v-if="!loading" type="button" class="btn btn-success" @click="save" :disabled="!changesMade">
            <span class="glyphicon glyphicon-floppy-disk"></span>
            Save Changes
        </button>
        <div v-else>
            <i class="fas fa-spinner fa-spin"></i>
            <span>Loading projects for you...</span>
        </div>
    </div>
</template>
<script>
import _ from 'lodash';
import axios from 'axios';

export default {
    name: 'PlatformVersionTable',
    props: {
        platforms: {
            type: Object,
            default: () => {}
        }
    },
    data() {
        return {
            loading: false,
            changesMade: false,
            data: [],
            inputs: {}
        };
    },
    methods: {
        addVersion(platform) {
            const versions = this.data.find(o => o.platform === platform).versions;
            if (versions.indexOf(this.inputs[platform]) < 0) {
                versions.push(this.inputs[platform]);
                this.changesMade = true;
            }
            this.inputs[platform] = '';
        },
        removeVersion(versions, version) {
            _.remove(versions, v => v === version);
            this.changesMade = true;
        },
        save() {
            const additions = {};
            const removals = {};
            for (const platform in this.platforms) {
                const versions = this.data.find(o => o.platform === platform.toLowerCase()).versions;
                additions[platform] = _.difference(versions, this.platforms[platform]);
                removals[platform] = _.difference(this.platforms[platform], versions);
            }
            let hasChanges = false;
            for (const platform in this.platforms) {
                if (additions[platform].length > 0 || removals[platform].length > 0) {
                    hasChanges = true;
                    break;
                }
            }
            if (!hasChanges) {
                // NO actual changes
                return;
            }
            this.loading = true;
            axios
                .post('/admin/versions/', {
                    additions,
                    removals
                })
                .then(() => {
                    this.changesMade = false;
                })
                .finally(() => {
                    this.loading = false;
                });
        }
    },
    created() {
        for (const platform in this.platforms) {
            this.inputs[platform] = '';
            this.data.push({
                platform: platform.toLowerCase(),
                versions: [...this.platforms[platform]]
            });
        }
    }
};
</script>
<style lang="scss" scoped>
.table {
    th svg {
        margin-right: 4px;
    }

    tr th:first-child,
    tr td:first-child {
        width: 40px;
    }

    .platform-version {
        display: inline-block;
        padding: 1px 4px 0 4px;
        margin-right: 5px;
        background-color: #cdcdcd;
        border-radius: 2px;
    }
}
</style>
