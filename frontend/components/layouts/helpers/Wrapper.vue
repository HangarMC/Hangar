<template>
    <div :class="clazz">
        <v-container>
            <slot></slot>
        </v-container>
    </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'nuxt-property-decorator';

@Component
export default class Wrapper extends Vue {
    @Prop({ type: String, required: true })
    clazz!: string;
}
</script>
