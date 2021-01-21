<template>
    <slot name="activator" v-bind:targetId="targetId"></slot>
    <teleport to="body">
        <div class="modal fade" :id="targetId" tabindex="-1" role="dialog" :aria-labelledby="labelId" aria-hidden="true">
            <div class="modal-dialog" role="document" :class="modalClass">
                <div class="modal-content">
                    <slot name="modal-content"></slot>
                </div>
            </div>
        </div>
    </teleport>
</template>
<script>
export default {
    name: 'HangarModal',
    props: {
        targetId: String,
        labelId: String,
        modalClass: String,
    },
};
</script>
