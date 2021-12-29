function refershFlowChart() {
    document.getElementById('flowchart').src += '?refresh=1';
}

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

function zoomStart() {
    /*document.getElementById('flowchart').*/
    contentWindow.hideLines();
    if (sections === undefined)
        sections = contentWindow.$('.section');
}

function zoomVal() {
    var zoomFactor = document.getElementById('actionForm:zoomFactor_input');
    if (zoomFactor == null) return '100%';
    return zoomFactor.value;
}

function zoom() {
    var zoomFactor = document.getElementById('actionForm:zoomFactor_input');
    if (zoomFactor == null) return;

    var flowchart = $(contentWindow.document.getElementsByTagName('html'));
    flowchart.css('zoom', zoomVal());
}

function zoomEnd(submit) {
    zoom();

    var active = contentWindow.$('.active').first();
    var scrollX, scrollY;
    if (active.length === 0) {
        scrollX = contentWindow.scrollX;
        scrollY = contentWindow.scrollY;
    }

    contentWindow.scrollTo(0, 0);
    contentWindow.showLines();

    if (active.length === 0) {
        contentWindow.scrollTo(scrollX, scrollY);
    } else {
        /*scroll to active object*/
        var zoomed = zoomVal().replace('%', '') / 100.0;
        var pos = active.offset();
        pos.top *= zoomed;
        pos.left *= zoomed;
        pos.top -= ((contentWindow.innerHeight - (active.outerHeight() * zoomed)) / 2);
        pos.left -= ((contentWindow.innerWidth - (active.outerWidth() * zoomed)) / 2);
        contentWindow.scrollTo(pos);
    }

    if (submit === undefined) return;

    var zoomFactor = document.getElementById('actionForm:zoomFactor_input');
    if (zoomFactor == null) return;

    var zooming = zoomFactor.value;
    if (zooming === zoomValue) return;

    zoomValue = zooming;
    submitZoom([
        {name: 'zoom', value: zooming}
    ]);
}

$(function () {
    leftPanel = $('.left-panel');
    leftGutter = $('.left-panel + .ui-splitter-gutter');
    leftToggle = $('.left-panel-toggle').click(toggleLeft).children('.ui-button-icon-left');
    rightPanel = $('.right-panel');
    rightGutter = $('.right-panel + .ui-splitter-gutter');
    rightToggle = $('.right-panel-toggle').click(toggleRight).children('.ui-button-icon-left');
    contentWindow = document.getElementById('flowchart').contentWindow;
});

var leftPanel, leftGutter, leftToggle;
var rightPanel, rightGutter, rightToggle;
var zoomValue;
var sections, contentWindow;
