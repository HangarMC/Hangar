import { defineConfig, presetAttributify, presetTypography, presetWind, transformerDirectives, transformerVariantGroup } from "unocss";
import type { VariantFunction } from "unocss";

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

export default defineConfig({
  presets: [presetWind(), presetAttributify(), presetTypography()],
  transformers: [transformerVariantGroup(), transformerDirectives()],
  safelist: "order-last button-primary button-secondary button-red button-transparent".split(" "),
  shortcuts: {
    "background-body": "bg-gray-100 dark:bg-charcoal-800",
    "background-default": "bg-gray-50 dark:bg-charcoal-600",
    "background-card": "dark:bg-[linear-gradient(30deg,_transparent_40%,_var(--primary-500)_250%)] scale-101",
    "shadow-default": "shadow-lg shadow-gray-300 dark:shadow-gray-900",
    "color-primary": "text-primary-500 dark:text-primary-300",
    "border-top-primary": "border-solid border-t-4 border-t-primary-500",
    "button-primary": "bg-primary-500 enabled:hover:bg-gray-700",
    "button-secondary": "bg-charcoal-600 enabled:hover:(bg-gray-700 dark:charcoal-600)",
    "button-transparent": "bg-transparent enabled:hover:(bg-gray-700)",
    "button-red": "bg-red-500 dark:bg-red-600 enabled:hover:(bg-red-400 dark:bg-red-500)",
    "text-gray": "text-gray-600 dark:text-gray-400",
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
        0: "var(--primary-0)",
        50: "var(--primary-50)",
        100: "var(--primary-100)",
        200: "var(--primary-200)",
        300: "var(--primary-300)",
        400: "var(--primary-400)",
        500: "var(--primary-500)",
        600: "var(--primary-600)",
        700: "var(--primary-700)",
        800: "var(--primary-800)",
        900: "var(--primary-900)",
      },
      gray: {
        50: "var(--gray-50)",
        100: "var(--gray-100)",
        200: "var(--gray-200)",
        300: "var(--gray-300)",
        400: "var(--gray-400)",
        500: "var(--gray-500)",
        600: "var(--gray-600)",
        700: "var(--gray-700)",
        800: "var(--gray-800)",
        900: "var(--gray-900)",
      },
      charcoal: {
        50: "var(--charcoal-50)",
        100: "var(--charcoal-100)",
        200: "var(--charcoal-200)",
        300: "var(--charcoal-300)",
        400: "var(--charcoal-400)",
        500: "var(--charcoal-500)",
        600: "var(--charcoal-600)",
        700: "var(--charcoal-700)",
        800: "var(--charcoal-800)",
        900: "var(--charcoal-900)",
            },
      secondary: {
        50: "var(--secondary-50)",
        100: "var(--secondary-100)",
        200: "var(--secondary-200)",
        300: "var(--secondary-300)",
        400: "var(--secondary-400)",
        500: "var(--secondary-500)",
        600: "var(--secondary-600)",
        700: "var(--secondary-700)",
        800: "var(--secondary-800)",
        900: "var(--secondary-900)",
      },
      white: "#f8fafc",
      black: "#0f172a",
    },
  },
});
