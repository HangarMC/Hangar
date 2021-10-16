<template>
    <HangarModal max-width="400" :submit-label="$t('general.confirm')" :title="title" :submit="submit0">
        <template #activator="props">
            <slot name="activator" v-bind="props" />
        </template>
        <v-textarea
            v-if="comment"
            v-model.trim="commentText"
            :label="$t('general.comment')"
            :rules="[$util.$vc.required()]"
            hide-details
            dense
            :rows="2"
            auto-grow
            autofocus
            filled
        />
        <slot />
    </HangarModal>
</template>

<script lang="ts">
import { Component, Prop } from 'nuxt-property-decorator';
import HangarModal from './HangarModal.vue';
import { HangarComponent } from '~/components/mixins';

@Component({
    components: { HangarModal },
})
export default class ConfirmModal extends HangarComponent {
    commentText: string = '';

    @Prop({ type: Boolean, default: false })
    comment!: boolean;

    @Prop({ type: String, required: true })
    title!: string;

    @Prop({ type: Function, required: true })
    submit!: (comment?: string) => Promise<void>;

    submit0() {
        return this.submit(this.commentText);
    }
}
</script>
