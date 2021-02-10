<template>
    <v-row v-if="!pendingVersion" justify="space-around">
        <v-col>
            <v-file-input v-model="file" filled :label="$t('version.new.upload')" accepts=".jar,.zip" />
        </v-col>
        <v-col cols="1">
            {{ $t('general.or') }}
        </v-col>
        <v-col>
            <v-text-field v-model.trim="url" filled clearable :label="$t('version.new.url')" />
        </v-col>
        <v-col cols="12">
            <v-btn block color="primary" :disabled="!canCreate" :loading="loading.create" @click="createVersion">
                {{ $t('general.create') }}
            </v-btn>
        </v-col>
    </v-row>
    <v-row v-else>
        <v-col cols="12">
            <v-text-field
                v-model="pendingVersion.versionString"
                :label="$t('version.new.form.versionString')"
                :disabled="pendingVersion.isFile"
                filled
                :rules="[$util.$vc.require('Version string')]"
            />
        </v-col>
        <v-col v-if="pendingVersion.isFile" cols="12">
            <v-text-field v-model="pendingVersion.fileInfo.name" :label="$t('version.new.form.fileName')" disabled filled />
        </v-col>
        <v-col v-if="pendingVersion.isFile" cols="12">
            <v-text-field v-model="pendingVersion.fileInfo.sizeBytes" :label="$t('version.new.form.fileSize')" disabled filled />
        </v-col>
        <v-col v-if="!pendingVersion.isFile" cols="12">
            <v-text-field
                v-model="pendingVersion.externalUrl"
                :label="$t('version.new.form.externalUrl')"
                filled
                :rules="[$util.$vc.require('External URL')]"
            />
        </v-col>
    </v-row>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { PendingVersion } from 'hangar-internal';
import { HangarProjectMixin } from '~/components/mixins';
import { ProjectPermission } from '~/utils/perms';
import { NamedPermission } from '~/types/enums';

// TODO implement ProjectVersionsNewPage
// was multiple pages before, now one
@Component({
    head: {
        title: 'Create version...', // TODO i18n
    },
})
@ProjectPermission(NamedPermission.CREATE_VERSION)
export default class ProjectVersionsNewPage extends HangarProjectMixin {
    file: File | null = null;
    url: string | null = null;
    pendingVersion: PendingVersion | null = null;
    loading = {
        create: false,
        submit: false,
    };

    get canCreate(): boolean {
        return (!!this.file || !!this.url) && !(!!this.file && !!this.url);
    }

    async createVersion() {
        this.loading.create = true;
        const data: FormData = new FormData();
        if (this.url) {
            data.append('url', this.url);
        } else if (this.file) {
            data.append('pluginFile', this.file);
        } else {
            this.$util.error('Must specify a url or upload file');
            return;
        }
        this.pendingVersion = await this.$api
            .requestInternal<PendingVersion>(`versions/version/${this.project.id}/upload`, true, 'post', data)
            .catch<any>(this.$util.handlePageRequestError);
        this.loading.create = false;
    }
}
</script>

<style lang="scss" scoped></style>
