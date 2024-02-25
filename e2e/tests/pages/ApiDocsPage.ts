Feature("Api Docs Page");

Scenario("Test Api Docs", async ({ I, util, IndexPage }) => {
    util.openHangarPage("/api-docs");
    I.seeElement(locate({ shadow: ["rapi-doc"] }));
    // TODO no clue how the shadow shit is supposed to work
    // I.seeElement(
    //     locate({ shadow: ["rapi-doc"] })
    //         .withDescendant("h1")
    //         .withText("Hangar API"),
    // );
    // I.waitForText("Hangar API");
    // I.click(locate("#link-get-\\/api\\/v1\\/keys"));
    // I.waitForText("Fetches a list of API Keys");
});
