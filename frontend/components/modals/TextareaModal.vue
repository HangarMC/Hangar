<template>
    <v-dialog v-model="dialog" max-width="500">
        <template #activator="{ attrs, on }">
            <slot name="activator" :attrs="attrs" :on="on" />
        </template>
        <v-card>
            <v-card-title>{{ title }}</v-card-title>
            <v-card-text>
                <v-form ref="modalForm" v-model="validForm">
                    <v-textarea
                        v-model.trim="message"
                        autofocus
                        filled
                        :label="label"
                        :rules="[$util.$vc.required(label)]"
                        :rows="2"
                        auto-grow
                        @keydown.enter.prevent=""
                    />
                </v-form>
            </v-card-text>
            <v-card-actions class="justify-end">
                <v-btn text color="warning" @click="dialog = false">
                    {{ $t('general.close') }}
                </v-btn>
                <v-btn color="success" :disabled="!validForm" :loading="loading" @click="submit0">
                    {{ $t('general.submit') }}
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
export default class TextareaModal extends HangarFormModal {
    message: string = '';

    @Prop({ type: String as PropType<string | TranslateResult>, required: true })
    title!: string | TranslateResult;

    @Prop({ type: String as PropType<string | TranslateResult>, required: true })
    label!: string | TranslateResult;

    @Prop({ type: Function as PropType<(msg: string) => Promise<void>>, required: true })
    submit!: (msg: string) => Promise<void>;

    $refs!: {
        messageForm: any;
    };

    submit0() {
        this.loading = true;
        this.submit(this.message).then(() => {
            this.loading = false;
            this.dialog = false;
        });
    }
}
</script>

<style lang="scss" scoped></style>
