function refreshStepList() {
    showStepList(leftPanel.css('display') === 'block');
}

function refreshFlowChart() {
    document.getElementById('flowchart').src += '?refresh=1';
}

function refreshProperties() {
    contentWindow.selectObject(contentWindow.$('.active'));
}

function toggleLeft() {
    showStepList(leftPanel.css('display') === 'none');
}

function toggleRight() {
    showPropertyList(rightPanel.css('display') === 'none');
}

function showStepList(show) {
    var display = show ? 'block' : 'none';
    leftGutter.css('display', display);
    leftPanel.css('display', display);
    /*leftToggle.removeClass('pi-angle-right').addClass('pi-angle-left');*/
    setToolPanel([
        {name: 'stepList', value: '' + show}
    ]);
}

function showPropertyList(show) {
    var display = show ? 'block' : 'none';
    rightGutter.css('display', display);
    rightPanel.css('display', display);
    /*rightToggle.removeClass('pi-angle-left').addClass('pi-angle-right');*/
    setToolPanel([
        {name: 'propertyList', value: '' + show}
    ]);
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

    scrollToObj(contentWindow.$('.active'));
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
        scrollToObj(active);
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

function scrollToObj(active) {
    console.log('scrollToObj(active:' + active.attr('class') + ')');
    if (active.hasClass('step')) {
        /*scroll to first data-source*/
        var ds = contentWindow.$('.data-source');
        if (ds.length === 0) return;
        active = ds.first();
    }

    var zoomed = zoomVal().replace('%', '') / 100.0;
    var innerHeight = contentWindow.innerHeight;
    var innerWidth = contentWindow.innerWidth;
    var outerHeight = active.outerHeight();
    var outerWidth = active.outerWidth();

    contentWindow.scrollTo(0, 0);
    var pos = active.offset();
    pos.top *= zoomed;
    pos.left *= zoomed;
    pos.top -= (innerHeight - (outerHeight * zoomed)) / 2;
    pos.left -= (innerWidth - (outerWidth * zoomed)) / 2;
    contentWindow.scrollTo(pos);
}

function updateEm(selectable) {
    contentWindow['update' + selectable]();
}

function propertyCreated() {
    /* init all behaviors of all input boxes such as auto-select-text */
    $('.ui-g-12').hide();
}

$(function () {
    leftPanel = $('.left-panel');
    leftGutter = $('.left-panel + .ui-splitter-gutter');
    leftToggle = $('.left-panel-toggle').click(toggleLeft).children('.ui-button-icon-left');
    rightPanel = $('.right-panel');
    rightGutter = $('.main-panel + .ui-splitter-gutter');
    rightToggle = $('.right-panel-toggle').click(toggleRight).children('.ui-button-icon-left');
    contentWindow = document.getElementById('flowchart').contentWindow;
});

var leftPanel, leftGutter, leftToggle;
var rightPanel, rightGutter, rightToggle;
var zoomValue;
var sections, contentWindow;
