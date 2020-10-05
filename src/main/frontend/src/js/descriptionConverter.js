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

    //preprocessing for tf2toolbox BBCode
    if (inputValue.search(/TF2Toolbox/gim) !== -1) {
        inputValue = inputValue
            .replace(/(\(List generated at .+?\[\/URL]\))((?:.|\n)+)/gim, '$2\n\n\n$1') //Move TF2Toolbox link to bottom
            .replace('(List generated at', '(List generated from')
            .replace(/[^\S\n]+\(List/gim, '(List')
            .replace(/\[b]\[u](.+?)\[\/u]\[\/b]/gim, '[b]$1[/b]\n') //Fix double emphasized titles
            .replace(/(\n)\[\*]\[b](.+?)\[\/b]/gim, '$1[*] $2');
    }

    //general BBcode conversion
    inputValue = inputValue
        .replace(/\[b]((?:.|\n)+?)\[\/b]/gim, '**$1**') //bold; replace [b] $1 [/b] with ** $1 **
        .replace(/\[i]((?:.|\n)+?)\[\/i]/gim, '*$1*')  //italics; replace [i] $1 [/u] with * $1 *
        .replace(/\[u]((?:.|\n)+?)\[\/u]/gim, '$1')  //remove underline;
        .replace(/\[s]((?:.|\n)+?)\[\/s]/gim, '~~ $1~~') //strikethrough; replace [s] $1 [/s] with ~~ $1 ~~
        .replace(/\[center]((?:.|\n)+?)\[\/center]/gim, '$1') //remove center;
        .replace(/\[quote=.+?]((?:.|\n)+?)\[\/quote]/gim, '$1') //remove [quote=] tags
        .replace(/\[size=.+?]((?:.|\n)+?)\[\/size]/gim, '## $1') //Size [size=] tags
        .replace(/\[color=.+?]((?:.|\n)+?)\[\/color]/gim, '$1') //remove [color] tags
        .replace(/\[list=1]((?:.|\n)+?)\[\/list]/gim, function (match, p1) {
            return p1.replace(/\[\*]/gim, '1. ');
        })
        .replace(/(\n)\[\*]/gim, '$1* ') //lists; replcae lists with + unordered lists.
        .replace(/\[\/*list]/gim, '')
        .replace(/\[img]((?:.|\n)+?)\[\/img]/gim, '![$1]($1)')
        .replace(/\[url=(.+?)]((?:.|\n)+?)\[\/url]/gim, '[$2]($1)')
        .replace(/\[code](.*?)\[\/code]/gim, '`$1`')
        .replace(/\[code]((?:.|\n)+?)\[\/code]/gim, function (match, p1) {
            return p1.replace(/^/gim, '    ');
        })
        .replace(/\[php](.*?)\[\/php]/gim, '`$1`')
        .replace(/\[php]((?:.|\n)+?)\[\/php]/gim, function (match, p1) {
            return p1.replace(/^/gim, '    ');
        })
        .replace(/\[pawn](.*?)\[\/pawn]/gim, '`$1`')
        .replace(/\[pawn]((?:.|\n)+?)\[\/pawn]/gim, function (match, p1) {
            return p1.replace(/^/gim, '    ');
        });

    //post processing for tf2toolbox BBCode
    if (inputValue.search(/TF2Toolbox/gim) !== -1) {
        inputValue = inputValue.replace(/\*\*.+?\*\*[\s]+?None[\s]{2}/gim, ''); //remove empty sections
    }

    output.value = inputValue;
}

$(function () {
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
