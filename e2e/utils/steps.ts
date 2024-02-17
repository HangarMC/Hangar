import { actor } from "codeceptjs";

module.exports = function () {
    const url = process.env.BROWSERSTACK_LOCAL === "true" ? "http://localhost:3333" : "https://hangar.papermc.dev";
    return actor({
        openHangarPage: function (path: string) {
            this.amOnPage(url + path);
        },
    });
};
