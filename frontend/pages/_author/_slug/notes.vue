<template>
    <v-card>
        <v-card-title>
            <span v-text="$t('notes.header')"></span>&nbsp;
            <NuxtLink :to="'/' + project.namespace.owner + '/' + project.namespace.slug">
                {{ project.namespace.owner + '/' + project.namespace.slug }}
            </NuxtLink>
        </v-card-title>
        <v-card-text>
            <v-text-field v-model="text" :placeholder="$t('notes.placeholder')">
                <template #append-outer>
                    <v-btn @click="addNote">{{ $t('notes.addNote') }}</v-btn>
                </template>
            </v-text-field>
            <h2>{{ $t('notes.notes') }}</h2>
            <v-data-table v-if="notes && notes.length > 0" :headers="headers" :items="notes" disable-filtering disable-sort hide-default-footer>
                <template #item.message="{ item }">{{ item.message }}</template>
                <template #item.user="{ item }">{{ item.user.name }}</template>
                <template #item.createdAt="{ item }">{{ $util.prettyDate(item.createdAt) }}</template>
            </v-data-table>
            <v-alert v-else type="info" prominent v-text="$t('notes.noNotes')" />
        </v-card-text>
    </v-card>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'nuxt-property-decorator';
import { Project } from 'hangar-api';
import { Note } from 'hangar-internal';
import { GlobalPermission } from '~/utils/perms';
import { NamedPermission } from '~/types/enums';

@Component
@GlobalPermission(NamedPermission.MOD_NOTES_AND_FLAGS)
export default class ProjectNotesPage extends Vue {
    @Prop({ required: true })
    project!: Project;

    // todo load notes
    notes: Array<Note> = [];

    text: string = '';

    addNote() {
        const note = {
            id: -1,
            message: this.text,
            user: this.$util.getCurrentUser(),
            createdAt: new Date().toISOString(),
        } as Note;
        this.text = '';
        this.notes.push(note);
        // TODO add new note to server
    }

    get headers() {
        return [
            { text: 'Date', value: 'createdAt', width: '10%' },
            { text: 'User', value: 'user', width: '10%' },
            { text: 'Message', value: 'message', width: '80%' },
        ];
    }
}
</script>

<style lang="scss" scoped></style>
