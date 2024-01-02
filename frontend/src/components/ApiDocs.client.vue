<script lang="ts" setup>
import "rapidoc";
</script>

<template>
  <rapi-doc
    spec-url="/v3/api-docs"
    render-style="view"
    allow-spec-url-load="false"
    allow-spec-file-load="false"
    show-header="false"
    server-url="/"
    default-api-server="/"
    heading-text="Hangar API"
  >
  </rapi-doc>
</template>

<style lang="scss">
rapi-doc {
  border-radius: 0.375rem;
}
</style>
