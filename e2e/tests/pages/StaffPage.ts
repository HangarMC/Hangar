Feature("Staff Page");

Scenario("Test Staff List", async ({ I, util, IndexPage }) => {
    util.openHangarPage("/staff");

    I.dontSee("No staff found");

    I.fillField("input[type='text']", "Some Value That doesnt Exist");
    I.waitForText("No staff found");
});
