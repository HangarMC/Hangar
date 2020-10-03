<template>
    <div class="float-right" id="upload-platform-tags">
        <template v-for="[platform, tag] in tagsArray" :key="platform">
            <div class="tags">
                <span
                    :style="{
                        color: tag.color.foreground,
                        backgroundColor: tag.color.background,
                        borderColor: tag.color.background,
                    }"
                    class="tag"
                >
                    {{ tag.name }}
                </span>
            </div>
            <div v-if="!empty(platforms)">
                <template v-for="version in platforms[platform].possibleVersions" :key="version">
                    <label :for="`version-${version}`">{{ version }}</label>
                    <input
                        form="form-publish"
                        :id="`version-${version}`"
                        type="checkbox"
                        name="versions"
                        :value="version"
                        :checked="tag.data && tag.data.indexOf(version) > -1"
                    />
                    <!--                    <template v-if="(index + 1) % 5 === 0" v-html="</div><div>"> </template>-->
                </template>
            </div>
        </template>
    </div>
</template>
<script>
import { isEmpty } from 'lodash-es';
import axios from 'axios';

export default {
    name: 'PlatformTags',
    props: {
        tags: Array,
    },
    data() {
        return {
            tagsArray: [],
            platforms: {},
        };
    },
    created() {
        for (const obj of this.tags) {
            this.tagsArray.push([Object.keys(obj)[0], obj[Object.keys(obj)[0]]]);
        }
        axios.get('/api/v1/platforms').then((res) => {
            for (const platform of res.data) {
                this.platforms[platform.name.toUpperCase()] = platform;
            }
        });
    },
    methods: {
        empty(obj) {
            return isEmpty(obj);
        },
    },
};
</script>
