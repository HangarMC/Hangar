<template>
    <div :class="`col-md-${size}`">
        <div class="card">
            <div class="card-header">
                <h4 class="card-title" v-text="title" />
            </div>
            <slot></slot>
        </div>
    </div>
</template>

<script>
export default {
    name: 'Card',
    props: {
        title: String,
        size: {
            type: Number,
            default: 12,
        },
    },
};
</script>
