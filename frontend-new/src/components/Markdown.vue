<script lang="ts" setup>
import { nextTick, ref, watch } from "vue";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useContext } from "vite-ssr/vue";
import { useI18n } from "vue-i18n";

const ctx = useContext();
const i18n = useI18n();
const props = defineProps<{
  raw: string;
}>();

watch(props, fetch, { deep: true });

const renderedMarkdown = ref<string>("");
const loading = ref<boolean>(true);
async function fetch() {
  if (!props.raw) return;
  renderedMarkdown.value = await useInternalApi<string>("pages/render", false, "post", {
    content: props.raw,
  }).catch<any>((e) => handleRequestError(e, ctx, i18n));
  loading.value = false;
  if (!import.meta.env.SSR) {
    await nextTick(setupAdmonition);
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
  <div class="markdown rounded p-4">
    <!-- eslint-disable-next-line vue/no-v-html -->
    <div v-if="!loading" v-bind="$attrs" v-html="renderedMarkdown" />
    <div v-else>Loading...</div>
  </div>
</template>

<style lang="scss">
@import "/src/assets/css/admonition.css";
@import "/src/assets/css/markdown.scss";
</style>
