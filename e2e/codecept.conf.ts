const browserstack = require("browserstack-local");
const util = require("util");
require("dotenv").config();

const BROWSERSTACK_USERNAME = process.env.BROWSERSTACK_USERNAME;
const BROWSERSTACK_ACCESS_KEY = process.env.BROWSERSTACK_ACCESS_KEY;

const DEBUG = process.env.BROWSERSTACK_DEBUG === "true";
const DEV = process.env.BROWSERSTACK_DEV === "true";
const LOCAL = process.env.BROWSERSTACK_LOCAL === "true";
const BUILD_NAME = process.env.BROWSERSTACK_BUILD_NAME || "browserstack-build-1" + (LOCAL ? "-local" : "");

exports.config = {
    name: "Hangar E2E",
    tests: "./tests/*_test.ts",
    output: "./output",
    timeout: 120,

    helpers: {
        WebDriver: {
            url: "https://hangar.benndorf.dev",
            user: DEV ? undefined : BROWSERSTACK_USERNAME,
            key: DEV ? undefined : BROWSERSTACK_ACCESS_KEY,
            browser: "Edge",
            capabilities: {
                os: "Windows",
                osVersion: "11",
                browserVersion: "latest",
                projectName: "Hangar E2E",
                buildName: BUILD_NAME,
                "browserstack.debug": DEBUG ? "true" : undefined,
                "browserstack.networkLogs": DEBUG ? "true" : undefined,
            },
        },
    },

    multiple: {
        bstack: {
            browsers: [
                {
                    browser: "Safari",
                    capabilities: {
                        os: "OS X",
                        osVersion: "Sonoma",
                        browserVersion: "latest",
                        projectName: "Hangar E2E",
                        buildName: BUILD_NAME,
                        "browserstack.debug": DEBUG ? "true" : undefined,
                        "browserstack.networkLogs": DEBUG ? "true" : undefined,
                    },
                },
                {
                    browser: "Edge",
                    capabilities: {
                        os: "Windows",
                        osVersion: "11",
                        browserVersion: "latest",
                        projectName: "Hangar E2E",
                        buildName: BUILD_NAME,
                        "browserstack.debug": DEBUG ? "true" : undefined,
                        "browserstack.networkLogs": DEBUG ? "true" : undefined,
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
            ? function () {
                  exports.bs_local.stop(() => {
                      console.log("Disconnected Local");
                  });
              }
            : undefined,

    include: {
        I: "./utils/steps.ts",
        IndexPage: "./pages/IndexPage.ts",
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
};
