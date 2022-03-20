<template>
  <table class="simple-table">
    <slot></slot>
  </table>
</template>

<style lang="scss">
.simple-table {
  td,
  th {
    padding: 0.5rem;
  }

  th {
    text-align: left;
  }

  tbody {
    tr {
      border-top: thin solid #e1e1e1;
      .dark & {
        border-top: thin solid #656565;
      }
    }
  }
}
</style>
