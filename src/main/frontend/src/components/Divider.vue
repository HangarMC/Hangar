<template>
    <div class="col-12">
        <div class="d-flex justify-content-center align-items-center">
            <div class="col-5 divider divider-before"></div>
            <div class="divider-text"><slot></slot></div>
            <div class="col-5 divider divider-after"></div>
        </div>
    </div>
</template>

<script>
export default {
    name: 'Divider',
};
</script>
<style scoped>
.divider-text {
    padding: 0 5px;
}
.divider {
    height: 2px;
    background-color: #00000082;
}
</style>
