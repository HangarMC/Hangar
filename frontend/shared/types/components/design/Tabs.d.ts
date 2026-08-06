import type { Component } from "vue";

export interface Tab<T extends string> {
  value: T;
  header: string;
  icon?: Component;
  show?: () => boolean;
  disable?: () => boolean;
}
