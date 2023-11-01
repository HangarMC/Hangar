import type { VariantFunction } from "unocss";
import { presetAttributify, presetTypography, presetWind, transformerDirectives, transformerVariantGroup } from "unocss";
import type { UnocssNuxtOptions } from "@unocss/nuxt";

export function parent(name: string): VariantFunction {
  return wrap(name, "." + name);
}

export function wrap(name: string, wrap: string): VariantFunction {
  return (matcher) => {
    if (!matcher.startsWith(name + ":")) return matcher;
    return {
      matcher: matcher.slice((name + ":").length),
      selector: (s) => `${wrap} ${s}`,
    };
  };
}

export default {
  presets: [presetWind(), presetAttributify(), presetTypography()],
  transformers: [transformerVariantGroup(), transformerDirectives()],
  autoImport: false,
  safelist: "order-last button-primary button-secondary button-red button-transparent".split(" "),
  shortcuts: {
    "background-body": "bg-gray-100 dark:bg-gray-900",
    "background-default": "bg-gray-50 dark:bg-gray-800",
    "background-card": "bg-slate-200 dark:bg-slate-700",
    "shadow-default": "shadow-lg shadow-gray-300 dark:shadow-gray-900",
    "color-primary": "text-primary-400 dark:text-primary-200",
    "border-top-primary": "border-solid border-t-4 border-t-primary-400",
    "button-primary": "bg-primary-400 enabled:hover:bg-primary-300",
    "button-secondary": "bg-secondary-500 enabled:hover:(bg-secondary-400 dark:bg-secondary-600)",
    "button-transparent": "bg-transparent enabled:hover:(bg-primary-400/15 text-primary-400 dark:text-primary-100)",
    "button-red": "bg-red-500 dark:bg-red-600 enabled:hover:(bg-red-400 dark:bg-red-500)",
    "text-gray": "text-gray-600 dark:text-gray-300",
    "text-gray-secondary": "text-gray-500 dark:text-gray-400",
  },
  rules: [],
  variants: [
    parent("error"),
    parent("filled"),
    wrap("input-focused", "input:focus ~"),
    wrap("input-hover", "input:not(:focus):hover ~"),
    wrap("input-focus-visible", "input:focus-visible ~"),
    wrap("select-focused", "select:focus ~"),
  ],
  theme: {
    colors: {
      transparent: "transparent",
      current: "currentColor",
      primary: {
        0: "#E6EDFD",
        50: "#CCDCFB",
        100: "#99B8F6",
        200: "#6695F2",
        300: "#3371ED",
        400: "#004EE9",
        500: "#003EBA",
        600: "#002F8C",
        700: "#001F5D",
        800: "#00102F",
        900: "#000817",
      },
      gray: {
        50: "#fafafa",
        100: "#f4f4f5",
        200: "#e4e4e7",
        300: "#d4d4d8",
        400: "#a1a1aa",
        500: "#71717a",
        600: "#52525b",
        700: "#3f3f46",
        800: "#27272a",
        900: "#18181b",
      },
      secondary: {
        50: "#f8fafc",
        100: "#f1f5f9",
        200: "#e2e8f0",
        300: "#cbd5e1",
        400: "#94a3b8",
        500: "#64748b",
        600: "#475569",
        700: "#334155",
        800: "#1e293b",
        900: "#0f172a",
      },
      white: "#f8fafc",
      black: "#0f172a",
    },
  },
} as UnocssNuxtOptions;
