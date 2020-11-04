<template>
    <div class="row">
        <div class="col-lg-8 col-12" style="z-index: 1">
            <Editor
                :save-call="ROUTES.parse('PAGES_SAVE', project.ownerName, project.slug, page.slug)"
                :delete-call="ROUTES.parse('PAGES_DELETE', project.ownerName, project.slug, page.slug)"
                :deletable="page.isDeletable"
                :enabled="canEditPages"
                :raw="page.contents"
                subject="Page"
                :extra-form-value="page.name"
                :open="editorOpen"
            ></Editor>
        </div>
        <div class="col-lg-4 col-12">
            <div class="row">
                <div class="col-lg-12 col-md-12">
                    <div v-if="project.recommendedVersionId" class="btn-group btn-download">
                        <a
                            :href="ROUTES.parse('VERSIONS_DOWNLOAD_RECOMMENDED', project.ownerName, project.slug)"
                            :title="$t('project.download.recommend._')"
                            data-toggle="tooltip"
                            data-placement="bottom"
                            class="btn btn-primary"
                        >
                            <i class="fas fa-download"></i>
                            {{ $t('general.download') }}
                        </a>
                        <button type="button" class="btn btn-primary dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                            <span class="sr-only">Toggle Dropdown</span>
                        </button>
                        <div class="dropdown-menu dropdown-menu-right">
                            <a :href="ROUTES.parse('VERSIONS_DOWNLOAD_RECOMMENDED', project.ownerName, project.slug)" class="dropdown-item">
                                {{ $t('general.download') }}
                            </a>
                            <a href="#" class="" @click.prevent></a>
                        </div>
                    </div>
                    <div class="stats minor">
                        <p>{{ $t('project.category.info', [formatCategory(project.category)]) }}</p>
                        <p>{{ $t('project.publishDate', [moment(project.createdAt).format('MMM D, YYYY')]) }}</p>
                        <p v-if="apiProject">
                            <span id="view-count">{{ apiProject.stats.views }} views</span>
                        </p>
                        <p v-if="apiProject">
                            <span id="star-count">{{ apiProject.stats.stars }}</span>
                            <a :href="ROUTES.parse('PROJECTS_SHOW_STARGAZERS', project.ownerName, project.slug)">
                                {{ apiProject.stats.views !== 1 ? ' stars' : ' star' }}
                            </a>
                        </p>
                        <p v-if="apiProject">
                            <span id="watcher-count">{{ apiProject.stats.watchers }}</span>
                            <a :href="ROUTES.parse('PROJECTS_SHOW_WATCHERS', project.ownerName, project.slug)">
                                {{ apiProject.stats.views !== 1 ? ' watchers' : ' watcher' }}
                            </a>
                        </p>
                        <p v-if="apiProject">
                            <span id="download-count">{{ apiProject.stats.downloads }} total download{{ apiProject.stats.downloads !== 1 ? 's' : '' }}</span>
                        </p>
                        <p v-if="project.licenseName && project.licenseUrl">
                            {{ $t('project.license.link') }}
                            <a :href="project.licenseUrl" target="_blank" ref="noopener">{{ project.licenseName }}</a>
                        </p>
                    </div>
                </div>

                <div class="col-lg-12 col-md-4">
                    <div class="card">
                        <div class="card-header">
                            <h3 class="card-title" v-text="$t('project.promotedVersions')"></h3>
                        </div>
                        <div v-if="apiProject" class="list-group promoted-list">
                            <a
                                v-for="(version, index) in apiProject.promoted_versions"
                                :key="`${index}-${version.version}`"
                                class="list-group-item list-group-item-action"
                                :href="ROUTES.parse('VERSIONS_SHOW', project.ownerName, project.slug, version.version)"
                            >
                                {{ version.version.substring(0, version.version.lastIndexOf('.')) }}
                                <Tag v-for="(tag, index) in version.tags" :key="index" :color="tag.color" :data="tag.display_data" :name="tag.name"></Tag>
                            </a>
                        </div>
                        <div v-else class="text-center py-4">
                            <i class="fas fa-spinner fa-spin fa-3x"></i>
                        </div>
                    </div>
                </div>
                <div class="col-lg-12 col-md-4">
                    <div class="card">
                        <div class="card-header">
                            <h3 class="float-left card-title" v-text="$t('page.plural')"></h3>
                            <button
                                v-if="canEditPages"
                                data-toggle="modal"
                                data-target="#new-page"
                                title="New"
                                class="new-page btn btn-primary btn-sm float-right"
                            >
                                <i class="fas fa-plus"></i>
                            </button>
                        </div>
                        <ul class="list-group">
                            <li
                                v-for="{ key: pg, value: pages } in rootPages"
                                :key="pg.id"
                                class="list-group-item"
                                style="position: relative; overflow: hidden"
                            >
                                <template v-if="pages.length">
                                    <a
                                        v-if="expanded[pg.name]"
                                        class="toggle-collapse page-collapse position-relative"
                                        @click.prevent="expanded[pg.name] = false"
                                    >
                                        <i class="far fa-minus-square"></i>
                                    </a>
                                    <a v-else class="toggle-collapse position-relative" @click.prevent="expanded[pg.name] = true">
                                        <i class="far fa-plus-square"></i>
                                    </a>
                                </template>
                                <div class="d-inline-block position-relative" style="z-index: 2">
                                    <a :href="ROUTES.parse('PAGES_SHOW', project.ownerName, project.slug, pg.slug)" class="href" v-text="pg.name"></a>
                                </div>

                                <transition v-if="pages.length" name="collapse">
                                    <ul v-show="pages.length && expanded[pg.name]" class="list-group sub-page-group">
                                        <li v-for="subpage in pages" :key="subpage.id" class="list-group-item">
                                            <a
                                                :href="ROUTES.parse('PAGES_SHOW', project.ownerName, project.slug, subpage.slug)"
                                                class="href"
                                                v-text="subpage.name"
                                            ></a>
                                        </li>
                                    </ul>
                                </transition>
                            </li>
                        </ul>
                    </div>
                </div>
                <div class="col-lg-12 col-md-4">
                    <MemberList
                        :filtered-members-prop="filteredMembers"
                        :can-manage-members="canManageMembers"
                        :settings-call="ROUTES.parse('PROJECTS_SHOW_SETTINGS', project.ownerName, project.slug)"
                    ></MemberList>
                </div>
            </div>
        </div>
    </div>
    <HangarModal target-id="new-page" label-id="new-page-label" modal-class="modal-lg">
        <template v-slot:modal-content>
            <div class="modal-header">
                <h4 v-show="!newPage.error" class="modal-title" id="new-page-label" v-text="$t('page.new.title')"></h4>
                <h4 v-show="newPage.error" class="modal-title" id="new-page-label-error" v-text="$t('page.new.error')"></h4>
                <button type="button" class="close" data-dismiss="modal" :aria-label="$t('general.close')">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body input-group">
                <div class="setting">
                    <div class="setting-description">
                        <h4 v-text="$t('project.page.name._')"></h4>
                        <p v-text="$t('project.page.name.info')"></p>
                    </div>
                    <div class="setting-content">
                        <label for="page-name" class="sr-only">Page Name</label>
                        <input class="form-control" type="text" id="page-name" v-model="newPage.pageName" />
                    </div>
                    <div class="clearfix"></div>
                </div>
                <div class="setting setting-no-border">
                    <div class="setting-description">
                        <h4 v-text="$t('project.page.parent._')"></h4>
                        <p v-text="$t('project.page.parent.info')"></p>
                    </div>
                    <div class="setting-content">
                        <label for="new-page-parent-select" class="sr-only"></label>
                        <select class="form-control select-parent" id="new-page-parent-select" v-model="newPage.parentPage">
                            <option disabled hidden :value="null">&lt;none&gt;</option>
                            <option v-for="pg in noHomePage(rootPages)" :key="pg.id" :value="{ id: pg.id, slug: pg.slug }" v-text="pg.name"></option>
                        </select>
                    </div>
                    <div class="clearfix"></div>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal" v-text="$t('general.close')"></button>
                <button id="continue-page" type="button" class="btn btn-primary" v-text="$t('general.continue')" @click="createPage"></button>
            </div>
        </template>
    </HangarModal>
</template>
<script>
import Editor from '@/components/Editor';
import Tag from '@/components/Tag';
import HangarModal from '@/components/HangarModal';
import moment from 'moment';
import { API } from '@/api';
import { go, slugify } from '@/utils';
import $ from 'jquery';
import MemberList from '@/components/MemberList';

$.ajaxSetup(window.ajaxSettings);

export default {
    name: 'ProjectView',
    components: { MemberList, Editor, Tag, HangarModal },
    data() {
        return {
            ROUTES: window.ROUTES,
            project: window.PROJECT,
            rootPages: window.ROOT_PAGES,
            page: window.PAGE,
            canEditPages: window.CAN_EDIT_PAGES,
            apiProject: null,
            newPage: {
                error: false,
                pageName: null,
                parentPage: null,
            },
            expanded: {},
            editorOpen: window.EDITOR_OPEN,
            canManageMembers: window.CAN_MANAGE_MEMBERS,
            filteredMembers: window.FILTERED_MEMBERS,
        };
    },
    created() {
        API.request(`projects/${this.project.ownerName}/${this.project.slug}`).then((res) => {
            this.apiProject = res;
        });
        this.expanded[this.page.name] = true;
    },
    methods: {
        moment,
        formatCategory(apiName) {
            const formatted = apiName.replace('_', ' ');
            return this.capitalize(formatted);
        },
        capitalize(input) {
            return input
                .toLowerCase()
                .split(' ')
                .map((s) => s.charAt(0).toUpperCase() + s.substring(1))
                .join(' ');
        },
        noHomePage(pages) {
            return pages.filter((pg) => pg.key.name !== 'Home').map((pg) => pg.key);
        },
        createPage() {
            let url = `/${this.project.ownerName}/${this.project.slug}/pages/`;
            let parentId = null;
            if (this.newPage.parentPage && this.newPage.parentPage.id !== -1) {
                parentId = this.newPage.parentPage.id;
                url += `${this.newPage.parentPage.slug}/${slugify(this.newPage.pageName)}/edit`;
            } else {
                url += `${slugify(this.newPage.pageName)}/edit`;
            }
            $.ajax({
                method: 'post',
                url,
                data: {
                    'parent-id': parentId,
                    content: '# ' + this.newPage.pageName + '\n',
                    name: this.newPage.pageName,
                },
                success() {
                    go(url);
                },
                error(err) {
                    console.error(err);
                    this.newPage.error = true;
                    setTimeout((self) => (self.newPage.error = false), 2000, this);
                },
            });
        },
    },
};
</script>
<style lang="scss" scoped>
.toggle-collapse {
    cursor: pointer;
    z-index: 2;
    margin-right: 4px;
}

.sub-page-group {
    position: relative;
    overflow: hidden;
    margin-top: 0;
    opacity: 1;
}

.collapse-enter-active,
.collapse-leave-active {
    transition: margin-top 0.7s ease-out, opacity 0.3s ease-out;
}

.collapse-enter-from {
    margin-top: -50%;
}

//.collapse-enter-from,
.collapse-leave-to {
    margin-top: -50%;
    opacity: 0;
}
</style>
