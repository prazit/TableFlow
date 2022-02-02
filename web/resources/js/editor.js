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
        /*TODO: need to calculate new scrollXY by zoomFactor and then remove scrollToActive below,
           keep scrollXY before zoom(previous-zoom) and the calculate after zoom(new-zoom) */
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


function lineStart() {
    /*start of line creation*/
    lines.lineStart = {
        left: window.scrollX,
        top: window.scrollY
    };
    window.scrollTo(0, 0);
}

function lineEnd() {
    /*end of line creation*/
    lines.lineEnd = {
        left: lines.lineStart,
        top: window.scrollY
    };
    window.scrollTo(lines.lineEnd);
}

function scrollToObj(active) {
    var thisScroll = Date.now(),
        diff = thisScroll - tflow.lastScroll;
    if (tflow.lastScroll != null && diff < tflow.allowScrollDuration) {
        //console.log('detected multiple scroll at once (duration:' + diff + ', allowDuration:' + allowScrollDuration + ') cancel scrollToObj(object:' + active.attr('class') + ')');
        /*return;*/
    }

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

    //console.log('scrollToObj(object:' + active.attr('class') + ', top:' + pos.top + ', left:' + pos.left + ')');
    tflow.lastScroll = Date.now();
}

function updateEm(selectableId) {
    contentWindow['update' + selectableId]();
    /*need to handle event of this selectable at the complete phase of update process above*/
}

function propertyCreated($scrollPanel) {
    /* init all behaviors of input boxes*/

    $('.ui-g-12').hide();

    /*TODO: scroll panel need to resize when the window resized or the splitter resized*/

    /*TODO: all input boxes: need the same width*/

    /*TODO: input-text: select all text when got the focus*/

    /*TODO: setFocus to the default field or first field*/
    if (tflow.setFocus != null) clearTimeout(tflow.setFocus);
    tflow.setFocus = setTimeout(setFocus, 1000);
}

function setFocus() {
    tflow.setFocus = null;

    var inputs = $('.properties').find('input');
    if (inputs.length > 0) {
        inputs[0].focus(function (ev) {
            console.log('"' + $(ev.currentTarget).attr('class') + '" got the focus.');
        });

        /*TODO: after set focus issue: dead loop will occurred after selectObj(step)*/

        /*TODO: after set focus issue: while refreshing the flowchart, lets try to click in the flowchart area that will stop the refresh process*/

        /*TODO: after set focus issue: selectObj( data-table ) then selectObj( column ) in the same table is not function correctly*/
    }
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

var tflow = {
        allowScrollDuration: 1000,
        lastScroll: Date.now()
    },
    leftPanel, leftGutter, leftToggle,
    rightPanel, rightGutter, rightToggle,
    zoomValue,
    sections, contentWindow;
