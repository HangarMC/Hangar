<template>
    <div>
        <v-list v-if="projects.length">
            <template v-for="project in projects">
                <v-divider :key="`${project.projectId}-divider`" class="mb-3" />
                <v-list-item :key="`${project.projectId}-item`">
                    <v-row>
                        <v-col class="grow">
                            <v-list-item-subtitle>
                                {{ $t('projectApproval.description', [project.changeRequester, `${project.namespace.owner}/${project.namespace.slug}`]) }}
                                <v-btn small icon color="primary" :to="`/${project.namespace.owner}/${project.namespace.slug}`" nuxt target="_blank">
                                    <v-icon small>mdi-open-in-new</v-icon>
                                </v-btn>
                            </v-list-item-subtitle>
                        </v-col>
                        <v-col class="shrink">
                            <VisibilityChangerModal
                                :prop-visibility="project.visibility"
                                small-btn
                                type="project"
                                :post-url="`projects/visibility/${project.projectId}`"
                            />
                        </v-col>
                        <v-col cols="12" class="pt-0">
                            <Markdown :raw="project.comment" class="mb-3" />
                        </v-col>
                    </v-row>
                </v-list-item>
            </template>
        </v-list>
        <v-card-text v-else>
            <v-alert type="info" text>
                {{ $t('projectApproval.noProjects') }}
            </v-alert>
        </v-card-text>
    </div>
</template>

<script lang="ts">
import { Component, Prop } from 'nuxt-property-decorator';
import { PropType } from 'vue';
import { ProjectApproval } from 'hangar-internal';
import { HangarComponent } from '~/components/mixins';
import VisibilityChangerModal from '~/components/modals/VisibilityChangerModal.vue';
import Markdown from '~/components/markdown/Markdown.vue';

@Component({
    components: { Markdown, VisibilityChangerModal },
})
export default class AdminList extends HangarComponent {
    @Prop({ type: Array as PropType<ProjectApproval[]>, required: true })
    projects!: ProjectApproval[];
}
</script>
