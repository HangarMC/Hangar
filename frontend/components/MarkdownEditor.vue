<template>
    <div style="position: relative">
        <div v-show="isEditing && !preview" class="ml-4">
            <v-textarea v-model="rawEdited" outlined :rows="rawEdited.split(/\r\n|\r|\n/g).length + 3" />
        </div>
        <Markdown v-show="!isEditing" :raw="raw" class="ml-4" />
        <Markdown v-if="preview" :raw="rawEdited" class="ml-4" inner-class="pl-5" />
        <v-btn v-show="!isEditing" class="page-btn edit-btn info" fab absolute icon x-small @click="isEditing = true">
            <v-icon>mdi-pencil</v-icon>
        </v-btn>
        <v-btn v-show="isEditing" class="page-btn save-btn success darken-2" fab absolute icon x-small :loading="loading.save" @click="savePage">
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
            v-show="isEditing"
            class="page-btn cancel-btn warning red darken-2"
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
import Markdown from '~/components/Markdown.vue';
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
    // TODO dynamic based on which buttons are shown
    left: 0;

    &.edit-btn,
    &.save-btn {
        top: -16px;
    }

    &.preview-btn {
        top: 22px;
    }

    &.delete-btn {
        top: 62px;
    }

    &.cancel-btn {
        top: 102px;
    }
}
</style>
<style lang="scss">
.v-text-field__slot textarea {
    padding-left: 10px;
}
</style>
