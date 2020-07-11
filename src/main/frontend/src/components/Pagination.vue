<template>
    <ul class="pagination">
        <li :class="{ disabled: !hasPrevious }">
            <a @click="previous">«</a>
        </li>
        <li v-if="current >= 3">
            <a @click="jump(1)">{{ 1 }}</a>
        </li>
        <li class="disabled" v-if="current >= 4 && total > 4">
            <a>...</a>
        </li>
        <li v-if="current === total && current - 3 > 0">
            <a @click="jump(current - 2)">{{ current - 2 }}</a>
        </li>
        <li v-if="current - 1 > 0">
            <a @click="jump(current - 1)">{{ current - 1 }}</a>
        </li>
        <li class="active">
            <a>{{ current }}</a>
        </li>
        <li v-if="current + 1 <= total">
            <a @click="jump(current + 1)">{{ current + 1 }}</a>
        </li>
        <li v-if="current === 1 && total > 3">
            <a @click="jump(current + 2)">{{ current + 2 }}</a>
        </li>
        <li class="disabled" v-if="total - current >= 3 && total > 4">
            <a>...</a>
        </li>
        <li v-if="total - current >= 2">
            <a @click="jump(total)">{{ total }}</a>
        </li>
        <li :class="{ disabled: !hasNext }" @click="next">
            <a>»</a>
        </li>
    </ul>
</template>

<script>
    export default {
        props: {
            current: {
                type: Number,
                required: true
            },
            total: {
                type: Number,
                required: true
            }
        },
        computed: {
            hasPrevious: function() {
                return this.current - 1 >= 1;
            },
            hasNext: function () {
                return this.current + 1 <= this.total;
            }
        },
        methods: {
            previous: function () {
                if(this.hasPrevious) {
                    this.$emit('prev')
                }
            },
            next: function () {
                if(this.hasNext) {
                    this.$emit('next')
                }
            },
            jump: function (page) {
                if(page > 0 <= this.total) {
                    this.$emit('jumpTo', page)
                }
            }
        }
    }
</script>

<style lang="scss">
    @import "./../scss/variables";

    .pagination {
        display: flex;
        justify-content: center;

        > li {
            margin-right: 1rem;
            cursor: pointer;

            &:last-child {
                margin-right: 0;
            }

            &.disabled a, &.disabled a:hover {
                background: transparent;
                border: 1px solid #ddd;
                color: inherit;
            }

            a {
                border: 1px solid #ddd;
                padding: 0.85rem 1.6rem;
                background: #ffffff;
                color: $sponge_grey;

                &:first-child, &:last-child {
                    border-radius: 0;
                }
            }

            &.active {
                > a, > a:hover {
                    cursor: pointer;
                    color: darken($sponge_yellow, 30);
                }
            }
        }
    }
</style>
