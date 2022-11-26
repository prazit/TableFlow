'use strict';

function blockScreen(text) {
    console.debug("blockScreen('", text, "');");
    if (text === undefined) text = "PLEASE WAIT";
    if (tflow.blockScreenText[0] !== undefined) tflow.blockScreenText[0].innerText = text;
    if (!tflow.blockScreen.hasClass('hidden')) return;
    Appanel.chains(tflow.blockScreen, "-hidden,ani-zoomInUp:0.5");
}

function unblockScreen() {
    if (tflow.blockScreen.hasClass('hidden')) return;
    console.debug("unblockScreen();");
    Appanel.chains(tflow.blockScreen, "ani-zoomOutDown:0.5,hidden");
}

var tflowBlockScreen = {
    blockScreen: null,
    blockScreenText: null
};

if (window['tflow'] === undefined)
    window['tflow'] = tflowBlockScreen;
else
    window['tflow'] = Object.assign(tflow, tflowBlockScreen);

$(function () {
    tflow.blockScreen = $('.screen-blocker');
    tflow.blockScreenText = tflow.blockScreen.find('.screen-block-text');
    unblockScreen();
});
