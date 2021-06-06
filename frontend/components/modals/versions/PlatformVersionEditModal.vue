<template>
    <v-dialog v-model="dialog" width="500">
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
            <v-card-title>{{ $t('version.edit.platformVersions', [platform.name]) }}</v-card-title>
            <v-card-text>
                <v-form v-model="validForm">
                    <v-checkbox
                        v-for="version in platform.possibleVersions"
                        :key="version"
                        v-model="selectedVersions"
                        class="platform-version-checkbox"
                        dense
                        hide-details
                        :label="version"
                        :rules="[$util.$vc.requireNonEmptyArray('Versions')]"
                        :value="version"
                    />
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
import { Component, mixins, Watch } from 'nuxt-property-decorator';
import { HangarFormModal, HangarProjectVersionMixin } from '~/components/mixins';
import { Platform } from '~/types/enums';

@Component
export default class PlatformVersionEditModal extends mixins(HangarFormModal, HangarProjectVersionMixin) {
    selectedVersions: string[] = [];
    edited = false;

    @Watch('dialog')
    onToggle(val: boolean) {
        if (val) {
            this.selectedVersions = this.projectVersion.platformDependencies[this.platform.name.toUpperCase() as Platform];
        } else {
            this.selectedVersions = [];
        }
        this.$nextTick(() => {
            this.edited = false;
        });
    }

    submit() {
        this.loading = true;
        this.$api
            .requestInternal(`versions/version/${this.project.id}/${this.projectVersion.id}/savePlatformVersions`, true, 'post', {
                platform: this.platform.name.toUpperCase(),
                versions: this.selectedVersions,
            })
            .catch(this.$util.handleRequestError)
            .then(() => {
                this.dialog = false;
                this.$nuxt.refresh();
            })
            .finally(() => {
                this.loading = false;
            });
    }

    @Watch('selectedVersions', { deep: true })
    onFormChange() {
        this.edited = true;
    }
}
</script>

<style lang="scss" scoped>
.platform-version-checkbox.v-input--selection-controls {
    margin-top: 0;
    padding-top: 0;
}
</style>
