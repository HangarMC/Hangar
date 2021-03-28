<template>
    <v-dialog v-model="dialog" width="500" persistent>
        <template #activator="{ on, attrs }">
            <v-btn v-bind="attrs" :class="activatorClass" v-on="on">
                <v-icon>mdi-flag</v-icon>
                {{ $t('project.actions.flag') }}
            </v-btn>
        </template>
        <v-card>
            <v-card-title> {{ $t('project.flag.flagProject', [project.name]) }} </v-card-title>
            <v-card-text>
                <v-form ref="modalForm" v-model="validForm">
                    <v-radio-group v-model="form.selection" :rules="[$util.$vc.require('A reason')]">
                        <v-radio v-for="(reason, index) in flagReasons" :key="index" :label="reason.title" :value="reason.type" />
                    </v-radio-group>
                    <v-textarea v-model.trim="form.comment" rows="3" filled :rules="[$util.$vc.require('A comment')]" :label="$t('general.comment')" />
                </v-form>
            </v-card-text>
            <v-card-actions class="justify-end">
                <v-btn text color="warning" @click.stop="dialog = false">{{ $t('general.close') }}</v-btn>
                <v-btn color="error" :disabled="!validForm" :loading="loading" @click.stop="submitFlag">{{ $t('general.submit') }}</v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>

<script lang="ts">
import { Component, mixins } from 'nuxt-property-decorator';
import { FlagReason } from 'hangar-internal';
import { HangarFormModal, HangarProjectMixin } from '~/components/mixins';

@Component
export default class FlagModal extends mixins(HangarFormModal, HangarProjectMixin) {
    flagReasons: FlagReason[] = [];
    form = {
        selection: null as string | null,
        comment: null as string | null,
    };

    submitFlag() {
        this.loading = true;
        this.$api
            .requestInternal('flags/', true, 'POST', {
                projectId: this.project.id,
                reason: this.form.selection,
                comment: this.form.comment,
            })
            .then(() => {
                this.loading = false;
                this.dialog = false;
                this.$util.success(this.$t('project.flag.flagSend'));
            })
            .catch((e) => {
                this.$util.handleRequestError(e);
                this.loading = false;
            });
    }

    async fetch() {
        this.flagReasons.push(...(await this.$api.requestInternal<FlagReason[]>('data/flagReasons', true).catch<any>(this.$util.handleRequestError)));
    }
}
</script>
