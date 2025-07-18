<template>
  <table class="simple-table">
    <slot />
  </table>
</template>

<style lang="scss">
.simple-table {
  table-layout: fixed;
  overflow-wrap: break-word;

  @media (min-width: 768px) {
    table-layout: auto;
  }

  td,
  th {
    padding: 0.5rem;
  }

  th {
    text-align: left;
  }

  tbody {
    tr {
      border-top-width: thin;
      border-top-style: solid;
      border-top-color: #e1e1e1;
      .dark & {
        border-top-color: #656565;
      }
    }
  }
}
</style>
