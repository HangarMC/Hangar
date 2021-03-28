<template>
    <v-dialog v-model="dialog" :max-width="maxWidth" @input="$emit('open')">
        <template #activator="props">
            <slot name="activator" v-bind="props" />
        </template>
        <v-card>
            <v-card-title>{{ title }}</v-card-title>
            <v-card-text>
                <v-form v-if="!noForm" ref="modalForm" v-model="validForm">
                    <slot />
                </v-form>
                <slot v-else />
            </v-card-text>
            <v-card-actions class="justify-end">
                <v-btn text color="warning" @click.stop="close">{{ $t('general.close') }}</v-btn>
                <slot name="other-btns" />
                <v-btn color="success" :disabled="(!noForm && !validForm) || submitDisabled" :loading="loading" @click.stop="submit0">
                    {{ submitLabel || $t('general.submit') }}
                </v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>

<script lang="ts">
import { Component, Prop } from 'nuxt-property-decorator';
import { PropType } from 'vue';
import { TranslateResult } from 'vue-i18n';
import { HangarFormModal } from '~/components/mixins';

@Component
export default class HangarModal extends HangarFormModal {
    @Prop({ type: String, default: '500' })
    maxWidth!: string;

    @Prop({ type: String as PropType<TranslateResult>, required: true })
    title!: TranslateResult;

    @Prop({ type: String as PropType<TranslateResult> })
    submitLabel!: TranslateResult | null;

    @Prop({ type: Function as PropType<() => Promise<void>>, required: true })
    submit!: () => Promise<void>;

    @Prop({ type: Boolean, default: false })
    submitDisabled!: boolean;

    @Prop({ type: Boolean, default: false })
    noForm!: boolean;

    $refs!: {
        modalForm: any;
    };

    close() {
        if (!this.noForm) {
            this.$refs.modalForm.reset();
        }

        this.dialog = false;
        this.$emit('close');
    }

    submit0() {
        this.loading = true;
        this.submit()
            .then(() => {
                this.dialog = false;
                if (!this.noForm) {
                    this.$refs.modalForm.reset();
                }
            })
            .finally(() => {
                this.loading = false;
            });
    }
}
</script>

<style lang="scss" scoped></style>
