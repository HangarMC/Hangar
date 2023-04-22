<script lang="ts" setup>
import { type TranslateResult, useI18n } from "vue-i18n";
import { DIFF_DELETE, DIFF_EQUAL, DIFF_INSERT, diff_match_patch as Diff } from "diff-match-patch";
import { computed } from "vue";
import Modal from "~/components/modals/Modal.vue";
import { useDomPurify } from "~/composables/useDomPurify";

const props = defineProps<{
  title: string | TranslateResult;
  left: string;
  right: string;
}>();

const i18n = useI18n();

const diff = computed(() => {
  const differ = new Diff();
  const textDiff = differ.diff_main(props.left, props.right);
  differ.diff_cleanupSemantic(textDiff);
  return textDiff;
});
// copied from DiffMatchPatch, just to change colors...
// todo we might want to adjust the colors for the different themes
const prettyDiff = computed(() => {
  const diffs = diff.value;
  const html = [];
  const patternAmp = /&/g;
  const patternLt = /</g;
  const patternGt = />/g;
  const patternPara = /\n/g;
  for (let x = 0; x < diffs.length; x++) {
    const op = diffs[x][0]; // Operation (insert, delete, equal)
    const data = diffs[x][1]; // Text of change.
    const text = data.replaceAll(patternAmp, "&amp;").replaceAll(patternLt, "&lt;").replaceAll(patternGt, "&gt;").replaceAll(patternPara, "&para;<br>");
    switch (op) {
      case DIFF_INSERT:
        html[x] = '<ins style="background:#46954a33;">' + text + "</ins>";
        break;
      case DIFF_DELETE:
        html[x] = '<del style="background:#c93c3733;">' + text + "</del>";
        break;
      case DIFF_EQUAL:
        html[x] = "<span>" + text + "</span>";
        break;
    }
  }
  return useDomPurify(html.join("").replaceAll("&para;", ""));
});
</script>

<template>
  <Modal :title="props.title" window-classes="w-250">
    <!-- eslint-disable-next-line vue/no-v-html -->
    <div v-html="prettyDiff"></div>
    <template #activator="{ on }">
      <slot name="activator" :on="on"></slot>
    </template>
  </Modal>
</template>
