/*
// PlainDraggable SAMPLE
window.addEventListener('load', function () {
    'use strict';

    var ELEMENT_SIZE = 60,
        startElement = document.getElementById('ex-191-0'),
        line = new LeaderLine(startElement, document.getElementById('ex-191-1'), {
            startPlug: 'disc',
            endPlug: 'disc'
        }),
        islandBBox = document.getElementById('ex-191-island').getBoundingClientRect();

    islandBBox = {
        left: islandBBox.left + window.pageXOffset - ELEMENT_SIZE,
        top: islandBBox.top + window.pageYOffset - ELEMENT_SIZE,
        right: islandBBox.right + window.pageXOffset,
        bottom: islandBBox.bottom + window.pageYOffset
    };

    new PlainDraggable(startElement, {
        onDrag: function (newPosition) {
            if (newPosition.left > islandBBox.left && newPosition.left < islandBBox.right &&
                newPosition.top > islandBBox.top && newPosition.top < islandBBox.bottom) {
                if (this.left <= islandBBox.left) {
                    newPosition.left = islandBBox.left;
                } else if (this.left >= islandBBox.right) {
                    newPosition.left = islandBBox.right;
                }
                if (this.top <= islandBBox.top) {
                    newPosition.top = islandBBox.top;
                } else if (this.top >= islandBBox.bottom) {
                    newPosition.top = islandBBox.bottom;
                }
            }
        },
        onMove: function () {
            line.position();
        },
        zIndex: false
    });

});
*/
