<template>
    <template v-if="pendingVersion">
        <div class="plugin-meta">
            <table class="plugin-meta-table">
                <tr>
                    <td><strong>Version</strong></td>
                    <td v-if="pendingVersion.versionString">
                        {{ pendingVersion.versionString }}
                    </td>
                    <td v-else>
                        <div class="form-group">
                            <label for="version-string-input" class="sr-only">Version String</label>
                            <input
                                id="version-string-input"
                                class="form-control"
                                type="text"
                                form="form-publish"
                                name="versionString"
                                required
                                placeholder="Version"
                            />
                        </div>
                    </td>
                </tr>
                <tr>
                    <td><strong>Description</strong></td>
                    <td v-if="pendingVersion.versionString && pendingVersion.description">
                        {{ pendingVersion.description }}
                    </td>
                    <td v-else-if="pendingVersion.versionString && !pendingVersion.description">No description</td>
                    <td v-else>
                        <div class="form-group">
                            <label for="version-description-input" class="sr-only">Version Description</label>
                            <input
                                type="text"
                                form="form-publish"
                                name="versionDescription"
                                class="form-control"
                                id="version-description-input"
                            />
                        </div>
                    </td>
                </tr>
                <template v-if="pendingVersion.fileName && !pendingVersion.externalUrl">
                    <tr>
                        <td><strong>File name</strong></td>
                        <td>{{ pendingVersion.fileName }}</td>
                    </tr>
                    <tr>
                        <td><strong>File size</strong></td>
                        <td>{{ filesize(pendingVersion.fileSize) }}</td>
                    </tr>
                </template>
                <template v-else>
                    <tr>
                        <td><strong>External URL</strong></td>
                        <td>
                            <div class="form-group">
                                <label for="external-url-input" class="sr-only"></label>
                                <input
                                    id="external-url-input"
                                    class="form-control"
                                    type="text"
                                    :value="pendingVersion.externalUrl"
                                    name="externalUrl"
                                    form="form-publish"
                                    required
                                />
                            </div>
                        </td>
                    </tr>
                </template>
                <tr>
                    <td><strong>Channel</strong></td>
                    <td class="form-inline">
                        <select id="select-channel" form="form-publish" name="channel-input" class="form-control">
                            <option
                                v-for="channel in channels"
                                :key="channel.id"
                                :value="channel.name"
                                :data-color="channel.color.hex"
                                :selected="channel.name === pendingVersion.channelName"
                            >
                                {{ channel.name }}
                            </option>
                        </select>
                        <a href="#">
                            <i
                                id="channel-new"
                                class="fas fa-plus"
                                data-toggle="modal"
                                data-target="#channel-settings"
                            ></i>
                        </a>
                    </td>
                </tr>
                <tr>
                    <td><strong>Platform</strong></td>
                    <td v-if="pendingVersion.platforms">
                        <PlatformTags :tags="pendingVersion.dependenciesAsGhostTags" />
                    </td>
                    <td v-else>
                        <PlatformChoice></PlatformChoice>
                    </td>
                </tr>
                <tr>
                    <td>
                        <label for="is-unstable-version" class="form-check-label">
                            <strong>Mark this version as unstable</strong>
                        </label>
                    </td>
                    <td class="rv">
                        <div class="form-check">
                            <input
                                id="is-unstable-version"
                                class="form-check-input"
                                form="form-publish"
                                name="unstable"
                                type="checkbox"
                                value="true"
                            />
                        </div>
                        <div class="clearfix"></div>
                    </td>
                </tr>
                <tr>
                    <td>
                        <label for="is-recommended-version" class="form-check-label">
                            <strong>Recommended</strong>
                        </label>
                    </td>
                    <td class="rv">
                        <div class="form-check">
                            <input
                                id="is-recommended-version"
                                class="form-check-input"
                                form="form-publish"
                                name="recommended"
                                type="checkbox"
                                checked
                                value="true"
                            />
                        </div>
                        <div class="clearfix"></div>
                    </td>
                </tr>
                <tr>
                    <td>
                        <label for="create-forum-post-version" class="form-check-label"></label>
                        <strong>Create forum post</strong>
                    </td>
                    <td class="rv">
                        <div class="form-check">
                            <input
                                id="create-forum-post-version"
                                class="form-check-input"
                                form="form-publish"
                                name="forum-post"
                                type="checkbox"
                                :checked="forumSync"
                                value="true"
                            />
                        </div>
                        <div class="clearfix"></div>
                    </td>
                </tr>
            </table>
        </div>

        <div class="release-bulletin">
            <div style="position: relative">
                <h3>Release Bulletin</h3>
                <p>What's new in this release?</p>

                <Editor enabled :raw="pendingVersion.description || ''" target-form="form-publish"></Editor>
            </div>
        </div>
    </template>
    <HangarForm
        id="form-upload"
        :action="ROUTES.parse('VERSIONS_UPLOAD', ownerName, projectSlug)"
        method="post"
        enctype="multipart/form-data"
        clazz="form-inline"
    >
        <label for="pluginFile" class="btn btn-info float-left">
            <input
                type="file"
                id="pluginFile"
                name="pluginFile"
                style="display: none"
                accept=".jar,.zip"
                @change="fileUploaded($event.target.name, $event.target.files)"
            />
            Select file
        </label>
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
    </HangarForm>
    <template v-if="pendingVersion">
        <HangarForm
            :action="
                pendingVersion.versionString
                    ? ROUTES.parse('VERSIONS_PUBLISH', ownerName, projectSlug, pendingVersion.versionString)
                    : ROUTES.parse('VERSIONS_PUBLISH_URL', ownerName, projectSlug)
            "
            method="post"
            id="form-publish"
            clazz="float-right"
        >
            <input type="hidden" class="channel-color-input" name="channel-color-input" :value="defaultColor" />
            <div>
                <input type="submit" name="create" value="Publish" class="btn btn-primary" />
            </div>
        </HangarForm>
    </template>
    <template v-else>
        <HangarForm
            :action="ROUTES.parse('VERSIONS_CREATE_EXTERNAL_URL', ownerName, projectSlug)"
            method="post"
            id="form-url-upload"
            clazz="form-inline"
        >
            <div class="input-group float-right" style="width: 50%">
                <input
                    type="text"
                    class="form-control"
                    id="externalUrl"
                    name="externalUrl"
                    placeholder="External URL"
                    style="width: 70%"
                />
                <div class="input-group-append">
                    <button class="btn btn-info" type="submit">Create Version</button>
                </div>
            </div>
        </HangarForm>
    </template>
</template>
<script>
import HangarForm from '@/components/HangarForm';
import PlatformChoice from '@/PlatformChoice';
import PlatformTags from '@/components/PlatformTags';
import Editor from '@/components/Editor';
import 'bootstrap/js/dist/tooltip';
import $ from 'jquery';
import filesize from 'filesize';

export default {
    name: 'CreateVersion',
    components: {
        HangarForm,
        PlatformChoice,
        PlatformTags,
        Editor,
    },
    props: {
        defaultColor: String,
        pendingVersion: Object,
        ownerName: String,
        projectSlug: String,
        channels: Array,
        forumSync: Boolean,
    },
    data() {
        return {
            MAX_FILE_SIZE: 20971520,
            ROUTES: window.ROUTES,
        };
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
                bs.find('[data-fa-i2svg]')
                    .removeClass('fa-exclamation-circle')
                    .addClass('fa-file-archive')
                    .tooltip('dispose');
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
                this.failurePlugin(
                    'That file is too big. Plugins may be no larger than ' + filesize(this.MAX_FILE_SIZE) + '.'
                );
                success = false;
            } else if (!fileName.endsWith('.zip') && !fileName.endsWith('.jar')) {
                this.failurePlugin('Only JAR and ZIP files are accepted.');
                success = false;
            }

            fileName = fileName.substr(fileName.lastIndexOf('\\') + 1, fileName.length);
            alert.find('.file-name').text(fileName);
            alert.find('.file-size').text(filesize(files[0].size));
            alert.fadeIn('slow');

            $('#form-url-upload').css('display', 'none');

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
    },
    mounted() {
        $('.btn-edit').click();
    },
};
</script>
