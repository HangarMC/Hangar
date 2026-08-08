process.loadEnvFile(".env");

export const config = {
    name: "Hangar E2E",
    tests: "./tests/**/*.ts",
    require: ["tsx/esm"],
    output: "./output",
    timeout: 120,

    helpers: {
        WebDriver: {
            url: "https://hangar.benndorf.dev",
            browser: "Edge",
            windowSize: "1920x1080",
            waitForTimeout: 15000,
        },
    },

    include: {
        util: "./utils/util.ts",
        IndexPage: "./utils/IndexPage.ts",
        I: "./utils/custom_steps.ts",
    },

    plugins: {
        pause: {},
        retryFailedStep: {
            enabled: true,
        },
        screenshot: {
            enabled: true,
        },
    },
} as CodeceptJS.MainConfig;
