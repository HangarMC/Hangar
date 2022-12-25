<script lang="ts" setup>
import { computed, nextTick, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { debounce } from "lodash-es";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import Spinner from "~/lib/components/design/Spinner.vue";
import { usePrismStore } from "~/store/prism";

const i18n = useI18n();
const props = withDefaults(
  defineProps<{
    raw: string;
    inline?: boolean;
  }>(),
  {
    inline: false,
  }
);

const dum = computed(() => props);
watch(dum, debounce(fetch, 250, { leading: false }), { deep: true });

const renderedMarkdown = ref<string>("");
const loading = ref<boolean>(false);
async function fetch() {
  if (!props.raw) return;
  loading.value = true;
  renderedMarkdown.value = await useInternalApi<string>("pages/render", "post", {
    content: props.raw,
  }).catch<any>((e) => handleRequestError(e));
  loading.value = false;
  if (!import.meta.env.SSR) {
    await nextTick(setupAdmonition);
    // if (typeof renderedMarkdown.value?.includes === "function" && renderedMarkdown.value?.includes("<code")) {
    await usePrismStore().handlePrism();
    // }
  }
}
await fetch();

function setupAdmonition() {
  /** @licence
   This code was taken from flexmark-java https://github.com/vsch/flexmark-java

   Copyright (c) 2015-2016, Atlassian Pty Ltd
   All rights reserved.

   Copyright (c) 2016-2018, Vladimir Schneider,
   All rights reserved.

   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions are met:

   * Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

   * Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
   AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
   IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
   DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
   FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
   DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
   SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
   CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
   OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
   OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
   */
  const divs = document.getElementsByClassName("adm-block");
  for (let i = 0; i < divs.length; i++) {
    const div = divs[i];
    if (div.classList.contains("adm-collapsed") || div.classList.contains("adm-open")) {
      const headings = div.getElementsByClassName("adm-heading");
      if (headings.length > 0) {
        headings[0].addEventListener("click", (event) => {
          const el = div;
          event.preventDefault();
          event.stopImmediatePropagation();
          if (el.classList.contains("adm-collapsed")) {
            el.classList.remove("adm-collapsed");
            el.classList.add("adm-open");
          } else {
            el.classList.add("adm-collapsed");
            el.classList.remove("adm-open");
          }
        });
      }
    }
  }
}
</script>

<template>
  <div :class="{ 'prose max-w-full rounded markdown break-words': true, 'p-4': !inline, inline: inline }">
    <!-- eslint-disable-next-line vue/no-v-html -->
    <div v-if="!loading" v-bind="$attrs" v-html="renderedMarkdown" />
    <div v-else><Spinner class="stroke-gray-400" /></div>
  </div>
</template>

<style lang="scss">
@import "@/lib/assets/css/admonition.css";
@import "@/lib/assets/css/markdown.scss";
</style>
