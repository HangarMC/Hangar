import { expect } from "chai";

Feature("Index Page");

Scenario("Test Project List", async ({ I, util, IndexPage }) => {
    util.openHangarPage("/");

    let placeholder = await I.grabAttributeFrom(IndexPage.searchField, "placeholder");
    expect(placeholder).to.not.eq("Search in 0 projects...");
    I.dontSee("There are no projects.");

    I.fillField(IndexPage.searchField, "Some Value That doesnt Exist");
    I.waitInUrl("query");
    I.see("There are no projects.");

    await util.browserStackStatus(true, "Test passed");
});
