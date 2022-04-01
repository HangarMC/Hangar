import { defineConfig } from "vite-plugin-windicss";
import colors from "windicss/colors";
import typography from "windicss/plugin/typography";

export default defineConfig({
  darkMode: "class",
  safelist: "order-last",
  attributify: true,
  plugins: [typography()],
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
        "background-dark-90": "#111111",
        "background-dark-80": "#181a1b",
        "background-light-10": "#f8faff",
        "background-light-0": "#ffffff",
        "primary-100": "#004ee9",
        "primary-70": "#aec9ff",
        "primary-50": "#ecf2fb",
      },
    },
  },
  shortcuts: {
    "background-header": "bg-background-light-0 dark:bg-background-dark-90",
    "background-body": "bg-background-light-10 dark:bg-background-dark-80",
    "color-primary": "text-primary-100 dark:text-primary-70",
    "border-top-primary": "border-solid border-t-4 border-t-primary-100",
  },
});
