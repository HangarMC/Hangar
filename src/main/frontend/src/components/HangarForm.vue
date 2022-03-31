<template>
    <form :action="action" :method="method" :enctype="enctype" :id="id" :class="clazz" :autocomplete="noAutocomplete ? 'off' : 'on'">
        <input type="hidden" :name="name" :value="value" />
        <slot></slot>
    </form>
</template>
<script>
export default {
    name: 'HangarForm',
    props: {
        action: String,
        method: String,
        enctype: String,
        id: String,
        clazz: String,
        noAutocomplete: Boolean,
    },
    data() {
        return {
            name: window.csrfInfo.parameterName,
            value: window.csrfInfo.token,
        };
    },
};
</script>
