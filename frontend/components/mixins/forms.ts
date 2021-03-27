import { Component, mixins, Prop, Watch } from 'nuxt-property-decorator';
import { HangarComponent } from './base';

@Component
export class HangarModal extends HangarComponent {
    dialog: boolean = false;

    @Prop({ type: String, default: '' })
    activatorClass!: string;

    @Watch('dialog')
    onToggleView(val: boolean) {
        if (!val) {
            this.$nextTick(() => {
                if (document.activeElement instanceof HTMLElement) {
                    document.activeElement.blur();
                }
                if (typeof this.$refs.modalForm !== 'undefined') {
                    // @ts-ignore
                    this.$refs.modalForm.reset();
                }
            });
        }
    }
}

@Component
export class HangarForm extends HangarComponent {
    loading: boolean = false;
    validForm: boolean = false;
}

@Component
export class HangarFormModal extends mixins(HangarModal, HangarForm) {}
