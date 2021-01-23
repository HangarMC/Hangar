<template>
    <div>{{ input }}</div>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { Prop } from 'vue-property-decorator';

@Component
export default class Markdown extends Vue {
    // TODO markdown renderer
    @Prop()
    input!: String;
}
</script>

<style scoped></style>
