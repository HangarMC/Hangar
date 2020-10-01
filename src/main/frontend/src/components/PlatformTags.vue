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
            <div></div>
        </template>
    </div>
</template>
<script>
import axios from 'axios';

export default {
    name: 'PlatformTags',
    props: {
        tags: Array,
    },
    data() {
        return {
            tagsArray: [],
        };
    },
    created() {
        console.log(this.tags);
        for (const obj of this.tags) {
            console.log(obj);
            this.tagsArray.push([Object.keys(obj)[0], obj[Object.keys(obj)[0]]]);
        }
        console.log(this.tagsArray);
        axios.get('/api/v1/platforms').then((res) => {
            console.log(res.data);
        });
    },
};
</script>
