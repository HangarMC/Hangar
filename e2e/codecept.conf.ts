const browserstack = require("browserstack-local");
const util = require("util");
require("dotenv").config();

const BROWSERSTACK_USERNAME = process.env.BROWSERSTACK_USERNAME;
const BROWSERSTACK_ACCESS_KEY = process.env.BROWSERSTACK_ACCESS_KEY;

const DEBUG = process.env.BROWSERSTACK_DEBUG === "true";
const DEV = process.env.BROWSERSTACK_DEV === "true";
const LOCAL = process.env.BROWSERSTACK_LOCAL === "true";
const BUILD_NAME = process.env.BROWSERSTACK_BUILD_NAME || "browserstack-build-1" + (LOCAL ? "-local" : "");

console.table({
    DEBUG,
    DEV,
    LOCAL,
    BUILD_NAME,
});

const windowSize = "1920x1080";
const defaultCapabilities = {
    browserVersion: "latest",
    projectName: "Hangar E2E",
    buildName: BUILD_NAME,
    "browserstack.debug": DEBUG ? "true" : undefined,
    "browserstack.networkLogs": DEBUG ? "true" : undefined,
    "browserstack.consoleLogs": DEBUG ? "info" : undefined,
    "browserstack.local": LOCAL ? "true" : "false",
};

exports.config = {
    name: "Hangar E2E",
    tests: "./tests/*.ts",
    output: "./output",
    timeout: 120,

    helpers: {
        WebDriver: {
            url: "https://hangar.benndorf.dev",
            user: DEV ? undefined : BROWSERSTACK_USERNAME,
            key: DEV ? undefined : BROWSERSTACK_ACCESS_KEY,
            browser: "Edge",
            windowSize,
            capabilities: {
                ...defaultCapabilities,
                os: "Windows",
                osVersion: "11",
            },
        },
    },

    multiple: {
        bstack: {
            browsers: [
                {
                    browser: "Safari",
                    windowSize,
                    capabilities: {
                        ...defaultCapabilities,
                        os: "OS X",
                        osVersion: "Sonoma",
                    },
                },
                {
                    browser: "Edge",
                    windowSize,
                    capabilities: {
                        ...defaultCapabilities,
                        os: "Windows",
                        osVersion: "11",
                    },
                },
            ],
        },
    },

    bootstrap:
        LOCAL && !DEV
            ? async function () {
                  console.log("Connecting Local");
                  exports.bs_local = new browserstack.Local();
                  exports.bs_local.start_async = util.promisify(exports.bs_local.start);
                  try {
                      await exports.bs_local.start_async({ key: BROWSERSTACK_ACCESS_KEY });
                      console.log("Connected. Now testing...");
                  } catch (er) {
                      console.log("Local start failed with error", er);
                      return;
                  }
              }
            : undefined,

    teardown:
        LOCAL && !DEV
            ? async function () {
                  exports.bs_local.stop(() => {
                      console.log("Disconnected Local");
                  });
              }
            : undefined,

    include: {
        util: "./utils/util.ts",
        IndexPage: "./utils/IndexPage.ts",
    },

    plugins: {
        pauseOnFail: {},
        retryFailedStep: {
            enabled: true,
        },
        tryTo: {
            enabled: true,
        },
        screenshotOnFail: {
            enabled: true,
        },
    },
} as CodeceptJS.MainConfig;
