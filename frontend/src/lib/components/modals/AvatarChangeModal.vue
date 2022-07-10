<template>
  <Modal title="Change avatar" @open="openModal">
    <template #activator="{ on }">
      <Button button-type="primary" @click="on.click">Change avatar</Button>
    </template>

    <div class="mb-2">
      <InputFile v-model="selectedFile" :placeholder="t('settings.avatar.inputPlaceholder')" :rules="[required()]" accept="image/png,image/jpeg" />
    </div>
    <cropper
      v-if="cropperInput"
      :src="cropperInput"
      class="h-200px"
      :min-height="150"
      :stencil-props="{
        handlers: {},
        movable: false,
        scalable: false,
        aspectRatio: 1,
      }"
      :resize-image="{
        adjustStencil: false,
      }"
      image-restriction="stencil"
      @change="changeImage"
    />
    <Button class="mt-2" button-type="primary" :disabled="!cropperResult" @click="save">{{ t("general.save") }}</Button>
  </Modal>
</template>

<script lang="ts" setup>
import { $fetch } from "ohmyfetch";
import { useI18n } from "vue-i18n";
import { Cropper, CropperResult } from "vue-advanced-cropper";
import Button from "~/lib/components/design/Button.vue";
import InputFile from "~/lib/components/ui/InputFile.vue";
import { required } from "~/lib/composables/useValidationHelpers";
import { onMounted, ref, watch } from "vue";
import Modal from "~/lib/components/modals/Modal.vue";

import "vue-advanced-cropper/dist/style.css";

const { t } = useI18n();

const props = defineProps<{
  avatar: string;
  action: string;
  csrfToken?: string;
}>();

const selectedFile = ref();
const cropperInput = ref();
const cropperResult = ref();

let reader: FileReader | null = null;
onMounted(() => {
  reader = new FileReader();
  reader.addEventListener(
    "load",
    () => {
      cropperInput.value = reader?.result;
    },
    false
  );
});

watch(selectedFile, (newValue) => {
  if (!newValue) return null;
  cropperResult.value = newValue;
  reader?.readAsDataURL(newValue);
});

async function openModal() {
  console.log("attempt to load old avatar into selector");
  try {
    const response = await fetch(props.avatar);
    const data = await response.blob();
    reader?.readAsDataURL(data);
  } catch (e) {
    console.log("error while fetching existing avatar", e);
  }
}

function changeImage({ canvas }: CropperResult) {
  canvas?.toBlob((blob) => {
    cropperResult.value = blob;
  });
}

async function save() {
  const form = new FormData();
  form.append("avatar", cropperResult.value);
  if (props.csrfToken) {
    form.append("csrf_token", props.csrfToken);
  }

  await $fetch(props.action, {
    method: "POST",
    body: form,
  });

  window.location.reload();
}
</script>
