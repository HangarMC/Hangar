<template>
    <!-- todo flag modal -->
    <v-dialog v-model="shown">
        <v-card>
            <v-card-title> Flag </v-card-title>
        </v-card>
    </v-dialog>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import { Prop } from 'vue-property-decorator';
import { Project } from 'hangar-api';

@Component
export default class FlagModal extends Vue {
    @Prop({ required: true })
    project!: Project;

    shown: Boolean = false;

    show() {
        this.shown = true;
    }

    hide() {
        this.shown = false;
    }

    toggle() {
        this.shown = !this.shown;
    }
}
</script>
