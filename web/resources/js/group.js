function blockScreen(text) {
    if (tflow.blockScreenText === undefined) {
        tflow.blockScreen = $('.screen-blocker');
        tflow.blockScreenText = tflow.blockScreen.find('.screen-block-text');
    }
    if (text === undefined) text = "PLEASE WAIT";
    if (tflow.blockScreenText[0] !== undefined) tflow.blockScreenText[0].innerText = text;
    tflow.blockScreen.show();
}

function unblockScreen() {
    tflow.blockScreen.hide();
}


var tflow = {
    tflowIsMustHave: true
};