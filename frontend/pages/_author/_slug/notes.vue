<template>
    <v-card>
        <v-card-title>
            <span v-text="$t('notes.header')"></span>&nbsp;
            <NuxtLink :to="'/' + project.namespace.owner + '/' + project.namespace.slug">
                {{ project.namespace.owner + '/' + project.namespace.slug }}
            </NuxtLink>
        </v-card-title>
        <v-card-text>
            <v-text-field v-model.trim="text" filled :placeholder="$t('notes.placeholder')" dense hide-details @keyup.enter="addNote">
                <template #append-outer>
                    <v-btn class="input-append-btn" color="primary" :disabled="!text" :loading="loading" @click="addNote">{{ $t('notes.addNote') }}</v-btn>
                </template>
            </v-text-field>
            <h2 class="mt-2">{{ $t('notes.notes') }}</h2>
            <v-data-table
                v-if="notes && notes.length > 0"
                :headers="headers"
                :items="notes"
                :loading="$fetchState.pending"
                disable-filtering
                disable-sort
                hide-default-footer
            >
                <template #item.createdAt="{ item }">{{ $util.prettyDate(item.createdAt) }}</template>
            </v-data-table>
            <v-alert v-else type="info" prominent v-text="$t('notes.noNotes')" />
        </v-card-text>
    </v-card>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { Note } from 'hangar-internal';
import { GlobalPermission } from '~/utils/perms';
import { NamedPermission } from '~/types/enums';
import { HangarProjectMixin } from '~/components/mixins';

@Component
@GlobalPermission(NamedPermission.MOD_NOTES_AND_FLAGS)
export default class ProjectNotesPage extends HangarProjectMixin {
    notes: Note[] = [];
    loading = false;
    text: string = '';

    head() {
        return this.$seo.head(
            'Notes | ' + this.project.name,
            this.project.description,
            this.$route,
            this.$util.projectUrl(this.project.namespace.owner, this.project.namespace.slug)
        );
    }

    addNote() {
        if (!this.text) {
            return;
        }
        this.loading = true;
        this.$api
            .requestInternal(`projects/notes/${this.project.id}`, true, 'post', {
                content: this.text,
            })
            .then(() => {
                this.$nuxt.refresh();
                this.text = '';
            })
            .catch(this.$util.handleRequestError)
            .finally(() => {
                this.loading = false;
            });
    }

    get headers() {
        return [
            { text: 'Date', value: 'createdAt', width: '10%' },
            { text: 'User', value: 'userName', width: '10%' },
            { text: 'Message', value: 'message', width: '80%' },
        ];
    }

    async fetch() {
        this.notes = (await this.$api.requestInternal<Note[]>(`projects/notes/${this.project.id}`, true).catch<any>(this.$util.handleRequestError)) || [];
    }
}
</script>

<style lang="scss" scoped></style>
