<template>
    <!-- need v-btn here to make it inline?! -->
    <v-btn-toggle borderless dense class="v-btn">
        <v-btn color="primary" :small="small" :loading="loading" @click="checkAndDownload">
            <v-icon>mdi-download-outline</v-icon>
            {{ external ? $t('version.page.downloadExternal') : $t('version.page.download') }}
        </v-btn>

        <v-dialog v-model="modalShown" max-width="600">
            <v-card>
                <v-card-title class="headline">{{
                    $t('version.page.confirmation.title', [project.name, version ? version.name : '', project.owner.name])
                }}</v-card-title>

                <v-card-text>
                    <v-alert type="error">{{ $t('version.page.confirmation.alert') }}</v-alert>
                    <em>{{ $t('version.page.confirmation.disclaimer') }}</em>
                </v-card-text>

                <v-card-actions>
                    <v-spacer></v-spacer>

                    <v-btn color="green" @click="modalShown = false">{{ $t('version.page.confirmation.deny') }}</v-btn>
                    <v-btn color="error" @click="download">{{ $t('version.page.confirmation.agree') }}</v-btn>
                </v-card-actions>
            </v-card>
        </v-dialog>

        <v-tooltip v-if="copyButton" v-model="copySuccessful" bottom>
            <template #activator="data">
                <v-btn color="primary" :small="small" v-bind="data.attrs" @click="copyDownloadUrl">
                    <v-icon>mdi-content-copy</v-icon>
                </v-btn>
            </template>
            <span>{{ $t('version.page.downloadUrlCopied') }}</span>
        </v-tooltip>

        <v-menu v-if="platformSelection" offset-y>
            <template #activator="{ on, attrs }">
                <v-btn color="primary" dark v-bind="attrs" v-on="on">
                    <v-icon v-text="`$vuetify.icons.${selectedPlatform.toLowerCase()}`" />
                </v-btn>
            </template>
            <v-list>
                <v-list-item-group v-model="selectedPlatform" color="primary" mandatory>
                    <v-list-item v-for="(platform, i) in Object.keys(project.recommendedVersions)" :key="i" :value="platform">
                        <v-list-item-icon>
                            <v-icon v-text="`$vuetify.icons.${platform.toLowerCase()}`"></v-icon>
                        </v-list-item-icon>
                        <v-list-item-content>
                            {{ platform.toLowerCase() }}
                        </v-list-item-content>
                    </v-list-item>
                </v-list-item-group>
            </v-list>
        </v-menu>
    </v-btn-toggle>
</template>

<script lang="ts">
import { Component, Prop } from 'nuxt-property-decorator';
import { HangarProject, HangarVersion, IPlatform } from 'hangar-internal';
import { HangarComponent } from '~/components/mixins';
import { Platform } from '~/types/enums';

@Component({})
export default class DownloadButton extends HangarComponent {
    @Prop({ default: true })
    copyButton!: boolean;

    @Prop({ default: false })
    platformSelection!: boolean;

    @Prop({ default: true })
    small!: boolean;

    @Prop({ required: true })
    project!: HangarProject;

    @Prop({ required: false })
    version!: HangarVersion;

    @Prop({ required: false })
    platform!: IPlatform;

    selectedPlatform!: Platform;

    modalShown: boolean = false;
    loading: boolean = false;
    token: string | null = null;
    copySuccessful: boolean = false;

    get external() {
        if (this.platformSelection) {
            return false; // no external naming on main page :shrug:
        } else {
            return this.version.externalUrl !== null;
        }
    }

    created() {
        if (this.platformSelection) {
            const keys = this.project.recommendedVersions ? Object.keys(this.project.recommendedVersions) : [];
            this.selectedPlatform = keys.length > 0 ? Platform[keys[0] as keyof typeof Platform] : Platform.PAPER;
        }
    }

    copyDownloadUrl() {
        let url = '';
        if (this.external) {
            url = this.version.externalUrl;
        } else {
            const versionString = this.platformSelection ? 'recommended' : this.version.name;
            const platform = this.platformSelection ? this.selectedPlatform : this.platform.name;
            url = `${window.location.protocol}//${window.location.host}/api/v1/projects/${this.project.namespace.owner}/${this.project.namespace.slug}/versions/${versionString}/${platform}/download`;
        }
        navigator.clipboard.writeText(url);
        this.copySuccessful = true;
        setTimeout(() => (this.copySuccessful = false), 1000);
    }

    async checkAndDownload() {
        if (await this.requiresConfirmation()) {
            this.modalShown = true;
            return;
        }
        this.download();
    }

    download() {
        if (this.external) {
            return window.open(this.version.externalUrl, '_blank');
        }
        const versionString = this.platformSelection ? 'recommended' : this.version.name;
        const platform = this.platformSelection ? this.selectedPlatform : this.platform.name;
        window.open(
            `/api/internal/versions/version/${this.project.namespace.owner}/${this.project.namespace.slug}/versions/${versionString}/${platform}/download?token=${this.token}`,
            '_blank'
        );
        this.token = null;
        this.modalShown = false;
    }

    async requiresConfirmation() {
        if (this.token != null) {
            return true;
        }
        if (this.external) {
            return window.open(this.version.externalUrl, '_blank');
        }
        this.loading = true;
        const versionString = this.platformSelection ? 'recommended' : this.version.name;
        const platform = this.platformSelection ? this.selectedPlatform : this.platform.name;
        try {
            await this.$api
                .requestInternal(
                    `versions/version/${this.project.namespace.owner}/${this.project.namespace.slug}/versions/${versionString}/${platform}/downloadCheck`
                )
                .then(() => (this.loading = false))
                .catch((err) => {
                    if (err.response && err.response.status === 428) {
                        this.token = err.response.data;
                    }
                    throw err;
                });
            this.loading = false;
            return false;
        } catch (ex) {
            this.loading = false;
            return true;
        }
    }
}
</script>
