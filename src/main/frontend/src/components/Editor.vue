<template>
    <template v-if="enabled">
        <!-- Edit -->
        <button type="button" class="btn btn-sm btn-edit btn-page btn-default" title="Edit" @click.stop="pageBtnClick($event.currentTarget)">
            <i class="fas fa-edit"></i> Edit
        </button>

        <!-- Preview -->
        <div class="btn-edit-container btn-preview-container" title="Preview">
            <button type="button" class="btn btn-sm btn-preview btn-page btn-default" @click.stop="pageBtnClick($event.currentTarget)">
                <i class="fas fa-eye"></i>
            </button>
        </div>

        <div v-if="saveable" class="btn-edit-container btn-save-container" title="Save">
            <button form="form-editor-save" type="submit" class="btn btn-sm btn-save btn-page btn-default" @click.stop="pageBtnClick($event.currentTarget)">
                <i class="fas fa-save"></i>
            </button>
        </div>

        <div v-if="cancellable" class="btn-edit-container btn-cancel-container" title="Cancel">
            <button
                type="button"
                class="btn btn-sm btn-cancel btn-page btn-default"
                @click.stop="
                    btnCancel();
                    pageBtnClick($event.currentTarget);
                "
            >
                <i class="fas fa-times"></i>
            </button>
        </div>

        <template v-if="deletable">
            <div class="btn-edit-container btn-delete container" title="Delete">
                <button
                    type="button"
                    class="btn btn-sm btn-page-delete btn-page btn-default"
                    data-toggle="modal"
                    data-target="#modal-page-delete"
                    @click.stop="pageBtnClick($event.currentTarget)"
                >
                    <i class="fas fa-trash"></i>
                </button>
            </div>

            <div class="modal fade" id="modal-page-delete" tabindex="-1" role="dialog" aria-labelledby="label-page-delete">
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
        </template>

        <!-- Edit window -->
        <div class="page-edit version-content-view" style="display: none">
            <textarea name="content" class="form-control" :form="targetForm || 'form-editor-save'" v-model="content"></textarea>
        </div>

        <!-- Preview window -->
        <div class="page-preview page-rendered version-content-view" style="display: none"></div>

        <HangarForm v-if="saveable" method="post" :action="saveCall" id="form-editor-save">
            <input v-if="extraFormValue" type="hidden" :value="extraFormValue" name="name" />
        </HangarForm>

        <div class="page-content page-rendered">{{ cooked }}</div>
    </template>
</template>
<script>
import HangarForm from '@/components/HangarForm';
import $ from 'jquery';
import axios from 'axios';
import { toggleSpinner } from '@/utils';

export default {
    name: 'Editor',
    components: {
        HangarForm,
    },
    emits: ['update:contentProp'],
    props: {
        saveCall: String,
        deleteCall: String,
        saveable: Boolean,
        deletable: Boolean,
        enabled: Boolean,
        raw: String,
        subject: String,
        cancellable: Boolean,
        targetForm: String,
        extraFormValue: String,
        contentProp: String,
    },
    computed: {
        content: {
            get() {
                return this.contentProp;
            },
            set(val) {
                this.$emit('update:contentProp', val);
            },
        },
    },
    data() {
        return {
            editing: false,
            previewing: false,
            cooked: null,
        };
    },
    methods: {
        showEditBtn(e) {
            return new Promise((resolve) => {
                this.animateEditBtn(e, '-34px', function () {
                    e.css('z-index', '1000');
                    resolve();
                });
            });
        },
        animateEditBtn(e, marginLeft) {
            return new Promise((resolve) => {
                e.animate({ marginLeft: marginLeft }, 100, function () {
                    resolve();
                });
            });
        },
        hideEditBtn(e, andThen) {
            this.animateEditBtn(e, '0', andThen);
        },
        async getPreview(raw, target) {
            toggleSpinner($(target).find('[data-fa-i2svg]').toggleClass('fa-eye'));
            const res = await axios.post(
                '/pages/preview',
                { raw },
                {
                    headers: {
                        [window.csrfInfo.headerName]: window.csrfInfo.token,
                    },
                }
            );
            toggleSpinner($(target).find('[data-fa-i2svg]').toggleClass('fa-eye'));
            return res.data;
        },
        btnCancel() {
            this.editing = false;
            this.previewing = false;

            // hide editor; show content
            $('.page-edit').hide();
            $('.page-preview').hide();
            $('.page-content').show();

            // move buttons behind
            $('.btn-edit-container').css('z-index', '-1000');

            // hide buttons
            const fromSave = function () {
                this.hideEditBtn($('.btn-save-container'), function () {
                    this.hideEditBtn($('.btn-preview-container'));
                });
            };

            var btnDelete = $('.btn-delete-container');
            var btnCancel = $('.btn-cancel-container');
            if (btnDelete.length) {
                this.hideEditBtn(btnDelete, function () {
                    this.hideEditBtn(btnCancel, fromSave);
                });
            } else {
                this.hideEditBtn(btnCancel, fromSave);
            }
        },
        async pageBtnClick(target) {
            if ($(target).hasClass('open')) return;
            $('button.open').removeClass('open').css('border', '1px solid #ccc');
            $(target).addClass('open').css('border-right-color', 'white');

            const otherBtns = $('.btn-edit-container');
            const editor = $('.page-edit');
            if ($(target).hasClass('btn-edit')) {
                this.editing = true;
                this.previewing = false;
                $(this).css('position', 'absolute').css('top', '');
                $(otherBtns).css('position', 'absolute').css('top', '');

                // open editor
                var content = $('.page-rendered');
                editor.find('textarea').css('height', content.css('height'));
                content.hide();
                editor.show();

                // show buttons
                this.showEditBtn($('.btn-preview-container'))
                    .then(() => {
                        return this.showEditBtn($('.btn-save-container'));
                    })
                    .then(() => {
                        return this.showEditBtn($('.btn-cancel-container'));
                    })
                    .then(() => {
                        return this.showEditBtn($('.btn-delete-container'));
                    });
            } else if ($(target).hasClass('btn-preview')) {
                // render markdown
                const preview = $('.page-preview');
                const raw = editor.find('textarea').val();
                editor.hide();
                preview.show();

                preview.html(await this.getPreview(raw));

                this.editing = false;
                this.previewing = true;
            } else if ($(target).hasClass('btn-save')) {
                // add spinner
                toggleSpinner($(target).find('[data-fa-i2svg]').toggleClass('fa-save'));
            }
        },
    },
    async created() {
        this.content = this.raw;
        this.cooked = await this.getPreview(this.raw);
    },
    mounted() {
        const btnEdit = $('.btn-edit');
        if (!btnEdit.length) return;

        const otherBtns = $('.btn-edit-container');

        const editText = $('.page-edit').find('textarea');
        editText
            .focus(() => {
                btnEdit
                    .css('border-color', '#66afe9')
                    .css('border-right', '1px solid white')
                    .css('box-shadow', 'inset 0 1px 1px rgba(0,0,0,.075), -3px 0 8px rgba(102, 175, 233, 0.6)');
                otherBtns.find('.btn').css('border-right-color', '#66afe9');
            })
            .blur(() => {
                $('.btn-page').css('border', '1px solid #ccc').css('box-shadow', 'none');
                $('button.open').css('border-right', 'white');
            });
    },
};
</script>
<style lang="scss" scoped>
.btn-edit,
.btn-edit-container {
    position: absolute;
    //margin-left: -34px;
    z-index: 1000;
}
</style>
