<template>
    <template v-if="enabled">
        <!-- Edit -->

        <button
            type="button"
            class="btn btn-sm btn-edit btn-page btn-default"
            :class="{ open: editing && !previewing }"
            :title="$t('general.edit')"
            @click.stop="edit"
        >
            <i class="fas fa-edit"></i><span v-text="$t('general.edit')"></span>
        </button>

        <!-- Preview -->
        <transition name="button-hide">
            <div v-if="editing" class="btn-edit-container btn-preview-container" :class="{ open: previewing }" :title="$t('general.preview')">
                <button type="button" class="btn btn-sm btn-preview btn-page btn-default" @click.stop="preview">
                    <i v-show="loading.preview" class="fas fa-loading"></i>
                    <i v-show="!loading.preview" class="fas fa-eye"></i>
                </button>
            </div>
        </transition>

        <transition name="button-hide">
            <div v-if="saveable && editing" class="btn-edit-container btn-save-container" :title="$t('general.save')">
                <button form="form-editor-save" type="submit" class="btn btn-sm btn-save btn-page btn-default">
                    <i class="fas fa-save"></i>
                </button>
            </div>
        </transition>

        <transition name="button-hide">
            <div v-if="cancellable && editing" class="btn-edit-container btn-cancel-container" :title="$t('general.cancel')">
                <button type="button" class="btn btn-sm btn-cancel btn-page btn-default" @click.stop="cancel">
                    <i class="fas fa-times"></i>
                </button>
            </div>
        </transition>

        <transition name="button-hide">
            <template v-if="deletable && editing">
                <div class="btn-edit-container btn-delete-container" :title="$t('general.delete')">
                    <button
                        type="button"
                        class="btn btn-sm btn-page-delete btn-page btn-default"
                        data-toggle="modal"
                        data-target="#modal-page-delete"
                        style="color: var(--danger)"
                    >
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </template>
        </transition>

        <!-- Edit window -->
        <div v-show="editing && !previewing" class="page-edit page-content-view" style="padding-top: 10px">
            <textarea name="content" class="form-control" :form="targetForm || 'form-editor-save'" v-model="content"></textarea>
        </div>
        <div v-show="editing && previewing" class="page-preview page-rendered page-content-view" v-html="previewContent" style="margin-top: 10px"></div>

        <HangarForm v-if="saveable" method="post" :action="saveCall" id="form-editor-save">
            <input v-if="extraFormValue" type="hidden" :value="extraFormValue" name="name" />
        </HangarForm>

        <teleport to="body">
            <div v-if="deletable" class="modal fade" id="modal-page-delete" tabindex="-1" role="dialog" aria-labelledby="label-page-delete">
                <div class="modal-dialog" role="document">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h4 class="modal-title" id="label-page-delete">Delete {{ subject.toLowerCase() }}</h4>
                            <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                                <span aria-hidden="true">&times;</span>
                            </button>
                        </div>
                        <div class="modal-body">Are you sure you want to delete this {{ subject.toLowerCase() }}? This cannot be undone.</div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                            <HangarForm method="post" :action="deleteCall" clazz="form-inline">
                                <button type="submit" class="btn btn-danger">Delete</button>
                            </HangarForm>
                        </div>
                    </div>
                </div>
            </div>
        </teleport>
    </template>
    <div v-show="!editing" class="page-content page-rendered page-content-view" v-html="preCooked"></div>
</template>
<script>
import { nextTick } from 'vue';
import HangarForm from '@/components/HangarForm';
import axios from 'axios';

export default {
    name: 'Editor',
    components: {
        HangarForm,
    },
    emits: ['update:contentProp'],
    props: {
        saveCall: String,
        deleteCall: String,
        saveable: {
            type: Boolean,
            default: true,
        },
        deletable: Boolean,
        enabled: {
            type: Boolean,
            default: true,
        },
        raw: String,
        subject: String,
        cancellable: {
            type: Boolean,
            default: true,
        },
        targetForm: String,
        extraFormValue: String,
        contentProp: String,
        open: Boolean,
    },
    computed: {
        content: {
            get() {
                if (typeof this.contentProp !== 'undefined') {
                    return this.contentProp;
                } else {
                    return this.inputContent;
                }
            },
            set(val) {
                if (typeof this.contentProp !== 'undefined') {
                    this.$emit('update:contentProp', val);
                } else {
                    this.inputContent = val;
                }
            },
        },
    },
    data() {
        return {
            editing: false,
            previewing: false,
            inputContent: null,
            preCooked: null,
            previewContent: null,
            loading: {
                preview: false,
            },
        };
    },
    methods: {
        edit() {
            this.editing = true;
            this.previewing = false;
        },
        async preview() {
            this.previewing = true;
            this.previewContent = await this.getPreview(this.content);
        },
        async cancel() {
            this.previewing = false;
            await nextTick();
            this.editing = false;
        },
        async getPreview(content) {
            if (content && content.trim().length > 0) {
                this.loading.preview = true;
                const res = await axios.post(
                    '/pages/preview',
                    { raw: content },
                    {
                        headers: {
                            [window.csrfInfo.headerName]: window.csrfInfo.token,
                        },
                    }
                );
                this.loading.preview = false;
                return res.data;
            } else {
                return '';
            }
        },
    },
    async created() {
        this.content = this.raw;
        this.preCooked = await this.getPreview(this.raw);
        if (this.open) {
            this.edit();
        }
    },
};
</script>
<style lang="scss" scoped>
@import '../scss/utils';

.btn-edit-container {
    margin-left: -34px;
    z-index: -1;

    &.open {
        z-index: 0;
    }

    &.open .btn-page {
        border-right-color: white;
    }

    &:not(.open) .btn-page {
        border: 1px solid #ccc;
    }
}
.btn-edit {
    @include towardsBottomRight(-47px, 20px);
    position: absolute;

    &.open {
        border-right-color: white;
    }

    &:not(.open) {
        border: 1px solid #ccc;
    }
}

.page-edit textarea {
    resize: vertical;
}

button.open:hover {
    background-color: white;
    cursor: default;
    border-color: $light;
}

.btn-page {
    @include transition6(border-color, ease-in-out, 0.15s, box-shadow, ease-in-out, 0.15s);
    border-radius: 4px 0 0 4px;
    padding: 6px;
    z-index: 1000;
}

.btn-page:focus {
    outline: none;
}

.btn-edit-container {
    position: absolute;
    overflow: hidden;
}

.btn-edit-container .btn {
    width: 35px;
}

.btn-preview-container {
    margin-top: 58px;
}
.btn-save-container {
    margin-top: 96px;
}
.btn-cancel-container {
    margin-top: 135px;
}
.btn-delete-container {
    margin-top: 175px;
}

.btn-page-delete {
    color: #c12e2a;
}

.button-hide-enter-active {
    transition: all 0.3s ease-out;
}

.button-hide-leave-active {
    transition: all 0.3s ease-in;
}

.button-hide-leave-to {
    z-index: -1;
}

.button-hide-enter-from,
.button-hide-leave-to {
    transform: translateX(34px);
}
</style>
