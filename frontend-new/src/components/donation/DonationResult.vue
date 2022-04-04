<script lang="ts" setup>
import Announcement from "~/components/Announcement.vue";
import { useRoute } from "vue-router";
import { computed } from "vue";

const route = useRoute();

const announcement = computed(() => {
  let text = "";
  const qs = route.query;
  if (qs.donation && qs.donation === "success" && qs.st && qs.st === "Completed") {
    text = "Donation successful! You donated " + qs.amt + " " + qs.cc + " " + qs.item_name;
  } else if (qs.donation && qs.donation === "failure") {
    text = "Donation failed.";
  } else {
    text = JSON.stringify(qs);
  }

  return {
    text,
    color: "#093962",
  };
});
</script>

<template>
  <div v-if="route.query['donation']" class="flex">
    <div class="basis-full">
      <Announcement :announcement="announcement" class="text-white" />
    </div>
  </div>
</template>
