<template>
    <v-card>
        <v-card-title>{{ $t('platformVersions.title') }}</v-card-title>
        <v-card-text>
            <v-simple-table>
                <template #default>
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
                                />
                            </td>
                        </tr>
                    </tbody>
                </template>
            </v-simple-table>
        </v-card-text>
        <v-card-actions>
            <v-btn type="success" @click="save">{{ $t('platformVersions.saveChanges') }}</v-btn>
        </v-card-actions>
    </v-card>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { IPlatform } from 'hangar-internal';
import { RootState } from '~/store';
import { GlobalPermission } from '~/utils/perms';
import { NamedPermission } from '~/types/enums';

@Component
@GlobalPermission(NamedPermission.MANUAL_VALUE_CHANGES)
export default class AdminVersionsPage extends Vue {
    get platforms(): IPlatform[] {
        return Array.from((this.$store.state as RootState).platforms.values());
    }

    // todo send to server, sort versions (here or on server?)!
    save() {}
}
</script>

<style lang="scss" scoped></style>
