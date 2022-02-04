<template>
    <div class="markdown-editor">
        <div v-show="isEditing && !preview">
            <!-- TODO validations for pageContent min/max length from validations state -->
            <v-textarea v-model="rawEdited" outlined :rows="rawEdited.split(/\r\n|\r|\n/g).length + 3" :rules="rules" />
        </div>
        <Markdown v-show="!isEditing" :raw="raw" />
        <Markdown v-if="preview" :raw="rawEdited" inner-class="pl-5" />
        <v-btn v-show="!isEditing" class="page-btn edit-btn info" fab absolute icon x-small @click="isEditing = true">
            <v-icon>mdi-pencil</v-icon>
        </v-btn>
        <v-btn v-show="isEditing && saveable" class="page-btn save-btn success darken-2" fab absolute icon x-small :loading="loading.save" @click="savePage">
            <v-icon>mdi-content-save</v-icon>
        </v-btn>
        <v-btn v-show="isEditing" class="page-btn preview-btn info" fab absolute icon x-small @click="preview = true">
            <v-icon>mdi-eye</v-icon>
        </v-btn>
        <v-btn v-show="isEditing && preview" class="page-btn preview-btn info" fab absolute icon x-small @click="preview = false">
            <v-icon>mdi-eye-off</v-icon>
        </v-btn>
        <DeletePageModal @delete="deletePage">
            <template #activator="{ on, attrs }">
                <v-btn
                    v-show="isEditing && deletable"
                    v-bind="attrs"
                    class="page-btn delete-btn error"
                    fab
                    absolute
                    icon
                    x-small
                    :loading="loading.delete"
                    v-on="on"
                >
                    <v-icon>mdi-delete</v-icon>
                </v-btn>
            </template>
        </DeletePageModal>

        <v-btn
            v-show="isEditing && cancellable"
            class="page-btn cancel-btn warning red darken-1"
            fab
            absolute
            icon
            x-small
            @click="
                isEditing = false;
                preview = false;
            "
        >
            <v-icon>mdi-close</v-icon>
        </v-btn>
    </div>
</template>

<script lang="ts">
import { Component, Prop, PropSync, Vue, Watch } from 'nuxt-property-decorator';
import { PropType } from 'vue';
import { Markdown } from '~/components/markdown';
import DeletePageModal from '~/components/modals/pages/DeletePageModal.vue';

@Component({
    components: {
        DeletePageModal,
        Markdown,
    },
})
export default class MarkdownEditor extends Vue {
    preview = false;
    rawEdited: string = '';
    loading = {
        save: false,
        delete: false,
    };

    @Prop({ default: null, type: String })
    raw!: string | null;

    @PropSync('editing', { default: false, type: Boolean })
    isEditing!: boolean;

    @Prop({ default: true, type: Boolean })
    deletable!: boolean;

    @Prop({ default: true, type: Boolean })
    cancellable!: boolean;

    @Prop({ default: true, type: Boolean })
    saveable!: boolean;

    @Prop({ default: () => [], type: Array as PropType<Function[]> })
    rules!: Function[];

    created() {
        this.rawEdited = this.raw || '';
    }

    savePage() {
        this.loading.save = true;
        this.$emit('save', this.rawEdited);
    }

    deletePage() {
        this.loading.delete = true;
        this.$emit('delete');
    }

    @Watch('isEditing')
    onEditChange(value: boolean) {
        if (!value) {
            this.preview = false;
            this.loading.save = false;
            this.loading.delete = false;
        }
    }
}
</script>

<style lang="scss" scoped>
.page-btn {
    left: -20px;

    &.edit-btn,
    &.save-btn {
        top: 5px;
    }

    &.preview-btn {
        top: 45px;
    }

    &.delete-btn {
        top: 78px;
    }

    &.cancel-btn {
        top: 118px;
    }
}

.edit-btn i {
    color: white !important;
}

.markdown-editor {
    position: relative;
    min-height: 200px;
}
</style>
<style lang="scss">
.v-text-field__slot textarea {
    padding-left: 10px;
}
</style>
