<script lang="ts" setup>
import { Visibility } from "#shared/types/backend";

const props = defineProps<{
  type: "project" | "version";
  propVisibility: Visibility;
  postUrl: string;
}>();

const i18n = useI18n();
const notification = useNotificationStore();
const router = useRouter();

const visibilities = computed(() => useBackendData.visibilities.filter((v) => v.canChangeTo));
const visibility = ref<Visibility>();
const reason = ref<string>("");

const showTextarea = computed(() => setVisibility.value?.showModal && props.propVisibility !== visibility.value);
const setVisibility = computed(() => useBackendData.visibilities.find((v) => v.name === visibility.value));
const currentVisibility = computed(() => useBackendData.visibilities.find((v) => v.name === props.propVisibility));

async function submit(): Promise<void> {
  await useInternalApi(props.postUrl, "post", {
    visibility: visibility.value,
    comment: setVisibility.value?.showModal ? reason.value : undefined,
  }).catch((err) => handleRequestError(err));
  reason.value = "";
  if (setVisibility.value) {
    notification.success(i18n.t("visibility.modal.success", [props.type, i18n.t(setVisibility.value?.title)]));
  }

  if (visibility.value === Visibility.SoftDelete) {
    await router.push("/");
  } else {
    router.go(0);
  }
}
</script>

<template>
  <Modal :title="i18n.t('visibility.modal.title', [type])" window-classes="w-150">
    <template #default>
      Currently: {{ currentVisibility ? i18n.t(currentVisibility.title) : "Unknown" }}
      <InputRadio v-for="vis in visibilities" :key="vis.name" v-model="visibility" :value="vis.name" :label="i18n.t(vis.title)" class="block" />

      <div v-if="showTextarea">
        <InputTextarea v-model.trim="reason" rows="2" :label="i18n.t('visibility.modal.reason')" class="pt-3" />
        <span class="text-sm">This will send the project owner a notification with the provided reason!</span>
        <br />
      </div>

      <Button class="mt-3" @click="submit">{{ i18n.t("general.submit") }}</Button>
    </template>
    <template #activator="{ on }">
      <Button v-bind="$attrs" class="mr-1" v-on="on">
        <IconMdiEye class="mr-1" />
        {{ i18n.t("visibility.modal.activatorBtn") }}
      </Button>
    </template>
  </Modal>
</template>
