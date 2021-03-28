<template>
    <v-dialog v-model="dialog" max-width="700">
        <template #activator="{ attrs, on }">
            <slot name="activator" :attrs="attrs" :on="on" />
        </template>
        <v-card>
            <v-card-title>{{ title }}</v-card-title>
            <v-card-text>
                <Markdown :raw="markdown"></Markdown>
            </v-card-text>
            <v-card-actions class="justify-end">
                <v-btn text color="warning" @click="dialog = false">{{ $t('general.close') }}</v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>

<script lang="ts">
import { Component, Prop } from 'nuxt-property-decorator';
import { PropType } from 'vue';
import { TranslateResult } from 'vue-i18n';
import { HangarFormModal } from '~/components/mixins';
import Markdown from '~/components/markdown/Markdown.vue';
@Component({
    components: { Markdown },
})
export default class MarkdownModal extends HangarFormModal {
    @Prop({ type: String as PropType<string | TranslateResult>, required: true })
    title!: string | TranslateResult;

    @Prop({ type: String, required: true })
    markdown!: string;
}
</script>

<style lang="scss" scoped></style>
