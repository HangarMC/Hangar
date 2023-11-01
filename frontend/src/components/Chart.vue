<script lang="ts" setup>
import { onMounted, watch } from "vue";
import "chartist-plugin-legend";
import type { BarChartData, BarChartOptions, LineChartData, LineChartOptions, PieChartData, PieChartOptions, BaseChart } from "chartist";
import { BarChart, LineChart, PieChart } from "chartist";

const props = defineProps<{
  id: string;
  barType: "Pie" | "Bar" | "Line";
  data: PieChartData | BarChartData | LineChartData;
  options: PieChartOptions | BarChartOptions | LineChartOptions;
}>();

let chart: BaseChart | null = null;

onMounted(draw);
watch(props.data, draw, { deep: true });

function draw() {
  if (chart) {
    chart.update(props.data, props.options);
  } else {
    switch (props.barType) {
      case "Pie":
        chart = new PieChart("#" + props.id, props.data as PieChartData, props.options);
        break;
      case "Bar":
        chart = new BarChart("#" + props.id, props.data as BarChartData, props.options);
        break;
      case "Line":
        chart = new LineChart("#" + props.id, props.data as LineChartData, props.options);
        break;
    }
  }
}
</script>

<template>
  <div :id="id" class="ct-chart" />
</template>

<style lang="scss">
@import "chartist/dist/index.scss";

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
