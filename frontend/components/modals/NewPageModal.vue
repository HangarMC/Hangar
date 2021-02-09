<template>
    <v-dialog v-model="dialog" max-width="500" persistent>
        <template #activator="{ on }">
            <v-btn icon class="primary" v-on="on"><v-icon>mdi-plus</v-icon></v-btn>
        </template>
        <v-card>
            <v-card-title>{{ $t('page.new.title') }}</v-card-title>
            <v-card-text>
                <v-form ref="pageForm" v-model="form.valid">
                    <v-text-field v-model.trim="form.name" filled :rules="[$util.$vc.require('Page name')]" :label="$t('page.new.name')" />
                    <v-select v-model="form.parent" :items="pages" filled clearable :label="$t('page.new.parent')" item-text="name" item-value="id" />
                </v-form>
            </v-card-text>
            <v-card-actions class="justify-end">
                <v-btn color="error" text @click="dialog = false">{{ $t('general.close') }}</v-btn>
                <v-btn color="success" :disabled="!form.valid" :loading="loading" @click="createPage">{{ $t('general.create') }}</v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>

<script lang="ts">
import { Component, Prop, Vue, Watch } from 'nuxt-property-decorator';
import { PropType } from 'vue';
import { HangarProjectPage } from 'hangar-internal';

@Component
export default class NewPageModal extends Vue {
    dialog = false;
    loading = false;
    form = {
        valid: false,
        name: '',
        parent: null as number | null,
    };

    @Prop({ type: Number, required: true })
    projectId!: number;

    @Prop({ type: Array as PropType<HangarProjectPage[]>, required: true })
    pages!: HangarProjectPage[];

    @Watch('dialog')
    onToggle() {
        if (typeof this.$refs.pageForm !== 'undefined') {
            // @ts-ignore // TODO how to fix this?
            this.$refs.pageForm.reset();
        }
    }

    createPage() {
        this.loading = true;
        this.$api
            .requestInternal<string>(`pages/create/${this.projectId}`, true, 'post', {
                name: this.form.name,
                parentId: this.form.parent,
            })
            .then((slug) => {
                this.dialog = false;
                this.$nuxt.refresh(); // Might be needed to refresh the HangarProject w/the new page? idk - jake
                this.$router.push(`/${this.$route.params.author}/${this.$route.params.slug}/pages/${slug}`);
            })
            .catch(this.$util.handleRequestError)
            .finally(() => {
                this.loading = false;
            });
    }
}
</script>
