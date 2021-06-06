<template>
    <v-dialog v-model="show" width="500">
        <template #activator="{ on, attrs }">
            <slot name="activator" :on="on" :attrs="attrs" />
        </template>
        <v-card>
            <v-card-title>
                <h4>{{ $t('donate.title', [donationTarget]) }}</h4>
                <v-btn icon @click="show = false">
                    <v-icon>mdi-close</v-icon>
                </v-btn>
            </v-card-title>
            <v-card-text>
                <v-row>
                    <v-col cols="12">
                        <v-btn-toggle v-model="monthly" mandatory>
                            <v-btn :value="true">
                                {{ $t('donate.monthly') }}
                            </v-btn>
                            <v-btn :value="false">
                                {{ $t('donate.oneTime') }}
                            </v-btn>
                        </v-btn-toggle>
                    </v-col>
                </v-row>

                <form action="https://sandbox.paypal.com/cgi-bin/webscr" method="post" @submit="popup($el)">
                    <!-- https://developer.paypal.com/docs/paypal-payments-standard/integration-guide/Appx-websitestandard-htmlvariables/ -->
                    <input type="hidden" name="business" :value="donationEmail" />
                    <input type="hidden" name="return" :value="returnUrl" />
                    <input type="hidden" name="cancel_return" :value="cancelReturnUrl" />
                    <!-- return method, 2 = POST, with all variables -->
                    <input type="hidden" name="rm" value="1" />
                    <input
                        type="hidden"
                        name="item_name"
                        :value="'Hangar: ' + (monthly ? $t('donate.monthly') : $t('donate.oneTime')) + ' Donation to ' + donationTarget"
                    />
                    <input type="hidden" name="quantity" value="1" />
                    <!-- locale -->
                    <input type="hidden" name="lc" value="US" />
                    <input type="hidden" name="currency_code" value="USD" />
                    <input type="hidden" name="charset" value="utf-8" />
                    <input type="hidden" name="notify_url" value="https://hangar.minidigger.me/paypal/ipn" />
                    <input type="hidden" name="custom" value="idk, maybe hangar user id?" />

                    <template v-if="monthly">
                        <input type="hidden" name="cmd" value="_xclick-subscriptions" />
                        <input type="hidden" name="a3" :value="amount" />
                        <input type="hidden" name="bn" :value="'Hangar_Subscribe_' + donationTarget + 'US'" />
                        <input type="hidden" name="no_note" value="1" />
                        <!-- 1 = do not prompt for an address -->
                        <input type="hidden" name="no_shipping" value="1" />
                        <!-- recurring -->
                        <input type="hidden" name="src" value="1" />
                        <!-- p3 + t3 = 1 month -->
                        <input type="hidden" name="p3" value="1" />
                        <input type="hidden" name="t3" value="M" />
                    </template>
                    <template v-else>
                        <input type="hidden" name="cmd" value="_donations" />
                        <input type="hidden" name="amount" :value="amount" />
                        <input type="hidden" name="bn" :value="'Hangar_Donate_' + donationTarget + 'US'" />
                    </template>

                    <v-row>
                        <v-col cols="12">
                            <v-btn-toggle v-model="amount">
                                <v-btn v-for="a in monthly ? monthlyAmounts : oneTimeAmounts" :key="a" :value="a"> ${{ a }} </v-btn>
                            </v-btn-toggle>
                        </v-col>
                    </v-row>

                    <v-row>
                        <v-col cols="12">
                            <div>{{ $t('donate.selectAmount') }}</div>
                        </v-col>
                    </v-row>

                    <v-row>
                        <v-col cols="12">
                            <div class="input-group mb-3">
                                <v-text-field v-model.number="amount" label="Amount" type="number" prefix="$" />
                            </div>
                            <small class="form-text">{{ $t('donate.legal', [donationTarget]) }}</small>
                            <div>
                                <v-btn type="submit" color="primary">
                                    {{ $t('donate.submit', [monthly ? $t('donate.monthly') : $t('donate.oneTime')]) }}
                                </v-btn>
                                <v-btn @click="show = false">
                                    {{ $t('general.close') }}
                                </v-btn>
                                <img alt="" width="1" height="1" src="https://www.paypalobjects.com/en_US/i/scr/pixel.gif" />
                            </div>
                        </v-col>
                    </v-row>
                </form>
            </v-card-text>
        </v-card>
    </v-dialog>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'nuxt-property-decorator';

@Component({})
export default class DonationModal extends Vue {
    monthly: boolean = true;
    amount: number = 4;
    show: boolean = false;

    @Prop({ required: true })
    donationTarget!: String;

    @Prop({ required: true })
    donationEmail!: String;

    @Prop({ required: true })
    returnUrl!: String;

    @Prop({ required: true })
    cancelReturnUrl!: String;

    @Prop({ required: true })
    oneTimeAmounts!: Array<number>;

    @Prop({ required: true })
    monthlyAmounts!: Array<number>;

    @Prop({ required: true })
    defaultAmount!: number;

    created() {
        this.amount = this.defaultAmount;
    }
}
</script>

<style scoped></style>
