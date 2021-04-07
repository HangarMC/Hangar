<template>
    <v-card>
        <v-card-title>{{ $t('platformVersions.title') }}</v-card-title>
        <v-card-text>
            <v-simple-table>
                <thead>
                    <tr>
                        <th class="text-left">{{ $t('platformVersions.platform') }}</th>
                        <th class="text-left">{{ $t('platformVersions.versions') }}</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="platform in platforms" :key="platform.name">
                        <td>{{ platform.name }}</td>
                        <td>
                            <v-combobox
                                v-model="platform.possibleVersions"
                                small-chips
                                deletable-chips
                                multiple
                                dense
                                hide-details
                                filled
                                :delimiters="[' ', ',', ';']"
                                append-icon=""
                            />
                        </td>
                    </tr>
                </tbody>
            </v-simple-table>
        </v-card-text>
        <v-card-actions class="justify-end">
            <v-btn text color="info" :disabled="!hasChanged" @click="reset">{{ $t('general.reset') }}</v-btn>
            <v-btn color="success" :loading="loading" :disabled="!hasChanged" @click="save">{{ $t('platformVersions.saveChanges') }}</v-btn>
        </v-card-actions>
    </v-card>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { IPlatform } from 'hangar-internal';
import { cloneDeep, isEqual } from 'lodash-es';
import { Context } from '@nuxt/types';
import { GlobalPermission } from '~/utils/perms';
import { NamedPermission } from '~/types/enums';
import { HangarComponent } from '~/components/mixins';

@Component
@GlobalPermission(NamedPermission.MANUAL_VALUE_CHANGES)
export default class AdminVersionsPage extends HangarComponent {
    loading = false;
    originalPlatforms!: IPlatform[];
    platforms!: IPlatform[];

    save() {
        this.loading = true;
        const data: { [key: string]: string[] } = {};
        this.platforms.forEach((pl) => {
            data[pl.enumName] = pl.possibleVersions;
        });
        this.$api
            .requestInternal('admin/platformVersions', true, 'post', data)
            .then(() => {
                this.$util.success(this.$t('platformVersions.success'));
                this.$nuxt.refresh();
            })
            .catch(this.$util.handleRequestError)
            .finally(() => {
                this.loading = false;
            });
    }

    reset() {
        this.platforms = cloneDeep(this.originalPlatforms);
    }

    get hasChanged() {
        return !isEqual(this.platforms, this.originalPlatforms);
    }

    head() {
        return {
            title: this.$t('platformVersions.title'),
        };
    }

    async asyncData({ $api, $util }: Context) {
        const data = await $api.requestInternal<IPlatform[]>('data/platforms', false).catch<any>($util.handlePageRequestError);
        return { platforms: data, originalPlatforms: cloneDeep(data) };
    }
}
</script>

<style lang="scss" scoped></style>
