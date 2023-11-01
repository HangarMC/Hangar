<script lang="ts" setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import type { ValidationRule } from "@vuelidate/core";
import type Easymde from "easymde";
import EasyMDE from "easymde";
import Markdown from "~/components/Markdown.vue";
import Button from "~/components/design/Button.vue";
import DeletePageModal from "~/components/modals/DeletePageModal.vue";
import { useValidation } from "~/composables/useValidationHelpers";
import ErrorTooltip from "~/components/design/ErrorTooltip.vue";
import { nextTick, parseMarkdown, useDomPurify } from "#imports";
import { usePrismStore } from "~/store/prism";
import { hasSlotContent } from "~/composables/useSlot";

const props = withDefaults(
  defineProps<{
    raw: string;
    editing: boolean;
    deletable: boolean;
    cancellable: boolean;
    saveable: boolean;
    maxlength?: number;
    errorMessages?: string[];
    rules?: ValidationRule<string | undefined>[];
    noPaddingTop?: boolean;
    maxHeight?: string;
    label?: string;
  }>(),
  {
    maxlength: 30_000,
    errorMessages: undefined,
    rules: undefined,
    noPaddingTop: false,
    maxHeight: "500px",
  }
);

const emit = defineEmits<{
  (e: "save", edited: string): void;
  (e: "delete"): void;
  (e: "update:editing", editing: boolean): void;
  (e: "update:raw", raw: string): void;
}>();

let easyMDE: Easymde | null = null;
const rawEdited = ref(props.raw || "");
const loading = reactive({
  save: false,
  delete: false,
});
const internalEditing = computed({
  get: () => props.editing,
  set: (value) => emit("update:editing", value),
});

const errorMessages = computed(() => props.errorMessages);
const { v, errors } = useValidation(props.label, props.rules, rawEdited, errorMessages);

if (process.client && props.editing) {
  onMounted(startEditing);
}

defineExpose({ rawEdited });

watch(
  ref(props),
  (val) => {
    if (!val.editing) {
      loading.save = false;
      loading.delete = false;
    }
  },
  { deep: true }
);

watch(rawEdited, (e) => emit("update:raw", e));

function savePage() {
  loading.save = true;
  emit("save", rawEdited.value);
}

function deletePage() {
  loading.delete = true;
  emit("delete");
}

async function startEditing() {
  internalEditing.value = true;
  await nextTick();
  const el = document.getElementById("markdown-editor");
  easyMDE = new EasyMDE({
    element: el as HTMLElement,
    autofocus: true,
    forceSync: true,
    indentWithTabs: false,
    spellChecker: false,
    promptURLs: true,
    showIcons: ["strikethrough", "code", "table"],
    shortcuts: {
      drawTable: "Cmd-Alt-T",
      toggleStrikethrough: "Cmd-Alt-S",
    },
    sideBySideFullscreen: false,
    syncSideBySidePreviewScroll: false,
    autoRefresh: {
      delay: 300,
    },
    maxHeight: props.maxHeight,
    previewClass: ["prose", "markdown", "background-default"],
    previewRender: (markdownPlaintext, previewElement) => {
      const html = useDomPurify(parseMarkdown(markdownPlaintext));
      previewElement.innerHTML = html;
      if (typeof html.includes === "function" && html.includes("<code")) {
        usePrismStore().handlePrism();
      }
      return null;
    },
    renderingConfig: {
      sanitizerFunction: (renderedHTML) => useDomPurify(renderedHTML),
    },
  });
  easyMDE.codemirror.on("change", (instance) => (rawEdited.value = instance.getValue()));
}

function stopEditing() {
  easyMDE?.toTextArea();
  easyMDE?.cleanup();
  internalEditing.value = false;
}
</script>

<template>
  <div class="relative">
    <slot name="title" />
    <div class="flex h-[1px]">
      <div class="absolute top-2 right-0 space-x-1">
        <Button v-if="!internalEditing" @click="startEditing()">
          <IconMdiPencil />
        </Button>
        <DeletePageModal @delete="deletePage">
          <template #activator="{ on }">
            <Button v-if="internalEditing && deletable" button-type="red" :disabled="loading.delete" v-on="on">
              <IconMdiDelete />
            </Button>
          </template>
        </DeletePageModal>
        <Button v-if="internalEditing && saveable" :disabled="loading.save || v.$invalid" @click="savePage">
          <IconMdiContentSave />
        </Button>
        <Button v-if="internalEditing && cancellable" @click="stopEditing()">
          <IconMdiClose />
        </Button>
      </div>
    </div>
    <div v-if="internalEditing && !noPaddingTop" class="mt-11" :class="{ 'mt-2': hasSlotContent($slots.title) }" />
    <div v-if="internalEditing">
      <textarea id="markdown-editor" v-model="rawEdited" class="text-left" :maxlength="maxlength"></textarea>
    </div>
    <Markdown v-if="!internalEditing" :raw="raw" />
    <ErrorTooltip :error-messages="errors" class="w-full absolute">
      <slot />
    </ErrorTooltip>
  </div>
</template>

<style lang="scss">
@import "easymde/dist/easymde.min.css";

.EasyMDEContainer {
  .editor-toolbar,
  .CodeMirror {
    clip-path: none !important;
    color: unset;

    .dark & {
      background: rgba(39, 39, 42, 1);
    }

    .light & {
      background: rgba(250, 250, 250, 1);
    }

    .CodeMirror-selected {
      background: #3297fd;
    }

    .CodeMirror-cursor {
      .dark & {
        border-left-color: rgb(224, 230, 240);
      }

      .light & {
        border-left-color: rgb(38, 38, 38);
      }
    }
  }

  .editor-preview {
    background: unset;
  }

  .markdown {
    margin: 0;
    padding: 1em;
  }

  .prose {
    max-width: 100%;
  }

  .editor-toolbar button.active,
  .editor-toolbar button:hover {
    background: unset;
  }

  .cm-s-easymde {
    .cm-header {
      display: inline-block;
      margin: 20px 0 0 0;
      line-height: 1.25;
    }

    .cm-header-1,
    .cm-header-2 {
      border-bottom: 1px solid;
      padding-bottom: 5px;

      .dark & {
        border-bottom-color: #57595e;
      }

      .light & {
        border-bottom-color: #e0e0e0;
      }
    }

    .cm-header-1 {
      font-size: 2.25em;
    }

    .cm-header-2 {
      font-size: 1.75em;
    }

    .cm-header-3 {
      font-size: 1.375em;
    }

    .cm-header-4 {
      font-size: 1.125em;
    }

    .cm-header-5 {
      line-height: 1.25;
    }

    .cm-header-6 {
      line-height: 1.25;
    }
  }
}
</style>
