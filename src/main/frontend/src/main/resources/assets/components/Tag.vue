<template>
    <div class="tags" :class="{ 'has-addons': data }">
        <span v-bind:style="{ color: color.foreground, background: color.background,
            'border-color': color.background }" class="tag"> {{ name }} </span>
        <span v-if="data" class="tag">{{ data }}</span>
    </div>
</template>

<script>
    export default {
        props: {
            name: String,
            data: String,
            color: Object
        }
    }
</script>
