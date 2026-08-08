<script lang="ts" setup>
import type { ValidationRule } from "@vuelidate/core";
import type Easymde from "easymde";
import EasyMDE from "easymde";

const i18n = useI18n();
const headings = ref<{ id: string; text?: string; level: number }[]>([]);

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
    status: [
      {
        className: "characters",
        defaultValue: (el: HTMLElement) => updateCharCount(el, rawEdited.value.length),
        onUpdate: (el: HTMLElement) => updateCharCount(el, easyMDE?.value().length ?? 0),
      },
    ],
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
  easyMDE.codemirror.on("beforeChange", (instance, change) => {
    if (change.origin === "setValue" || !change.update) return;
    const delta = change.text.join("\n").length - (change.removed?.join("\n").length ?? 0);
    if (delta > 0 && instance.getValue().length + delta > props.maxlength) {
      change.cancel();
    }
  });
  easyMDE.codemirror.on("change", (instance) => (rawEdited.value = instance.getValue()));
}

function updateCharCount(el: HTMLElement, length: number) {
  el.textContent = `${length} / ${props.maxlength}`;
  el.classList.toggle("over-limit", length > props.maxlength);
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
      <div class="absolute top-2 right-0 z-10 flex gap-1">
        <MarkdownToc v-if="!internalEditing && headings.length > 0" :headings="headings" />
        <Button
          v-if="!internalEditing"
          variant="outline"
          tone="neutral"
          size="sm"
          icon-only
          :title="i18n.t('general.edit')"
          :aria-label="i18n.t('general.edit')"
          @click="startEditing()"
        >
          <IconMdiPencil />
        </Button>
        <DeletePageModal @delete="deletePage">
          <template #activator="{ on }">
            <Button
              v-if="internalEditing && deletable"
              variant="ghost"
              tone="danger"
              size="sm"
              icon-only
              :disabled="loading.delete"
              :title="i18n.t('general.delete')"
              :aria-label="i18n.t('general.delete')"
              v-on="on"
            >
              <IconMdiDelete />
            </Button>
          </template>
        </DeletePageModal>
        <Button
          v-if="internalEditing && cancellable"
          variant="ghost"
          tone="neutral"
          size="sm"
          icon-only
          :title="i18n.t('general.close')"
          :aria-label="i18n.t('general.close')"
          @click="stopEditing()"
        >
          <IconMdiClose />
        </Button>
        <Button
          v-if="internalEditing && saveable"
          size="sm"
          icon-only
          :disabled="loading.save || v.$invalid"
          :title="i18n.t('general.save')"
          :aria-label="i18n.t('general.save')"
          @click="savePage"
        >
          <IconMdiContentSave />
        </Button>
      </div>
    </div>
    <div v-if="internalEditing && !noPaddingTop" class="mt-11" :class="{ 'mt-2': hasSlotContent($slots.title) }" />
    <div v-if="internalEditing">
      <textarea ref="editor" v-model="rawEdited" class="text-left" :maxlength="maxlength" />
    </div>
    <Markdown v-if="!internalEditing" :raw="raw" :show-toc="false" @headings="(h) => (headings = h)" />
    <ErrorTooltip :error-messages="errors" class="w-full absolute">
      <span />
    </ErrorTooltip>
  </div>
</template>

<style lang="scss">
@use "easymde/dist/easymde.min.css";

.EasyMDEContainer {
  .editor-toolbar,
  .CodeMirror {
    clip-path: none !important;
    color: unset;
    border-color: var(--gray-300);
    background: var(--input-surface);

    .dark & {
      border-color: var(--gray-700);
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

  .editor-toolbar {
    border-radius: 6px 6px 0 0;

    button {
      border: none;
      border-radius: 4px;
      color: inherit;
    }

    button:hover {
      background: var(--input-surface-hover);
    }

    button.active {
      background: color-mix(in srgb, var(--primary-500) 15%, transparent);
      color: var(--primary-ink);
    }

    i.separator {
      border-left-color: var(--gray-300);
      border-right-color: transparent;
    }

    .dark & {
      button.active {
        background: color-mix(in srgb, var(--primary-300) 18%, transparent);
        color: var(--primary-300);
      }

      i.separator {
        border-left-color: var(--gray-700);
      }
    }
  }

  .CodeMirror {
    border-radius: 0 0 6px 6px;
    line-height: 1.65;
  }

  .editor-statusbar {
    color: var(--gray-500);
    padding: 6px 2px;

    .over-limit {
      color: #b91c1c;
      font-weight: 500;

      .dark & {
        color: #fca5a5;
      }
    }
  }

  .editor-preview-side {
    border-color: var(--gray-300);
    background: var(--input-surface);

    .dark & {
      border-color: var(--gray-700);
    }
  }

  .cm-s-easymde {
    // useMarked renders headings one level down, so `#` is an h2, `##` an h3, and so on
    .cm-header-1 {
      font-size: 1.5em;
      font-weight: 700;
      line-height: 1.5;
      border-bottom: 1px solid;
      padding-bottom: 5px;

      .dark & {
        border-bottom-color: #57595e;
      }

      .light & {
        border-bottom-color: #e0e0e0;
      }
    }

    .cm-header-2 {
      font-size: 1.25em;
      font-weight: 600;
      line-height: 1.6;
    }

    .cm-header-3 {
      font-size: 1em;
      font-weight: 600;
      line-height: 1.5;
    }

    .cm-header-4 {
      font-size: 0.83em;
      font-weight: 700;
      line-height: 1.65;
    }

    .cm-header-5,
    .cm-header-6 {
      font-size: 0.67em;
      font-weight: 700;
      line-height: 1.65;
    }

    // CodeMirror tags inline code and fenced code alike as .cm-comment; only a fenced line has it as its line's sole span
    // the backticks are their own spans, so horizontal padding would break the run into separate chips
    .cm-comment {
      padding: 0.15em 0;
      background: #dfe0e1;
      color: #000;
      font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
      font-size: 0.875em;

      .dark & {
        background: #37393b;
        color: #fff;
      }
    }

    .cm-comment:only-child {
      padding: 0;
      background: none;
      color: inherit;
      font-size: 1em;
    }

    .CodeMirror-line:has(> span > .cm-comment:only-child) {
      background: #f2f2f3;

      .dark & {
        background: #37393b;
      }
    }
  }
}
</style>
