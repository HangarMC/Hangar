<template>
    <div class="row">
        <div class="col-md-9">
            <div class="version-list">
                <div v-if="loading">
                    <i class="fas fa-spinner fa-spin"></i>
                    <span>Loading versions for you...</span>
                </div>
                <div v-else-if="filteredVersions.length === 0">
                    <span>No versions found!</span>
                    <span v-if="canUpload"> You may upload a new version in the sidebar!</span>
                </div>
                <div v-else>
                    <div class="list-group">
                        <a
                            v-for="(version, index) in filteredVersions"
                            :href="ROUTES.parse('VERSIONS_SHOW', htmlDecode(ownerName), htmlDecode(projectSlug), version.url_name)"
                            class="list-group-item list-group-item-action"
                            :class="[classForVisibility(version.visibility)]"
                            :key="index"
                        >
                            <div class="container-fluid">
                                <div class="row">
                                    <div class="col-4 col-md-2 col-lg-2" :set="(channel = version.tags.find((filterTag) => filterTag.name === 'Channel'))">
                                        <div class="row">
                                            <div class="col-12">
                                                <span class="text-bold">{{ version.name }}</span>
                                            </div>
                                            <div class="col-12">
                                                <span v-if="channel" class="channel" v-bind:style="{ background: channel.color.background }">{{
                                                    channel.data
                                                }}</span>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-8 col-md-6 col-lg-4">
                                        <Tag
                                            v-for="tag in version.tags.filter((filterTag) => filterTag.name !== 'Channel')"
                                            v-bind:key="tag.name + ':' + tag.data"
                                            v-bind="tag"
                                        ></Tag>
                                    </div>
                                    <div class="col-md-4 col-lg-3 d-none d-md-block">
                                        <div class="row">
                                            <div class="col-12">
                                                <i class="fas fa-fw fa-calendar"></i>
                                                {{ formatDate(version.created_at) }}
                                            </div>
                                            <div class="col-12">
                                                <i class="far fa-fw fa-file"></i>
                                                <span v-if="version.file_info.size_bytes">
                                                    {{ formatSize(version.file_info.size_bytes) }}
                                                </span>
                                                <span v-else> (external) </span>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-md-3 col-lg-3 d-none d-lg-block">
                                        <div class="row">
                                            <div class="col-12">
                                                <i class="fas fa-fw fa-user-tag"></i>
                                                {{ version.author }}
                                            </div>
                                            <div class="col-12">
                                                <i class="fas fa-fw fa-download"></i>
                                                {{ version.stats.downloads }} Downloads
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </a>
                    </div>
                    <Pagination :current="current" :total="total" @prev="page--" @next="page++" @jumpTo="page = $event"></Pagination>
                </div>
            </div>
        </div>
          <div class="col-md-3">
            <div class="row" v-if="canUpload" >
              <div class="col-12">
                <a class="btn btn-primary mb-2 w-100" :href="ROUTES.parse('VERSIONS_SHOW_CREATOR', ownerName, projectSlug)">
                  Upload a New Version
                </a>
              </div>
            </div>

            <div class="card channels">
                <div class="card-header">
                    <h3 class="card-title float-left">Channels</h3>
                    <input v-model="channelFilter.allChecked" type="checkbox" class="float-right channels-all" @change="checkAll" aria-label="Check All" />
                </div>

                <ul class="list-group list-channel">
                    <li v-for="channel in channels" :key="channel.id" class="list-group-item">
                        <span class="channel" :style="{ backgroundColor: channel.color.hex }" v-text="channel.name"></span>
                        <input
                            v-model="channelFilter.filter"
                            :value="channel.name"
                            type="checkbox"
                            class="float-right"
                            @change="updateCheckAll"
                            :aria-label="channel.name"
                        />
                    </li>
                    <li v-if="canEditChannels" class="list-group-item text-right">
                        <a :href="ROUTES.parse('CHANNELS_SHOW_LIST', ownerName, projectSlug)" class="btn btn-sm bg-warning"> Edit </a>
                    </li>
                </ul>
            </div>
            <MemberList
                :can-manage-members="canManageMembers"
                :filtered-members-prop="filteredMembers"
                :settings-call="ROUTES.parse('PROJECTS_SHOW_SETTINGS', ownerName, projectSlug)"
            ></MemberList>
        </div>
    </div>
</template>

<script>
import fileSize from 'filesize';
import moment from 'moment';

import Tag from '@/components/Tag';
import Pagination from '@/components/Pagination';
import { Visibility } from '@/enums';
import MemberList from '@/components/MemberList';
import { API } from '@/api';

export default {
    components: {
        MemberList,
        Tag,
        Pagination,
    },
    data() {
        return {
            ROUTES: window.ROUTES,
            page: 1,
            limit: 10,
            ownerName: window.PROJECT_OWNER,
            projectSlug: window.PROJECT_SLUG,
            versions: [],
            totalVersions: 0,
            canUpload: false,
            loading: true,
            channels: window.CHANNELS,
            canEditChannels: window.CAN_EDIT_CHANNELS,
            filteredMembers: window.FILTERED_MEMBERS,
            canManageMembers: window.CAN_MANAGE_MEMBERS,
            channelFilter: {
                filter: window.CHANNELS.map((ch) => ch.name),
                allChecked: true,
            },
        };
    },
    created() {
        this.update();
        API.request('permissions', 'GET', { author: window.PROJECT_OWNER, slug: window.PROJECT_SLUG }).then((response) => {
            this.canUpload = response.permissions.includes('create_version');
        });
        this.$watch(
            () => this.page,
            () => {
                this.update();
                window.scrollTo(0, 0);
            }
        );
    },
    methods: {
        update() {
            API.request(`projects/${this.ownerName}/${this.projectSlug}/versions`, 'GET', {
                limit: this.limit,
                offset: this.offset,
            }).then((response) => {
                this.versions = response.result;
                this.totalVersions = response.pagination.count;
                this.loading = false;
            });
        },
        formatSize(size) {
            return fileSize(size);
        },
        formatDate(date) {
            return moment(date).format('MMM D, YYYY');
        },
        classForVisibility(visibility) {
            return Visibility.fromName(visibility).class;
        },
        htmlDecode(htmlEncoded) {
            const parser = new DOMParser();
            const dom = parser.parseFromString('<!doctype html><body>' + htmlEncoded, 'text/html');
            return dom.body.textContent;
        },
        checkAll() {
            if (this.channelFilter.allChecked) {
                this.channelFilter.filter = this.channels.map((ch) => ch.name);
            } else {
                this.channelFilter.filter = [];
            }
        },
        updateCheckAll() {
            this.channelFilter.allChecked = this.channelFilter.filter.length === this.channels.length;
        },
    },
    computed: {
        offset() {
            return (this.page - 1) * this.limit;
        },
        current() {
            return Math.ceil(this.offset / this.limit) + 1;
        },
        total() {
            return Math.ceil(this.totalVersions / this.limit);
        },
        filteredVersions() {
            return this.versions.filter((v) => this.channelFilter.filter.indexOf(v.tags.find((t) => t.name === 'Channel').data) > -1);
        },
    },
};
</script>

<style lang="scss">
.version-list {
    .list-group > .list-group-item > .container-fluid > .row {
        display: flex;
        align-items: center;
    }
    .btn {
        margin-bottom: 1rem;
    }
}
</style>
