const { I } = inject();
const { TOTP } = require("totp-generator");
const { jwtDecode } = require("jwt-decode");

module.exports = new (class {
    url = process.env.BROWSERSTACK_LOCAL === "true" ? "http://localhost:3333" : "https://hangar.papermc.dev";
    jwt?: string = null;

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

    public async login(admin?: boolean) {
        this.openHangarPage("/auth/login?returnUrl=/?done");
        I.fillField("input[name='username']", admin ? "e2e_admin" : "e2e_user");
        I.fillField("input[name='password']", process.env.E2E_PASSWORD);
        I.click(locate("button").withText("Login"));
        I.waitForText("Use totp", 10);
        const totp = TOTP.generate(process.env.E2E_TOTP_SECRET);
        I.fillField("input", totp.otp);
        I.click("Use totp");
        I.waitInUrl("/?done");
        I.waitForText("FIND YOUR", 5);
    }

    public async getJwt() {
        if (this.validateToken(this.jwt)) {
            return this.jwt;
        }

        const result = await fetch(this.url + "/api/v1/authenticate?apiKey=" + process.env.E2E_API_KEY, {
            method: "POST",
        });
        const json = await result.json();
        this.jwt = json.token;
        return this.jwt;
    }

    validateToken(token: unknown): token is string {
        if (!token || typeof token !== "string") {
            return false;
        }
        const decoded = jwtDecode(token);
        if (!decoded.exp) {
            return false;
        }
        return decoded.exp * 1000 > Date.now() - 10 * 1000; // check against 10 seconds earlier to mitigate tokens expiring mid-request
    }

    public async deleteProject(name: string) {
        const result = await fetch(this.url + "/api/internal/projects/project/" + name + "/manage/hardDelete", {
            method: "POST",
            headers: { Authorization: "Bearer " + (await this.getJwt()), "Content-Type": "application/json" },
            body: JSON.stringify({ content: "E2E" }),
        });
    }

    public async deleteOrg(name: string) {
        const result = await fetch(this.url + "/api/internal/organizations/org/" + name + "/delete", {
            method: "POST",
            headers: { Authorization: "Bearer " + (await this.getJwt()), "Content-Type": "application/json" },
            body: JSON.stringify({ content: "E2E" }),
        });
        console.log(await result.text());
    }
})();
