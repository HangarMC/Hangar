<template>
    <v-card>
        <v-card-text>
            <v-textarea v-model="converter.bbCode" hide-details dense :rows="6" filled :label="$t('project.new.step4.convertLabels.bbCode')" />

            <div>
                <v-btn block color="primary" class="my-3" :loading="converter.loading" @click="convertBBCode">
                    <v-icon left large>mdi-chevron-double-down</v-icon>
                    {{ $t('project.new.step4.convert') }}
                    <v-icon right large>mdi-chevron-double-down</v-icon>
                </v-btn>
            </div>

            <v-textarea v-model="converter.markdown" hide-details dense :rows="6" filled :label="$t('project.new.step4.convertLabels.output')" />

            <div class="mb-4 mt-4">
                {{ $t('project.new.step4.preview') }}
            </div>

            <Markdown :raw="converter.markdown" />
        </v-card-text>
    </v-card>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { HangarComponent } from '~/components/mixins';
import Markdown from '~/components/markdown/Markdown.vue';
@Component({
    components: { Markdown },
})
export default class BBCodePage extends HangarComponent {
    converter = {
        bbCode: '',
        markdown: '',
        loading: false,
    };

    head() {
        return this.$seo.head('BBCode Converter', null, this.$route, null);
    }

    convertBBCode() {
        this.converter.loading = true;
        this.$api
            .requestInternal<string>('pages/convert-bbcode', false, 'post', {
                content: this.converter.bbCode,
            })
            .then((markdown) => {
                this.converter.markdown = markdown;
            })
            .catch(this.$util.handleRequestError)
            .finally(() => {
                this.converter.loading = false;
            });
    }
}
</script>

<style scoped></style>
