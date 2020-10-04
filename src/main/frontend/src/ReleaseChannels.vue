<template>
    <table class="table table-dark">
        <thead class="thead-dark text-center">
            <tr>
                <th scope="col">
                    <i class="fas fa-tag"></i>
                    Channel Name
                </th>
                <th scope="col">
                    <i class="fas fa-list-ol"></i>
                    Version Count
                </th>
                <th scope="col">
                    <i class="fas fa-edit"></i>
                    Edit
                </th>
                <th scope="col">
                    <i class="fas fa-trash"></i>
                    Trash
                </th>
            </tr>
        </thead>
        <tbody>
            <tr v-for="{ versionCount, channel } in channels" :key="channel.id">
                <td>
                    <div class="channel" :style="{ backgroundColor: channel.color.hex }">{{ channel.name }}</div>
                </td>
                <td class="version-count" :style="{ color: versionCount ? 'var(--danger)' : 'var(--success)' }">
                    {{ versionCount }}
                </td>
                <td>
                    <div class="btn btn-sm btn-warning" @click="editChannel(channel)">Edit</div>
                </td>
                <td>
                    <div class="btn btn-sm" :class="versionCount ? 'btn-danger' : 'btn-warning'" @click.prevent="delChannel(channel, versionCount)">Delete</div>
                </td>
            </tr>
            <tr>
                <td colspan="4">
                    <button type="button" class="btn btn-sm btn-block btn-primary" @click="addChannel">
                        <i class="fas fa-plus"></i>
                        Add Channel
                    </button>
                </td>
            </tr>
        </tbody>
    </table>
    <NewChannel
        :edit="edit"
        v-model:name-prop="newChannel.name"
        v-model:color-prop="newChannel.color"
        v-model:non-reviewed-prop="newChannel.nonReviewed"
        @channel-created="editFinal($event)"
    ></NewChannel>
    <form ref="edit-form" :action="ROUTES.parse('CHANNELS_SAVE', ownerName, projectSlug, newChannel.oldName)" method="post" class="d-none">
        <input type="hidden" name="name" :value="newChannel.name" class="d-none" required />
        <input type="hidden" name="color" :value="newChannel.color.hex" class="d-none" required />
        <input type="hidden" name="nonReviewed" :value="newChannel.nonReviewed" class="d-none" required />
        <input type="hidden" :name="CSRF_INFO.parameterName" :value="CSRF_INFO.token" required />
    </form>
</template>
<script>
import $ from 'jquery';
import 'bootstrap/js/dist/modal';
import NewChannel from '@/components/NewChannel';

export default {
    name: 'ReleaseChannels.vue',
    components: {
        NewChannel,
    },
    data() {
        return {
            CSRF_INFO: window.csrfInfo,
            ROUTES: window.ROUTES,
            ownerName: window.OWNER_NAME,
            projectSlug: window.PROJECT_SLUG,
            channels: window.CHANNELS,
            newChannel: {
                name: null,
                color: {
                    hex: null,
                    value: null,
                },
                nonReviewed: false,
                oldName: null,
            },
            edit: true,
        };
    },
    methods: {
        editChannel(channel) {
            this.edit = true;
            this.newChannel.oldName = channel.name;
            this.newChannel.name = `${channel.name}`;
            this.newChannel.color.hex = channel.color.hex;
            this.newChannel.color.value = channel.color.value;
            this.newChannel.nonReviewed = channel.isNonReviewed;
            $('#channel-settings').modal('show');
        },
        delChannel(channel, versionCount) {
            $('#delete-form').attr('action', this.ROUTES.parse('CHANNELS_DELETE', this.ownerName, this.projectSlug, channel.name));
            if (versionCount > 0) {
                $('#version-count-on-delete').text(versionCount);
                $('#modal-delete').modal('show');
            } else {
                $('#delete-form').submit();
            }
        },
        editFinal(state) {
            if (state === 'EDIT') {
                if (!this.$refs['edit-form'].reportValidity()) {
                    location.reload();
                }
                this.$refs['edit-form'].submit();
            } else if (state === 'NEW') {
                this.$refs['edit-form'].action = this.ROUTES.parse('CHANNELS_CREATE', this.ownerName, this.projectSlug);
                if (!this.$refs['edit-form'].reportValidity()) {
                    location.reload();
                }
                this.$refs['edit-form'].submit();
            } else {
                location.reload();
            }
        },
        addChannel() {
            this.edit = false;
            this.newChannel.oldName = null;
            this.newChannel.name = null;
            this.newChannel.color.hex = null;
            this.newChannel.color.value = null;
            this.newChannel.nonReviewed = false;
            $('#channel-settings').modal('show');
        },
    },
};
</script>
<style lang="scss" scoped>
table {
    text-align: center;

    th {
        vertical-align: middle;
    }

    .version-count {
        font-weight: bold;
        font-size: 1.5em;
    }
}
</style>
