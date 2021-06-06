<template>
    <div>
        <v-simple-table :key="`${platform}-deps-table`" class="ma-2">
            <thead>
                <tr>
                    <th>{{ $t('general.name') }}</th>
                    <th>{{ $t('general.required') }}</th>
                    <th>{{ $t('general.link') }}</th>
                    <th v-if="!noEditing">
                        {{ $t('general.delete') }}
                    </th>
                </tr>
            </thead>
            <tbody>
                <template v-if="!isNew">
                    <tr v-for="dep in version.pluginDependencies[platform]" :key="`${platform}-${dep.name}`">
                        <td>{{ dep.name }}</td>
                        <td><v-simple-checkbox v-model="dep.required" :ripple="false" /></td>
                        <td>
                            <v-text-field
                                v-model.trim="dep.externalUrl"
                                dense
                                hide-details
                                :placeholder="$t('version.new.form.externalUrl')"
                                :disabled="dep.namespace !== null && Object.keys(dep.namespace).length !== 0"
                                :rules="
                                    dep.namespace !== null && Object.keys(dep.namespace).length !== 0 ? [] : [$util.$vc.require('version.new.form.externalUrl')]
                                "
                                clearable
                            />
                            <v-autocomplete
                                v-model="dep.namespace"
                                dense
                                hide-details
                                hide-no-data
                                :placeholder="$t('version.new.form.hangarProject')"
                                class="mb-2"
                                :items="results[dep.name]"
                                :item-text="getNamespace"
                                :item-value="getNamespace"
                                return-object
                                clearable
                                auto-select-first
                                :disabled="!!dep.externalUrl"
                                :rules="!!dep.externalUrl ? [] : [$util.$vc.require('version.new.form.hangarProject')]"
                                @update:search-input="onSearch($event, dep.name)"
                            />
                        </td>
                        <td v-if="!noEditing">
                            <v-btn icon color="error" @click="deleteDep(dep.name)">
                                <v-icon>mdi-delete</v-icon>
                            </v-btn>
                        </td>
                    </tr>
                </template>

                <template v-if="!noEditing || isNew">
                    <tr v-for="(newDep, index) in newDeps" :key="`newDep-${index}`">
                        <td>
                            <v-text-field
                                v-model.trim="newDep.name"
                                dense
                                hide-details
                                flat
                                :label="$t('general.name')"
                                :rules="[$util.$vc.require($t('general.name'))]"
                                :disabled="noEditing"
                            />
                        </td>
                        <td><v-simple-checkbox v-model="newDep.required" :ripple="false" /></td>
                        <td>
                            <v-text-field
                                v-model.trim="newDep.externalUrl"
                                dense
                                hide-details
                                :placeholder="$t('version.new.form.externalUrl')"
                                :disabled="newDep.namespace !== null && Object.keys(newDep.namespace).length !== 0"
                                :rules="
                                    newDep.namespace !== null && Object.keys(newDep.namespace).length !== 0
                                        ? []
                                        : [$util.$vc.require('version.new.form.externalUrl')]
                                "
                                clearable
                            />
                            <v-autocomplete
                                v-model="newDep.namespace"
                                dense
                                hide-details
                                hide-no-data
                                :placeholder="$t('version.new.form.hangarProject')"
                                class="mb-2"
                                :items="newDepResults[index]"
                                :item-text="getNamespace"
                                :item-value="getNamespace"
                                return-object
                                clearable
                                auto-select-first
                                :disabled="!!newDep.externalUrl"
                                :rules="!!newDep.externalUrl ? [] : [$util.$vc.require('version.new.form.hangarProject')]"
                                @update:search-input="onNewDepSearch($event, index)"
                            />
                        </td>
                        <td v-if="!noEditing">
                            <v-btn icon color="error" @click="deleteNewDep(index)">
                                <v-icon>mdi-delete</v-icon>
                            </v-btn>
                        </td>
                    </tr>
                </template>
            </tbody>
        </v-simple-table>
        <div v-if="!noEditing" class="ma-2">
            <v-btn color="primary" block @click="addNewDep">
                <v-icon left> mdi-plus </v-icon>
                {{ $t('general.add') }}
            </v-btn>
        </div>
    </div>
</template>

<script lang="ts">
import { Component, Prop, PropSync, Vue, Watch } from 'nuxt-property-decorator';
import { PropType } from 'vue';
import { DependencyVersion, PaginatedResult, PluginDependency, Project, ProjectNamespace } from 'hangar-api';
import { Platform } from '~/types/enums';

@Component
export default class DependencyTable extends Vue {
    results: Record<string, ProjectNamespace[]> = {};
    newDepResults: ProjectNamespace[][] = [];

    @Prop({ type: Object as PropType<DependencyVersion>, required: true })
    version!: DependencyVersion;

    @Prop({ type: String as PropType<Platform>, required: true })
    platform!: Platform;

    @Prop({ type: Boolean, default: false })
    noEditing!: boolean;

    @Prop({ type: Boolean, default: false })
    isNew!: boolean;

    @PropSync('newDepsProp', { type: Array as PropType<Partial<PluginDependency>[]>, default: () => [] })
    newDeps!: Partial<PluginDependency>[];

    addNewDep() {
        this.newDeps.push({
            name: undefined,
            required: false,
            namespace: null,
            externalUrl: null,
        });
        this.newDepResults.push([]);
    }

    getNamespace(namespace: ProjectNamespace) {
        return `${namespace.owner}/${namespace.slug}`;
    }

    onSearch(val: string, name: string) {
        if (val) {
            this.$api
                .request<PaginatedResult<Project>>(`projects?relevance=true&limit=25&offset=0&q=${val}`)
                .then((projects) => {
                    if (!this.results[name]) {
                        this.$set(this.results, name, []);
                    }
                    this.results[name].push(
                        ...projects.result
                            .filter((p) => p.namespace.owner !== this.$route.params.author || p.namespace.slug !== this.$route.params.slug)
                            .map((p) => p.namespace)
                    );
                })
                .catch(console.error);
        }
    }

    deleteDep(name: string) {
        this.$delete(
            this.version.pluginDependencies[this.platform],
            this.version.pluginDependencies[this.platform].findIndex((pd) => pd.name === name)
        );
    }

    deleteNewDep(index: number) {
        this.$delete(this.newDeps, index);
    }

    onNewDepSearch(val: string, index: number) {
        if (val) {
            this.$api
                .request<PaginatedResult<Project>>(`projects?relevance=true&limit=25&offset=0&q=${val}`)
                .then((projects) => {
                    this.newDepResults[index].push(...projects.result.map((p) => p.namespace));
                })
                .catch(console.error);
        }
    }

    @Watch('newDepResults', { deep: true })
    onFormChange() {
        this.$emit('change');
    }

    @Watch('version', { deep: true })
    onOtherFormChange() {
        this.$emit('change');
    }

    reset() {
        this.newDeps = [];
        this.newDepResults = [];
        this.results = {};
        if (this.version.pluginDependencies[this.platform]) {
            for (const dep of this.version.pluginDependencies[this.platform]) {
                if (dep.namespace) {
                    this.results[dep.name] = [dep.namespace];
                } else {
                    this.results[dep.name] = [];
                }
            }
        }
    }
}
</script>

<style lang="scss" scoped></style>
