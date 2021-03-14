<template>
    <!-- todo fix the layout of this modal, some buttons and shit, also i18n, also am tired -->
    <v-dialog v-model="show">
        <template #activator="{ on, attrs }">
            <slot name="activator" :on="on" :attrs="attrs" />
        </template>
        <v-card>
            <v-card-title>
                <h4>Donate to ....</h4>
                <v-btn @click="show = false">&times;</v-btn>
            </v-card-title>
            <v-card-text>
                <v-row>
                    <v-col cols="12">
                        <button type="button" :class="'btn ' + (monthly ? 'btn-primary' : 'btn-default')" @click="monthly = true">Monthly</button>
                        <button type="button" :class="'btn ' + (monthly ? 'btn-default' : 'btn-primary')" @click="monthly = false">One-Time</button>
                    </v-col>
                </v-row>

                <form action="https://sandbox.paypal.com/cgi-bin/webscr" method="post" @submit="popup($el)">
                    <!-- https://developer.paypal.com/docs/paypal-payments-standard/integration-guide/Appx-websitestandard-htmlvariables/ -->
                    <input type="hidden" name="business" :value="donationEmail" />
                    <input type="hidden" name="return" :value="returnUrl" />
                    <input type="hidden" name="cancel_return" :value="cancelReturnUrl" />
                    <!-- return method, 2 = POST, with all variables -->
                    <input type="hidden" name="rm" value="1" />
                    <input type="hidden" name="item_name" :value="'Hangar: ' + (monthly ? 'Monthly ' : 'One-Time ') + 'Donation to ' + donationTarget" />
                    <input type="hidden" name="quantity" value="1" />
                    <!-- locale -->
                    <input type="hidden" name="lc" value="US" />
                    <input type="hidden" name="currency_code" value="USD" />
                    <input type="hidden" name="charset" value="utf-8" />
                    <input type="hidden" name="notify_url" value="https://hangar.minidigger.me/paypal/ipn" />
                    <input type="hidden" name="custom" value="idk, maybe hangar user id?" />

                    <template v-if="monthly">
                        <input type="hidden" name="cmd" value="_xclick-subscriptions" />
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
                        <input type="hidden" name="bn" :value="'Hangar_Donate_' + donationTarget + 'US'" />
                    </template>

                    <v-row>
                        <v-col cols="12">
                            <button
                                v-for="a in monthly ? monthlyAmounts : oneTimeAmounts"
                                :key="a"
                                :class="'btn ' + (amount === a ? 'btn-primary' : 'btn-default')"
                                @click.prevent="amount = a"
                            >
                                ${{ a }}
                            </button>
                        </v-col>
                    </v-row>

                    <v-row>
                        <v-col cols="12">
                            <div>Select an amount above or enter an amount below</div>
                        </v-col>
                    </v-row>

                    <v-row>
                        <v-col cols="12">
                            <div class="input-group mb-3">
                                <input v-model="amount" type="number" class="form-control" :name="monthly ? 'a3' : 'amount'" />
                                <span class="input-group-text">USD</span>
                            </div>
                            <small class="form-text">By donating to X you agree to Y and that tacos are delicious</small>
                            <div>
                                <input type="submit" :value="'Donate ' + (monthly ? 'Monthly ' : 'One-Time ')" class="btn btn-primary" />
                                <img alt="" width="1" height="1" src="https://www.paypalobjects.com/en_US/i/scr/pixel.gif" />
                            </div>
                        </v-col>
                    </v-row>
                </form>
            </v-card-text>
            <v-card-actions>
                <v-btn @click="show = false">Close</v-btn>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'nuxt-property-decorator';

@Component({})
export default class DonationModal extends Vue {
    monthly: boolean = true;
    amount: number = 20;
    show: boolean = false;

    @Prop({ required: true })
    donationTarget!: String;

    @Prop({ required: true })
    donationEmail!: String;

    @Prop({ required: true })
    returnUrl!: String;

    @Prop({ required: true })
    cancelReturnUrl!: String;

    oneTimeAmounts: Array<number> = [10, 20, 30];
    monthlyAmounts: Array<number> = [5, 10, 15, 20];
}
</script>

<style scoped></style>
