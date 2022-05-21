<script lang="ts" setup>
import { useHead } from "@vueuse/head";
import { useSeo } from "~/composables/useSeo";
import { useRoute, useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import Card from "~/components/design/Card.vue";
import { useAuthStore } from "~/store/auth";
import { useBackendDataStore } from "~/store/backendData";
import { useInternalApi } from "~/composables/useApi";
import { handleRequestError } from "~/composables/useErrorHandling";
import { useContext } from "vite-ssr/vue";
import { computed, ref } from "vue";
import InputText from "~/components/ui/InputText.vue";
import MemberList from "~/components/projects/MemberList.vue";
import Button from "~/components/design/Button.vue";
import Alert from "~/components/design/Alert.vue";
import { JoinableMember } from "hangar-internal";
import { maxLength, minLength, pattern, required, validOrgName } from "~/composables/useValidationHelpers";
import { useVuelidate } from "@vuelidate/core";

const route = useRoute();
const router = useRouter();
const i18n = useI18n();
const ctx = useContext();
const backendData = useBackendDataStore();
const v = useVuelidate();

const currentUser = useAuthStore().user;

const name = ref<string>("");
const members = ref<JoinableMember[]>([]);

const canCreate = computed<boolean>(() => !v.value.$invalid && !v.value.$pending);

useHead(useSeo(i18n.t("organization.new.title"), null, route, null));

async function create() {
  try {
    await useInternalApi("organizations/create", true, "post", {
      name: name.value,
      members: members.value,
    });
    await router.push("/" + name.value);
  } catch (e) {
    handleRequestError(e, ctx, i18n);
  }
}
</script>

<template>
  <Card>
    <template #header>{{ i18n.t("organization.new.title") }}</template>
    <p class="mb-2">{{ i18n.t("organization.new.text") }}</p>

    <template v-if="currentUser.headerData.organizationCount < backendData.validations.maxOrgCount">
      <InputText
        v-model="name"
        class="mt-2"
        :label="i18n.t('organization.new.name')"
        :maxlength="backendData.validations.org.max"
        :rules="[
          required(),
          minLength()(backendData.validations.org.min),
          maxLength()(backendData.validations.org.max),
          pattern()(backendData.validations.org.regex),
          validOrgName(),
        ]"
      />

      <MemberList v-model="members" :author="name" organization disable-saving class="mt-4 shadow-0"></MemberList>

      <Button class="mt-4" :disabled="!canCreate" @click="create">
        <IconMdiCheck class="float-left" />
        {{ i18n.t("form.memberList.create") }}
      </Button>
    </template>

    <Alert v-else type="danger">
      {{ i18n.t("organization.new.error.tooManyOrgs", [backendData.validations.maxOrgCount]) }}
    </Alert>
  </Card>
</template>

<route lang="yaml">
meta:
  requireLoggedIn: true
</route>
