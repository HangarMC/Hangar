Feature("Authors Page");

Scenario("Test Author List", async ({ I, util, IndexPage }) => {
    util.openHangarPage("/authors");

    I.dontSee("No authors found");

    I.fillField("input[type='text']", "Some Value That doesnt Exist");
    I.waitForText("No authors found");
});
