<template>
  <Modal
    ref="modal"
    :title="title || t('organization.settings.changeAvatar')"
    window-classes="w-full max-w-2xl !rounded-lg border border-gray-200 dark:border-gray-800 shadow-xl"
    close-button-right
    @open="openModal"
    @close="reset"
  >
    <template #activator="{ on }">
      <slot name="activator" :on="on">
        <Button button-type="primary" v-bind="$attrs" @click.prevent="on.click">{{ t("organization.settings.changeAvatar") }}</Button>
      </slot>
    </template>

    <p class="mb-4 text-sm text-gray">Choose a PNG, JPG, or WebP image. Reposition and resize it inside the square before saving.</p>

    <div
      class="group flex cursor-pointer items-center gap-3 rounded-lg border border-dashed border-gray-300 p-3 transition-colors hover:border-gray-400 dark:border-gray-700 dark:hover:border-gray-600"
      role="button"
      tabindex="0"
      @click="openFilePicker"
      @keydown.enter.prevent="openFilePicker"
      @keydown.space.prevent="openFilePicker"
    >
      <span class="inline-flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-md bg-gray-100 text-gray dark:bg-charcoal-500">
        <IconMdiImagePlusOutline class="text-xl" />
      </span>
      <span class="min-w-0 flex-grow">
        <span class="block font-semibold">{{ selectedFile?.name || "Choose an image" }}</span>
        <span class="block truncate text-xs text-gray">
          {{ selectedFile ? formatSize(selectedFile.size) : "Click to browse your files" }}
        </span>
      </span>
      <Button button-type="secondary" size="medium" tabindex="-1" @click.stop="openFilePicker">Browse</Button>
      <input ref="fileInput" class="sr-only" type="file" accept="image/png,image/jpeg,image/webp" @change="onFileChange" />
    </div>

    <div class="mt-4 overflow-hidden rounded-lg border bg-black dark:border-gray-800">
      <cropper
        v-if="cropperInput"
        :src="cropperInput"
        class="h-80"
        :min-height="200"
        :default-size="{
          width: 256,
          height: 256,
        }"
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
      <div v-else class="flex h-80 flex-col items-center justify-center gap-2 text-gray">
        <IconMdiImageOutline class="text-4xl" />
        <span>No image selected</span>
      </div>
    </div>

    <div class="mt-4 flex items-center justify-end gap-2">
      <Button button-type="secondary" size="medium" @click="modal?.close()">Cancel</Button>
      <Button button-type="primary" size="medium" :disabled="!cropperResult || saving" :loading="saving" @click.prevent="save">
        <IconMdiContentSaveOutline class="mr-1" />
        {{ t("general.save") }}
      </Button>
    </div>
  </Modal>
</template>

<script lang="ts" setup>
import { Cropper } from "vue-advanced-cropper";
import type { CropperResult } from "vue-advanced-cropper";

import "vue-advanced-cropper/dist/style.css";

const { t } = useI18n();
const notifications = useNotificationStore();

const props = defineProps<{
  avatar: string;
  action: string;
  csrfToken?: string;
  title?: string;
  fieldName?: string;
}>();

const selectedFile = ref<File>();
const cropperInput = ref<ArrayBuffer>();
const mimeType = ref<string>();
const cropperResult = ref<Blob | null>();
const modal = useTemplateRef("modal");
const fileInput = useTemplateRef("fileInput");
const saving = ref(false);

function onFileChange(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0];
  if (file) selectedFile.value = file;
}

function openFilePicker() {
  fileInput.value?.click();
}

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

async function save() {
  if (!cropperResult.value) return;
  saving.value = true;
  const form = new FormData();
  form.append(props.fieldName || "avatar", cropperResult.value);
  if (props.csrfToken) {
    form.append("csrf_token", props.csrfToken);
  }

  try {
    await useInternalApi(props.action, "POST", form, { timeout: 10_000 });
    notifications.success("Saved!");
    window.location.reload();
  } catch (err) {
    handleRequestError(err, "Error while saving avatar");
  } finally {
    saving.value = false;
  }
}

function reset() {
  cropperResult.value = undefined;
  selectedFile.value = undefined;
  cropperInput.value = undefined;
  mimeType.value = undefined;
  saving.value = false;
  if (fileInput.value) fileInput.value.value = "";
}
</script>
