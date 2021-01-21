<template>
    <div class="setting">
        <div class="setting-description">
            <slot name="description" :setting-name="name" :desc="desc">
                <h4 :class="{ danger }">
                    {{ name }} <i v-if="optional">({{ $t('general.optional') }})</i>
                </h4>
                <p v-if="desc" :id="`${name.toLowerCase()}-desc`" v-text="desc"></p>
            </slot>
        </div>
        <div class="setting-content">
            <slot name="content"></slot>
        </div>
        <slot></slot>
        <div class="clearfix"></div>
    </div>
</template>

<script>
export default {
    name: 'Setting',
    props: {
        name: String,
        desc: String,
        optional: Boolean,
        danger: Boolean,
    },
};
</script>
