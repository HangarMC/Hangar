<template>
    <template v-if="pendingVersion">
        <div class="plugin-meta">
            <div class="row justify-content-around">
                <div class="input-group meta-info col-xl-3 col-lg-3 col-12">
                    <div class="input-group-prepend">
                        <label for="version-string-input" class="input-group-text">Version</label>
                    </div>
                    <input
                        type="text"
                        id="version-string-input"
                        class="form-control"
                        required
                        v-model="payload.versionString"
                        :disabled="pendingVersion.versionString"
                    />
                </div>
                <template v-if="pendingVersion.fileName && !pendingVersion.externalUrl">
                    <div class="input-group meta-info col-xl-6 col-lg-5 col-12">
                        <div class="input-group-prepend">
                            <label for="file-name-input" class="input-group-text">File name</label>
                        </div>
                        <input id="file-name-input" type="text" class="form-control" :value="pendingVersion.fileName" disabled />
                    </div>
                    <div class="input-group meta-info col-xl-3 col-lg-4 col-12">
                        <div class="input-group-prepend">
                            <label for="file-size-input" class="input-group-text">File size</label>
                        </div>
                        <input id="file-size-input" type="text" class="form-control" :value="filesize(pendingVersion.fileSize)" disabled />
                    </div>
                </template>
                <div v-else class="col-xl-9 col-lg-9 col-12 input-group meta-info">
                    <div class="input-group-prepend">
                        <label for="external-url-input" class="input-group-text">External URL</label>
                    </div>
                    <input id="external-url-input" class="form-control" type="text" required v-model="payload.externalUrl" />
                </div>
            </div>
            <div class="row justify-content-around">
                <div class="col-xl-4 col-lg-9 col-md-6 col-12 input-group">
                    <div class="input-group-prepend">
                        <label for="select-channel" class="input-group-text">Channel</label>
                    </div>
                    <select id="select-channel" class="custom-select" v-model="payload.channel" :style="{ backgroundColor: payload.channel.color.hex }">
                        <option
                            v-for="channel in channels"
                            :key="channel.id"
                            :value="{ name: channel.name, color: channel.color, nonReviewed: channel.nonReviewed }"
                        >
                            {{ channel.name }}
                        </option>
                    </select>
                    <div class="input-group-append">
                        <NewChannel
                            v-model:name-prop="createdChannel.name"
                            v-model:color-prop="createdChannel.color"
                            v-model:non-reviewed-prop="createdChannel.nonReviewed"
                            @channel-created="setChannel"
                        >
                            <template v-slot:activator="slotProps">
                                <button
                                    class="btn btn-outline-primary"
                                    type="button"
                                    data-toggle="modal"
                                    :data-target="`#${slotProps.targetId}`"
                                    @click.prevent
                                >
                                    <i class="fas fa-plus"></i>
                                </button>
                            </template>
                        </NewChannel>
                    </div>
                </div>
                <div class="input-group meta-info meta-info-checkbox col-xl-2 col-md-4 col-6">
                    <div class="input-group-prepend">
                        <label for="is-unstable-version" class="input-group-text">
                            <span>Unstable</span>
                        </label>
                    </div>
                    <div class="input-group-append">
                        <div class="input-group-text">
                            <input id="is-unstable-version" class="form-check-input" type="checkbox" v-model="payload.unstable" />
                        </div>
                    </div>
                </div>
                <div class="input-group meta-info meta-info-checkbox col-xl-3 col-md-4 col-6">
                    <div class="input-group-prepend">
                        <label for="is-recommended-version" class="input-group-text">
                            <span>Recommended</span>
                        </label>
                    </div>
                    <div class="input-group-append">
                        <div class="input-group-text">
                            <input id="is-recommended-version" class="form-check-input" type="checkbox" v-model="payload.recommended" />
                        </div>
                    </div>
                </div>
                <div class="input-group meta-info meta-info-checkbox col-xl-2 col-md-4 col-6">
                    <div class="input-group-prepend">
                        <label for="create-forum-post-version" class="input-group-text">
                            <span>Forum Post</span>
                        </label>
                    </div>
                    <div class="input-group-append">
                        <div class="input-group-text">
                            <input id="create-forum-post-version" class="form-check-input" type="checkbox" v-model="payload.forumSync" />
                        </div>
                    </div>
                </div>
            </div>
            <div id="deps-management" class="row">
                <div class="col-12">
                    <DependencySelection
                        v-model:platforms-prop="payload.platforms"
                        v-model:dependencies-prop="payload.dependencies"
                        :prev-version="pendingVersion.prevVersion"
                    ></DependencySelection>
                </div>
            </div>
        </div>

        <div class="release-bulletin">
            <div style="position: relative; z-index: 5">
                <h3>Release Bulletin</h3>
                <p>What's new in this release?</p>

                <Editor
                    enabled
                    :raw="pendingVersion.description || ''"
                    target-form="form-publish"
                    v-model:content-prop="payload.content"
                    :saveable="false"
                    :cancellable="false"
                    open
                ></Editor>
            </div>
        </div>
    </template>
    <div class="row">
        <div class="col-6">
            <HangarForm id="form-upload" :action="ROUTES.parse('VERSIONS_UPLOAD', ownerName, projectSlug)" method="post" enctype="multipart/form-data">
                <div class="input-group" style="width: 50%">
                    <label for="pluginFile" style="flex-wrap: wrap">
                        <div style="flex: 0 0 100%; margin-bottom: 10px" v-if="!pendingVersion">Either upload a file...</div>
                        <div :class="'btn btn-primary btn-block' + (pendingVersion ? ' mt-1' : '')" style="flex: 0 0 100%">
                            <template v-if="!pendingVersion"> Upload</template>
                            <template v-else> Change file</template>
                        </div>
                    </label>
                    <input
                        type="file"
                        id="pluginFile"
                        name="pluginFile"
                        accept=".jar,.zip"
                        style="display: none"
                        @change="fileUploaded($event.target.name, $event.target.files)"
                    />
                </div>
            </HangarForm>
        </div>
        <div class="col-6">
            <div class="alert-file file-project float-right" style="display: none">
                <div class="alert alert-info float-left">
                    <i class="far fa-file-archive"></i>
                    <strong class="file-name"></strong>
                    <span class="file-size float-right"></span>
                </div>
                <div class="file-upload float-right">
                    <button
                        data-toggle="tooltip"
                        data-placement="right"
                        title="Sign plugin"
                        type="submit"
                        name="submit"
                        form="form-upload"
                        class="btn btn-info btn-block btn-sign"
                    >
                        <i class="fas fa-pencil-alt"></i>
                    </button>
                </div>
            </div>
            <template v-if="pendingVersion">
                <button class="btn btn-primary float-right mt-1 mr-1" @click="publish">Publish</button>
            </template>
            <template v-else>
                <HangarForm :action="ROUTES.parse('VERSIONS_CREATE_EXTERNAL_URL', ownerName, projectSlug)" method="post" id="form-url-upload">
                    <div class="input-group">
                        <label for="externalUrl" style="margin-bottom: 10px">...or specify an external URL</label>
                        <input type="text" class="form-control" id="externalUrl" name="externalUrl" placeholder="External URL" style="width: 70%" />
                        <div class="input-group-append">
                            <button class="btn btn-primary" type="submit">Create Version</button>
                        </div>
                    </div>
                </HangarForm>
            </template>
        </div>
    </div>
</template>
<script>
import HangarForm from '@/components/HangarForm';
import DependencySelection from '@/components/DependencySelection';
import Editor from '@/components/Editor';
import NewChannel from '@/components/NewChannel';
import 'bootstrap/js/dist/tooltip';
import $ from 'jquery';
import 'bootstrap/js/dist/collapse';
import filesize from 'filesize';
import axios from 'axios';
import { remove } from 'lodash-es';

const channels = [];
for (const channel of window.CHANNELS) {
    channels.push({
        color: channel.color,
        nonReviewed: channel.isNonReviewed,
        name: channel.name,
    });
}

export default {
    name: 'CreateVersion',
    components: {
        NewChannel,
        HangarForm,
        DependencySelection,
        Editor,
    },
    props: {
        pendingVersion: Object,
        ownerName: String,
        projectSlug: String,
        forumSync: Boolean,
    },
    data() {
        return {
            channels,
            MAX_FILE_SIZE: 20971520,
            ROUTES: window.ROUTES,
            createdChannel: {
                name: null,
                color: {
                    hex: null,
                    value: null,
                },
                nonReviewed: false,
            },
            payload: {
                versionString: null,
                description: null,
                externalUrl: null,
                channel: {
                    name: null,
                    color: {
                        hex: null,
                        value: null,
                    },
                    nonReviewed: false,
                },
                platforms: {},
                dependencies: {},
                unstable: false,
                recommended: false,
                forumSync: null,
                content: '',
            },
        };
    },
    created() {
        if (this.pendingVersion) {
            if (this.pendingVersion.versionString) {
                this.payload.versionString = this.pendingVersion.versionString;
                this.payload.description = this.pendingVersion.description;

                for (const platform of this.pendingVersion.platforms) {
                    this.payload.platforms[platform.name] = platform.versions;
                }
                this.payload.dependencies = this.pendingVersion.dependencies;
            } else {
                this.payload.externalUrl = this.pendingVersion.externalUrl;
            }
            this.payload.channel.name = this.pendingVersion.channelName;
            this.payload.channel.color.hex = this.pendingVersion.channelColor.hex;
            this.payload.channel.color.value = this.pendingVersion.channelColor.value;
            this.payload.channel.nonReviewed = this.channels.find(
                (ch) => ch.name === this.pendingVersion.channelName && ch.color.hex === this.pendingVersion.channelColor.hex
            ).nonReviewed;
            this.payload.forumSync = this.forumSync;
        }
    },
    methods: {
        getAlert() {
            return $('.alert-file');
        },
        clearIcon(e) {
            return e.removeClass('fa-spinner').removeClass('fa-spin').addClass('fa-pencil-alt').addClass('fa-upload');
        },
        reset() {
            const alert = this.getAlert();
            alert.hide();

            const control = alert.find('.file-upload');
            control.find('button').removeClass('btn-danger').addClass('btn-success').prop('disabled', false);
            this.clearIcon(control.find('[data-fa-i2svg]')).addClass('fa-pencil-alt');

            const bs = alert.find('.alert');
            bs.removeClass('alert-danger').addClass('alert-info');
            bs.find('[data-fa-i2svg]').attr('data-prefix', 'far');
            if (bs.find('[data-fa-i2svg]').data('ui-tooltip')) {
                bs.find('[data-fa-i2svg]').removeClass('fa-exclamation-circle').addClass('fa-file-archive').tooltip('dispose');
            }
            return alert;
        },
        failure(message) {
            const alert = this.getAlert();

            const bs = alert.find('.alert');
            bs.removeClass('alert-info').addClass('alert-danger');
            const noticeIcon = bs.find('[data-fa-i2svg]');

            noticeIcon.attr('data-prefix', 'fas');
            noticeIcon.toggleClass('fa-file-archive').toggleClass('fa-exclamation-circle');

            bs.tooltip({
                placement: 'left',
                title: message,
            });

            function flash(amount) {
                if (amount > 0) {
                    bs.find('[data-fa-i2svg]').fadeOut('fast', function () {
                        bs.find('[data-fa-i2svg]').fadeIn('fast', flash(amount - 1));
                    });
                }
            }

            flash(7);
        },
        failurePlugin(message) {
            this.failure(message);
            const alert = this.getAlert();
            const control = alert.find('.file-upload');
            control.find('button').removeClass('btn-success').addClass('btn-danger').prop('disabled', true);
            this.clearIcon(control.find('[data-fa-i2svg]')).addClass('fa-times');
        },
        fileUploaded(name, files) {
            const alert = this.reset();
            if (files.length === 0) {
                $('#form-upload')[0].reset();
                return;
            }

            let fileName = files[0].name;
            const fileSize = files[0].size;
            if (!fileName) {
                alert.fadeOut(1000);
                return;
            }

            let success = true;
            if (fileSize > this.MAX_FILE_SIZE) {
                this.failurePlugin('That file is too big. Plugins may be no larger than ' + filesize(this.MAX_FILE_SIZE) + '.');
                success = false;
            } else if (!fileName.endsWith('.zip') && !fileName.endsWith('.jar')) {
                this.failurePlugin('Only JAR and ZIP files are accepted.');
                success = false;
            }

            fileName = fileName.substr(fileName.lastIndexOf('\\') + 1, fileName.length);
            alert.find('.file-name').text(fileName);
            alert.find('.file-size').text(filesize(files[0].size));
            alert.fadeIn('slow');

            document.getElementById('form-url-upload').style.display = 'none';

            if (success) {
                const alertInner = alert.find('.alert');
                const button = alert.find('button');
                const icon = button.find('[data-fa-i2svg]');

                alertInner.removeClass('alert-info alert-danger').addClass('alert-success');
                button.removeClass('btn-info').addClass('btn-success');

                icon.addClass('fa-upload');

                const newTitle = 'Upload plugin';
                button.tooltip('hide').data('original-title', newTitle).tooltip();
            }
        },
        filesize,
        scrollTo(selector) {
            $([document.documentElement, document.body]).animate(
                {
                    scrollTop: $(selector).offset().top - 80,
                },
                600
            );
        },
        setChannel() {
            remove(this.channels, (ch) => !window.CHANNELS.find((och) => ch.name === och.name));
            this.channels.push({
                color: {
                    value: this.createdChannel.color.value,
                    hex: this.createdChannel.color.hex,
                },
                nonReviewed: this.createdChannel.nonReviewed,
                name: this.createdChannel.name,
            });
            this.payload.channel = {
                color: {
                    hex: this.createdChannel.color.hex,
                    value: this.createdChannel.color.value,
                },
                nonReviewed: this.createdChannel.nonReviewed,
                name: this.createdChannel.name,
            };
        },
        publish() {
            const requiredProps = [
                {
                    propName: 'forumSync',
                },
                {
                    propName: 'recommended',
                },
                {
                    propName: 'unstable',
                },
                {
                    propName: 'versionString',
                    selector: '#version-string-input',
                },
                {
                    propName: 'channel.color',
                },
                {
                    propName: 'channel.name',
                },
                {
                    propName: 'content',
                    selector: '.page-content-view',
                },
            ];

            $('.invalid-input').removeClass('invalid-input');

            // Validations
            for (const prop of requiredProps) {
                const val = prop.propName.split('.').reduce((o, i) => o[i], this.payload);
                if (typeof val === 'undefined' || val === null || val === '') {
                    if (prop.selector) {
                        const el = $(prop.selector);
                        el.addClass('invalid-input');
                        this.scrollTo(prop.selector);
                    }

                    return;
                }
            }
            const parentEl = $('#deps-management');
            const depCollapseEl = $('#dep-collapse');
            for (const platform in this.payload.dependencies) {
                for (const dep of this.payload.dependencies[platform]) {
                    if (!dep.project_id && !dep.external_url) {
                        this.scrollTo('#deps-management');
                        depCollapseEl.collapse('show');
                        $(`#${platform}-${dep.name}-link-cell`).addClass('invalid-input');
                        return;
                    }
                }
            }
            if (Object.keys(this.payload.platforms).length === 0) {
                this.scrollTo('#deps-management');
                depCollapseEl.collapse('show');
                parentEl.addClass('invalid-input');
                return;
            }
            for (const platform in this.payload.platforms) {
                if (!this.payload.platforms[platform].length) {
                    depCollapseEl.collapse('show');
                    this.scrollTo('#deps-management');
                    $(`#${platform}-row`).addClass('invalid-input');
                    return;
                }
            }

            const platformDeps = [];
            for (const platform in this.payload.platforms) {
                platformDeps.push({
                    name: platform,
                    versions: this.payload.platforms[platform],
                });
            }

            if (this.payload.content == null) {
                this.payload.content = '';
            }

            const url = window.ROUTES.parse('VERSIONS_SAVE_NEW_VERSION', this.ownerName, this.projectSlug, this.payload.versionString);
            axios
                .post(
                    url,
                    {
                        ...this.payload,
                        platforms: platformDeps,
                    },
                    {
                        headers: {
                            [window.csrfInfo.headerName]: window.csrfInfo.token,
                        },
                    }
                )
                .then(() => {
                    const publishUrl = window.ROUTES.parse('VERSIONS_PUBLISH', this.ownerName, this.projectSlug, this.payload.versionString);
                    const form = $(`<form action="${publishUrl}" method="post" style="display: none"></form>`).appendTo('body');
                    form.append(`<input name="${window.csrfInfo.parameterName}" value="${window.csrfInfo.token}" />`);
                    form.submit();
                });
        },
    },
};
</script>
<style lang="scss" scoped>
.input-group {
    margin-bottom: 1em;

    &.meta-info {
        width: unset;

        input:disabled {
            background-color: #00000017;
        }

        &.meta-info-checkbox {
            justify-content: center;

            .input-group-prepend > * {
                border-right: none;

                span {
                    position: relative;
                    top: 1px;
                }
            }

            .input-group-append > * {
                border-left: none;
            }
        }
    }
}
</style>
