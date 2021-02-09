<template>
    <v-card>
        <v-card-title>
            <NewPageModal :pages="project.pages" :project-id="project.id" activator-class="mr-2" />
            {{ $t('page.plural') }}
        </v-card-title>
        <v-card-text>
            <v-treeview :items="project.pages">
                <template #label="props">
                    <v-btn v-if="!props.item.home" nuxt :to="`/${$route.params.author}/${$route.params.slug}/pages/${props.item.slug}`" color="info" text exact>
                        {{ props.item.name }}
                    </v-btn>
                    <v-btn v-else nuxt :to="`/${$route.params.author}/${$route.params.slug}`" color="info" text exact>
                        <v-icon left>mdi-home</v-icon>
                        {{ props.item.name }}
                    </v-btn>
                </template>
            </v-treeview>
        </v-card-text>
    </v-card>
</template>

<script lang="ts">
import { Component } from 'nuxt-property-decorator';
import { HangarProjectMixin } from '../mixins';
import NewPageModal from '~/components/modals/pages/NewPageModal.vue';

@Component({
    components: {
        NewPageModal,
    },
})
export default class ProjectPageList extends HangarProjectMixin {}
</script>

<style lang="scss" scoped>
@import 'assets/utils';

.v-treeview a.v-btn {
    @include undoCapitalization();
}
</style>
