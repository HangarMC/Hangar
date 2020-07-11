<template>
    <ProjectList :owner="user" :offset="(page - 1) * limit" :limit="limit"
                 @prevPage="page--" @nextPage="page++" @jumpToPage="page = $event"></ProjectList>
</template>

<script>
    import ProjectList from "./components/ProjectList";

    export default {
        components: {
            ProjectList
        },
        data: function() {
            return {
                page: 1,
                limit: 5,
                user: window.USERNAME
            }
        }
    }
</script>
