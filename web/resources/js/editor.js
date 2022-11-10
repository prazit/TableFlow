function warning(msg) {
    notification([
        {name: 'message', value: msg},
        {name: 'type', value: 'notiWarn'}
    ]);
}

function blockScreen(text) {
    if (tflow.blockScreenText === undefined) tflow.blockScreenText = $('.screen-block-text');
    if (text === undefined) text = "PLEASE WAIT";
    if (tflow.blockScreenText[0] !== undefined) tflow.blockScreenText[0].innerText = text;
    $('.screen-blocker').show();
}

function unblockScreen() {
    $('.screen-blocker').hide();
}

function updateProperty(className) {
    var $properties = $('.properties');
    var $property = $properties.find('.' + className);
    var id = $property.attr('id');
    tflow.postRefreshElement = function () {
        hideDebugInfo($properties);
    };
    refreshElement([
        {name: 'componentId', value: id}
    ]);
}

function focusProperty(millis, className) {
    if (tflow.setFocus == null) {
        tflow.setFocus = setTimeout(focusProperty, (millis === undefined ? tflow.setFocusTimeout : millis));
        return;
    }

    clearTimeout(tflow.setFocus);
    tflow.setFocus = null;

    var $properties = $('.properties'),
        $property, inputs,
        input = undefined;
    if (className !== undefined) {
        $property = $properties.find('.' + className);
        $properties.find('input').each(function (i, e) {
            $(e).removeClass('focus');
        });
        inputs = $property.find('input').addClass('focus');
    } else {
        $property = $properties;
        inputs = $property.find('input.focus');
    }

    if (inputs.length === 0) {
        inputs = $property.find('input');
        if (inputs.length > tflow.propertyFirstIndex) {
            var index = tflow.propertyFirstIndex;
            while (input === undefined && index < inputs.length) {
                if (!inputs[index].disabled) {
                    input = inputs[index];
                    continue;
                }
                index++;
            }
        }
    } else {
        input = inputs[0];
    }

    /* ignore next-input */
    if (input !== undefined && !$(input).hasClass('next-input')) {
        input.focus(function (ev) {
            console.log('"' + $(ev.currentTarget).attr('class') + '" got the focus.');
        });
    }
}

function setFlowchart(page) {
    tflow.page = page;
}

function refreshStepList() {
    showStepList(leftPanel.css('display') === 'block');
    console.log('refreshStepList completed.');
}

function refreshFlowChart() {
    var src = document.getElementById('flowchart').src;
    src = src.substr(0, src.lastIndexOf("/") + 1) + tflow.page + '?refresh=1';
    document.getElementById('flowchart').src = src;
    refreshToolbars();
}

function refreshToolbars() {
    contentReady(function () {
        setToolPanel([
            {name: 'refresh', value: 'all'}
        ]);
    }, 'refershToolbars');
}

function refreshDatabaseList() {
    contentWindow.refreshDatabaseList();
}

function refreshLocalList() {
    contentWindow.refreshLocalList();
}

function refreshSFTPList() {
    contentWindow.refreshSFTPList();
}

function toggleLeft() {
    showStepList(leftPanel.css('display') === 'none');
}

function toggleRight() {
    showPropertyList(rightPanel.css('display') === 'none');
}

function toggleActionButtons() {
    contentReady(function () {
        showActionButtons(contentWindow.$('.flow-chart').hasClass('hide-actions'));
    }, 'toggleActionButtons');
}

function toggleColumnNumbers() {
    contentReady(function () {
        showColumnNumbers(contentWindow.$('.flow-chart').hasClass('hide-numbers'));
    }, 'toggleColumnNumbers');
}

function showStepList(show, fromServer) {
    var display = show ? 'block' : 'none';
    leftGutter.css('display', display);
    leftPanel.css('display', display);
    /*leftToggle.removeClass('pi-angle-right').addClass('pi-angle-left');*/
    setToolPanel([
        {name: 'stepList', value: '' + show},
        {name: 'fromMenu', value: '' + (fromServer === undefined)}
    ]);
}

function showPropertyList(show, fromServer) {
    var display = show ? 'block' : 'none';
    rightGutter.css('display', display);
    rightPanel.css('display', display);
    /*rightToggle.removeClass('pi-angle-left').addClass('pi-angle-right');*/
    setToolPanel([
        {name: 'propertyList', value: '' + show},
        {name: 'fromMenu', value: '' + (fromServer === undefined)}
    ]);
}

function showActionButtons(show, fromServer) {
    contentReady(function () {
        var display = 'hide-actions';

        contentWindow.lineStart();
        if (show) {
            contentWindow.$('.flow-chart').removeClass(display);
        } else {
            contentWindow.$('.flow-chart').addClass(display);
        }
        contentWindow.lineEnd();

        setToolPanel([
            {name: 'actionButtons', value: '' + show},
            {name: 'fromMenu', value: '' + (fromServer === undefined)}
        ]);
    }, 'showActionButtons');
}

function showColumnNumbers(show, fromServer) {
    contentReady(function () {
        var display = 'hide-numbers';

        contentWindow.lineStart();
        if (show) {
            contentWindow.$('.flow-chart').removeClass(display);
        } else {
            contentWindow.$('.flow-chart').addClass(display);
        }
        contentWindow.lineEnd();

        setToolPanel([
            {name: 'columnNumbers', value: '' + show},
            {name: 'fromMenu', value: '' + (fromServer === undefined)}
        ]);
    }, 'showColumnNumbers');
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
    console.log("zoomEnd(submit:" + (submit !== undefined ? submit : false) + ")");
    zoom();

    var active = contentWindow.$('.active').first();
    /*var scrollX, scrollY;
    if (active.length === 0) {
        scrollX = contentWindow.scrollX;
        scrollY = contentWindow.scrollY;
    }*/

    contentWindow.scrollTo(0, 0);
    contentWindow.showLines();

    /*if (active.length === 0) {
        contentWindow.scrollTo(scrollX, scrollY);
    } else {*/
    /*TODO: Future Feature: need to calculate new scrollXY by zoomFactor and then remove scrollToActive below,
       keep scrollXY before zoom(previous-zoom) and the calculate after zoom(new-zoom) */
    scrollToObj(active);
    /*}*/

    if (submit === undefined) return;

    var zoomFactor = document.getElementById('actionForm:zoomFactor_input');
    if (zoomFactor == null) return;

    var zooming = zoomFactor.value;
    if (zooming === zoomValue) return;

    zoomValue = zooming;
    if (tflow.zoomValue == zoomValue) return;
    tflow.zoomValue = zoomValue;

    submitZoom([
        {name: 'zoom', value: zoomValue}
    ]);
}

function serverStart() {
    /*show progress and disable all server action (allow client action)*/
    $('.container').addClass('server');
    contentReady(function () {
        contentWindow.$('.flow-chart').addClass('server');
    });
}

function serverEnd() {
    /*hide progress bar and enable all server action*/
    $('.container').removeClass('server');
    contentReady(function () {
        contentWindow.$('.flow-chart').removeClass('server');
    });
}

function scrollToObj(active) {
    var thisScroll = Date.now(),
        diff = thisScroll - tflow.lastScroll;
    if (tflow.lastScroll != null && diff < tflow.allowScrollDuration) {
        //console.log('detected multiple scroll at once (duration:' + diff + ', allowDuration:' + allowScrollDuration + ') cancel scrollToObj(object:' + active.attr('class') + ')');
        /*return;*/
    }

    if (active === undefined || active.length === 0 || active.hasClass('step')) {
        active = contentWindow.$(".selectable");
        // 0=step, 1=project, 2=first-selectable
        if (active.length > 2) {
            active = $(active[2]);
        } else {
            active = $(active[0]);
        }
    }

    var zoomed = zoomVal().replace('%', '') / 100.0;
    var innerHeight = contentWindow.innerHeight;
    var innerWidth = contentWindow.innerWidth;
    var outerHeight = active.outerHeight();
    var outerWidth = active.outerWidth();

    /*TODO: Future Feature: no need to scroll when the object already stand in the current screen*/
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
}

function hideDebugInfo($container) {
    console.log('hideDebugInfo( showDebugInfo:' + tflow.showDebugInfo + ', $container:' + $container.attr('class') + ' )');
    if (!tflow.showDebugInfo) {
        $container.find('.debug').hide();
    }
}

function propertyCreated() {
    if (tflow.propertyCreatedEnding) return;
    tflow.propertyCreatedEnding = true;

    /* init all behaviors of input boxes*/
    var $scrollPanel = $('.properties');
    hideDebugInfo($scrollPanel);

    /*scroll panel need to resize when the window resized or the splitter resized*/
    $scrollPanel.css('width', '100%');

    /*input-text: select all text when got the focus*/
    $scrollPanel.find('input[type=text]').each(function (i, e) {
        $(e).on('focus', function (ev) {
            ev.currentTarget.select();
        });
    });

    /*setFocus to the default field or first field*/
    focusProperty();

    /*Tab index system*/
    refreshTabIndex();

    tflow.propertyCreatedEnding = false;
}

function refreshTabIndex() {
    /*reset tab-index system*/
    tflow.propertyPanel = $('.properties');
    contentWindow.tflow.selectables = contentWindow.$('.selectable');

    /*select next object when press TAB on the last property*/
    tflow.propertyPanel.find('.next-input').off('focus').on('focus', function (ev) {
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
    tflow.propertyPanel.find('.prev-input').off('focus').on('focus', function (ev) {
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

var tflow = {
        page: "blank.xhtml",
        allowScrollDuration: 1000,
        lastScroll: Date.now(),

        isFirstFlow: true,

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

$(function () {
    leftPanel = $('.left-panel');
    leftGutter = $('.left-panel + .ui-splitter-gutter');
    leftToggle = $('.left-panel-toggle').click(toggleLeft).children('.ui-button-icon-left');
    rightPanel = $('.right-panel');
    rightGutter = $('.main-panel + .ui-splitter-gutter');
    rightToggle = $('.right-panel-toggle').click(toggleRight).children('.ui-button-icon-left');
    tflow.page = $('input[name=page]').attr('value');
    contentWindow = document.getElementById('flowchart').contentWindow;
});
