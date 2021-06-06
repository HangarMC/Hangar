<template>
    <v-dialog v-model="dialog" persistent max-width="500">
        <template #activator="{ on, attrs }">
            <slot name="activator" :attrs="attrs" :on="on" />
        </template>
        <v-card>
            <v-card-title>{{ $t('page.delete.title') }}</v-card-title>
            <v-card-text>{{ $t('page.delete.text') }}</v-card-text>
            <v-card-actions class="justify-end">
                <v-btn text color="error" @click="dialog = false">
                    {{ $t('general.close') }}
                </v-btn>
                <v-btn color="warning" @click="$emit('delete')">
                    {{ $t('general.delete') }}
                </v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { HangarModal } from '../../mixins';

@Component
export default class DeletePageModal extends HangarModal {}
</script>

<style lang="scss" scoped></style>
