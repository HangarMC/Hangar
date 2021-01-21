<template>
    <div class="btn-group">
        <button
            class="btn btn-sm btn-light dropdown-toggle"
            id="visibility-actions"
            type="button"
            data-toggle="dropdown"
            aria-haspopup="true"
            aria-expanded="false"
        >
            <i class="fas fa-eye"></i> Change Visibility
        </button>
        <div class="dropdown-menu" aria-labelledby="visibility-actions">
            <a
                v-for="visibility in visibilityValues"
                :key="visibility.name"
                href="#"
                @click.prevent="
                    visibility.showModal ? (selected = visibility.name) : visibility.name === projectVisibility ? '' : changeVisibility(visibility.name)
                "
                class="dropdown-item"
                :data-toggle="visibility.showModal ? 'modal' : ''"
                :data-target="visibility.showModal ? '#modal-visibility-comment' : ''"
            >
                <i v-show="visibility.name === projectVisibility" class="fa fa-check text-dark"></i>
                {{ $t(`visibility.name.${visibility.name}`) }}
            </a>
        </div>
        <HangarModal target-id="modal-visibility-comment" label-id="modal-visibility-comment">
            <template v-slot:modal-content>
                <div class="modal-header">
                    <h4 class="modal-title">Comment</h4>
                    <button type="button" class="close" data-dismiss="modal" :aria-label="$t('general.close')">
                        <span aria-hidden="true">&times;</span>
                    </button>
                </div>
                <div class="modal-body">
                    <textarea v-model.trim="comment" class="form-control" rows="3" aria-labelledby="modal-visibility-comment"></textarea>
                </div>
                <div class="modal-footer">
                    <div class="btn-group">
                        <button class="btn btn-default" data-dismiss="modal" v-text="$t('general.close')"></button>
                        <button class="btn btn-primary" :disabled="!comment" @click="changeVisibility(selected)">
                            <i class="fas fa-pencil-alt"></i> Submit
                        </button>
                    </div>
                </div>
            </template>
        </HangarModal>
    </div>
</template>

<script>
import axios from 'axios';
import { stringify } from 'qs';

import { Visibility } from '@/enums';
import HangarModal from '@/components/HangarModal';

export default {
    name: 'BtnHide',
    components: { HangarModal },
    props: {
        namespace: String,
        projectVisibility: String,
    },
    data() {
        return {
            selected: null,
            visibilityValues: Visibility.values,
            comment: null,
        };
    },
    methods: {
        changeVisibility(visibility) {
            const url = `/${this.namespace}/visible/${visibility}`;
            axios
                .post(
                    url,
                    stringify({
                        comment: this.comment,
                    }),
                    {
                        headers: {
                            [window.csrfInfo.headerName]: window.csrfInfo.token,
                            'content-type': 'application/x-www-form-urlencoded;charset=utf-8',
                        },
                    }
                )
                .then(() => {
                    location.reload();
                })
                .catch(() => {
                    console.error('Error changing project visibility');
                });
        },
    },
};
</script>
