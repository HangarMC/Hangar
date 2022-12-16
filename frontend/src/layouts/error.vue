<template>
  <main>
    <Header />
    <Container class="min-h-[80vh]">
      <Card max-width="400" class="mx-auto">
        <h1>{{ error.statusCode }}</h1>
        <p>{{ text }}</p>
        <!-- todo nuxt button? -->
        <Button nuxt to="/" color="secondary">
          <IconMdiHome />
          {{ t("general.home") }}
        </Button>
      </Card>
    </Container>
    <Notifications />
    <Footer />
  </main>
</template>

<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { type NuxtError } from "nuxt/app";
import { computed } from "#imports";
import Card from "~/lib/components/design/Card.vue";
import Button from "~/lib/components/design/Button.vue";

const props = defineProps<{
  error: NuxtError;
}>();

const { t } = useI18n();

const text = computed(() => {
  switch (props.error.statusCode) {
    case 404:
      return t("error.404");
    case 401:
      return t("error.401");
    case 403:
      return t("error.403");
    default:
      return t("error.unknown");
  }
});

const head = computed(() => {
  let title = t("error.unknown");
  switch (props.error.statusCode) {
    case 404:
      title = t("error.404");
      break;
    case 401:
      title = props.error.message!;
      break;
  }
  return {
    title,
  };
});
</script>
