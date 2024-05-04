Feature("New Project Page");

Scenario("Test New Project", async ({ I, util, IndexPage }) => {
    await util.login();
    util.openHangarPage("/new");
    I.click("Agree");

    const name = "E2E-" + util.randomNumber(10000);
    I.fillField("input[name='name']", name);
    I.fillField("input[name='description']", "E2E Test Project");
    I.selectOption("select[name='category']", "Games");
    I.click("Continue");

    I.waitForText("Links");
    I.click("Add section");
    I.click("Add");
    I.click("Continue");
    I.waitForText("Name is required");
    I.fillField(locate("input").at(1), "Link");
    I.fillField(locate("input").at(2), "https://papermc.io");
    I.click("Continue");

    I.waitInUrl("/e2e_user/" + name);
    I.waitForText("Welcome to your new project");
    await I.waitForText("Games");

    await util.deleteProject(name);
});
