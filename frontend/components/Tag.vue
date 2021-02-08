<template>
    <div class="tags" :class="{ 'has-addons': data }">
        <span
            :style="{
                color: color.foreground,
                background: color.background,
                'border-color': color.background,
            }"
            class="tag"
        >
            {{ name }}
        </span>
        <span v-if="data" class="tag">{{ data }}</span>
    </div>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { Prop } from 'vue-property-decorator';
@Component({})
export default class DocsPage extends Vue {
    @Prop()
    name!: String;

    @Prop()
    data!: String;

    @Prop()
    color!: Object;
}
</script>

<style lang="scss" scoped></style>
