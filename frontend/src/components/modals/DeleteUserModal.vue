<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import type { User } from "hangar-api";
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useVuelidate } from "@vuelidate/core";
import Button from "~/components/design/Button.vue";
import Modal from "~/components/modals/Modal.vue";
import { useInternalApi } from "~/composables/useApi";
import { required } from "#imports";
import { handleRequestError } from "~/composables/useErrorHandling";
import Tooltip from "~/components/design/Tooltip.vue";
import InputTextarea from "~/components/ui/InputTextarea.vue";
import { useNotificationStore } from "~/store/notification";

const props = defineProps<{
  user: User;
}>();

const i18n = useI18n();
const router = useRouter();
const v = useVuelidate();
const comment = ref<string>("");

async function confirm(close: () => void) {
  try {
    await useInternalApi(`admin/yeet/${props.user.name}`, "post", {
      content: comment.value,
    });
    close();
    useNotificationStore().success("Deleted user");
  } catch (e) {
    handleRequestError(e);
  }
}
</script>

<template>
  <Modal title="Delete user">
    <template #default="{ on }">
      <InputTextarea v-model="comment" :rules="[required()]" label="Reason" />
      <Button button-type="red" class="mt-3" :disabled="v.$invalid" @click="confirm(on.click)">{{ i18n.t("general.confirm") }}</Button>
    </template>
    <template #activator="{ on }">
      <Tooltip>
        <template #content> Delete user </template>
        <Button button-type="red" size="small" class="mr-1 inline-flex" v-on="on">
          <IconMdiDelete />
        </Button>
      </Tooltip>
    </template>
  </Modal>
</template>
