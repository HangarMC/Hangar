//import { expect } from "chai";

Feature("Main Page");

Scenario("Test", async ({ I }) => {
    I.openHangarPage("/");
    const placeholder = await I.grabAttributeFrom("input[name='query']", "placeholder");

    //expect(placeholder).to.not.contain("1");
    // await I.executeScript('browserstack_executor: {"action": "setSessionStatus", "arguments": {"status":"passed","reason": "Product matched!"}}');
});
