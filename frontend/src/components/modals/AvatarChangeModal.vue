<template>
  <Modal ref="modal" :title="t('organization.settings.changeAvatar')" window-classes="w-125" @open="openModal">
    <template #activator="{ on }">
      <slot name="activator" :on="on">
        <Button button-type="primary" v-bind="$attrs" @click.prevent="on.click">{{ t("organization.settings.changeAvatar") }}</Button>
      </slot>
    </template>

    <div class="mb-2">
      <InputFile
        v-model="selectedFile"
        :placeholder="t('settings.avatar.inputPlaceholder')"
        :rules="[required(), maxFileSize()(useBackendData.validations.project.maxFileSize)]"
        accept="image/png,image/jpeg,image/webp"
      />
    </div>
    <cropper
      v-if="cropperInput"
      :src="cropperInput"
      class="h-200px"
      :min-height="150"
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
    <Button class="mt-2" button-type="primary" :disabled="!cropperResult" @click.prevent="save">{{ t("general.save") }}</Button>
  </Modal>
</template>

<script lang="ts" setup>
import { Cropper, type CropperResult } from "vue-advanced-cropper";

import "vue-advanced-cropper/dist/style.css";

const { t } = useI18n();
const notifications = useNotificationStore();

const props = defineProps<{
  avatar: string;
  action: string;
  csrfToken?: string;
}>();

const v = useVuelidate({ $stopPropagation: true });
const selectedFile = ref<File>();
const cropperInput = ref<ArrayBuffer>();
const mimeType = ref<string>();
const cropperResult = ref<Blob | null>();
const modal = useTemplateRef("modal");

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
  if (!(await v.value.$validate())) return;
  const form = new FormData();
  form.append("avatar", cropperResult.value!);
  if (props.csrfToken) {
    form.append("csrf_token", props.csrfToken);
  }

  try {
    await useInternalApi(props.action, "POST", form, { timeout: 10_000 });

    window.location.reload();
  } catch (err) {
    handleRequestError(err, "Error while saving avatar");
    reset();
    modal.value?.close();
  }
}

function reset() {
  cropperResult.value = undefined;
  selectedFile.value = undefined;
  cropperInput.value = undefined;
  v.value.$reset();
}
</script>
