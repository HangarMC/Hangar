<template>
    <div>
        <v-sheet :color="color" class="darken-1" rounded elevation="2">
            <!-- eslint-disable-next-line vue/no-v-html -->
            <div v-if="!$fetchState.pending" class="pa-3 page-rendered" :class="innerClass" v-html="renderedMarkdown" />
            <v-row v-else no-gutters justify="center">
                <v-progress-circular indeterminate color="primary" size="64" class="my-3" />
            </v-row>
        </v-sheet>
        <slot />
    </div>
</template>

<script lang="ts">
import { Component, Prop, PropSync, Vue, Watch } from 'nuxt-property-decorator';

@Component
export default class Markdown extends Vue {
    loading = true;
    renderedMarkdown: string = '';
    @PropSync('raw', { required: true })
    rawMarkdown!: string | null;

    @Prop({ default: 'accent', type: String })
    color!: string;

    @Prop({ default: '', type: String })
    innerClass!: string;

    @Watch('rawMarkdown')
    onRawMarkdownChange() {
        this.$fetch();
    }

    async fetch() {
        if (!this.rawMarkdown) return;
        this.renderedMarkdown = await this.$api
            .requestInternal<string>('pages/render', false, 'post', {
                content: this.rawMarkdown,
            })
            .catch<any>(this.$util.handleRequestError);
        this.loading = false;
    }
}
</script>

<style lang="scss">
@import 'assets/markdown';
</style>
