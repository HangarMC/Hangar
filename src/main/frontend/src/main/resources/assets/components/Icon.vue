<template>
    <a :href="linkUrl">
        <img class="user-avatar" :class="extraClasses" :src="src" :alt="name" />
    </a>
</template>

<script>
    export default {
        props: {
            src: {
                type: String,
                required: true
            },
            href: String,
            extraClasses: String,
            name: String
        },
        computed: {
            linkUrl: function () {
                if(this.href != null) {
                    return this.href;
                } else if(this.name != null) {
                    return jsRoutes.controllers.Users.showProjects(this.name, null).absoluteURL()
                } else {
                    return "#";
                }
            }
        }
    }
</script>
