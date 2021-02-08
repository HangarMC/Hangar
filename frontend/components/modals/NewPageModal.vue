<template>
    <div>
        <!-- todo fix this -->
        <div class="modal-header">
            <h4 v-show="!newPage.error" id="new-page-label" class="modal-title" v-text="$t('page.new.title')"></h4>
            <h4 v-show="newPage.error" id="new-page-label-error" class="modal-title" v-text="$t('page.new.error')"></h4>
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
                    <input id="page-name" v-model="newPage.pageName" class="form-control" type="text" />
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
                    <select id="new-page-parent-select" v-model="newPage.parentPage" class="form-control select-parent">
                        <option disabled hidden :value="null">&lt;none&gt;</option>
                        <option v-for="pg in noHomePage(rootPages)" :key="pg.id" :value="{ id: pg.id, slug: pg.slug }" v-text="pg.name"></option>
                    </select>
                </div>
                <div class="clearfix"></div>
            </div>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-default" data-dismiss="modal" v-text="$t('general.close')"></button>
            <button id="continue-page" type="button" class="btn btn-primary" @click="createPage" v-text="$t('general.continue')"></button>
        </div>
    </div>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';

@Component
export default class NewPageModal extends Vue {}
</script>
