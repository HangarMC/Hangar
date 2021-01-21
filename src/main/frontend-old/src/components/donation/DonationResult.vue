<template>
    <div class="row">
        <div class="col-12">
            <div class="announcement" style="background-color: #093962">
                <template v-if="qs['donation'] && qs['donation'] === 'success' && qs['st'] && qs['st'] === 'Completed'">
                    Donation successful!<br />
                    You donated {{ qs['amt'] }} {{ qs['cc'] }}<br />
                    {{ qs['item_name'] }}
                </template>
                <template v-else-if="qs['donation'] && qs['donation'] === 'failure'"> Donation failed. </template>
                <template v-else>
                    {{ qs }}
                </template>
            </div>
        </div>
    </div>
</template>

<script>
import qs from 'qs';

export default {
    name: 'DonationResult',
    computed: {
        qs() {
            return qs.parse(window.location.search.substr(1));
        },
    },
};
</script>

<style scoped></style>
