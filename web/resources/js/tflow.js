'use strict';

function blockScreen(text) {
    console.debug("blockScreen('", text, "');");
    if (tflow.blockScreen == null) {
        tflow.blockScreen = $('.screen-blocker');
        tflow.blockScreenText = tflow.blockScreen.find('.screen-block-text');
    }
    if (text === undefined) text = "PLEASE WAIT";
    if (tflow.blockScreenText[0] !== undefined) tflow.blockScreenText[0].innerText = text;
    tflow.blockScreen.show();
}

function unblockScreen() {
    console.debug("unblockScreen();");
    if (tflow.blockScreen != null) tflow.blockScreen.hide();
}

var tflowBlockScreen = {
    blockScreen: null,
    blockScreenText: null
};

if (window['tflow'] === undefined)
    window['tflow'] = tflowBlockScreen;
else
    window['tflow'] = Object.assign(tflow, tflowBlockScreen);
