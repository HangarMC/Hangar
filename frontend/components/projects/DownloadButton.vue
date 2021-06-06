<template>
    <div class="button-group d-inline-block">
        <v-menu v-if="platformSelection" offset-y>
            <template #activator="{ on, attrs }">
                <v-btn style="min-width: 32px" class="px-2" color="primary darken-1" dark v-bind="attrs" elevation="0" v-on="on">
                    <v-icon v-text="`$vuetify.icons.${selectedPlatform.toLowerCase()}`" />
                </v-btn>
            </template>
            <v-list>
                <v-list-item-group v-model="selectedPlatform" color="primary" mandatory>
                    <v-list-item v-for="(platform, i) in Object.keys(project.recommendedVersions)" :key="i" :value="platform">
                        <v-list-item-icon>
                            <v-icon v-text="`$vuetify.icons.${platform.toLowerCase()}`" />
                        </v-list-item-icon>
                        <v-list-item-content>
                            {{ $store.state.platforms.get(platform).name }}
                        </v-list-item-content>
                    </v-list-item>
                </v-list-item-group>
            </v-list> </v-menu
        ><v-btn color="primary" :small="small" :loading="loading" elevation="0" @click="checkAndDownload">
            <v-icon>mdi-download-outline</v-icon>
            {{ external ? $t('version.page.downloadExternal') : $t('version.page.download') }} </v-btn
        ><HangarModal
            ref="confirmModal"
            max-width="600"
            :close-label="$t('version.page.confirmation.deny')"
            close-color="success"
            submit-color="error"
            :submit-label="$t('version.page.confirmation.agree')"
            no-form
            :title="$t('version.page.confirmation.title', [project.name, version ? version.name : '', project.owner.name])"
            :submit="download"
        >
            <v-alert type="error">
                {{ $t('version.page.confirmation.alert') }}
            </v-alert>
            <em>{{ $t('version.page.confirmation.disclaimer') }}</em> </HangarModal
        ><v-tooltip v-if="copyButton" v-model="copySuccessful" bottom>
            <template #activator="{ attrs }">
                <v-btn style="min-width: 32px" class="px-2" color="primary lighten-1" :small="small" v-bind="attrs" elevation="0" @click="copyDownloadUrl">
                    <v-icon>mdi-content-copy</v-icon>
                </v-btn>
            </template>
            <span>{{ $t('version.page.downloadUrlCopied') }}</span>
        </v-tooltip>
    </div>
</template>

<script lang="ts">
import { Component, Prop } from 'nuxt-property-decorator';
import { HangarVersion, IPlatform } from 'hangar-internal';
import { HangarProjectMixin } from '~/components/mixins';
import { Platform } from '~/types/enums';
import HangarModal from '~/components/modals/HangarModal.vue';

@Component({
    components: { HangarModal },
})
export default class DownloadButton extends HangarProjectMixin {
    @Prop({ default: true })
    copyButton!: boolean;

    @Prop({ default: false })
    platformSelection!: boolean;

    @Prop({ default: true })
    small!: boolean;

    @Prop({ required: false })
    version!: HangarVersion | null;

    @Prop({ required: false })
    platform!: IPlatform | null;

    selectedPlatform!: Platform;

    loading: boolean = false;
    token: string | null = null;
    copySuccessful: boolean = false;

    $refs!: {
        confirmModal: HangarModal;
    };

    get external() {
        if (this.platformSelection) {
            return this.project.recommendedVersions[this.selectedPlatform] !== null;
        } else {
            return this.version!.externalUrl !== null;
        }
    }

    get externalUrl(): string | null {
        if (this.platformSelection) {
            return this.project.recommendedVersions[this.selectedPlatform];
        } else {
            return this.version!.externalUrl;
        }
    }

    created() {
        if (this.platformSelection) {
            const keys = this.project.recommendedVersions ? Object.keys(this.project.recommendedVersions) : [];
            this.selectedPlatform = keys.length > 0 ? Platform[keys[0] as keyof typeof Platform] : Platform.PAPER;
        }
    }

    copyDownloadUrl() {
        let url;
        if (this.external) {
            url = this.externalUrl;
        } else {
            const versionString = this.platformSelection ? 'recommended' : this.version!.name;
            const platform = this.platformSelection ? this.selectedPlatform : this.platform!.name;
            url = `${window.location.protocol}//${window.location.host}/api/v1/projects/${this.project.namespace.owner}/${this.project.namespace.slug}/versions/${versionString}/${platform}/download`;
        }
        if (url) {
            navigator.clipboard.writeText(url);
            this.copySuccessful = true;
            setTimeout(() => (this.copySuccessful = false), 1000);
        }
    }

    async checkAndDownload() {
        if (await this.requiresConfirmation()) {
            this.$refs.confirmModal.open();
            return;
        }
        return this.download();
    }

    download(): Promise<any> {
        if (this.external) {
            return Promise.resolve(window.open(this.externalUrl || '', '_blank'));
        }
        const versionString = this.platformSelection ? 'recommended' : this.version!.name;
        const platform = this.platformSelection ? this.selectedPlatform : this.platform!.name;
        window.open(
            `/api/internal/versions/version/${this.project.namespace.owner}/${this.project.namespace.slug}/versions/${versionString}/${platform}/download?token=${this.token}&t=${this.jwt}`,
            '_blank'
        );
        this.token = null;
        return Promise.resolve();
    }

    async requiresConfirmation() {
        if (this.token != null) {
            return true;
        }
        if (this.external) {
            return false;
        }
        this.loading = true;
        const versionString = this.platformSelection ? 'recommended' : this.version!.name;
        const platform = this.platformSelection ? this.selectedPlatform : this.platform!.name.toLowerCase();
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
<style lang="scss" scoped>
@import '~vuetify/src/styles/styles';

.button-group {
    .v-btn {
        border-radius: 0;
    }
    .v-btn:first-of-type {
        border-bottom-left-radius: $border-radius-root;
        border-top-left-radius: $border-radius-root;
    }
    .v-btn:last-of-type {
        border-bottom-right-radius: $border-radius-root;
        border-top-right-radius: $border-radius-root;
    }
}
</style>
