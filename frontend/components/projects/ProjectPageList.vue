<template>
    <v-card>
        <v-card-title>
            <NewPageModal v-if="$perms.canEditPage" :pages="project.pages" :project-id="project.id" activator-class="mr-2" />
            {{ $t('page.plural') }}
        </v-card-title>
        <v-card-text>
            <v-treeview :items="project.pages" item-key="slug" :open.sync="open" transition>
                <template #label="props">
                    <v-btn
                        v-if="!props.item.home"
                        nuxt
                        :to="`/${$route.params.author}/${$route.params.slug}/pages/${props.item.slug}`"
                        color="info"
                        text
                        exact
                        class="text-transform-unset"
                    >
                        <!--TODO custom icons-->
                        {{ props.item.name }}
                    </v-btn>
                    <v-btn v-else nuxt :to="`/${$route.params.author}/${$route.params.slug}`" color="info" text exact class="text-transform-unset">
                        <v-icon left> mdi-home </v-icon>
                        {{ props.item.name }}
                    </v-btn>
                </template>
            </v-treeview>
        </v-card-text>
    </v-card>
</template>

<script lang="ts">
import { Component, Watch } from 'nuxt-property-decorator';
import { HangarProjectMixin } from '../mixins';
import NewPageModal from '~/components/modals/pages/NewPageModal.vue';

@Component({
    components: {
        NewPageModal,
    },
})
export default class ProjectPageList extends HangarProjectMixin {
    open: string[] = [];

    created() {
        this.updateTreeview();
    }

    @Watch('$route')
    routeChanged() {
        this.updateTreeview();
    }

    updateTreeview() {
        const slugs = this.$route.fullPath.split('/').slice(4);
        if (slugs.length) {
            for (let i = 0; i < slugs.length; i++) {
                const slug = slugs.slice(0, i + 1).join('/');
                if (!this.open.includes(slug)) {
                    this.open.push(slug);
                }
            }
        } else if (this.project.pages.length === 1) {
            this.open.push(this.project.pages[0].slug);
        }
    }
}
</script>

<style lang="scss" scoped>
.v-treeview {
    overflow-x: auto;

    &::v-deep .v-treeview-node__label {
        overflow: visible;
    }
}
</style>
