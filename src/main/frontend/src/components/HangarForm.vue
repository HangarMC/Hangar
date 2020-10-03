<template>
    <form :action="action" :method="method" :enctype="enctype" :id="id" :class="clazz">
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
    },
    data() {
        return {
            name: window.csrfInfo.parameterName,
            value: window.csrfInfo.token,
        };
    },
};
</script>
