<template>
    <v-dialog v-model="dialog" max-width="700">
        <template #activator="{ on: dialogOn, attrs }">
            <v-tooltip bottom>
                <template #activator="{ on: tooltipOn }">
                    <v-btn small icon color="warning" :class="activatorClass" v-bind="attrs" v-on="{ ...dialogOn, ...tooltipOn }">
                        <v-icon small> mdi-pencil </v-icon>
                    </v-btn>
                </template>
                <span>{{ $t('general.edit') }}</span>
            </v-tooltip>
        </template>
        <v-card>
            <v-card-title>{{ $t('version.edit.pluginDeps', [platform.name]) }}</v-card-title>
            <v-card-text>
                <v-form v-model="validForm">
                    <DependencyTable ref="depTable" :platform="platform.name.toUpperCase()" :version="formVersion" @change="edited = true" />
                </v-form>
            </v-card-text>
            <v-card-actions class="justify-end">
                <v-btn text color="warning" @click.stop="dialog = false">
                    {{ $t('general.close') }}
                </v-btn>
                <v-btn color="success" :disabled="!validForm || !edited" :loading="loading" @click.stop="submit">
                    {{ $t('general.save') }}
                </v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>

<script lang="ts">
import { Component, mixins, Vue, Watch } from 'nuxt-property-decorator';
import { DependencyVersion, PluginDependency } from 'hangar-api';
import { cloneDeep } from 'lodash-es';
import { HangarFormModal, HangarProjectVersionMixin } from '~/components/mixins';
import DependencyTable from '~/components/modals/versions/DependencyTable.vue';
import { Platform } from '~/types/enums';

@Component({
    components: { DependencyTable },
})
export default class DependencyEditModal extends mixins(HangarFormModal, HangarProjectVersionMixin) {
    edited = false;
    formVersion: DependencyVersion = {
        pluginDependencies: {} as Record<Platform, PluginDependency[]>,
    };

    submit() {
        this.loading = true;
        this.$api
            .requestInternal(`versions/version/${this.project.id}/${this.projectVersion.id}/savePluginDependencies`, true, 'post', {
                platform: this.platform.name.toUpperCase(),
                pluginDependencies: [...this.formVersion.pluginDependencies[this.platform.name.toUpperCase() as Platform], ...this.$refs.depTable.newDeps],
            })
            .then(() => {
                this.dialog = false;
                this.$nuxt.refresh();
            })
            .catch(this.$util.handleRequestError)
            .finally(() => {
                this.loading = false;
            });
    }

    $refs!: {
        depTable: DependencyTable;
    };

    @Watch('dialog')
    onToggle(val: boolean) {
        if (val) {
            this.formVersion.pluginDependencies = Vue.observable(cloneDeep(this.projectVersion.pluginDependencies));
        } else {
            this.formVersion.pluginDependencies = {} as Record<Platform, PluginDependency[]>;
        }
        this.$nextTick(() => {
            this.$refs.depTable.reset();
            this.$nextTick(() => {
                this.edited = false;
            });
        });
    }
}
</script>

<style lang="scss" scoped></style>
