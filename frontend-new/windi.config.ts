import { defineConfig } from "vite-plugin-windicss";
import colors from "windicss/colors";
import typography from "windicss/plugin/typography";
import plugin from "windicss/plugin";

export default defineConfig({
  darkMode: "class",
  safelist: "order-last button-primary button-secondary button-red button-transparent",
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
    colors: {
      transparent: "transparent",
      current: "currentColor",
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
      sky: colors.sky,
      blue: colors.blue,
      lime: colors.lime,
      slate: colors.slate,
      red: colors.red,
      gray: colors.zinc,
      secondary: colors.slate,
      white: colors.zinc[50],
      black: colors.zinc[900],
    },
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
        red: {
          500: "#ff544b",
        },
      },
    },
  },
  shortcuts: {
    "background-body": "bg-gray-100 dark:bg-gray-900",
    "background-default": "bg-gray-50 dark:bg-gray-800",
    "color-primary": "text-primary-400 dark:text-primary-200",
    "border-top-primary": "border-solid border-t-4 border-t-primary-400",
    "button-primary": "bg-primary-400 enabled:hover:bg-primary-300",
    "button-secondary": "bg-secondary-500 enabled:hover:(bg-secondary-400 dark:bg-secondary-600)",
    "button-transparent": "bg-transparent enabled:hover:(bg-primary-400/15 text-primary-400 dark:text-primary-100)",
    "button-red": "bg-red-500 dark:bg-red-600 enabled:hover:(bg-red-400 dark:bg-red-500)",
  },
});
