<template>
    <v-dialog v-model="dialog" max-width="500" persistent>
        <template #activator="{ on }">
            <v-btn v-if="pageRoots.length < validations.project.maxPageCount" icon class="primary" :class="activatorClass" v-on="on">
                <v-icon>mdi-plus</v-icon>
            </v-btn>
        </template>
        <v-card>
            <v-card-title>{{ $t('page.new.title') }}</v-card-title>
            <v-card-text>
                <v-form ref="modalForm" v-model="validForm">
                    <v-text-field
                        v-model.trim="form.name"
                        filled
                        :loading="validateLoading"
                        :rules="[
                            $util.$vc.required($t('page.new.name')),
                            $util.$vc.regex($t('page.new.name'), validations.project.pageName.regex),
                            $util.$vc.maxLength(validations.project.pageName.max),
                            $util.$vc.minLength(validations.project.pageName.min),
                        ]"
                        :error-messages="nameErrorMessages"
                        :label="$t('page.new.name')"
                    />
                    <v-select
                        v-model="form.parent"
                        :items="pageRoots"
                        filled
                        clearable
                        hide-details
                        :label="$t('page.new.parent')"
                        item-text="name"
                        item-value="id"
                    />
                </v-form>
            </v-card-text>
            <v-card-actions class="justify-end">
                <v-btn color="error" text @click="dialog = false">
                    {{ $t('general.close') }}
                </v-btn>
                <v-btn color="success" :disabled="!validForm" :loading="loading" @click="createPage">
                    {{ $t('general.create') }}
                </v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>

<script lang="ts">
import { Component, Prop, Watch } from 'nuxt-property-decorator';
import { PropType } from 'vue';
import { HangarProjectPage } from 'hangar-internal';
import { TranslateResult } from 'vue-i18n';
import { AxiosError } from 'axios';
import { HangarFormModal } from '~/components/mixins';

@Component
export default class NewPageModal extends HangarFormModal {
    validateLoading = false;
    nameErrorMessages: TranslateResult[] = [];
    form = {
        name: '',
        parent: null as number | null,
    };

    @Prop({ type: Number, required: true })
    projectId!: number;

    @Prop({ type: Array as PropType<HangarProjectPage[]>, required: true })
    pages!: HangarProjectPage[];

    get pageRoots(): HangarProjectPage[] {
        return this.flatDeep(this.pages);
    }

    // TODO these should be sorted into somewhat of an order
    flatDeep(pages: HangarProjectPage[]): HangarProjectPage[] {
        let ps: HangarProjectPage[] = [];
        for (const page of pages) {
            if (page.children.length > 0) {
                ps = ps.concat(this.flatDeep(page.children));
            }
            ps.push(page);
        }
        return ps;
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
                this.$nuxt.refresh();
                this.$router.push(`/${this.$route.params.author}/${this.$route.params.slug}/pages/${slug}`);
            })
            .catch(this.$util.handleRequestError)
            .finally(() => {
                this.loading = false;
            });
    }

    @Watch('form', { deep: true })
    checkName(val: NewPageModal['form']) {
        if (!val.name) return;
        this.validateLoading = true;
        this.nameErrorMessages = [];
        this.$api
            .requestInternal('pages/checkName', true, 'get', {
                projectId: this.projectId,
                name: val.name,
                parentId: val.parent,
            })
            .catch((err: AxiosError) => {
                if (!err.response?.data.isHangarApiException) {
                    return this.$util.handleRequestError(err);
                }
                this.nameErrorMessages.push(this.$t(err.response.data.message));
            })
            .finally(() => {
                this.validateLoading = false;
            });
    }
}
</script>
