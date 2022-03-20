<script lang="ts" setup>
import { onMounted, watch } from "vue";
import * as Chartist from "chartist";
import "chartist-plugin-legend";

const props = defineProps<{
  id: string;
  barType: "Pie" | "Bar" | "Line" | "Candle";
  data: Chartist.IChartistData;
  options: Chartist.IChartOptions;
}>();

let chart: Chartist.IChartistBase<Chartist.IChartOptions> | null = null;

onMounted(draw);
watch(props.data, draw, { deep: true });

function draw() {
  if (chart) {
    chart.update(props.data, props.options);
  } else {
    chart = new Chartist[props.barType]("#" + props.id, props.data, props.options);
  }
}
</script>

<template>
  <div :id="id" class="ct-chart" />
</template>

<style lang="scss">
@import "chartist/dist/scss/chartist";

.ct-label {
  stroke: rgba(0, 0, 0, 0.7);
}

.ct-grid {
  stroke: rgba(0, 0, 0, 0.2);
}

.dark {
  .ct-label {
    color: rgba(255, 255, 255, 0.7);
  }

  .ct-grid {
    stroke: rgba(255, 255, 255, 0.2);
  }
}

// ct legend plugin stuff
.ct-legend {
  position: relative;
  z-index: 10;
  list-style: none;
  text-align: center;

  li {
    position: relative;
    padding-right: 10px;
    padding-left: 23px;
    margin-bottom: 3px;
    cursor: pointer;
    display: inline-block;
  }

  li:before {
    width: 12px;
    height: 12px;
    position: absolute;
    left: 0;
    content: "";
    border: 3px solid transparent;
    border-radius: 2px;
  }

  li.inactive:before {
    background: transparent;
  }

  &.ct-legend-inside {
    position: absolute;
    top: 0;
    right: 0;
  }

  @for $i from 0 to length($ct-series-colors) {
    .ct-series-#{$i}:before {
      background-color: nth($ct-series-colors, $i + 1);
      border-color: nth($ct-series-colors, $i + 1);
    }
  }
}
</style>
