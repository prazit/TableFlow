function refreshStepList() {
    showStepList(leftPanel.css('display') === 'block');
}

function refreshFlowChart() {
    document.getElementById('flowchart').src += '?refresh=1';
    refreshToolbars();
}

function refreshProperties() {
    contentWindow.selectObject(contentWindow.$('.active'));
}

function refreshToolbars() {
    contentReady(function () {
        setToolPanel([
            {name: 'refresh', value: 'all'}
        ]);
    }, 'refershToolbars');
}

function toggleLeft() {
    showStepList(leftPanel.css('display') === 'none');
}

function toggleRight() {
    showPropertyList(rightPanel.css('display') === 'none');
}

function toggleActionButtons() {
    showActionButtons(contentWindow.$('.flow-chart').hasClass('hide-actions'));
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

function showActionButtons(show) {
    var display = 'hide-actions';

    if (show) {
        contentWindow.$('.flow-chart').removeClass('hide-actions');
    } else {
        contentWindow.$('.flow-chart').addClass('hide-actions');
    }
    contentWindow.showLines();

    setToolPanel([
        {name: 'actionButtons', value: '' + show}
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

function propertyCreated() {
    if (tflow.propertyCreatedEnding) return;
    tflow.propertyCreatedEnding = true;

    /* init all behaviors of input boxes*/
    var $scrollPanel = $('.properties');
    tflow.propertyPanel = $scrollPanel;

    if (!tflow.showDebugInfo) {
        $scrollPanel.find('.ui-g-12').hide();
    }

    /*scroll panel need to resize when the window resized or the splitter resized*/
    $scrollPanel.css('width', '100%');

    /*input-text: select all text when got the focus*/
    $scrollPanel.find('input[type=text]').each(function (i, e) {
        $(e).on('focus', function (ev) {
            ev.currentTarget.select();
        });
    });

    /*setFocus to the default field or first field*/
    if (tflow.setFocus != null) clearTimeout(tflow.setFocus);
    tflow.setFocus = setTimeout(setFocus, tflow.setFocusTimeout);

    /*selectables used by Tab index system below*/
    if (contentWindow.tflow.selectables == null) {
        contentWindow.tflow.selectables = contentWindow.$('.selectable');
    }

    /*select next object when press TAB on the last property*/
    $scrollPanel.find('.next-input').on('focus', function (ev) {
        /*need to change focus to another input before refreshProperties() to avoid auto focus on this field again by Browser*/
        tflow.propertyPanel.find('input')[0].focus(function () {/*nothing*/
        });

        var selectableId = contentWindow.getSelectableId(tflow.propertyPanel),
            nextObject = null,
            selectables = contentWindow.tflow.selectables,
            last = selectables.length - 2,
            id;
        for (var i = 0; i <= last; i++) {
            id = contentWindow.getSelectableId($(selectables[i]));
            if (selectableId === id) {
                nextObject = $(selectables[i + 1]);
                break;
            }
        }
        if (nextObject == null) {
            nextObject = $(contentWindow.tflow.selectables[0]);
        }
        contentWindow.selectObject(nextObject);
    });

    /*select previous object when press Shift+TAB on the first property*/
    $scrollPanel.find('.prev-input').on('focus', function (ev) {
        /*need to change focus to another input before refreshProperties() to avoid auto focus on this field again by Browser*/
        tflow.propertyPanel.find('input')[0].focus(function () {/*nothing*/
        });

        var selectableId = contentWindow.getSelectableId(tflow.propertyPanel),
            nextObject = null,
            selectables = contentWindow.tflow.selectables,
            last = selectables.length - 1,
            id;
        for (var i = 1; i <= last; i++) {
            id = contentWindow.getSelectableId($(selectables[i]));
            if (selectableId === id) {
                nextObject = $(selectables[i - 1]);
                break;
            }
        }
        if (nextObject == null) {
            nextObject = $(contentWindow.tflow.selectables[last]);
        }
        contentWindow.selectObject(nextObject);
    });

    tflow.propertyCreatedEnding = false;
}

function resetTabIndex() {
    /*reset tab-index system*/
    if(tflow.propertyPanel == null) return;

    tflow.propertyPanel.find('.next-object,.prev-object').off('focus');
    tflow.selectables = null;
}

function setFocus() {
    tflow.setFocus = null;

    var inputs = $('.properties').find('input');

    if (inputs.length > tflow.propertyFirstIndex) {
        inputs[tflow.propertyFirstIndex].focus(function (ev) {
            console.log('"' + $(ev.currentTarget).attr('class') + '" got the focus.');
        });
    }
}

function contentReady(func, label) {
    if (contentWindow !== undefined && contentWindow.tflow !== undefined && contentWindow.tflow.ready) {
        func();
        //console.log('contentReady(' + label + ') is immediately completed.');
        return;
    }

    var interval = setInterval(function () {
        if (contentWindow !== undefined && contentWindow.tflow !== undefined && contentWindow.tflow.ready) {
            clearInterval(interval);
            func();
            //console.log('contentReady(' + label + ') is completed.');
        } else {
            //console.log('contentReady(' + label + ') is skipped.');
        }
    }, 100);
}

$(function () {
    leftPanel = $('.left-panel');
    leftGutter = $('.left-panel + .ui-splitter-gutter');
    leftToggle = $('.left-panel-toggle').click(toggleLeft).children('.ui-button-icon-left');
    rightPanel = $('.right-panel');
    rightGutter = $('.main-panel + .ui-splitter-gutter');
    rightToggle = $('.right-panel-toggle').click(toggleRight).children('.ui-button-icon-left');
    contentWindow = document.getElementById('flowchart').contentWindow;
    refreshToolbars();
});

var tflow = {
        allowScrollDuration: 1000,
        lastScroll: Date.now(),

        setFocus: null,
        setFocusTimeout: 500,

        propertyPanel: null,
        propertyCreatedEnding: false,
        propertyFirstIndex: 2,

        showDebugInfo: false
    },
    leftPanel, leftGutter, leftToggle,
    rightPanel, rightGutter, rightToggle,
    zoomValue,
    sections, contentWindow;
