import { defineConfig } from "vite-plugin-windicss";
import colors from "windicss/colors";
import typography from "windicss/plugin/typography";
import plugin from "windicss/plugin";

export default defineConfig({
  darkMode: "class",
  safelist: "order-last button-primary button-gray button-red",
  attributify: true,
  plugins: [
    typography(),
    plugin(({ addVariant }) => {
      addVariant("error", ({ style }) => {
        return style.parent(".error");
      });
      addVariant("filled", ({ style }) => {
        return style.parent(".filled");
      });
      addVariant("input-focused", ({ style }) => {
        return style.wrapSelector((s) => "input:focus ~ " + s);
      });
      addVariant("input-focus-visible", ({ style }) => {
        return style.wrapSelector((s) => "input:focus-visible ~ " + s);
      });
      addVariant("select-focused", ({ style }) => {
        return style.wrapSelector((s) => "select:focus ~ " + s);
      });
    }),
  ],
  theme: {
    extend: {
      typography: {
        DEFAULT: {
          css: {
            maxWidth: "65ch",
            color: "inherit",
            lineHeight: "1.6em",
            a: {
              color: "inherit",
              opacity: 0.75,
              fontWeight: "500",
              textDecoration: "underline",
              "&:hover": {
                opacity: 1,
                color: colors.teal[600],
              },
            },
            b: { color: "inherit" },
            strong: { color: "inherit" },
            em: { color: "inherit" },
            h1: {
              color: "inherit",
              fontWeight: "600",
              marginBottom: "0.6em",
            },
            h2: {
              color: "inherit",
              fontWeight: "600",
              marginTop: "inherit",
              marginBottom: "0.6em",
            },
            h3: {
              color: "inherit",
              marginTop: "inherit",
              marginBottom: "0.6em",
            },
            h4: {
              color: "inherit",
              marginTop: "inherit",
              marginBottom: "0.6em",
            },
            code: { color: "inherit" },
            li: {
              marginTop: "0.2em",
              marginBottom: "0.2em",
            },
          },
        },
      },
      colors: {
        primary: {
          0: "#E6EDFD", // old primary-50
          50: "#CCDCFB",
          100: "#99B8F6", // old primary-70
          200: "#6695F2",
          300: "#3371ED",
          400: "#004EE9", // old primary-100
          500: "#003EBA",
          600: "#002F8C",
          700: "#001F5D",
          800: "#00102F",
          900: "#000817",
        },
        "primary-light": {
          0: "#FFFFFF",
          100: "#F5F8FE",
          200: "#EBF1FD",
          300: "#E0EAFC",
          400: "#D6E3FB",
          500: "#CCDCFB",
          600: "#C2D4FA",
          700: "#B8CDF9",
          800: "#ADC6F8",
          900: "#A3BFF7",
          1000: "#99B8F6",
        },
        "background-dark-90": "#111111",
        "background-dark-80": "#181a1b",
        "background-light-10": "#f8faff",
        "background-light-0": "#ffffff",
      },
    },
  },
  shortcuts: {
    "background-header": "bg-background-light-0 dark:bg-background-dark-90",
    "background-body": "bg-background-light-10 dark:bg-background-dark-80",
    "color-primary": "text-primary-400 dark:text-primary-100",
    "border-top-primary": "border-solid border-t-4 border-t-primary-400",
    "button-primary": "bg-primary-400 disabled:(bg-primary-100 dark:(bg-primary-800 text-neutral-500) cursor-not-allowed) enabled:hover:bg-primary-300",
    "button-submit": "bg-primary-400 disabled:(bg-primary-100 dark:(bg-primary-800 text-neutral-500) cursor-not-allowed) enabled:hover:bg-primary-300",
    "button-red":
      "bg-red-500 dark:bg-red-600 disabled:(bg-red-300 dark:(bg-red-900 text-neutral-400) cursor-not-allowed) enabled:hover:(bg-red-400 dark:bg-red-500)",
    "button-gray":
      "bg-zinc-500 dark:bg-slate-700 disabled:(bg-zinc-300 text-neutral-500 dark:bg-zinc-800 cursor-not-allowed) enabled:hover:(bg-zinc-400 dark:bg-slate-600)",
  },
});
