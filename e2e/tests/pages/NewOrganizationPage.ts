Feature("New Org Page");

Scenario("Test New Org", async ({ I, util, IndexPage }) => {
    await util.login();
    util.openHangarPage("/neworganization");
    const name = "E2EOrg-" + util.randomNumber(10000);
    I.fillField("input[name='name']", name);
    I.wait(1);
    I.click("button[title='Create Org']");
    I.waitInUrl("/" + name);
    await I.see(name);

    await util.deleteOrg(name);
});
