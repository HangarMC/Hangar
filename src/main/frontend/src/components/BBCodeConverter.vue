<template>
    <HangarModal target-id="convert-modal" modal-class="modal-xl">
        <template v-slot:activator>
            <button type="button" class="btn btn-secondary btn-block" data-toggle="modal" data-target="#convert-modal">
                Convert project description from Spigot
            </button>
        </template>
        <template v-slot:modal-content>
            <div class="modal-header">
                <h5 class="modal-title" id="convert-modal-label">Convert project description</h5>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <ul class="nav nav-tabs">
                    <li class="nav-item">
                        <a class="nav-link active" id="convert-tab" data-toggle="tab" href="#convert" role="tab" aria-controls="convert" aria-selected="true"
                            >Convert</a
                        >
                    </li>
                    <li class="nav-item" @click="showMDPreview">
                        <a class="nav-link" id="preview-tab" data-toggle="tab" href="#preview" role="tab" aria-controls="preview" aria-selected="false"
                            >Preview</a
                        >
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" id="howto-tab" data-toggle="tab" href="#getbbcode" role="tab" aria-controls="getbbcode" aria-selected="false"
                            >How to get the BBCode</a
                        >
                    </li>
                </ul>
                <div class="tab-content" id="convert-content">
                    <div class="tab-pane fade show active" id="convert" role="tabpanel" aria-labelledby="convert-tab">
                        <div class="container-fluid" id="converter-windows">
                            <textarea
                                v-model="input"
                                class="form-control"
                                id="input-window"
                                rows="12"
                                cols="50"
                                aria-label="Input"
                                placeholder="Paste your BBCode here:"
                            ></textarea>
                            <div class="py-3 text-center">
                                <button type="button" class="btn btn-primary" @click="convert">
                                    Convert
                                    <i class="ml-1 fas fa-arrows-alt-v"></i>
                                </button>
                            </div>
                            <textarea
                                v-model="output"
                                class="form-control"
                                id="output-window"
                                rows="12"
                                cols="50"
                                aria-label="Output"
                                placeholder="Output:"
                            ></textarea>
                        </div>
                    </div>
                    <div class="tab-pane fade" id="preview" role="tabpanel" aria-labelledby="preview-tab" v-html="outputHtml"></div>
                    <div class="tab-pane fade" id="getbbcode" role="tabpanel" aria-labelledby="howto-tab">
                        To get the BBCode of your Spigot project, do the following:
                        <br />1. Go to your project and click on "Edit Resource".
                        <img src="https://i.imgur.com/8CyLMf3.png" alt="Edit Project" />
                        <br /><br />Click on the wrench symbol in the description editor.
                        <img src="https://i.imgur.com/FLVIuQK.png" width="425" height="198" alt="Show BBCode" />
                        <br /><br />Copy paste the new contents into the upper converter texbox, do changes to the output if you like, and hit save!
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <div class="btn-group">
                    <button type="button" class="btn btn-primary" data-dismiss="modal" :disabled="!output" @click="saveOutput">Save and Close</button>
                    <button type="button" class="btn btn-warning" data-dismiss="modal">Close</button>
                </div>
            </div>
        </template>
    </HangarModal>
</template>

<script>
import axios from 'axios';

import HangarModal from '@/components/HangarModal';
export default {
    name: 'BBCodeConverter',
    emits: ['update:proj-page-content'],
    components: { HangarModal },
    props: {
        projPageContent: String,
    },
    data() {
        return {
            input: '',
            output: '',
            outputHtml: '',
        };
    },
    methods: {
        convert() {
            axios
                .post(
                    window.ROUTES.parse('PAGES_BB_CONVERT'),
                    {
                        raw: this.input,
                    },
                    window.ajaxSettings
                )
                .then((res) => {
                    this.output = res.data;
                });
        },
        showMDPreview() {
            axios
                .post(
                    window.ROUTES.parse('PAGES_SHOW_PREVIEW'),
                    {
                        raw: this.output,
                    },
                    window.ajaxSettings
                )
                .then((res) => {
                    this.outputHtml = res.data;
                });
        },
        saveOutput() {
            this.$emit('update:proj-page-content', this.output);
        },
    },
};
</script>
