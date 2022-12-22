<script lang="ts" setup>
import { useHead } from "@vueuse/head";
import { useRoute, useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { computed, ref } from "vue";
import { useVuelidate } from "@vuelidate/core";
import { useSeo } from "~/composables/useSeo";
import Card from "~/lib/components/design/Card.vue";
import { useAuthStore } from "~/store/auth";
import { useBackendDataStore } from "~/store/backendData";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import InputText from "~/lib/components/ui/InputText.vue";
import Button from "~/lib/components/design/Button.vue";
import Alert from "~/lib/components/design/Alert.vue";
import { maxLength, minLength, pattern, required } from "~/lib/composables/useValidationHelpers";
import { validOrgName } from "~/composables/useHangarValidations";
import { definePageMeta } from "#imports";

definePageMeta({
  loginRequired: true,
});

const route = useRoute();
const router = useRouter();
const i18n = useI18n();
const backendData = useBackendDataStore();
const v = useVuelidate();

const currentUser = useAuthStore().user;

const name = ref<string>("");

const canCreate = computed<boolean>(() => !v.value.$invalid && !v.value.$pending);

useHead(useSeo(i18n.t("organization.new.title"), null, route, null));

async function create() {
  try {
    await useInternalApi("organizations/create", "post", {
      name: name.value,
    });
    await router.push("/" + name.value);
  } catch (e: any) {
    handleRequestError(e);
  }
}
</script>

<template>
  <Card>
    <template #header>{{ i18n.t("organization.new.title") }}</template>
    <p class="mb-3">{{ i18n.t("organization.new.text") }}</p>

    <div v-if="currentUser.headerData.organizationCount < backendData.validations.maxOrgCount" class="mt-2">
      <InputText
        v-model="name"
        class="max-w-100 pr-2"
        :label="i18n.t('organization.new.name')"
        counter
        :maxlength="backendData.validations.org.max"
        :rules="[
          required(),
          minLength()(backendData.validations.org.min),
          maxLength()(backendData.validations.org.max),
          pattern()(backendData.validations.org.regex),
          validOrgName(),
        ]"
      />

      <Button size="medium" :disabled="!canCreate" @click="create">
        {{ i18n.t("form.memberList.create") }}
      </Button>
    </div>

    <Alert v-else type="danger">
      {{ i18n.t("organization.new.error.tooManyOrgs", [backendData.validations.maxOrgCount]) }}
    </Alert>
  </Card>
</template>
