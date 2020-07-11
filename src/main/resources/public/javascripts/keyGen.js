//=====> EXTERNAL CONSTANTS

var pluginId = null;
var keyGenText = null;
var keyRevokeText = null;

var KEY_TYPE_DEPLOYMENT = 0;


//=====> HELPER FUNCTIONS

function bindKeyGen(e) {
    e.click(function() {
        var spinner = $(this).find('.spinner').toggle();
        var $this = $(this);
        $.ajax({
            url: '/api/v1/projects/' + pluginId + '/keys/new',
            method: 'post',
            data: {'key-type': KEY_TYPE_DEPLOYMENT},
            dataType: 'json',
            success: function(key) {
                console.log(key);
                $('.input-key').val(key.value);
                $this.removeClass('btn-key-gen btn-info')
                    .addClass('btn-key-revoke btn-danger')
                    .data('key-id', key.id)
                    .off('click');
                $this.find('.text').text(keyRevokeText);

                bindKeyRevoke($this);
            },
            complete: function() {
                e.find('.spinner').toggle();
            }
        })
    });
}

function bindKeyRevoke(e) {
    e.click(function() {
        var spinner = $(this).find('.spinner').toggle();
        var $this = $(this);
        $.ajax({
            url: '/api/v1/projects/' + pluginId + '/keys/revoke',
            method: 'post',
            data: {'id': $(this).data('key-id')},
            success: function() {
                $('.input-key').val('');
                $this.removeClass('btn-key-revoke btn-danger')
                    .addClass('btn-key-gen btn-info')
                    .off('click');
                $this.find('.text').text(keyGenText);
                bindKeyGen($this);
            },
            complete: function() {
                e.find('.spinner').toggle();
            }
        })
    });
}


//=====> DOCUMENT READY

$(function() {
    bindKeyGen($('.btn-key-gen'));
    bindKeyRevoke($('.btn-key-revoke'));
});
