import { expect } from "chai";

Feature("Index Page");

Scenario("Test Project List", async ({ I, util, IndexPage }) => {
    util.openHangarPage("/");

    // check that projects loaded
    let placeholder = await I.grabAttributeFrom(IndexPage.searchField, "placeholder");
    expect(placeholder).to.not.eq("Search in 0 projects...");
    I.dontSee("There are no projects.");

    // check that query works
    I.fillField(IndexPage.searchField, "Some Value That doesnt Exist");
    I.waitInUrl("query");
    I.see("There are no projects.");
    I.fillField(IndexPage.searchField, "Test");

    // check that buttons work
    I.click(IndexPage.mostDownloadsButton);
    I.waitInUrl("sort=-downloads");
    I.waitToHide("//*[contains(text(),'There are no projects.')]");
});

Scenario("Test Project List Query", async ({ I, util, IndexPage }) => {
    util.openHangarPage("/?page=1&query=Test&sort=-downloads&category=admin_tools");

    // check that query works
    // I.seeInField(IndexPage.searchField, "Test"); // TODO fix me
    I.seeElement(locate("button").withAttr({ "aria-label": "Page 2", disabled: "" }));
    I.seeElement(locate("button.bg-gradient-to-r").withText("Most Downloads"));
    // TODO test minecraft version (maybe selecting a sub version should open the tree too?)
    I.seeElement(locate("input").withAttr({ type: "checkbox", value: "admin_tools", checked: "" }));
});
