Feature("New Version Page");

Scenario("Test New Version", async ({ I, util }) => {
    await util.login();

    const projectName = "e2e_test-" + util.randomNumber();
    await util.createProject(projectName, process.env.E2E_USER_ID);
    util.openHangarPage("/e2e_user/" + projectName + "/versions/new");

    const name = "1." + util.randomNumber() + "." + util.randomNumber();

    // channel
    I.click(locate("button").withText("Create channel"));

    I.fillField("name", "e2e_test");
    I.seeElement(locate("button").withText("Create").withAttr({ disabled: "" }));
    I.seeElement(locate(".popper").withText("Channel Name is invalid"));

    I.fillField("name", "e2etest");
    I.dontSeeElement(locate(".popper").withText("Channel Name is invalid"));

    I.fillField("description", "Description");
    I.click(locate("div").withAttr({ "data-value": "#FFC800" }));

    I.dontSeeElement(locate("button").withText("Create").withAttr({ disabled: "" }));
    I.click(locate("dialog[data-title='Add a new channel']").find("button").withText("Create"));

    const disabled = await tryTo(() => I.seeElement(locate("button").withText("Create").withAttr({ disabled: "" })));
    if (disabled) {
        I.seeElement(locate(".popper").withText("channel with this color"));
        await I.executeScript(() => {
            const els = document.querySelectorAll<HTMLButtonElement>("button[data-close]");
            els[1].click();
        });
        I.selectOption("channel", "e2etest");
    }

    I.seeElement(locate("select").withText("e2etest"));

    // download
    I.click(locate("button").withText("Provide a URL"));
    I.fillField("url", "dum");
    I.seeElement(locate(".popper").withText("Invalid URL format"));
    I.seeElement(locate("button").withText("Next").withAttr({ disabled: "" }));

    I.fillField("url", "https://google.com");
    I.dontSeeElement(locate(".popper").withText("Invalid URL format"));

    I.click(locate("button").withText("Next"));
    I.seeElement(locate(".popper").withText("Select at least one platform!"));

    I.checkOption("Paper-0");
    I.dontSeeElement(locate("button").withText("Next").withAttr({ disabled: "" }));
    I.click(locate("button").withText("Next"));

    // version
    I.wait(1);
    I.click(locate("button").withText("Next"));
    I.seeElement(locate(".popper").withText("Version is required"));

    I.fillField("version", name);
    I.click(locate("button").withText("Next"));

    // dependencies
    I.checkOption("1.19.4");
    I.checkOption("1.18.2");

    I.click(locate("button").withText("Add"));
    I.click(locate("button").withText("URL"));

    I.fillField("externalurl-0", "https://google.com");
    I.fillField("name-0", "Google");
    I.click(locate("button").withText("Next"));

    // whats new
    I.type("# E2E Version " + name);
    I.click(locate("button").withText("Create").at(2)); // 1 is create project/org

    // check
    I.waitInUrl("/e2e_user/e2e_test/versions");
    I.seeElement(locate("h2").withText(name));
    I.seeElement(locate("span").withText("1.18.2, 1.19.4"));
    await I.seeElement(locate(".tags").withText("e2etest"));

    await util.deleteVersion(projectName, name);
    await util.deleteChannel(projectName, "e2etest");
    await util.deleteProject(projectName);
});
