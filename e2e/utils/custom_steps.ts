import { actor } from "codeceptjs";

export default function () {
    return actor({
        waitTillUrlContains(urlPart: string, sec: number | null = null) {
            const client = this.browser;
            const aSec = sec || this.options.waitForTimeoutInSeconds;
            let currUrl = "";

            return client
                .waitUntil(
                    async function () {
                        const url = await this.getUrl();
                        return url.indexOf(urlPart) > -1;
                    },
                    { timeout: aSec * 1000 },
                )
                .catch((e) => {
                    e = wrapError(e);
                    if (e.message.indexOf("timeout")) {
                        throw new Error(`expected url to include ${urlPart}, but found ${currUrl}`);
                    }
                    throw e;
                });
        },
    });
}

function wrapError(e) {
    if (e && typeof e === "object" && !e.message) {
        const err = new Error(e.error || e.timeoutMsg || String(e));
        err.stack = e.stack;
        return err;
    }
    return e;
}
