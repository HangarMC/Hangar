import $ from 'jquery';

//=====> CONSTANTS

var KEY_RETURN = 13;

//=====> EXTERNAL CONSTANTS

var PROJECT_NAME = window.PROJECT_NAME;

//=====> DOCUMENT READY

$(function() {
  var name = $('#name');
  name.on('input', function() {
    var val = $(this).val();
    $('#btn-rename').prop('disabled', val.length === 0 || val === PROJECT_NAME);
  });

  name.keydown(function(e) {
    if (e.which === KEY_RETURN) {
      e.preventDefault();
      $('#btn-rename').click();
    }
  });

  $('.dropdown-license')
    .find('a')
    .click(function() {
      var btn = $('.btn-license');
      var text = $(this).text();
      btn.find('.license').text(text);
      var name = $('input[name="license-name"]');
      if ($(this).hasClass('license-custom')) {
        name.val('');
        name.show().focus();
      } else {
        name.hide();
        name.val(text);
      }
    });

  // Basically, hides the form value if its empty. Makes the controller simpler
  $('#save').submit(function() {
    $(':input[form=save]')
      .filter(function() {
        return !this.value;
      })
      .attr('disabled', true);

    return true;
  });
});
