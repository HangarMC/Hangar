<template>
    <v-dialog v-model="shown" width="500" persistent>
        <template #activator="{ on, attrs }">
            <v-btn v-bind="attrs" v-on="on">
                <v-icon>mdi-flag</v-icon>
                {{ $t('project.actions.flag') }}
            </v-btn>
        </template>
        <v-card>
            <v-card-title> {{ $t('project.flag.flagProject', [project.name]) }} </v-card-title>
            <v-card-text>
                <v-form ref="flagForm" v-model="form.valid">
                    <v-radio-group v-model="form.selection" :rules="[$util.$vc.require('A reason')]">
                        <v-radio v-for="(reason, index) in flagReasons" :key="index" :label="reason.title" :value="reason.type" />
                    </v-radio-group>
                    <v-textarea v-model.trim="form.comment" rows="3" filled :rules="[$util.$vc.require('A comment')]" :label="$t('general.comment')" />
                </v-form>
            </v-card-text>
            <v-card-actions class="justify-end">
                <v-btn text color="warning" @click.stop="shown = false">{{ $t('general.close') }}</v-btn>
                <v-btn color="error" :disabled="!form.valid" :loading="loading" @click.stop="submitFlag">{{ $t('general.submit') }}</v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'nuxt-property-decorator';
import { Prop } from 'vue-property-decorator';
import { FlagReason, HangarProject } from 'hangar-internal';

@Component
export default class FlagModal extends Vue {
    flagReasons: FlagReason[] = [];
    shown = false;
    loading = false;
    form = {
        valid: false,
        selection: null as string | null,
        comment: null as string | null,
    };

    @Prop({ required: true })
    project!: HangarProject;

    submitFlag() {
        this.loading = true;
        // TODO endpoint
        setTimeout(
            (self: FlagModal) => {
                self.loading = false;
                self.shown = false;
            },
            1000,
            this
        );
    }

    @Watch('shown')
    onToggle() {
        if (this.$refs.flagForm) {
            // @ts-ignore // TODO how to fix this?
            this.$refs.flagForm.reset();
        }
    }

    async fetch() {
        this.flagReasons.push(...(await this.$api.requestInternal<FlagReason[]>('data/flagReasons', true)));
    }
}
</script>
