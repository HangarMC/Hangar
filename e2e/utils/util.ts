const { I } = inject();

module.exports = new (class {
    url = process.env.BROWSERSTACK_LOCAL === "true" ? "http://localhost:3333" : "https://hangar.papermc.dev";

    public openHangarPage(path: string) {
        I.amOnPage(this.url + path);
        I.waitForFunction(() => window["hangarLoaded"], 10);
    }

    public async browserStackStatus(passed: boolean, reason: string) {
        if (process.env.BROWSERSTACK_DEV === "true") return;
        await I.executeScript(
            `browserstack_executor: {"action": "setSessionStatus", "arguments": {"status":"${passed ? "passed" : "failed"}","reason": "${reason}"}}`,
        );
    }
})();
