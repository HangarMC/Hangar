import { expect } from "chai";

Feature("Index Page");

Scenario("Test Project List", async ({ I, util, IndexPage }) => {
    util.openHangarPage("/");

    let placeholder = await I.grabAttributeFrom(IndexPage.searchField, "placeholder");
    expect(placeholder).to.not.contain("0");

    I.fillField(IndexPage.searchField, "Some Value That doesnt Exist");
    I.waitInUrl("query");
    placeholder = await I.grabAttributeFrom(IndexPage.searchField, "placeholder");
    expect(placeholder).to.contain("0");

    await util.browserStackStatus(true, "Test passed");
});
