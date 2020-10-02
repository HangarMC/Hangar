<template>
    <template v-if="pendingVersion">
        <div class="plugin-meta">
            <table class="plugin-meta-table">
                <tr>
                    <td><strong>Version</strong></td>
                    <td>
                        <div class="form-group">
                            <label for="version-string-input" class="sr-only">Version String</label>
                            <input
                                type="text"
                                id="version-string-input"
                                class="form-control"
                                form="form-publish"
                                name="versionString"
                                required
                                v-model="payload.versionString"
                                :disabled="pendingVersion.versionString"
                            />
                        </div>
                    </td>
                </tr>
                <tr>
                    <td><strong>Description</strong></td>
                    <td>
                        <div class="form-group">
                            <label for="version-description-input" class="sr-only">Version Description</label>
                            <input
                                type="text"
                                id="version-description-input"
                                class="form-control"
                                v-model="payload.description"
                                :disabled="pendingVersion.versionString && pendingVersion.description"
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
                                <input id="external-url-input" class="form-control" type="text" required v-model="payload.externalUrl" />
                            </div>
                        </td>
                    </tr>
                </template>
                <tr>
                    <td><strong>Channel</strong></td>
                    <td class="form-inline">
                        <select id="select-channel" class="form-control" v-model="payload.channel.name">
                            <option v-for="channel in channels" :key="channel.id" :value="channel.name" :data-color="channel.color.hex">
                                {{ channel.name }}
                            </option>
                        </select>
                        <a href="#">
                            <i id="channel-new" class="fas fa-plus" data-toggle="modal" data-target="#channel-settings"></i>
                        </a>
                    </td>
                </tr>
                <!--                <tr>
                    <td><strong>Platform</strong></td>
                    <td v-if="pendingVersion.platforms">
                        <PlatformTags :tags="pendingVersion.dependenciesAsGhostTags" />
                    </td>
                    <td v-else>
                        <PlatformChoice></PlatformChoice>
                    </td>
                </tr>-->
                <DependencySelection
                    v-model:platforms-prop="payload.platforms"
                    v-model:dependencies-prop="payload.dependencies"
                    :tags="pendingVersion ? pendingVersion.dependenciesAsGhostTags : null"
                ></DependencySelection>
                <tr>
                    <td>
                        <label for="is-unstable-version" class="form-check-label">
                            <strong>Mark this version as unstable</strong>
                        </label>
                    </td>
                    <td class="rv">
                        <div class="form-check">
                            <input id="is-unstable-version" class="form-check-input" type="checkbox" v-model="payload.unstable" />
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
                            <input id="is-recommended-version" class="form-check-input" type="checkbox" v-model="payload.recommended" />
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
                            <input id="create-forum-post-version" class="form-check-input" type="checkbox" v-model="payload.forumSync" />
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

                <Editor
                    enabled
                    :raw="pendingVersion.description || ''"
                    target-form="form-publish"
                    v-model:content-prop="payload.content"
                ></Editor>
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
        <button class="btn btn-primary float-right mt-1 mr-1" @click="publish">Publish</button>
        <!--        <HangarForm-->
        <!--            :action="-->
        <!--                pendingVersion.versionString-->
        <!--                    ? ROUTES.parse('VERSIONS_PUBLISH', ownerName, projectSlug, pendingVersion.versionString)-->
        <!--                    : ROUTES.parse('VERSIONS_PUBLISH_URL', ownerName, projectSlug)-->
        <!--            "-->
        <!--            method="post"-->
        <!--            id="form-publish"-->
        <!--            clazz="float-right"-->
        <!--        >-->
        <!--            <input type="hidden" class="channel-color-input" name="channel-color-input" :value="defaultColor" />-->
        <!--            <div>-->
        <!--                <input type="submit" name="create" value="Publish" class="btn btn-primary" />-->
        <!--            </div>-->
        <!--        </HangarForm>-->
    </template>
    <template v-else>
        <HangarForm
            :action="ROUTES.parse('VERSIONS_CREATE_EXTERNAL_URL', ownerName, projectSlug)"
            method="post"
            id="form-url-upload"
            clazz="form-inline"
        >
            <div class="input-group float-right" style="width: 50%">
                <input type="text" class="form-control" id="externalUrl" name="externalUrl" placeholder="External URL" style="width: 70%" />
                <div class="input-group-append">
                    <button class="btn btn-info" type="submit">Create Version</button>
                </div>
            </div>
        </HangarForm>
    </template>
</template>
<script>
import HangarForm from '@/components/HangarForm';
// import PlatformChoice from '@/PlatformChoice';
import DependencySelection from '@/components/DependencySelection';
// import PlatformTags from '@/components/PlatformTags';
import Editor from '@/components/Editor';
import 'bootstrap/js/dist/tooltip';
import $ from 'jquery';
import filesize from 'filesize';
import axios from 'axios';

export default {
    name: 'CreateVersion',
    components: {
        HangarForm,
        // PlatformChoice,
        DependencySelection,
        // PlatformTags,
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
            payload: {
                versionString: null,
                description: null,
                externalUrl: null,
                channel: {
                    name: null,
                    color: null,
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
            this.payload.versionString = this.pendingVersion.versionString;
            this.payload.description = this.pendingVersion.description;
            this.payload.externalUrl = this.pendingVersion.externalUrl;
            this.payload.channel.name = this.pendingVersion.channelName;
            this.payload.channel.color = this.pendingVersion.channelColor;
            this.payload.forumSync = this.forumSync;

            for (const platform of this.pendingVersion.platforms) {
                this.payload.platforms[platform.name] = platform.versions;
            }
            this.payload.dependencies = this.pendingVersion.dependencies;
            // console.log(this.pendingVersion.platforms);
            // console.log(this.pendingVersion.dependencies);
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
        publish() {
            const requiredProps = ['forumSync', 'recommended', 'unstable', 'versionString', 'channel.color', 'channel.name'];

            // Validations
            for (const prop of requiredProps) {
                if (prop.split('.').reduce((o, i) => o[i], this.payload) == null) {
                    console.error(`Missing needed prop ${prop}`);
                    return;
                }
            }
            for (const platform in this.payload.dependencies) {
                for (const dep of this.payload.dependencies[platform]) {
                    if (!dep.project_id && !dep.external_url) {
                        console.error(`Missing link for ${dep.name} on ${platform}`);
                        return;
                    }
                }
            }
            for (const platform in this.payload.platforms) {
                if (!this.payload.platforms[platform].versions.length) {
                    console.error(`need to specify platform versions for ${platform}`);
                    return;
                }
            }

            console.log('VALIDATED!!');
            console.log(this.payload);

            const platformDeps = [];
            for (const platform in this.payload.platforms) {
                platformDeps.push({
                    name: platform,
                    versions: this.payload.platforms[platform].versions,
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
                    $('form')
                        .attr(
                            'action',
                            window.ROUTES.parse('VERSIONS_PUBLISH', this.ownerName, this.projectSlug, this.payload.versionString)
                        )
                        .attr('method', 'post')
                        .submit();
                });
        },
    },
    mounted() {
        $('.btn-edit').click();
    },
    // watch: {
    //     payload: {
    //         handler(val) {
    //             console.log(val);
    //         },
    //         deep: true,
    //     },
    // },
};
</script>
