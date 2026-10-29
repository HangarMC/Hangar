import type { Component } from "vue";

export interface Tab<T extends string> {
  value: T;
  header: string;
  icon?: Component;
  show?: () => boolean;
  disable?: () => boolean;
  /**
  Draws a divider above this tab, setting it apart from the group before it.
  */
  separated?: boolean;
}
