<template>
    <HangarModal :title="$t('visibility.modal.title', [type])" :submit="submit" no-form :submit-disabled="disableSubmit">
        <template #activator="{ on, attrs }">
            <v-btn :small="smallBtn" v-bind="attrs" color="warning" class="mr-1" :class="activatorClass" v-on="on">
                <v-icon :small="smallBtn" left> mdi-eye </v-icon>
                {{ $t('visibility.modal.activatorBtn') }}
            </v-btn>
        </template>
        <v-radio-group v-model="formVisibility" mandatory>
            <v-radio v-for="vis in visibilities" :key="vis.name" :value="vis.name" :label="$t(vis.title)" />
        </v-radio-group>
        <v-form ref="reasonForm" v-model="validForm">
            <!-- TODO this should be a markdown editor since the reason is rendered in markdown-->
            <v-textarea
                v-if="showTextarea"
                v-model.trim="reason"
                filled
                hide-details
                auto-grow
                rows="2"
                :rules="[$util.$vc.required()]"
                :label="$t('visibility.modal.reason')"
            />
        </v-form>
    </HangarModal>
</template>

<script lang="ts">
import { Component, Prop, Watch } from 'nuxt-property-decorator';
import { IVisibility } from 'hangar-internal';
import { PropType } from 'vue';
import { HangarFormModal } from '~/components/mixins';
import { Visibility } from '~/types/enums';
import HangarModal from '~/components/modals/HangarModal.vue';

@Component({
    components: { HangarModal },
})
export default class VisibilityChangerModal extends HangarFormModal {
    @Prop({ type: String as PropType<Visibility>, required: true })
    propVisibility!: Visibility;

    @Prop({ type: String as PropType<'project' | 'version'>, required: true })
    type!: 'project' | 'version';

    @Prop({ type: String, required: true })
    postUrl!: string;

    @Prop({ type: Boolean, default: false })
    smallBtn!: boolean;

    visibilities: IVisibility[] = [];
    formVisibility: Visibility = this.propVisibility;
    reason: string = '';

    $refs!: {
        reasonForm: any;
    };

    submit(): Promise<void> {
        return this.$api
            .requestInternal(this.postUrl, true, 'post', {
                visibility: this.formVisibility,
                comment: this.currentIVis.showModal ? this.reason : null,
            })
            .then(() => {
                this.reason = '';
                this.$util.success(this.$t('visibility.modal.success', [this.type, this.$t(this.currentIVis?.title)]));
                this.$nuxt.refresh();
                this.$auth.refreshUser();
            })
            .catch(this.$util.handleRequestError);
    }

    @Watch('currentVisibility')
    onPropChange() {
        this.formVisibility = this.propVisibility;
        this.$refs.reasonForm.resetValidation();
    }

    get disableSubmit(): boolean {
        return this.propVisibility === this.formVisibility || !this.validForm;
    }

    get showTextarea(): boolean {
        return this.currentIVis?.showModal && this.propVisibility !== this.formVisibility;
    }

    get currentIVis(): IVisibility {
        return this.visibilities.find((v) => v.name === this.formVisibility)!;
    }

    async fetch() {
        this.visibilities = (await this.$api.requestInternal<IVisibility[]>('data/visibilities', false).catch<any>(this.$util.handlePageRequestError)) || [];
    }
}
</script>

<style lang="scss" scoped></style>
