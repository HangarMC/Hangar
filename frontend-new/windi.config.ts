import { defineConfig } from "vite-plugin-windicss";
import colors from "windicss/colors";
import typography from "windicss/plugin/typography";

export default defineConfig({
  darkMode: "class",
  attributify: true,
  plugins: [typography()],
  theme: {
    extend: {
      typography: {
        DEFAULT: {
          css: {
            maxWidth: "65ch",
            color: "inherit",
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
            h1: { color: "inherit" },
            h2: { color: "inherit" },
            h3: { color: "inherit" },
            h4: { color: "inherit" },
            code: { color: "inherit" },
          },
        },
      },
      colors: {
        backgroundDark90: '#111111',
        backgroundDark80: '#181a1b',
        backgroundLight10: '#f8faff',
        backgroundLight0: '#ffffff',
        primary100: '#004ee9',
        primary70: '#aec9ff',
        primary50: '#ecf2fb'
      },
    },
  },
  shortcuts: {
      'backgroundHeader': 'bg-backgroundLight0 dark:bg-backgroundDark90',
      'backgroundBody': 'bg-backgroundLight10 dark:bg-backgroundDark80',
      'colorPrimary': 'text-primary100 dark:text-primary70',
  },
});
