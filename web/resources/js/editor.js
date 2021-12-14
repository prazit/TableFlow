var leftPanel, leftGutter, leftToggle;
var rightPanel, rightGutter, rightToggle;

function toggleLeft() {
    if (leftPanel.css('display') === 'none') {
        //to show
        leftGutter.show();
        leftPanel.show();
        leftToggle.removeClass('pi-angle-right').addClass('pi-angle-left');
    } else {
        //to hide
        leftGutter.hide();
        leftPanel.hide();
        leftToggle.removeClass('pi-angle-left').addClass('pi-angle-right');
    }
}

function toggleRight() {
    if (rightPanel.css('display') === 'none') {
        //to show
        rightGutter.show();
        rightPanel.show();
        rightToggle.removeClass('pi-angle-left').addClass('pi-angle-right');
    } else {
        //to hide
        rightGutter.hide();
        rightPanel.hide();
        rightToggle.removeClass('pi-angle-right').addClass('pi-angle-left');
    }
}

var flowchart, zoomFactor;

function zoom() {
    if (undefined === zoomFactor)
        zoomFactor = document.getElementById('actionForm:zoomFactor');
    if (undefined === flowchart)
        flowchart = $(document.getElementById('flowchart').contentWindow.document.getElementsByTagName('html'));
    console.log('zoom:' + zoomFactor.value);
    flowchart.css('zoom', zoomFactor.value);
}

$(function () {
    leftPanel = $('.left-panel');
    leftGutter = $('.left-panel + .ui-splitter-gutter');
    leftToggle = $('.left-panel-toggle').click(toggleLeft).children('.ui-button-icon-left');
    rightPanel = $('.right-panel');
    rightGutter = $('.right-panel + .ui-splitter-gutter');
    rightToggle = $('.right-panel-toggle').click(toggleRight).children('.ui-button-icon-left');
});