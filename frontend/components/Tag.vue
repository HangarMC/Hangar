<template>
    <div class="tags" :class="{ 'has-addons': cData && !shortForm }">
        <span
            :style="{
                color: cColor.foreground,
                background: cColor.background,
                'border-color': cColor.background,
            }"
            class="tag"
        >
            {{ shortForm && cData ? cData : cName }}
        </span>
        <span v-if="cData && !shortForm" class="tag">{{ cData }}</span>
    </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'nuxt-property-decorator';
import { Tag } from 'hangar-api';
import { PropType } from 'vue';

@Component
export default class TagComponent extends Vue {
    @Prop({ type: String })
    name!: string;

    @Prop({ type: String })
    data!: string;

    @Prop({ type: Object as PropType<{ foreground?: string; background: string }> })
    color!: { foreground?: string; background: string };

    @Prop({ type: Object as PropType<Tag> })
    tag!: Tag;

    @Prop({ type: Boolean, default: false })
    shortForm!: boolean;

    get cName() {
        return this.tag ? this.tag.name : this.name;
    }

    get cData() {
        return this.tag ? this.tag.data : this.data;
    }

    get cColor() {
        return this.tag ? this.tag.color : this.color;
    }
}
</script>

<style lang="scss" scoped>
.tags {
    display: inline-flex;
    flex-wrap: wrap;
    justify-content: flex-start;
    align-items: center;

    &.has-addons {
        .tag:first-child {
            border-bottom-right-radius: 0;
            border-top-right-radius: 0;
            margin-right: 0;
        }

        .tag:nth-child(2) {
            border-bottom-left-radius: 0;
            border-top-left-radius: 0;
            border-left: none;
            margin-left: 0;
        }
    }

    .tag {
        border: 1px solid #dcdcdc;
        display: flex;
        background-color: #f5f5f5;
        color: #495057;
        border-radius: 3px;
        font-size: 0.75em;
        height: 2em;
        padding-left: 0.75em;
        padding-right: 0.75em;
        white-space: nowrap;
        margin: 5px;
    }
}
</style>
