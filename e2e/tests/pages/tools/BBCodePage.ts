import { expect } from "chai";

Feature("BBCode Page");

Scenario("Test BBCode", async ({ I, util, IndexPage }) => {
    util.openHangarPage("/tools/bbcode");
    I.fillField("textarea", "[b]Bold[/b]");
    I.click("Convert");
    const result = await I.grabValueFrom(locate("textarea").at(2));
    expect(result).to.equal("**Bold**");
    I.seeElement("strong");
});
