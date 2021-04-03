<template>
    <HangarModal :title="$t('project.flag.flagProject', [project.name])" :submit="submitFlag">
        <template #activator="{ on: dialogOn, attrs }">
            <v-tooltip :disabled="!project.userActions.flagged" bottom>
                <template #activator="{ on: tooltipOn }">
                    <div v-on="tooltipOn">
                        <!-- wrap in div so tooltip shows up when the button is disabled -->
                        <v-btn v-bind="attrs" color="warning" :disabled="project.userActions.flagged" :class="activatorClass" v-on="{ dialogOn }">
                            <v-icon>mdi-flag</v-icon>
                            {{ $t('project.actions.flag') }}
                        </v-btn>
                    </div>
                </template>
                <span>{{ $t('project.flag.flagSent') }}</span>
            </v-tooltip>
        </template>
        <v-radio-group v-model="form.selection" :rules="[$util.$vc.require('A reason')]">
            <v-radio v-for="(reason, index) in flagReasons" :key="index" :label="$t(reason.title)" :value="reason.type" />
        </v-radio-group>
        <v-textarea v-model.trim="form.comment" rows="3" filled :rules="[$util.$vc.require('A comment')]" :label="$t('general.comment')" />
    </HangarModal>
</template>

<script lang="ts">
import { Component, mixins } from 'nuxt-property-decorator';
import { FlagReason } from 'hangar-internal';
import { HangarFormModal, HangarProjectMixin } from '~/components/mixins';
import HangarModal from '~/components/modals/HangarModal.vue';

@Component({
    components: { HangarModal },
})
export default class FlagModal extends mixins(HangarFormModal, HangarProjectMixin) {
    flagReasons: FlagReason[] = [];
    form = {
        selection: null as string | null,
        comment: null as string | null,
    };

    submitFlag() {
        return this.$api
            .requestInternal('flags/', true, 'POST', {
                projectId: this.project.id,
                reason: this.form.selection,
                comment: this.form.comment,
            })
            .then(() => {
                this.$util.success(this.$t('project.flag.flagSend'));
                this.$nuxt.refresh();
            })
            .catch(this.$util.handleRequestError);
    }

    async fetch() {
        this.flagReasons.push(...(await this.$api.requestInternal<FlagReason[]>('data/flagReasons', true).catch<any>(this.$util.handleRequestError)));
    }
}
</script>
