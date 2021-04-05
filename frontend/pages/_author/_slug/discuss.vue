<template>
    <div>
        <div id="discourse-comments">
            <iframe
                id="discourse-embed-frame"
                ref="iframe"
                :src="url"
                referrerpolicy="no-referrer-when-downgrade"
                sandbox="allow-scripts allow-popups allow-popups-to-escape-sandbox"
            ></iframe>
        </div>
        <div v-if="$util.isLoggedIn()">
            <MarkdownEditor saveable editing :cancellable="false" :deletable="false" @save="postReply"></MarkdownEditor>
        </div>
        <div v-else>
            <a @click="$auth.login($route.fullPath)">{{ $t('project.discuss.login') }}</a>
            {{ $t('project.discuss.toReply') }}
        </div>
    </div>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { HangarProjectMixin } from '~/components/mixins';
import MarkdownEditor from '~/components/markdown/MarkdownEditor.vue';

interface DiscourseEmbed {
    discourseUrl: string;
    discourseEmbedUrl: string | null;
    discourseUserName: string | null;
    topicId: number | null;
}

// code in this class is adapted from https://papermc.io/forums/javascripts/embed.js
@Component({
    components: { MarkdownEditor },
})
export default class ProjectDiscussPage extends HangarProjectMixin {
    // TODO get topic id from project, get discourse url from backend
    DE: DiscourseEmbed = { discourseUrl: 'http://localhost/', topicId: 13, discourseUserName: null, discourseEmbedUrl: null };

    $refs!: {
        iframe: any;
    };

    mounted() {
        window.addEventListener('message', this.postMessageReceived, false);
    }

    // TODO implement
    postReply(message: string) {
        console.log('reply ' + message);
    }

    get url() {
        const queryParams: any = {};

        if (this.DE.discourseEmbedUrl) {
            if (this.DE.discourseEmbedUrl.indexOf('/') === 0) {
                console.error('discourseEmbedUrl must be a full URL, not a relative path');
            }

            queryParams.embed_url = encodeURIComponent(this.DE.discourseEmbedUrl);
        }

        if (this.DE.discourseUserName) {
            queryParams.discourse_username = this.DE.discourseUserName;
        }

        if (this.DE.topicId) {
            queryParams.topic_id = this.DE.topicId;
        }

        let src = this.DE.discourseUrl + 'embed/comments';
        const keys = Object.keys(queryParams);
        if (keys.length > 0) {
            src += '?';

            for (let i = 0; i < keys.length; i++) {
                if (i > 0) {
                    src += '&';
                }

                const k = keys[i];
                src += k + '=' + queryParams[k];
            }
        }
        return src;
    }

    // Thanks http://amendsoft-javascript.blogspot.ca/2010/04/find-x-and-y-coordinate-of-html-control.html
    findPosY(obj: any) {
        let top = 0;
        if (obj.offsetParent) {
            while (1) {
                top += obj.offsetTop;
                if (!obj.offsetParent) break;
                obj = obj.offsetParent;
            }
        } else if (obj.y) {
            top += obj.y;
        }
        return top;
    }

    normalizeUrl(url: string) {
        return url.replace(/^https?(:\/\/)?/, '');
    }

    postMessageReceived(e: any) {
        if (!e) {
            return;
        }
        // todo origin is null?
        // if (!this.normalizeUrl(this.DE.discourseUrl).includes(this.normalizeUrl(e.origin))) {
        //     return;
        // }

        if (e.data) {
            if (e.data.type === 'discourse-resize' && e.data.height) {
                this.$refs.iframe.height = e.data.height + 'px';
            }

            if (e.data.type === 'discourse-scroll' && e.data.top) {
                // find iframe offset
                const destY = this.findPosY(this.$refs.iframe) + e.data.top;
                window.scrollTo(0, destY);
            }
        }
    }
}
</script>

<style lang="scss" scoped>
iframe {
    border: none;
    width: 100%;
}
</style>
<!-- custom css for forums
:root {
    --tertiary: #1976d2;
}

html {
    background-color: unset;
    overflow: hidden;
}

.logo {
    display: none;
}
-->
