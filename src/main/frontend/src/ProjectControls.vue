<template>
    <div class="float-right project-controls">
        <span v-if="flagReported" class="flag-msg"> <i class="fas fa-thumbs-up"></i> Flag submitted for review </span>
        <template v-if="visibility !== 'softDelete'">
            <template v-if="!isOwner">
                <button class="btn btn-default" @click="star">
                    <span v-show="value.starred"><i class="fas fa-star"></i></span>
                    <span v-show="!value.starred"><i class="far fa-star"></i></span>
                    <span v-text="' ' + value.starCount"></span>
                </button>
                <button class="btn btn-default" @click="watch">
                    <span v-show="value.watching"><i class="fas fa-eye-slash"></i></span>
                    <span v-show="!value.watching"><i class="fas fa-eye"></i></span>
                    <span v-text="value.watching ? ' Unwatch' : ' Watch'"></span>
                </button>
            </template>
            <template v-else>
                <span class="minor stars-static"> <i class="fas fa-star"></i> {{ value.starCount }} </span>
            </template>
        </template>
        <template v-if="hasUser && !isOwner && !hasUserFlags && visibility !== 'softDelete'">
            <button class="btn btn-default" data-toggle="modal" data-target="#modal-flag"><i class="fa-flag fas"></i> {{ $t('project.flag._') }}</button>
        </template>
        <template v-if="hasUser && (hasModNotes || hasViewLogs)">
            <button class="btn btn-alert dropdown-toggle" type="button" id="admin-actions" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                Admin Actions
            </button>
            <div class="dropdown-menu" aria-labelledby="admin-actions">
                <a v-if="hasModNotes" :href="ROUTES.parse('PROJECTS_SHOW_FLAGS', ownerName, projectSlug)" class="dropdown-item">
                    Flag history ({{ flagCount }})
                </a>
                <a v-if="hasModNotes" :href="ROUTES.parse('PROJECTS_SHOW_NOTES', ownerName, projectSlug)" class="dropdown-item">
                    Staff notes ({{ noteCount }})
                </a>
                <a v-if="hasViewLogs" :href="`${ROUTES.parse('SHOW_LOG')}?projectFilter=${ownerName}${projectSlug}`" class="dropdown-item">
                    User Action Logs
                </a>
                <a :href="`https://papermc.io/forums/${ownerName}`" class="dropdown-item">
                    Forum <i class="fas fa-external-link-alt" aria-hidden="true"></i>
                </a>
            </div>
        </template>
    </div>
</template>
<script>
import $ from 'jquery';

$.ajaxSetup(window.ajaxSettings);

export default {
    name: 'ProjectControls',
    props: {
        visibility: String,
        hasUser: Boolean,
        isOwner: Boolean,
        isStarred: Boolean,
        isWatching: Boolean,
        hasUserFlags: Boolean,
        starCount: Number,
        flagCount: Number,
        noteCount: Number,
        hasModNotes: Boolean,
        hasViewLogs: Boolean,
        flagReported: Boolean,
    },
    data() {
        return {
            ROUTES: window.ROUTES,
            ownerName: window.PROJECT_OWNER,
            projectSlug: window.PROJECT_SLUG,
            value: {
                starred: false,
                watching: false,
                starCount: 0,
            },
            starIncrement: 0,
        };
    },
    methods: {
        watch() {
            if (!this.checkHasUser()) return;
            this.value.watching = !this.value.watching;
            $.ajax({
                type: 'post',
                url: window.ROUTES.parse('PROJECTS_SET_WATCHING', this.ownerName, this.projectSlug, this.value.watching),
            });
        },
        star() {
            if (!this.checkHasUser()) return;
            this.value.starCount += this.starIncrement;
            this.value.starred = this.starIncrement > 0;
            $.ajax({
                type: 'post',
                url: window.ROUTES.parse('PROJECTS_TOGGLE_STARRED', this.ownerName, this.projectSlug),
            });
            this.starIncrement *= -1;
        },
      checkHasUser() {
          if (!this.hasUser) {
            // TODO some alert or modal?
            alert("Please login first");
            return false;
          }
          return true;
      }
    },
    created() {
        this.value.starred = this.isStarred;
        this.value.watching = this.isWatching;
        this.value.starCount = this.starCount;
        this.starIncrement = this.isStarred ? -1 : 1;
    },
    mounted() {
        const flagList = $('#list-flags');
        if (flagList.length) {
            flagList.find('li').on('click', function () {
                flagList.find(':checked').removeAttr('checked');
                $(this).find('input').prop('checked', true);
            });
        }
        const flagMsg = $('.flag-msg');
        if (flagMsg.length) {
            flagMsg.hide().fadeIn(1000).delay(2000).fadeOut(1000);
        }
    },
};
</script>
<style lang="scss" scoped>
.project-controls > * {
    margin-left: 4px;

    .stars-static {
        color: gray;
        padding-right: 5px;
    }
}
</style>
