<script lang="ts" setup>
import type { ValidationRule } from "@vuelidate/core";
import type Easymde from "easymde";
import EasyMDE from "easymde";

const props = withDefaults(
  defineProps<{
    raw?: string | null;
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
    raw: undefined,
    maxlength: 30_000,
    errorMessages: undefined,
    rules: undefined,
    noPaddingTop: false,
    maxHeight: "500px",
    label: undefined,
  }
);

const emit = defineEmits<{
  (e: "save", edited: string): void;
  (e: "delete"): void;
  (e: "update:editing", editing: boolean): void;
  (e: "update:raw", raw: string): void;
}>();

const editor = useTemplateRef("editor");
let easyMDE: Easymde | undefined;
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

if (import.meta.client && props.editing) {
  onMounted(startEditing);
}

defineExpose({ rawEdited });

watch(
  () => props.editing,
  (val) => {
    if (!val) {
      loading.save = false;
      loading.delete = false;
    }
  }
);

watch(
  () => props.raw,
  (val) => {
    if (val !== rawEdited.value) {
      rawEdited.value = val || "";
    }
  },
  { immediate: true }
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
  easyMDE = new EasyMDE({
    element: editor.value!,
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
    previewClass: ["prose", "dark:prose-invert", "markdown", "background-default"],
    previewRender: (markdownPlaintext, previewElement) => {
      const html = useDomPurify(parseMarkdown(markdownPlaintext)?.html);
      previewElement.innerHTML = html;
      if (typeof html.includes === "function" && html.includes("<code")) {
        usePrismStore().handlePrism();
      }
      // eslint-disable-next-line unicorn/no-null
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
  <div class="markdown-editor relative">
    <slot name="title" />
    <div v-if="internalEditing" class="flex items-center justify-end gap-2" :class="hasSlotContent($slots.title) ? 'absolute top-2 right-2 z-2' : 'px-2 py-2'">
      <DeletePageModal @delete="deletePage">
        <template #activator="{ on }">
          <Button v-if="deletable" button-type="red" class="!h-9 !w-9 !p-0" :disabled="loading.delete" aria-label="Delete content" v-on="on">
            <IconMdiDelete />
          </Button>
        </template>
      </DeletePageModal>
      <Button v-if="saveable" class="!h-9 !w-9 !p-0" :disabled="loading.save || v.$invalid" aria-label="Save content" @click="savePage">
        <IconMdiContentSave />
      </Button>
      <Button v-if="cancellable" button-type="secondary" class="!h-9 !w-9 !p-0" aria-label="Cancel editing" @click="stopEditing()">
        <IconMdiClose />
      </Button>
    </div>
    <div v-else class="absolute top-2 right-2 z-2">
      <Button button-type="secondary" class="!h-9 !w-9 !p-0" aria-label="Edit content" @click="startEditing()">
        <IconMdiPencil />
      </Button>
    </div>
    <div v-if="internalEditing">
      <textarea ref="editor" v-model="rawEdited" class="text-left" :maxlength="maxlength" />
    </div>
    <Markdown v-if="!internalEditing" :raw="raw" />
    <ErrorTooltip :error-messages="errors" class="w-full absolute">
      <span />
    </ErrorTooltip>
  </div>
</template>

<style scoped>
.markdown-editor :deep(.markdown) {
  margin-top: 0;
  padding: 1rem;
}

.markdown-editor :deep(.markdown > div > :first-child) {
  margin-top: 0;
}

.markdown-editor :deep(.markdown > div > :last-child) {
  margin-bottom: 0;
}
</style>

<style lang="scss">
@use "easymde/dist/easymde.min.css";

.EasyMDEContainer {
  margin-inline: 0.5rem;

  .editor-toolbar {
    padding: 0.5rem 0.75rem;
  }

  .CodeMirror {
    padding: 0;
  }

  .CodeMirror-lines {
    padding: 0.75rem 0;
  }

  .CodeMirror pre.CodeMirror-line,
  .CodeMirror pre.CodeMirror-line-like {
    padding: 0 1rem;
  }

  .editor-toolbar,
  .CodeMirror {
    clip-path: none !important;
    color: unset;

    .dark & {
      background: rgba(39, 39, 42, 1);
      border-color: rgb(63, 63, 70);
    }

    .light & {
      background: rgba(250, 250, 250, 1);
      border-color: rgb(212, 212, 216);
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

    .dark & {
      border-color: rgb(63, 63, 70);
    }

    .light & {
      border-color: rgb(212, 212, 216);
    }
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
