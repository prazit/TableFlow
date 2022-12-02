'use strict';

function blockScreen(text) {
    if (text === undefined) text = "PLEASE WAIT";
    tflow.blockScreenText[0].innerText = text;

    if (!tflow.blockScreen.hasClass('hidden')) return;

    console.debug("blockScreen('", text, "');");
    /*TODO: problem when I call blockScreen during (pf-ajax)load process, the animation is not play until the load process is end*/
    Appanel.chains(tflow.blockScreen, "-hidden,ani-block-screen:0.5");
}

function unblockScreen() {
    if (tflow.blockScreen.hasClass('hidden')) return;
    console.debug("unblockScreen();");
    Appanel.chains(tflow.blockScreen, "ani-unblock-screen:0.5,hidden");
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
