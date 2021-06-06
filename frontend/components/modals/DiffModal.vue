<template>
    <v-dialog v-model="dialog" max-width="700">
        <template #activator="{ attrs, on }">
            <slot name="activator" :attrs="attrs" :on="on" />
        </template>
        <v-card>
            <v-card-title>{{ title }}</v-card-title>
            <!-- eslint-disable-next-line vue/no-v-html -->
            <v-card-text v-html="prettyDiff" />
            <v-card-actions class="justify-end">
                <v-btn text color="warning" @click="dialog = false">
                    {{ $t('general.close') }}
                </v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>

<script lang="ts">
import { Component, Prop } from 'nuxt-property-decorator';
import { PropType } from 'vue';
import { TranslateResult } from 'vue-i18n';
import { diff_match_patch as Diff, DIFF_DELETE, DIFF_INSERT, DIFF_EQUAL } from 'diff-match-patch';
import { HangarFormModal } from '~/components/mixins';
import Markdown from '~/components/markdown/Markdown.vue';

@Component({
    components: { Markdown },
})
export default class MarkdownModal extends HangarFormModal {
    @Prop({ type: String as PropType<string | TranslateResult>, required: true })
    title!: string | TranslateResult;

    @Prop({ type: String, required: true })
    left!: string;

    @Prop({ type: String, required: true })
    right!: string;

    get diff() {
        const diff = new Diff();
        const textDiff = diff.diff_main(this.left, this.right);
        diff.diff_cleanupSemantic(textDiff);
        return textDiff;
    }

    // copied from DiffMatchPatch, just to change colors...
    get prettyDiff() {
        const diffs = this.diff;
        const html = [];
        const patternAmp = /&/g;
        const patternLt = /</g;
        const patternGt = />/g;
        const patternPara = /\n/g;
        for (let x = 0; x < diffs.length; x++) {
            const op = diffs[x][0]; // Operation (insert, delete, equal)
            const data = diffs[x][1]; // Text of change.
            const text = data.replace(patternAmp, '&amp;').replace(patternLt, '&lt;').replace(patternGt, '&gt;').replace(patternPara, '&para;<br>');
            switch (op) {
                case DIFF_INSERT:
                    html[x] = '<ins style="background:#46954a33;">' + text + '</ins>';
                    break;
                case DIFF_DELETE:
                    html[x] = '<del style="background:#c93c3733;">' + text + '</del>';
                    break;
                case DIFF_EQUAL:
                    html[x] = '<span>' + text + '</span>';
                    break;
            }
        }
        return html.join('').replace(/&para;/g, '');
    }
}
</script>

<style lang="scss" scoped></style>
