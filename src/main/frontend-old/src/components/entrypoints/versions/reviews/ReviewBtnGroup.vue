<template>
    <div class="col-md-12 header-flags">
        <div class="clearfix">
            <h1 class="float-left">
                <a :href="ROUTES.parse('VERSIONS_SHOW', project.ownerName, project.slug, version.versionStringUrl)" class="btn btn-primary">
                    <i class="fas fa-arrow-left"></i>
                </a>
                {{ project.name }}
                <i v-text="version.versionString"></i>
            </h1>
        </div>
        <p class="user date float-left">
            <a :href="ROUTES.parse('USERS_SHOW_PROJECTS', project.ownerName)">
                <strong v-text="project.ownerName"></strong>
            </a>
            released this version on {{ new Date(version.createdAt).toLocaleDateString() }} at {{ new Date(version.createdAt).toLocaleTimeString() }}
        </p>
        <div class="btn-toolbar float-right" role="toolbar" aria-label="Review Actions">
            <div v-if="!version.reviewState.checked">
                <div class="btn-group btn-group-sm mr-2" role="group" aria-label="Version Actions">
                    <a class="btn btn-info" @click.prevent="skip">
                        {{ version.reviewState.value !== 2 ? 'Remove from queue' : 'Add to queue' }}
                    </a>
                    <a :href="ROUTES.parse('PROJECTS_SHOW', project.ownerName, project.slug)" class="btn btn-info"> Project Page</a>
                    <a :href="ROUTES.parse('VERSIONS_DOWNLOAD_JAR', project.ownerName, project.slug, version.versionStringUrl)" class="btn btn-info"
                        >Download File</a
                    >
                </div>
                <div class="btn-group btn-group-sm">
                    <template v-if="mostRecentUnfinishedReview">
                        <template v-if="mostRecentIsCurrentUser">
                            <button class="btn btn-danger" data-toggle="modal" data-target="#modal-review-stop">
                                <i class="fas fa-stop-circle mr-1"></i> {{ $t('review.stop') }}
                            </button>
                            <button class="btn btn-success" @click="approve(false)">
                                <i class="fas fa-thumbs-up mr-1"></i> {{ $t('user.queue.approve') }}
                            </button>
                            <button class="btn btn-success" @click="approve(true)">
                                <i class="fas fa-thumbs-up mr-1"></i> {{ $t('user.queue.approvePartial') }}
                            </button>
                        </template>
                        <button v-else class="btn btn-danger" data-toggle="modal" data-target="#modal-review-takeover">
                            <i class="fas fa-clipboard mr-1"></i>{{ $t('review.takeover') }}
                        </button>
                    </template>
                    <button v-else class="btn btn-success" @click="start"><i class="fas fa-terminal mr-1"></i>{{ $t('review.start') }}</button>
                </div>
            </div>
            <div v-else>
                <span class="btn-group btn-group-sm">
                    <a class="btn btn-danger" @click.prevent="reopen"><i class="fas fa-undo mr-1"></i>Undo approval</a>
                </span>
            </div>
        </div>
    </div>
    <HangarModal target-id="modal-review-stop" label-id="modal-review-stop">
        <template v-slot:modal-content>
            <div class="modal-header">
                <h4 class="modal-title" v-text="$t('review.stop')"></h4>
                <button type="button" class="close" data-dismiss="modal" :aria-label="$t('general.close')">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                {{ $t('review.whystop') }}
                <br />
                <textarea v-model.trim="reason.stop" class="form-control" rows="3"></textarea>
            </div>
            <div class="modal-footer">
                <button class="btn btn-default" data-dismiss="modal" v-text="$t('general.cancel')"></button>
                <button class="btn btn-danger" @click="stop"><i class="fas fa-stop-circle"></i> {{ $t('review.stop') }}</button>
            </div>
        </template>
    </HangarModal>
    <HangarModal target-id="modal-review-takeover" label-id="modal-review-takeover">
        <template v-slot:modal-content>
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" :aria-label="$t('general.close')">
                    <span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title" v-text="$t('review.takeover')"></h4>
            </div>
            <div class="modal-body">
                {{ $t('review.whytakeover') }}
                <br />
                <textarea v-model="reason.takeover" class="form-control" rows="3"></textarea>
            </div>
            <div class="modal-footer">
                <button class="btn btn-default" data-dismiss="modal" v-text="$t('general.cancel')"></button>
                <button class="btn btn-review-takeover-submit btn-primary" @click="takeover">
                    <i class="fas fa-clipboard"></i> {{ $t('review.takeover') }}
                </button>
            </div>
        </template>
    </HangarModal>
</template>

<script>
import { toggleSpin } from '@/utils';
import axios from 'axios';
import { stringify } from 'querystring';
import HangarModal from '@/components/HangarModal';
import $ from 'jquery';
import 'bootstrap/js/dist/modal';

export default {
    name: 'ReviewBtnGroup',
    components: { HangarModal },
    data() {
        return {
            ROUTES: window.ROUTES,
            project: window.PROJECT,
            version: window.VERSION,
            mostRecentUnfinishedReview: window.MOST_RECENT_UNFINISHED_REVIEW,
            mostRecentIsCurrentUser: window.MOST_RECENT_IS_CURRENT_USER || false,
            reason: {
                stop: null,
                takeover: null,
            },
        };
    },
    methods: {
        skip() {
            axios
                .post(
                    this.ROUTES.parse('REVIEWS_BACKLOG_TOGGLE', this.project.ownerName, this.project.slug, this.version.versionStringUrl),
                    null,
                    window.ajaxSettings
                )
                .then(() => {
                    location.reload();
                });
        },
        stop(e) {
            const icon = e.target.querySelector('[data-fa-i2svg]');
            toggleSpin(icon).classList.toggle('fa-stop-circle');
            axios
                .post(
                    this.ROUTES.parse('REVIEWS_STOP_REVIEW', this.project.ownerName, this.project.slug, this.version.versionStringUrl),
                    stringify({
                        content: this.reason.stop,
                    }),
                    {
                        headers: {
                            'Content-Type': 'application/x-www-form-urlencoded;charset=utf-8',
                            [window.csrfInfo.headerName]: window.csrfInfo.token,
                        },
                    }
                )
                .then(() => {
                    location.reload();
                })
                .finally(() => {
                    toggleSpin(icon).classList.toggle('fa-times-circle-o');
                });
        },
        approve(partial = false) {
            const promises = [];
            promises.push(
                axios.post(
                    this.ROUTES.parse('REVIEWS_APPROVE_REVIEW', this.project.ownerName, this.project.slug, this.version.versionStringUrl),
                    null,
                    window.ajaxSettings
                )
            );
            const urlKey = partial ? 'VERSIONS_APPROVE_PARTIAL' : 'VERSIONS_APPROVE';
            promises.push(
                axios.post(this.ROUTES.parse(urlKey, this.project.ownerName, this.project.slug, this.version.versionStringUrl), null, window.ajaxSettings)
            );
            Promise.all(promises).then(() => {
                location.reload();
            });
        },
        takeover(e) {
            toggleSpin(e.target.querySelector('[data-fa-i2svg]')).classList.toggle('fa-clipboard');
            axios
                .post(
                    this.ROUTES.parse('REVIEWS_TAKEOVER_REVIEW', this.project.ownerName, this.project.slug, this.version.versionStringUrl),
                    stringify({ content: this.reason.takeover }),
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
        start(event) {
            toggleSpin(event.target.querySelector('[data-fa-i2svg]')).classList.toggle('fa-terminal');
            event.target.disabled = true;
            axios
                .post(
                    this.ROUTES.parse('REVIEWS_CREATE_REVIEW', this.project.ownerName, this.project.slug, this.version.versionStringUrl),
                    null,
                    window.ajaxSettings
                )
                .then(() => {
                    location.reload();
                })
                .finally(() => {
                    toggleSpin(event.target.querySelector('[data-fa-i2svg]')).classList.add('fa-terminal');
                });
        },
        reopen(e) {
            const icon = e.target.querySelector('[data-fa-i2svg]');
            toggleSpin(icon).classList.toggle('fa-undo');
            e.target.disabled = true;
            axios
                .post(
                    this.ROUTES.parse('REVIEWS_REOPEN_REVIEW', this.project.ownerName, this.project.slug, this.version.versionStringUrl),
                    null,
                    window.ajaxSettings
                )
                .then(() => {
                    location.reload();
                })
                .finally(() => {
                    toggleSpin(icon).classList.toggle('fa-undo');
                });
        },
    },
    mounted() {
        $('#modal-review-stop').on('shown.bs.modal', function () {
            $(this).find('textarea').focus();
        });
        $('#modal-review-takeover').on('shown.bs.modal', function () {
            $(this).find('textarea').focus();
        });
    },
};
</script>
