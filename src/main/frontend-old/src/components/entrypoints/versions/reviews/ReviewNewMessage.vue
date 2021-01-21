<template>
    <div class="col-md-12">
        <div class="input-group" style="width: 100%; margin-top: 1em; border-radius: 4px 4px 0 0">
            <div class="input-group-prepend">
                <span class="input-group-text">Review Message</span>
            </div>
            <textarea v-model.trim="message" class="form-control" autocomplete="off" aria-label="Review Message"></textarea>
            <div class="input-group-append">
                <button class="btn btn-primary" @click="addMessage" :disabled="!message">
                    <i class="fas fa-clipboard"></i>
                    {{ $t('review.addmessage') }}
                </button>
            </div>
        </div>
    </div>
</template>

<script>
import axios from 'axios';
import { stringify } from 'querystring';
import { toggleSpin } from '@/utils';

export default {
    name: 'ReviewNewMessage',
    data() {
        return {
            ROUTES: window.ROUTES,
            project: window.PROJECT,
            version: window.VERSION,
            message: null,
        };
    },
    methods: {
        addMessage(e) {
            toggleSpin(e.target.querySelector('[data-fa-i2svg]')).classList.toggle('fa-clipboard');
            axios
                .post(
                    this.ROUTES.parse('REVIEWS_ADD_MESSAGE', this.project.ownerName, this.project.slug, this.version.versionStringUrl),
                    stringify({ content: this.message }),
                    {
                        headers: {
                            'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8',
                            [window.csrfInfo.headerName]: window.csrfInfo.token,
                        },
                    }
                )
                .then(() => {
                    location.reload();
                });
        },
    },
};
</script>
