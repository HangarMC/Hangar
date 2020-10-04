<template>
    <HangarModal target-id="channel-settings" label-id="settings-label">
        <template v-slot:activator="slotProps">
            <slot name="activator" v-bind:targetId="slotProps.targetId"></slot>
        </template>
        <template v-slot:modal-content>
            <div class="modal-header">
                <h4 v-if="!edit" class="modal-title">New Channel</h4>
                <h4 v-else class="modal-title">Edit Channel</h4>
                <button type="button" class="close" data-dismiss="modal" aria-label="Cancel">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <p style="font-size: 12px" class="text-center">
                    Name must be: unique; alphanumeric, have no spaces, and max {{ maxChannelNameLen }} characters.
                </p>
                <div class="form-inline">
                    <div id="channel-input" class="input-group">
                        <input
                            type="text"
                            class="form-control"
                            :maxlength="`${maxChannelNameLen}`"
                            v-model="name"
                            aria-label="Channel Name"
                            placeholder="Channel Name"
                        />
                        <div class="input-group-append">
                            <div class="input-group-text color-popover-container">
                                <div v-show="showPopover" class="color-popover-arrow"></div>
                                <div v-show="showPopover" class="color-popover">
                                    <table>
                                        <tr v-for="(colorArr, index) in colorTable" :key="index">
                                            <td
                                                v-for="{ value, hex } in colorArr"
                                                :key="value"
                                                @click.prevent="closePopover(hex, value)"
                                                :style="{ color: hex }"
                                                class="color-circle"
                                            >
                                                <i class="fas fa-circle fa-lg"></i>
                                            </td>
                                        </tr>
                                    </table>
                                </div>
                                <a
                                    id="color-popover-open"
                                    href="#"
                                    :style="{ color: color.hex || 'black', cursor: 'pointer' }"
                                    @click.prevent="showPopover = !showPopover"
                                >
                                    <span v-show="!color.hex">
                                        <i class="fas fa-lg fa-question-circle"></i>
                                    </span>
                                    <span v-show="color.hex">
                                        <i class="fas fa-lg fa-circle"></i>
                                    </span>
                                </a>
                            </div>
                        </div>
                    </div>
                    <div class="form-check form-check-inline">
                        <input type="checkbox" class="form-check-input" id="new-channel-exclude-input" v-model="nonReviewed" />
                        <label for="new-channel-exclude-input" class="form-check-label">Exclude from moderation review queue?</label>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button id="close-modal" type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                <button type="button" class="btn btn-primary" @click="finalizeChannel">{{ edit ? 'Edit' : 'Create' }}</button>
            </div>
        </template>
    </HangarModal>
</template>
<script>
import $ from 'jquery';
import 'bootstrap/js/dist/modal';
import HangarModal from '@/components/HangarModal';

const colors2dArray = [];
for (let i = 0; i < window.COLORS.length; i += 4) {
    colors2dArray.push(window.COLORS.slice(i, i + 4));
}

export default {
    name: 'NewChannel',
    components: {
        HangarModal,
    },
    emits: ['channel-created', 'update:nameProp', 'update:nonReviewedProp', 'update:colorProp'],
    props: {
        edit: {
            type: Boolean,
            default: false,
        },
        nameProp: String,
        nonReviewedProp: Boolean,
        colorProp: Object,
    },
    computed: {
        name: {
            get: function () {
                return this.nameProp;
            },
            set: function (val) {
                this.$emit('update:nameProp', val);
            },
        },
        nonReviewed: {
            get: function () {
                return this.nonReviewedProp;
            },
            set: function (val) {
                this.$emit('update:nonReviewedProp', val);
            },
        },
        color: {
            get: function () {
                return this.colorProp;
            },
            set: function (val) {
                this.$emit('update:colorProp', val);
            },
        },
    },
    data() {
        return {
            colorTable: colors2dArray,
            showPopover: false,
            maxChannelNameLen: window.MAX_CHANNEL_NAME_LEN,
        };
    },
    methods: {
        async closePopover(hex, value) {
            if (hex && typeof value !== 'undefined') {
                this.color.hex = hex;
                this.color.value = value;
            }
            this.showPopover = false;
        },
        finalizeChannel() {
            $('.invalid-input').removeClass('invalid-input');
            if (!this.name || this.name.trim().length === 0 || !/^[a-zA-Z0-9]+$/.test(this.name) || this.name.length > this.maxChannelNameLen) {
                $('#channel-input').addClass('invalid-input');
                return;
            } else if (!this.color.hex || !/^#[0-9a-f]{6}$/i.test(this.color.hex)) {
                $('#color-popover-open').addClass('invalid-input');
                return;
            }
            this.$emit('channel-created', this.edit ? 'EDIT' : 'NEW');
            $('#channel-settings').modal('hide');
            $(document.body).removeClass('modal-open');
            $('.modal-backdrop').remove();
        },
    },
};
</script>
<style lang="scss" scoped>
.color-popover-container {
    position: relative;

    .color-popover-arrow {
        position: absolute;
        width: 20px;
        height: 20px;
        background-color: #a6a6a6;
        box-shadow: 3px -3px 2px 2px #00000066;
        transform: rotate(45deg);
        right: -14px;
    }

    .color-popover {
        position: absolute;
        background-color: #ddd;
        box-shadow: 3px 3px 2px 2px #00000066;
        left: 110%;
        padding: 5px;
        border-radius: 4px;

        .color-circle {
            border-radius: 50%;
            cursor: pointer;
        }
    }
}
</style>
