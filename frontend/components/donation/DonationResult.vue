<template>
    <div v-if="qs['donation']" class="row">
        <div class="col-12">
            <Announcement :announcement="announcement" />
        </div>
    </div>
</template>

<script lang="ts">
import { Component, Vue } from 'nuxt-property-decorator';
import Announcement from '~/components/layouts/Announcement.vue';

@Component({
    components: { Announcement },
})
export default class DonationResult extends Vue {
    get qs() {
        return this.$route.query;
    }

    get announcement() {
        let text = '';
        if (this.qs.donation && this.qs.donation === 'success' && this.qs.st && this.qs.st === 'Completed') {
            text = 'Donation successful! You donated ' + this.qs.amt + ' ' + this.qs.cc + ' ' + this.qs.item_name;
        } else if (this.qs.donation && this.qs.donation === 'failure') {
            text = 'Donation failed.';
        } else {
            text = JSON.stringify(this.qs);
        }

        return {
            text,
            color: '#093962',
        };
    }
}
</script>

<style scoped></style>
