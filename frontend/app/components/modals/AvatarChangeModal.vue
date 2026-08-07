<template>
  <Modal ref="modal" :title="title" window-classes="w-125" @open="openModal">
    <template #activator="{ on }">
      <slot name="activator" :on="on">
        <Button v-bind="$attrs" @click.prevent="on.click">{{ title }}</Button>
      </slot>
    </template>

    <div class="mb-3 flex flex-wrap items-center gap-3">
      <Button variant="outline" tone="neutral" :disabled="loading.save || loading.reset" @click.prevent="fileInput?.click()">
        <IconMdiFolderOpen />
        {{ t("settings.avatar.choose") }}
      </Button>
      <span class="min-w-0 flex-1 truncate text-sm text-gray-secondary">
        {{ selectedFile?.name ?? (cropperInput ? t("settings.avatar.currentImage") : "") }}
      </span>
      <input ref="fileInput" type="file" class="hidden" accept="image/png,image/jpeg,image/webp" @change="onFileChange" />
    </div>

    <cropper
      v-if="cropperInput"
      :src="cropperInput"
      class="h-250px overflow-hidden rounded-md"
      :min-height="150"
      :default-size="defaultCropSize"
      :canvas="{
        imageSmoothingQuality: 'high',
      }"
      :stencil-props="{
        handlers: { eastNorth: true, westNorth: true, eastSouth: true, westSouth: true },
        movable: true,
        scalable: true,
        aspectRatio: 1,
      }"
      :resize-image="{
        adjustStencil: false,
      }"
      image-restriction="stencil"
      @change="changeImage"
    />
    <button
      v-else
      type="button"
      class="h-250px w-full flex flex-col items-center justify-center gap-2 rounded-md border-2 border-dashed text-sm text-gray-secondary transition-colors"
      :class="dragging ? 'border-primary-500 background-card' : 'border-gray-300 hover:background-card dark:border-gray-700'"
      @click.prevent="fileInput?.click()"
      @dragover.prevent="dragging = true"
      @dragleave.prevent="dragging = false"
      @drop.prevent="onDrop"
    >
      <IconMdiImagePlus class="text-3xl" />
      {{ t("settings.avatar.dropHint") }}
    </button>

    <p v-if="cropperInput" class="mt-2 text-sm text-gray-secondary">{{ t("settings.avatar.hint") }}</p>

    <template #footer="{ on }">
      <Button
        v-if="resetAction"
        variant="outline"
        tone="danger"
        class="mr-auto"
        :loading="loading.reset"
        :disabled="loading.save"
        @click.prevent="resetToDefault"
      >
        <IconMdiCached />
        {{ t("settings.avatar.reset") }}
      </Button>
      <Button variant="ghost" tone="neutral" :disabled="loading.save || loading.reset" v-on="on">{{ t("general.cancel") }}</Button>
      <Button :disabled="!cropperResult || loading.reset" :loading="loading.save" @click.prevent="save">{{ t("general.save") }}</Button>
    </template>
  </Modal>
</template>

<script lang="ts" setup>
import { Cropper } from "vue-advanced-cropper";
import type { CropperResult } from "vue-advanced-cropper";

import "vue-advanced-cropper/dist/style.css";

const { t } = useI18n();
const notifications = useNotificationStore();

const props = withDefaults(
  defineProps<{
    avatar: string;
    action: string;
    csrfToken?: string;
    field?: string;
    resetAction?: string;
    title?: string;
  }>(),
  {
    csrfToken: undefined,
    field: "avatar",
    resetAction: undefined,
    title: undefined,
  }
);

const title = computed(() => props.title ?? t("organization.settings.changeAvatar"));

const v = useVuelidate({ $stopPropagation: true });
const selectedFile = ref<File>();
const cropperInput = ref<ArrayBuffer>();
const mimeType = ref<string>();
const cropperResult = ref<Blob | null>();
const modal = useTemplateRef("modal");
const fileInput = useTemplateRef("fileInput");
const dragging = ref(false);
const loading = reactive({ save: false, reset: false });

let reader: FileReader | undefined;
onMounted(() => {
  reader = new FileReader();
  reader.addEventListener(
    "load",
    () => {
      cropperInput.value = reader?.result as ArrayBuffer;
    },
    false
  );
  reader.addEventListener("loadend", (e) => {
    if (!e || !e.target || !e.target.result) return;
    const arr = new Uint8Array(e.target.result as ArrayBuffer).subarray(0, 4);
    let header = "";
    for (const element of arr) {
      header += element.toString(16);
    }
    switch (header) {
      case "89504e47":
        mimeType.value = "image/png";
        break;
      case "47494638":
        mimeType.value = "image/gif";
        break;
      case "ffd8ffe0":
      case "ffd8ffe1":
      case "ffd8ffe2":
      case "ffd8ffe3":
      case "ffd8ffe8":
        mimeType.value = "image/jpeg";
        break;
      default:
        mimeType.value = selectedFile.value?.type;
        break;
    }
  });
});

function onFileChange(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0];
  if (file) selectedFile.value = file;
}

function onDrop(e: DragEvent) {
  dragging.value = false;
  const file = e.dataTransfer?.files?.[0];
  if (file) selectedFile.value = file;
}

watch(selectedFile, (newValue) => {
  if (!newValue) {
    return;
  }
  if (newValue.size >= useBackendData.validations.project.maxFileSize) {
    notifications.error(t("validation.maxFileSize"));
    selectedFile.value = undefined;
    return;
  }

  cropperResult.value = newValue;
  reader?.readAsDataURL(newValue);
});

async function openModal() {
  if (isDefaultAvatar(props.avatar)) return;
  try {
    const response = await fetch(props.avatar, { cache: "no-cache" });
    const data = await response.blob();
    reader?.readAsDataURL(data);
  } catch (err) {
    notifications.error("Error while fetching existing avatar");
    console.error("error while fetching existing avatar", err);
  }
}

function changeImage({ canvas }: CropperResult) {
  canvas?.toBlob((blob) => {
    cropperResult.value = blob;
  }, mimeType.value);
}

function defaultCropSize({ imageSize }: { imageSize: { width: number; height: number } }) {
  const size = Math.min(imageSize.width, imageSize.height);
  return { width: size, height: size };
}

async function save() {
  if (!(await v.value.$validate())) return;
  const form = new FormData();
  form.append(props.field, cropperResult.value!);
  if (props.csrfToken) {
    form.append("csrf_token", props.csrfToken);
  }

  loading.save = true;
  try {
    await useInternalApi(props.action, "POST", form, { timeout: 10_000 });

    window.location.reload();
  } catch (err) {
    handleRequestError(err, "Error while saving avatar");
    reset();
    modal.value?.close();
    loading.save = false;
  }
}

async function resetToDefault() {
  if (!props.resetAction) return;
  loading.reset = true;
  try {
    await useInternalApi(props.resetAction, "POST");

    window.location.reload();
  } catch (err) {
    handleRequestError(err, "Error while resetting avatar");
    reset();
    modal.value?.close();
    loading.reset = false;
  }
}

function reset() {
  cropperResult.value = undefined;
  selectedFile.value = undefined;
  cropperInput.value = undefined;
  if (fileInput.value) fileInput.value.value = "";
  v.value.$reset();
}
</script>
