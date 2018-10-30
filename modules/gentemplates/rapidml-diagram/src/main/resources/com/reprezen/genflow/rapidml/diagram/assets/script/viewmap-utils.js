var ViewmapUtils = {

    ALL_LABELS_RIGHT_MARGIN : 10, //FIXME: remove this

    initCache: function () {
        if (!this.getCachedResult || !this.putCachedResult) {
            var cache = [];
            var cacheSize = 0;
            var successesCount = 0;
            var missesCount = 0;
            var MAX_SIZE = 500;
            this.putCachedResult = function (input, result) {
                if (cacheSize >= MAX_SIZE) {
                    //I don't see any efficient removals in JS, so lets just reset the whole cache
                    this.initCache();
                }
                cache[input] = result;
                cacheSize++;
            };
            this.getCachedResult = function (input) {
                var result = cache[input];
                if (result) {
                    successesCount++;
                } else {
                    missesCount++;
                }
                return result;
            };
            this.dumpStats = function () {
                console.log("Cache stats: size: " + cacheSize +
                    ", successes : " + successesCount +
                    ", misses: " + missesCount);
            }
        }
        return this;
    },

    createSVGtext : function(caption) {
        if (this.getCachedResult) {
            var cachedResult = this.getCachedResult(caption);
            if (cachedResult) {
                return cachedResult;
            }
        }
        var result = ViewmapUtils.doMeasureSVGtext(this.defaultStyler(caption));
        // console.log("Measuring: " + caption + ", bbox: " + result.width + ", " + result.height);

        if (this.putCachedResult) {
            this.putCachedResult(caption, result);
        }
        return result;
    },

    /**
     * Default styler for doMeasureSVGtext for Rapid diagrams, where important part of CSS rules
     * have selectors: '.node text' and '.label'
     */
    defaultStyler : function(textToMeasure) {
        return function(svgText, rootNode) {
            var g = document.createElementNS('http://www.w3.org/2000/svg', 'g');
            g.setAttributeNS(null, "class", "node");
            svgText.setAttributeNS(null, "class", "label");
            g.appendChild(svgText);
            svgText.appendChild(document.createTextNode(textToMeasure));
            rootNode.appendChild(g);
            return function() {
                rootNode.removeChild(g);
            };
        };
    },


    /**
     * Diagram Layout is calculated before rendering of the chart in DOM,
     * so we don't have a real labels to measure.
     * <p/>
     * This method solves this problem by trying to predict the measuring of the text,
     * by temporary creation of the similar svg:text and measuring its BBox.
     * </p>
     * This method accepts the function 'styler : function(svgText, rootNode) which
     * <ul>
     * <li>accepts the empty not-attached svg:text and the root node</li>
     * <li>attaches the svg text to root node, possibly placing arbitrary amount of the intermediate nodes</li>
     * <li>styles all the nodes as necessary</li>
     * <li>returns the function 'dispose : function(rootNode)' that will remove all the created structure from dom</li>
     * </ul>
     * It allows to match styles from non-trivial CSS rules, e.g '.node text' which needs a specific parent nodes.
     * */
    doMeasureSVGtext: function (styler) {
        /*const*/var FAR_ENOUGH = -1000;
        var svgText = document.createElementNS('http://www.w3.org/2000/svg', 'text');
        svgText.setAttributeNS(null, "x", FAR_ENOUGH);
        svgText.setAttributeNS(null, "y", FAR_ENOUGH);

        var rootNode = document.getElementById("main");
        var disposer = styler(svgText, rootNode);

        var frozenBBox = svgText.getBBox();
        if (disposer) disposer();
        var bbox = {};
        bbox.width = frozenBBox.width;
        bbox.height = frozenBBox.height;

        //FIXME: why it is still here? margins are clearly responsibility of the caller
        bbox.width += ViewmapUtils.ALL_LABELS_RIGHT_MARGIN;
        return bbox;
    }

};
ViewmapUtils.initCache();


