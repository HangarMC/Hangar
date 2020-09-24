import $ from "jquery";
import {initChannelManager} from "@/js/channelManage";

//=====> EXTERNAL CONSTANTS

var DEFAULT_COLOR = null;

//=====> HELPER FUNCTIONS

function initChannelNew(color) {
  initChannelManager(
    "#channel-new",
    "",
    color,
    "New channel",
    null,
    null,
    "Create channel",
    false
  );
}

function getForm() {
  return $("#form-publish");
}

function getSelect() {
  return $("#select-channel");
}

function setColorInput(val) {
  getForm()
    .find(".channel-color-input")
    .val(val);
}

//=====> DOCUMENT READY

$(function() {
  setTimeout(function() {
    initChannelNew(DEFAULT_COLOR);
  }, 200);

  getSelect().change(function() {
    setColorInput(
      $(this)
        .find(":selected")
        .data("color")
    );
  });

  // eslint-disable-next-line no-unused-vars
  var onCustomSubmit = function(
    toggle,
    channelName,
    channelHex,
    title, // eslint-disable-line no-unused-vars
    submit, // eslint-disable-line no-unused-vars
    nonReviewed // eslint-disable-line no-unused-vars
  ) {
    // Add new name to select
    var select = getSelect();
    var exists =
      select.find("option").find(function() {
        return (
          $(this)
            .val()
            .toLowerCase() === channelName.toLowerCase()
        );
      }).length !== 0;

    if (!exists) {
      setColorInput(channelHex);
      select.find(":selected").removeAttr("selected");
      select.append(
        '<option data-color="' +
          channelHex +
          '" ' +
          'value="' +
          channelName +
          '" ' +
          "selected>" +
          channelName +
          "</option>"
      );
    }

    $("#channel-settings").modal("hide");
    initChannelNew(DEFAULT_COLOR);
  };
});
