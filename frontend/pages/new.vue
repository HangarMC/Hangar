<template>
    <v-stepper v-model="step" vertical>
        <StepperStep :current-step="step" :this-step="1" @continue="step = 2" @back="">
            <v-card class="mb-12 pa-1" min-height="200px" v-html="$t('project.new.step1.text')" />
        </StepperStep>

        <StepperStep :current-step="step" :this-step="2" @continue="step = 3" @back="step = 1">
            <v-card class="mb-12" min-height="200px">
                <v-row align="center">
                    <v-col cols="6" sm="3">
                        <v-subheader v-text="$t('project.new.step2.userselect')" />
                    </v-col>
                    <v-col cols="6" sm="3">
                        <v-select :items="users" dense> </v-select>
                    </v-col>

                    <v-col cols="6" sm="3">
                        <v-subheader v-text="$t('project.new.step2.projectname')" />
                    </v-col>
                    <v-col cols="6" sm="3">
                        <v-text-field single-line dense></v-text-field>
                    </v-col>

                    <v-col cols="12" sm="4">
                        <v-subheader v-text="$t('project.new.step2.projectsummary')" />
                    </v-col>
                    <v-col cols="12" sm="8">
                        <v-text-field single-line dense></v-text-field>
                    </v-col>

                    <v-col cols="12" sm="4">
                        <v-subheader v-text="$t('project.new.step2.projectcategory')" />
                    </v-col>
                    <v-col cols="12" sm="8">
                        <v-select :items="categories" dense> </v-select>
                    </v-col>
                </v-row>
            </v-card>
        </StepperStep>

        <StepperStep :current-step="step" :this-step="3" :optional="true" @continue="step = 4" @back="step = 2">
            <v-card class="mb-12 pa-1" min-height="200px">
                <v-row>
                    <v-subheader v-text="$t('project.new.step3.links')" />
                    <v-divider />

                    <v-col cols="12" sm="4">
                        <v-subheader v-text="$t('project.new.step3.homepage')" />
                    </v-col>
                    <v-col cols="12" sm="8">
                        <v-text-field single-line dense></v-text-field>
                    </v-col>

                    <v-col cols="12" sm="4">
                        <v-subheader v-text="$t('project.new.step3.issues')" />
                    </v-col>
                    <v-col cols="12" sm="8">
                        <v-text-field single-line dense></v-text-field>
                    </v-col>

                    <v-col cols="12" sm="4">
                        <v-subheader v-text="$t('project.new.step3.source')" />
                    </v-col>
                    <v-col cols="12" sm="8">
                        <v-text-field single-line dense></v-text-field>
                    </v-col>

                    <v-col cols="12" sm="4">
                        <v-subheader v-text="$t('project.new.step3.support')" />
                    </v-col>
                    <v-col cols="12" sm="8">
                        <v-text-field single-line dense></v-text-field>
                    </v-col>

                    <v-subheader v-text="$t('project.new.step3.licence')" />
                    <v-divider />

                    <v-col cols="12" sm="4">
                        <v-subheader v-text="$t('project.new.step3.type')" />
                    </v-col>
                    <v-col cols="12" sm="8">
                        <v-select dense :items="licences"></v-select>
                    </v-col>

                    <v-col cols="12" sm="4">
                        <v-subheader v-text="$t('project.new.step3.url')" />
                    </v-col>

                    <v-col cols="12" sm="8">
                        <v-text-field single-line dense></v-text-field>
                    </v-col>

                    <v-subheader v-text="$t('project.new.step3.seo')" />
                    <v-divider />

                    <v-col cols="12" sm="4">
                        <v-subheader v-text="$t('project.new.step3.keywords')" />
                    </v-col>

                    <v-col cols="12" sm="8">
                        <v-combobox chips deletable-chips multiple dense></v-combobox>
                    </v-col>
                </v-row>
            </v-card>
        </StepperStep>

        <StepperStep :current-step="step" :this-step="4" :optional="true" @continue="step = 5" @back="step = 3">
            <v-tabs v-model="spigotConvertTab" fixed-tabs>
                <v-tab v-text="$t('project.new.step4.convert')"></v-tab>
                <v-tab v-text="$t('project.new.step4.preview')"></v-tab>
                <v-tab v-text="$t('project.new.step4.tutorial')"></v-tab>
            </v-tabs>
            <v-tabs-items v-model="spigotConvertTab">
                <!-- todo spigot bbcode converter thingy -->
                <v-tab-item>1 </v-tab-item>
                <v-tab-item>2 </v-tab-item>
                <v-tab-item>3 </v-tab-item>
            </v-tabs-items>
        </StepperStep>

        <StepperStep :current-step="step" :this-step="5" :buttons="false">
            {{ $t('project.new.step5.text') }}
            <v-progress-circular indeterminate color="red"></v-progress-circular>
        </StepperStep>
    </v-stepper>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import StepperStep from '~/components/StepperStep.vue';
@Component({
    components: { StepperStep },
})
export default class NewPage extends Vue {
    step = 1;
    spigotConvertTab = 0;

    // TODO get user name + orgs user has access too
    get users() {
        return ['Paper', 'Some Org'];
    }

    // TODO get categories
    get categories() {
        return ['Shit Plugins', 'Good Plugins'];
    }

    // TODO do we want to get those from the server?
    get licences() {
        return ['MIT', 'Apache 2.0', 'GPL', 'LGPL'];
    }

    // TODO implement actual logic
}
</script>

<style lang="scss" scoped>
.v-divider {
    width: 100%;
    flex: none;
}
</style>
