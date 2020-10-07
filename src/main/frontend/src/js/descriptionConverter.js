import $ from 'jquery';

function showMDPreview() {
    let preview = document.getElementById('preview');
    let text = document.getElementById('output-window').value;
    $.ajax({
        type: 'post',
        url: '/pages/preview',
        data: JSON.stringify({ raw: text }),
        contentType: 'application/json',
        dataType: 'html',
        success: function (result) {
            preview.innerHTML = result;
        },
    });
}

// https://github.com/JonDum/BBCode-To-Markdown-Converter/blob/gh-pages/index.js
function convert() {
    let input = document.getElementById('input-window');
    let output = document.getElementById('output-window');

    let inputValue = input.value;
    //TODO request to controller
    output.value = inputValue;
}

$(function () {
    $.ajaxSetup(window.ajaxSettings);
    document.getElementById('preview-tab').onclick = function () {
        showMDPreview();
    };

    let input = document.getElementById('input-window');
    input.onchange = function () {
        convert();
    };
    input.onkeyup = function () {
        convert();
    };
});
