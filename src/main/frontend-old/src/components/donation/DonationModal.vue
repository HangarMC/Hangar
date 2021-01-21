<template>
    <HangarModal target-id="donation-form" label-id="donation-label">
        <template v-slot:activator="slotProps">
            <slot name="activator" v-bind:targetId="slotProps.targetId"></slot>
        </template>
        <template v-slot:modal-content>
            <div class="modal-header">
                <h4>Donate to ....</h4>
                <button type="button" class="close" data-dismiss="modal" aria-label="Cancel">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <div class="row">
                    <div class="col-xs-12">
                        <button type="button" @click="monthly = true" :class="'btn ' + (monthly ? 'btn-primary' : 'btn-default')">Monthly</button>
                        <button type="button" @click="monthly = false" :class="'btn ' + (monthly ? 'btn-default' : 'btn-primary')">One-Time</button>
                    </div>
                </div>

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

                    <div class="row">
                        <div class="col-xs-12">
                            <button
                                v-for="a in monthly ? monthlyAmounts : oneTimeAmounts"
                                :key="a"
                                @click.prevent="amount = a"
                                :class="'btn ' + (amount === a ? 'btn-primary' : 'btn-default')"
                            >
                                ${{ a }}
                            </button>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-xs-12">
                            <div>Select an amount above or enter an amount below</div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="col-xs-12">
                            <div class="input-group mb-3">
                                <input type="number" class="form-control" :name="monthly ? 'a3' : 'amount'" v-model="amount" />
                                <span class="input-group-text">USD</span>
                            </div>
                            <small class="form-text">By donating to X you agree to Y and that tacos are delicious</small>
                            <div>
                                <input type="submit" :value="'Donate ' + (monthly ? 'Monthly ' : 'One-Time ')" class="btn btn-primary" />
                                <img alt="" width="1" height="1" src="https://www.paypalobjects.com/en_US/i/scr/pixel.gif" />
                            </div>
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button id="close-modal" type="button" class="btn btn-default" data-dismiss="modal">Close</button>
            </div>
        </template>
    </HangarModal>
</template>

<script>
import HangarModal from '@/components/HangarModal';

export default {
    name: 'DonationModal',
    components: { HangarModal },
    props: {
        donationTarget: {
            type: String,
            required: true,
        },
        donationEmail: {
            type: String,
            required: true,
        },
        returnUrl: {
            type: String,
            required: true,
        },
        cancelReturnUrl: {
            type: String,
            required: true,
        },
        oneTimeAmounts: {
            type: Array,
            default: () => [10, 20, 30],
        },
        monthlyAmounts: {
            type: Array,
            default: () => [5, 10, 15, 20],
        },
    },
    data() {
        return {
            monthly: true,
            amount: 20,
        };
    },
};
</script>

<style scoped></style>
