import { locate } from "codeceptjs";

Feature("Linkout Page");

Scenario("Test Linkout", async ({ I, util, IndexPage }) => {
    util.openHangarPage("/linkout?remoteUrl=https://google.com");
    I.seeElement(locate("a").withAttr({ href: "https://google.com" }));
    I.click(locate("a").withAttr({ href: "https://google.com" }));
    I.waitTillUrlContains("google.com");
});
