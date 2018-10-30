/**
 * Created with JetBrains WebStorm.
 * User: vipul-jain
 * Date: 3/1/14
 * Time: 12:47 PM
 * To change this template use File | Settings | File Templates.
 *
 * Variables
 * margin - default margin between nodes
 * padding - default padding between child's of nodes
 * transitionDuration - animation time for chart update
 * container - container that's hold the chart ( svg element )
 * svg  - svg element where out chart renders
 * width - container width
 * height - container height
 * root - root of data element ( data element )
 * rootNode - root node of data element ( svg element )
 *
 * chartGroup - the chart layer inside container which holds the all element of nodes, of flowLayout
 * chartLines - the link layer inside container which holds the extra links over the flowLayout
 *
 */

d3.custom.chart.flow = function(transitionDuration) {
    if (transitionDuration == null) transitionDuration = 300;
    // public variables with default settings
    var margin = {top:10, right:10, bottom:10, left:10}, // defaults
        padding = {top:30, right:10, bottom:10, left:10},// defaults
        chartGroup,
        chartLines,
        container,
        svg,
        width,
        height,
        root,
        rootNode,
        rootObject,
        scrollbarAffordance,
        top_padding = 13;

    /* A scale of colors for URI links. A resource can have several URI links and each of them has its own color.
     * The first element is used for the first link, the second for the second link and so on.*/
    var resourceUriLinkColors = d3.scale.ordinal().range(
    		["#1f77b4", "#ff7f0e", "#2ca02c", "#d62728", "#9467bd", "#8c564b", "#e377c2", "#7f7f7f", "#bcbd22", "#17becf"]);

    var referenceEmbedColors = d3.scale.ordinal().range(
			["#CCEBEB", "#99D6D6", "#86a9d1", "#c3d4e8", "#CCEBEB", "#99D6D6", "#86a9d1", "#c3d4e8"]);

    var flow = d3.custom.layout.flow()
        .margin(margin)
        .padding(padding)
        .nodeWidth(40) // width of node properties Kind of min width
        .nodeHeight(10) // height node properties Kind of min height
        .containerWidth(40)  // width of node container ( rect ) Kind of min width
        .containerHeight(30)
        .linkPadding(6); // height of node container ( rect ) Kind of min height

    function chart(selection) {
//      First node or data
        rootNode = selection.node();

//      set time out on resize to adjust the nodes for new height/width
        function debounce(fn, timeout) {
            var timeoutID = -1;
            return function() {
                if (timeoutID > -1) {
                    window.clearTimeout(timeoutID);
                }
                timeoutID = window.setTimeout(fn, timeout);
            }
        }

//      On window resize calculate new height and width of client view for main container
        function resize(selectedNode,isResize) {
            var domContainerWidth  = (parseInt(d3.select(rootNode).style("width"))),
                domContainerHeight = (parseInt(d3.select(rootNode).style("height"))),
                flowWidth = 0;

            if (root.height > domContainerHeight) {
                scrollbarAffordance = 0;
            } else {
                scrollbarAffordance = 0;
            }

            flowWidth = domContainerWidth - scrollbarAffordance;
            flow.width(flowWidth);

            if(isResize == undefined)isResize = true;
            chart.update(selectedNode,isResize);

            svg.transition().duration(transitionDuration)
                .attr("width", function(d) {
                    return domContainerWidth;
                })
                .attr("height", function(d) {
                    return d.height + margin.top + margin.bottom;
                })
                .select(".chartGroup")
                .attr("width", function(d) {
                    return flowWidth;
                })
                .attr("height", function(d) {
                    return d.height + margin.top + margin.bottom;
                })
                .select(".background")
                .attr("width", function(d) {
                    return flowWidth;
                })
                .attr("height", function(d) {
                    return d.height + margin.top + margin.bottom;
                });
        }

//      window resize function binding
        d3.select(window).on('resize', function() {
            debounce(resize, 50)();
        });

/**
 * This the main part of code where the chart and links layer are going to be render
 * selection - the selected element on DOM which hold the all svg elements
 * */
        selection.each(function(arg) {
            root = arg;
            container = d3.select(this);

            var i = 0;

            if (!svg) {
//              svg container
                svg = container.append("svg")
                    .attr("id", "main")
                    .attr("class", "svg chartSVG")
                    .attr("transform", "translate(0, 0)")
                    .style("shape-rendering", "auto") // shapeRendering options; [crispEdges|geometricPrecision|optimizeSpeed|auto]
                    .style("text-rendering", "auto"); // textRendering options;  [auto|optimizeSpeed|optimizeLegibility|geometricPrecision]

//                for now commenting shadow code

//                var defs = svg.append( 'defs' );
//                var filter = defs.append( 'filter' )
//                    .attr( 'id', 'dropShadow' )
//                filter.append( 'feGaussianBlur' )
//                    .attr( 'in', 'SourceAlpha' )
//                    .attr( 'stdDeviation', 2 ) // !!! important parameter - blur
//                    .attr( 'result', 'blur' );
//                filter.append( 'feOffset' )
//                    .attr( 'in', 'blur' )
//                    .attr( 'dx', 2 ) // !!! important parameter - x-offset
//                    .attr( 'dy', 2 ) // !!! important parameter - y-offset
//                    .attr( 'result', 'offsetBlur' );
//                var feMerge = filter.append( 'feMerge' );
//                feMerge.append( 'feMergeNode' )
//                    .attr( 'in", "offsetBlur' )
//                feMerge.append( 'feMergeNode' )
//                    .attr( 'in', 'SourceGraphic' );

                // this is only for debugging purposes,
                // allows to highlight svg:text using: <text filter="url(#fill_solid_yellow)">
                var filterDef = svg.append("defs").append("filter")
                    .attr("id", "fill_solid_yellow")
                    .attr("x", 0)
                    .attr("y", 0)
                    .attr("width", 1)
                    .attr("height", 1);
                filterDef.append("feFlood")
                    .attr("flood-color", "yellow");
                filterDef.append("feComposite")
                    .attr("in", "SourceGraphic");

//              Create arrow head marker for link
                svg.append("defs").append("marker")
                    .attr("id", "arrowhead_rlink")
                    .attr("refX", 3)
                    .attr("refY", 2)
                    .attr("markerWidth", 6)
                    .attr("markerHeight", 5)
                    .attr("orient", "auto")
                    .append("path")
                    .attr("d", "M 0,0 V 4 L6,2 Z");

//              Create circle head marker for link
                svg.append("defs").append("marker")
                    .attr("id", "markerCircle_rlink")
                    .attr("refX", 3)
                    .attr("refY", 2)
                    .attr("markerWidth", 6)
                    .attr("markerHeight", 5)
                    .attr("orient", "auto")
                    .append("circle")
                        .attr("cx", "3")
                        .attr("cy", "2")
                        .attr("r", "1.5");

//              Create arrow head marker for request response
                svg.append("defs").append("marker")
                    .attr("id", "arrowhead_msg")
                    .attr("refX", 3)
                    .attr("refY", 2)
                    .attr("markerWidth", 6)
                    .attr("markerHeight", 5)
                    .attr("orient", "auto")
                    .append("path")
                    .attr("d", "M 0,0 V 4 L6,2 Z");

//              Create circle head marker for request response
                svg.append("defs").append("marker")
                    .attr("id", "markerCircle_msg")
                    .attr("refX", 3)
                    .attr("refY", 2)
                    .attr("markerWidth", 6)
                    .attr("markerHeight", 5)
                    .attr("orient", "auto")
                    .append("circle")
                    .attr("cx", "3")
                    .attr("cy", "2")
                    .attr("r", "1.5");

//              Create horizontal line marker for URI parameter
                svg.append("defs").append("marker")
                    .attr("id", "markerH")
                    .attr("refX", 5)
                    .attr("refY", 10)
                    .attr("markerWidth", 10)
                    .attr("markerHeight", 10)
                        .append("line")
                        .attr("x1", "0")
                        .attr("y1", "10")
                        .attr("x2", "10")
                        .attr("y2", "10")
                        .style("stroke-width", "1.5")
                        .style("stroke", "black");

//              Create vertical line marker for URI parameter
                svg.append("defs").append("marker")
                    .attr("id", "markerV")
                    .attr("refX", 10)
                    .attr("refY", 5)
                    .attr("markerWidth", 10)
                    .attr("markerHeight", 10)
                        .append("line")
                        .attr("x1", "10")
                        .attr("y1", "0")
                        .attr("x2", "10")
                        .attr("y2", "10")
                        .style("stroke-width", "1.5")
                        .style("stroke", "black");

//              Init chartGroup object for nodes
                chartGroup = svg.append("svg:g")
                    .attr("class", "chartGroup");

//              Add background rectangle of container
                chartGroup.append("svg:rect")
                    .attr("class", "background");

//              Init chartLines object for links
                chartLines = svg.append('svg:g')
                    .attr('class','chartLines');

            }

            chart.resizeChart = function(a,b){
                return resize(a,b);
            };

            /*
            * Find out which nodes should be connected with links
            * reference links and URI links
            * */
            chart.getUpdatedLinkData = function(nodes){
                var extraLinkData = [];

                function resetReferenceLinkPoints(){
                    var _referenceLinks = extraLinkData.filter(function(_links){
                        return _links.type == "referenceLink";
                    });

                    for( _referenceIndex in _referenceLinks){
                        var _link = _referenceLinks[_referenceIndex];
                        var _toHorizontal = true;
                        if(_link.source_resource.x < _link.target.x ){
                            if(_link.source_resource.y == _link.target.y){
                                if(Math.abs(_link.target.x - (_link.source_resource.x+_link.source_resource.width)) > 150){
                                }else{
                                    _toHorizontal = false;
                                }
                            }
                        }
                        _link._toHorizontal = _toHorizontal;
                    }

                    var mapTargetsH = [];
                    var occurrencesH = [];
                    var mapTargetsV = [];
                    var occurrencesV = [];
                    for(var intIndex=0; intIndex<_referenceLinks.length;intIndex++){
                        var currentLink = _referenceLinks[intIndex];
                        if(currentLink._toHorizontal){
                            if(mapTargetsH.indexOf(currentLink.target) == -1){
                                mapTargetsH.push(currentLink.target);
                                occurrencesH.push(1);
                            }else{
                                occurrencesH[mapTargetsH.indexOf(currentLink.target)] += 1;
                            }
                        }else{
                            if(mapTargetsV.indexOf(currentLink.target) == -1){
                                mapTargetsV.push(currentLink.target);
                                occurrencesV.push(1);
                            }else{
                                occurrencesV[mapTargetsV.indexOf(currentLink.target)] += 1;
                            }
                        }
                    }

                    /**
                     * Calculating number of target for each resources
                     * */
                    var mapPositionH = [];
                    var mapPositionV = [];
                    var occurrencepositionH = [];
                    var occurrencepositionV = [];
                    for(var intIndex=0; intIndex<_referenceLinks.length;intIndex++){
                        var currentLink = _referenceLinks[intIndex];
                        if(currentLink._toHorizontal){
                            if(mapPositionH.indexOf(currentLink.target) == -1){
                                mapPositionH.push(currentLink.target);
                                occurrencepositionH.push(0);
                                currentLink.extra.totalTarget = occurrencesH[mapTargetsH.indexOf(currentLink.target)];
                                currentLink.extra.targetCount = 0;
                            }else{
                                occurrencepositionH[mapPositionH.indexOf(currentLink.target)] += 1;
                                currentLink.extra.totalTarget = occurrencesH[mapTargetsH.indexOf(currentLink.target)];
                                currentLink.extra.targetCount = occurrencepositionH[mapPositionH.indexOf(currentLink.target)]
                            }
                        }else{
                            if(mapPositionV.indexOf(currentLink.target) == -1){
                                mapPositionV.push(currentLink.target);
                                occurrencepositionV.push(0);
                                currentLink.extra.totalTarget = occurrencesV[mapTargetsV.indexOf(currentLink.target)];
                                currentLink.extra.targetCount = 0;
                            }else{

                                occurrencepositionV[mapPositionV.indexOf(currentLink.target)] += 1;
                                currentLink.extra.totalTarget = occurrencesV[mapTargetsV.indexOf(currentLink.target)];
                                currentLink.extra.targetCount = occurrencepositionV[mapPositionV.indexOf(currentLink.target)]
                            }
                        }
                    }
                }

                function resetURILines(){
                    function resetOneURILine(resource) {
                        var bindings = DiagramSpecifics.getURISegmentBindings(resource);
                        bindings.forEach(function(binding, idx) {
                            var nextLink = {};
                            nextLink.type = "URILink";
                            nextLink.id = resource.id + "_" + binding.matchedSegment.id;
                            nextLink.source = binding.matchedSegment;
                            nextLink.target = binding.matchedFeature;
                            nextLink.source_resource = resource;
                            nextLink.segmentBinding = binding;

                            //FIXME: this is weird place to compute it, remove it
                            nextLink.extra = {};
                            nextLink.extra.count = idx;
                            nextLink.extra.pad = 5 * idx + 5;

                            extraLinkData.push(nextLink);
                        });
                    }

                    nodes.forEach(function(d) {
                        if (d.objecttype == "ObjectResource" || d.objecttype == "CollectionResource") {
                            resetOneURILine(d);
                        }
                    });
                }

                /**
                 * Find out available ReferenceLines in chart
                 * task/ZEN-496
                 * Reference link visible in all expanded/collapsed states
                 *
                 * */
                function resetReferenceLine(){
                    var _resourceObjects = nodes.filter(function(d){
                        return d.objecttype == "ObjectResource" || d.objecttype == "CollectionResource";
                    });

                    // more than zero because it is possible external unconnected links
                    if(_resourceObjects.length > 0){
                        for(_resourceIndex in _resourceObjects){
                            var _referenceLinks = flow.getReferences(_resourceObjects[_resourceIndex],_resourceObjects);
                            for( _referenceIndex in _referenceLinks){
                                extraLinkData.push(_referenceLinks[_referenceIndex]);
                            }
                        }
                    }

                    resetReferenceLinkPoints();
                }

                function resetRequestLink(){
                    var _methodObjects = nodes.filter(function(d){
                        return d.objecttype == "Method";
                    });

                    if(_methodObjects.length > 0){
                        for(_methodIndex in _methodObjects){
                            var _method = _methodObjects[_methodIndex];
                            if(_method.children){
                                var _responses = _method.children.filter(function(_response){
                                    return _response.objecttype == "Request";
                                });
                                if(_responses.length > 0){
                                    for(_responseIndex in _responses){
                                        var _response = _responses[_responseIndex];
                                        var _linkObject = {};
                                        _linkObject.id = _method.name+"_"+_response.id;
                                        _linkObject.source = _method ;
                                        _linkObject.target = _response ;
                                        _linkObject.type = "requestLink";
                                        extraLinkData.push(_linkObject);
                                    }
                                }
                            }
                        }
                    }
                }

                function resetResponseLink(){
                    var _methodObjects = nodes.filter(function(d){
                        return d.objecttype == "Method";
                    });

                    if(_methodObjects.length > 0){
                        for(_methodIndex in _methodObjects){
                            var _method = _methodObjects[_methodIndex];
                            if(_method.children){
                                var _responses = _method.children.filter(function(_response){
                                    return _response.objecttype == "Response";
                                });
                                if(_responses.length > 0){
                                    for(_responseIndex in _responses){
                                        var _response = _responses[_responseIndex];
                                        var _linkObject = {};
                                        _linkObject.id = _method.name+"_"+_response.id;
                                        _linkObject.source = _response;
                                        _linkObject.target = _method ;
                                        _linkObject.type = "responseLink";
                                        extraLinkData.push(_linkObject);
                                    }
                                }
                            }
                        }
                    }
                }

                resetURILines();     // function to find URI links
                resetReferenceLine(); // function to find reference links
                resetRequestLink();   // function to find request links in method
                resetResponseLink();  // function to find response links in method

                return extraLinkData;
            };

            /*
            * Update links layer according to data
            * */
            chart.updateExtraLinks = function(nodes){
                var linkData = this.getUpdatedLinkData(nodes);
                var _sideLinkRef = [];

                /**
                 * Sharp     arc in link ( r = 3 )
                 *  |¯         a 3,3 0 0 0 -3,3    type 1
                 *  |_         a 3,3 0 0 0 3,3     type 2
                 *   ¯|        a 3,3 0 0 1 3,3     type 3
                 *   _|        a 3,3 0 0 1 -3,3    type 4
                 * */
                function getArc(_type,r,toDown,toLeft){
                    var arc = "a "+r+","+r+" 0 0 0 "+r+","+r+"";
                    var type = _type+"";
                    switch (type){
                        case "1":
                            if(toDown){
                                arc = "a "+r+","+r+" 0 0 0 "+ ( -1*r ) +","+r+"";
                            }else{
                                arc = "a "+r+","+r+" 0 0 1 "+ r +","+( -1*r )+"";
                            }
                        break;
                        case "2":
                            if(toLeft){
                                arc = "a "+r+","+r+" 0 0 0 "+r+","+r+"";
                            }else{
                                arc = "a "+r+","+r+" 0 0 1 "+(-1* r)+","+(-1*r)+"";
                            }

                            break;
                        case "3":
//                            arc = "a "+r+","+r+" 0 0 1 "+r+","+r+"";
                            if(toLeft){ // mean right to left
                                arc = "a "+r+","+r+" 0 0 1 "+r+","+r+"";
                            }else{
                                arc = "a "+r+","+r+" 0 0 0 "+ (-1*r)+","+(-1*r)+"";
                            }
                            break;
                        case "4":
                            if(toDown){
                                arc = "a "+r+","+r+" 0 0 1 "+ ( -1*r ) +","+r+"";
                            }else{
                                arc = "a "+r+","+r+" 0 0 0 "+ r +","+ ( -1*r)+"";
                            }
                            break;
                        default :
                            arc = "a "+r+","+r+" 0 0 0 "+r+","+r+"";
                    }
                    return arc;
                }

                function linkPassTo(resourceId){
                    var _getAdded = _sideLinkRef.filter(function(t){
                        return t.id == resourceId;
                    });

                    if(_getAdded.length > 0){
                        if(_getAdded[0].side){
                            _getAdded[0].side += 1;
                        }else{
                            _getAdded[0].side = 1;
                        }
                    }else{
                        var _tempObj = {};
                        _tempObj.id = resourceId;
                        _tempObj.side = 1;
                        _sideLinkRef.push(_tempObj);
                    }
                }

                function getSidePadding(resourceId){
                    var _getAdded = _sideLinkRef.filter(function(t){
                        return t.id == resourceId;
                    });
                    if(_getAdded.length > 0){
                        if(_getAdded[0].side){
                             if(_getAdded[0].side > 1)
                                return _getAdded[0].side*5 + 20;
                             else
                                return 25;
                        }
                    }
                    return 20;
                }

//              detect IE and it's versions for some svg capabilities check
                function detectIE() {
                    var ua = window.navigator.userAgent;
                    var msie = ua.indexOf('MSIE ');
                    var trident = ua.indexOf('Trident/');

                    if (msie > 0) {
                        // IE 10 or older => return version number
                        return parseInt(ua.substring(msie + 5, ua.indexOf('.', msie)), 10);
                    }

                    if (trident > 0) {
                        // IE 11 (or newer) => return version number
                        var rv = ua.indexOf('rv:');
                        return parseInt(ua.substring(rv + 3, ua.indexOf('.', rv)), 10);
                    }

                    // other browser
                    return false;
                }

                // If IE > 9 then render the all links
                if (detectIE() > 9){
                    chartLines.selectAll("g.extraLinks").remove();
                }
                var tempList = [];
                // function to draw the right path of reference links according to nods positions
                function getComputedPath(d){
                    var _path = "";
                    var x1,x2,y1,y2;
//                    return rout.getPath(d.source, d.source_resource, d.target);
                    var _marg = 10 + d.extra.pad ;

                    d.target._isTarget = true;

                    function getX2Position(node,fromleft){
                        var _target = node.target;
                        var _fragment = ( _target.width / (parseInt (node.extra.totalTarget) + 1) );
                        var _x2 = _target.x + _fragment +( _fragment * node.extra.targetCount );
                        if(!fromleft){
                            _x2 = _target.x + _target.width - ( _fragment +( _fragment * node.extra.targetCount ) );
                        }
                        return _x2;
                    }

                    x1 = d.source.x + d.source.width;
                    /**
                     * Divide height with number of outgoing link and find equal fragment
                     * */
                    if(d.isCollapsed){
                        var _fragment = ( d.source.height / (parseInt (d.total) + 1) );
                        y1 = d.source.y + _fragment +( _fragment * ( parseInt(d.extra.count) ) );
                    }else{
                        y1 = d.source.y + (d.source.height/2);
                    }

                    x2 = d.target.x;
                    var _expectedWidth = d.extra.totalTarget * flow.linkPadding();
                    var _expectedHeight = d.extra.totalTarget * flow.linkPadding();

                    if(d.source_resource.x < d.target.x ){
                        //left
                        if(d.source_resource.y == d.target.y){
                            if(Math.abs(d.target.x - (d.source_resource.x+d.source_resource.width)) > 150){
//                                x2 = d.target.x + (d.target.width * 0.05) + getSidePadding(d.target.id);
                                x2 = getX2Position(d,true);
                                y2 = d.target.y;
                                d.target._expectedWidth = _expectedWidth;
                            }else{
                                //beside
//                                y2 = d.target.y + (d.target.height/2);
                                var _fragment = ( d.target.height / (parseInt ( d.extra.totalTarget) + 1) );
                                y2 = d.target.y + _fragment +( _fragment * ( parseInt(d.extra.targetCount) ) );
                                d.target._expectedHeight = _expectedHeight;
                            }
                        }else{
                            d.target._expectedWidth = _expectedWidth;
                            if(d.source_resource.y < d.target.y){
                                //left to right source above to the target
//                                x2 = d.target.x + (d.target.width * 0.05) + getSidePadding(d.target.id);
                                x2 = getX2Position(d,true);
                                y2 = d.target.y;
                            }else{
                                //left to right source down to the target
//                                x2 = d.target.x + (d.target.width * 0.05) + getSidePadding(d.target.id);
                                x2 = getX2Position(d,true);
                                y2 = d.target.y + d.target.height;
                            }
                        }
                    }else{
                        //right
                        d.target._expectedWidth = _expectedWidth;
                        if(d.source_resource.y == d.target.y){
                            // same level
//                            x2 = d.target.x + d.target.width - ((d.target.width * 0.05) + getSidePadding(d.target.id));
                            x2 = getX2Position(d,false);
                            y2 = d.target.y ;
                        }else{
                            if(d.source_resource.y < d.target.y){
                                //right to left source above to the target
//                                x2 = d.target.x + d.target.width - ((d.target.width * 0.05) + getSidePadding(d.target.id));
                                x2 = getX2Position(d,false);
                                y2 = d.target.y;
                            }else{
                                //right to left source down to the target
//                                x2 = d.target.x + d.target.width - ((d.target.width * 0.05) + getSidePadding(d.target.id));
                                x2 = getX2Position(d,false);
                                y2 = d.target.y + d.target.height;
                            }
                        }
                    }

                    linkPassTo(d.source_resource.id);
                    linkPassTo(d.target.id);

                    /**
                     * This method will add minimum padding to root node so
                     *
                     * */

                     function addTopPadding(_padding){
                        if(rootObject._topPadding){
                            if(rootObject._topPadding > _padding){
                                rootObject._topPadding = _padding;
                            }
                        }else{
                            rootObject._topPadding = _padding;
                        }
                    }

                    /**
                     * Here is the code to separate line which are in same resource this
                     *
                     * For each new link it will come 4 px more outside than previous link
                     * Change in this code (i.e. change 4 to 5 ) also reflect change in layout js
                     * where the last padding is calculated. which inform next node that I have this much links
                     *
                     * */

 //                  var _cx = (d.source_resource.x + d.source_resource.width + 5 + (getSidePadding(d.source_resource.id) *0.5)) - 3 ;
                    var _cx = (d.source_resource.x + d.source_resource.width + 10 + ( d.extra.count * flow.linkPadding() ) );
                    _path = "M"+x1+","+y1 + "H"+ _cx ;
                    if(d.source_resource.x < d.target.x ){
                        // left to right
                        if(d.source_resource.y == d.target.y){
                            // same level
                            if(Math.abs(d.target.x - (d.source_resource.x+d.source_resource.width)) > 150){
//                                _path += "V"+ (d.source_resource.y - (getSidePadding(d.source_resource.id)*0.4 )- _marg) +"H" + (x2-getSidePadding(d.target.id));
//                                _path += "H"+x2+"V"+ y2 ;
                                var arc1 = "", arc2 = "",sp = 0;
                                if(y1 > y2){
                                    arc1 = getArc(4,3,false,true);
                                    arc2 = getArc(1,3,false,true);
                                    sp = -3;
                                }
                                var _cy = (d.source_resource.y - (getSidePadding(d.source_resource.id)*0.4 )- _marg - sp - (d.extra.count* 2) );
                                if(tempList.indexOf(_cy) > -1 ){
                                    _cy = _cy - 5;
                                    tempList.push(_cy);
                                }else{
                                    tempList.push(_cy);
                                }
                                addTopPadding(_cy);

                                _path += arc1 +"V"+ _cy + arc2 +"H" + (x2-getSidePadding(d.target.id));
                                _path += "H"+(x2)+ getArc(3,3,false,true) +"V"+ (y2) ;

                            }else{
                                //beside
//                                _path += "V"+ (y2);
                                var arc1 = "", arc2 = "", sp = 0;
                                if(Math.abs(y1-y2) > 3){
                                    if(y1 < y2){
                                        arc1 = getArc(3,3,false,true);
                                        arc2 = getArc(2,3,true,true);
                                        sp = 3;
                                    }else{
                                        arc1 = getArc(4,3,false,true);
                                        arc2 = getArc(1,3,false,true);
                                        sp = -3;
                                    }
                                }
                                _path += arc1 +"V"+ ( y2 - sp );
                                if(_cx > (x2-getSidePadding(d.target.id))){
                                    _path += arc2 +"H" + (_cx + Math.abs(sp) + 1);
                                }else{
                                    _path += arc2 +"H" + (x2-getSidePadding(d.target.id) + 4 );
                                }
                                _path += "V"+ y2 +"H"+ x2 ;
                            }
                        }else{
                            if(d.source_resource.y < d.target.y){
                                //left to right source above to the target
                                var cy = d.source_resource.y + d.source_resource.height + _marg;
                                if(cy > (y2 - (_marg/2)-(getSidePadding(d.source_resource.id)*0.1)) ){
                                    cy = (y2 - (_marg/2)-(getSidePadding(d.source_resource.id)*0.1));
                                }
                                _path += getArc(3,3,true,true) +"V"+ cy + "H" + ( x2 );
                                _path += "V"+ (y2 - (_marg/2)-(getSidePadding(d.source_resource.id)*0.1)) + "H"+ x2 + "V"+ y2;
                            }else{
                                //left to right source down to the target
                                var cy = d.source_resource.y - _marg;
                                if(cy < (y2 + (_marg/2)+(getSidePadding(d.source_resource.id)*0.1)) ){
                                    cy = ( y2 + (_marg/2)+(getSidePadding(d.source_resource.id)*0.1));
                                }
                                _path += getArc(4,3,false,true) +"V"+ cy + "H" + ( x2 );// + getSidePadding(d.target.id) );
                                _path += "V"+ y2 ;//+ "H"+x2;
                            }
                        }
                    }else{
                        // right to left
                        if(d.source_resource.y == d.target.y){
                            // same level
                            var arc1 = "", arc2 = "",sp = 0;
                            if(y1 > y2){
                                arc1 = getArc(4,3,false,false);
                                arc2 = getArc(3,3,false,false);
                                sp = -3;
                            }
                            var _cy = (d.source_resource.y - (getSidePadding(d.source_resource.id)*0.5) - sp - (d.extra.count* 2));
                            if(tempList.indexOf(_cy) > -1 ){
                                _cy = _cy - 5;
                                tempList.push(_cy);
                            }else{
                                tempList.push(_cy);

                            }
                            addTopPadding(_cy);
                            _path += arc1 +"V"+ _cy + arc2 +"H" + ( x2 + getSidePadding(d.target.id) );
                            _path += "H"+ x2 +getArc(1,3,true,false)+"V"+ y2 ;

                        }else{
                            if(d.source_resource.y < d.target.y){
                                //right to left source above to the target
                                var cy = d.source_resource.y + d.source_resource.height + _marg;
                                var arc2 = "",arc3 = "";
                                if(cy > (y2 - (_marg/2)-(getSidePadding(d.source_resource.id)*0.1)) ){
                                    cy = (y2 - (_marg/2)-(getSidePadding(d.source_resource.id)*0.1));
                                }
                                var sp = 0;
                                if( _cx > (x2 +_marg + getSidePadding(d.target.id)) ){
                                    arc2 = getArc(4,3,true,true);
                                    arc3 = getArc(1,3,true,true);
                                    sp = 3;
                                }else{
                                    arc2 = getArc(2,3,true,false);
                                    arc3 = getArc(3,3,true,false);
                                    sp = -3;
                                }
//                                _path += getArc(3,3,true,true)+ "V" + ( cy-4 )+ a2 + "H" + ( x2 +_marg + getSidePadding(d.target.id) + 3 );
//                                _path += a3 +"V"+ (y2 - (_marg/2)-(getSidePadding(d.source_resource.id)*0.1) - 3 ) + a2 +"H"+ x2 + a3 + "V"+ y2;

                                _path += getArc(3,3,true,true)+ "V" + cy +"H" + ( x2 +_marg + getSidePadding(d.target.id) + sp );
                                _path += "V"+ (y2 - (_marg/2)-(getSidePadding(d.source_resource.id)*0.1) - 3 ) + "H"+ x2 + getArc(1,3,true,true) +  "V"+ y2;
                            }else{
                                //right to left source down to the target
                                var cy = d.source_resource.y - _marg;
                                if(cy < (y2 + (_marg/2)+(getSidePadding(d.source_resource.id)*0.1)) ){
                                     cy = (y2 + (_marg/2)+(getSidePadding(d.source_resource.id)*0.1));
                                }
                                var arc2 = "",arc3 = "", sp = 0;
                                if( _cx > (x2 +_marg + getSidePadding(d.target.id)) ){
                                    arc2 = getArc(3,3,false,false);
                                    arc3 = getArc(1,3,true,true);
                                    sp = 3;
                                }else{
                                    arc2 = getArc(1,3,false,true);
                                    arc3 = getArc(3,3,true,false);
                                    sp = -3;
                                }

                                _path += getArc(4,3,false,false) +"V"+ cy  + "H" + ( x2 + _marg +getSidePadding(d.target.id) );
                                _path += "V"+ (y2 + (_marg/2)+(getSidePadding(d.source_resource.id)*0.1)) + "H"+ x2 + getArc(2,3,false,false) +"V"+ y2;
                            }
                        }
                    }

                    return _path;
                }

                function getURLPath(link){
                    //nextLink.id = resource.id + "_" + _id;
                    //nextLink.source = binding.matchedSegment;
                    //nextLink.target = binding.matchedFeature;
                    //nextLink.source_resource = resource;
                    //nextLink.segmentBinding = binding;

                    var segment = link.source;
                    var x1 = segment.x + segment.width / 2 - ViewmapUtils.ALL_LABELS_RIGHT_MARGIN / 2;
                    var y1 = segment.y + segment.height + 2;
                    var feature = link.target;
                    var x2 = feature.x - 5;
                    var y2 = feature.y + feature.height / 2;
                    var linkIndex = link.extra.count;
                    var topGap = (linkIndex + 1) * DiagramSpecifics.URI_LINK_VGAP;
                    var leftGap = (linkIndex + 1) * DiagramSpecifics.URI_LINK_HGAP;
                    var resourceX = link.source_resource.x;

                    // sharp types, see getArc
                    /*const*/var FROM_TOP_TO_LEFT = 4;
                    /*const*/var FROM_RIGHT_TO_BOTTOM = 1;
                    /*const*/var FROM_TOP_TO_RIGHT = 2;

                    /*const*/var ARC = 3;

                    return "M" + x1 + "," + y1
                        + "V" + (y1 + topGap - ARC)
                        + getArc(FROM_TOP_TO_LEFT, ARC, true, false)
                        + "H" + (resourceX + leftGap + ARC)
                        + getArc(FROM_RIGHT_TO_BOTTOM, ARC, true, false)
                        + "V" + (y2 - ARC)
                        + getArc(FROM_TOP_TO_RIGHT, ARC, true, true)
                        + "H" + x2
                        ;
                }

                function getResponsePath(d) {
                    // preconditions:
                    // d.source - response
                    // d.target - method
                    var x1 = d.source.x + d.source.width;
                    var y1 = d.source.y + (d.source.height / 2);
                    var x2 = d.target.x + d.target.width + 10;

                    var responsesMinMax = DiagramSpecifics.collectChildrenVerticalMinMax(d.target, function(ch) {
                        return ch.objecttype == 'Response';
                    });
                    var y2 = responsesMinMax ? responsesMinMax.getMiddle() : y1;

                    var arc1 = "", arc2 = "", sp = 0;
                    if (Math.abs(y1 - y2) > 5) {
                        if (y1 < y2) {
                            arc1 = getArc(3, 3, true, true);
                            arc2 = getArc(2, 3, true, true);
                            sp = 3;
                        } else {
                            arc1 = getArc(4, 3, false, true);
                            arc2 = getArc(1, 3, false, true);
                            sp = -3;
                        }
                    }

                    var mid = Math.abs(x2 - x1) / 2;
                    var _path = "M" + x1 + "," + y1 + "H" + (x1 + mid - 6) + arc1 + "V" + (y2 - sp) + arc2 + "H" + x2;
                    return _path;
                }

                function getRequestPath(d){
                    // preconditions:
                    // d.source - method
                    // d.target - request

                    var x1 = d.source.x + d.source.width + 10;
                    var x2 = d.target.x + d.target.width;
                    var y2 = d.target.y + (d.target.height / 2);
                    var y1 = y2;

                    var _path = "M"+x1+","+y1 + "H"+ (x2+margin.right) + "V" + y2 +"H"+x2;
                    return _path;
                }

                function getExternalReferenceLinkPath(d){
                    var _path,x1,x2,y1,y2;
                    x1 = d.source.x + d.source.width;
                    y1 = d.source.y + (d.source.height/2);
                    x2 = d.source.x + d.source.width + 30;
                    if(d.isCollapsed){
                        var _fragment = ( d.source.parent.height / (parseInt (d.total) + 1) );
                        y1 = d.source.parent.y + _fragment +( _fragment * ( parseInt(d.extra.count) ) );
                    }else{
                        y1 = d.source.y + (d.source.height/2);
                    }
                    y2 = y1;
                    var arc1 = "",
                        arc2 = "",
                        sp = 0;

                    if(Math.abs(y1-y2)> 5){
                        if(y1 < y2){
                            arc1 = getArc(3,3,true,true);
                            arc2 = getArc(2,3,true,true);
                            sp = 3;
                        }else{
                            arc1 = getArc(4,3,false,true);
                            arc2 = getArc(1,3,false,true);
                            sp = -3;
                        }
                    }

                    var mid = Math.abs(x2-x1)/2;
                    _path = "M"+ x1 +","+ y1 + "H"+ ( x1 + mid - 6 )+ arc1 + "V" + ( y2 - sp ) + arc2 +"H"+ x2;
                    return _path
                }
/**
 * =====================================================================
 * Init the link object which bind links to svg element
 * Drawing process is divided in 3 part  Enter , Update , Remove
 * - linkEnter ( create the links append it to element to ui )
 * - linkUpdate ( update the link portions according to reflect data, If link is already present then no need to re-render them just update it. )
 * - linkRemove ( if link is no longer exist then remove it from ui )
 *
 * Note - If you change any style attribute at linkEnter make sure that it will not override by linkUpdate
 *        because after linkEnter, linkUpdate will update all the link attributes like path and color (for URI param).
 * ======================================================================
 */
                var link = chartLines.selectAll("g.extraLinks")
                    .data(linkData, function(d) { return d.id || (d.id = ++i); });

                var linkEnter = link.enter().append("svg:g")
                    .attr("class", "extraLinks");

                function dumpPath(d, path) {
                    //switch (d.type) {
                    //    case "URILink":
                    //    case "responseLink":
                    //    case "requestLink":
                    //        return path;
                    //}
                    // console.log("Path for " + d.type + ": " + path);
                    return path;
                }

                linkEnter.each(function(d){
                    d3.select(this)
                        .append("path")
                        .attr("class",function(d){
                            if(d.type == "referenceLink") return "link";
                            if(d.type == "externalReferenceLink") return "externalLink";
                            if(d.type == "responseLink") return "responseLink";
                            if(d.type == "requestLink") return "requestLink";
                            return "urlLinks";
                        })
                        .attr("id", d.id + "-lnk")
                        .attr("marker-end", "url(#arrowhead_rlink)")
                        .attr("marker-start", "url(#markerCircle_rlink)")
                        .attr("d",function(d){
                            var _path = "";
                            if(d.type == "referenceLink"){
                                return dumpPath(d, getComputedPath(d));
                            }
                            if(d.type == "URILink"){
                                return dumpPath(d, getURLPath(d,true));
                            }
                            if(d.type == "responseLink"){
                                return dumpPath(d, getResponsePath(d));
                            }
                            if(d.type == "requestLink"){
                                return dumpPath(d, getRequestPath(d));
                            }
                            if(d.type == "externalReferenceLink"){
                                return dumpPath(d, getExternalReferenceLinkPath(d));
                            }
                            return dumpPath(d, _path);
                        });
                        if(d.type == "referenceLink"){
                            var text = d3.select(this).append("text")
                             .attr("dx",-5)
                             .attr("dy",-3)
                             .append("textPath")
                             .attr("xlink:href","#" + d.id + "-lnk")
                             .style("text-anchor","end")
                             .attr("startOffset","100%")
                             .text(d.cardinality);
                        }
                });

                linkEnter.transition()
                    .duration(transitionDuration)
                    .style("opacity", 0);

//              Update the links with animation effect
                var linkUpdate = link.transition()
                    .duration(transitionDuration)
                    .style("opacity", 1);

                linkUpdate.each(function(d){

                     //responseLink
                    d3.select(this).select(".link").transition()
                        .duration(transitionDuration)
                        .attr("marker-end", "url(#arrowhead_rlink)")
                        .attr("marker-start", "url(#markerCircle_rlink)")
                        .attr("d",function(d){
                            return dumpPath(d, getComputedPath(d));
                        });

                    d3.select(this).select(".externalLink").transition()
                        .duration(transitionDuration)
                        .attr("marker-end", "url(#arrowhead_msg)")
                        .attr("marker-start", "url(#markerCircle_msg)")
                        .attr("d",function(d){
                            return dumpPath(d, getExternalReferenceLinkPath(d));
                        });

                    d3.select(this).select(".urlLinks")
                        .style("stroke",function(d){
                            return resourceUriLinkColors(d.extra.count);
                        })
                        .transition()
                        .duration(transitionDuration)

                        .attr("marker-end", "url(#markerV)")
                        .attr("marker-start", "url(#markerH)")
                        .attr("d",function(d){
                            return dumpPath(d, getURLPath(d,false));
                        });

                    d3.select(this).select(".responseLink").transition()
                        .duration(transitionDuration)
                        .attr("marker-end", "url(#arrowhead_msg)")
                        .attr("marker-start", "url(#markerCircle_msg)")
                        .attr("d",function(d){
                            return dumpPath(d, getResponsePath(d));
                        });

                    d3.select(this).select(".requestLink").transition()
                        .duration(transitionDuration)
                        .attr("marker-end", "url(#arrowhead_msg)")
                        .attr("marker-start", "url(#markerCircle_msg)")
                        .attr("d",function(d){
                            return dumpPath(d, getRequestPath(d));
                        });

                });

//              Exit links
                link.exit().transition()
                    .duration(transitionDuration)
                    .style("opacity", 1e-6)
                    .remove();
            };

            /**
             *  Chart update method called on resize and render
             *  source - selected node.
             *  isResize - is called from resize, Because on resize node may change the position
             * */
            chart.update = function(source,isResize) {
                if(!source) source = root;
                /**
                 * Generate the nodes from data flow will help to generates each node separately from data object.
                 * */
                var nodes = flow(root);
                rootObject = nodes[0];


                /**
                 * function for container color according to data objecttype
                 * */

//                function color(d) {
//                	var light_blue = "#dae8f5";
//                    var blue = "#c6dbef";
//                    var dark_blue = "#b2cfea";
//                    var color = dark_blue;
//                    switch (d.objecttype){
//	                    case "ObjectResource":  case "CollectionResource":
//	                    	color = light_blue;
//	                        break;
//	                    case "DataType":  case "Method":
//	                    	color = blue;
//	                        break;
//	                    default :
//                    }
//
//                    return color;
//                }

                /**
                 * Toggle children on click.
                 * Only allow those element which have child to collapse/expand
                 * */
                function click(d) {
                    if(d != root &&(d.objecttype != "Response" && d.objecttype != "Request")){
                        if (d.children) {
                            d._children = d.children;
                            d.children = null;
                        } else {
                            d.children = d._children;
                            d._children = null;
                        }
                        resize(d,false);
                    }
                }

                function clickSelection(d) {
                    if (d.objecttype === 'Method') {
                        chartChangeSelection(d.parent.id + "." + d.name + "." + d.type, d.objecttype);
                    } else {
                        chartChangeSelection(d.id, d.objecttype);
                    }
                    svg.selectAll('.selected').classed('selected', false);
                    d3.select(this).classed('selected', true);
                }


                /**
                 * First searches children of 'host' for the first matched 'childFilter'.
                 * Only when one is found, executes the 'pathSelector' to find or create a svg:path
                 * and updates its "d" attribute to place the port as follows:
                 * - horizontally: at the right side of the 'host'
                 * - vertically: at the center of the child's visuals.
                 *
                 * @return d3-selection for svg:path or null
                 */
                function relocateRequestResponsePortCenteredAt(host, pathSelector, childFilter) {
                    var minTopMaxBottom = DiagramSpecifics.collectChildrenVerticalMinMax(host, childFilter);
                    if (!minTopMaxBottom) {
                        return null;
                    }

                    var anchorMiddle = minTopMaxBottom.getMiddle();
                    var result = pathSelector()
                        .attr("d", function (d) {
                            var w = 20, h = 15, _r = 3;

                            var x1 = d.width - 1;
                            //we need host-local coordinates since path is child of host' svg:g
                            var anchorMiddleLocal = anchorMiddle - d.y;
                            var y1 = anchorMiddleLocal - h / 2;

                            var path = "M" + x1 + "," + y1
                                + "h" + (w - _r)
                                + "a" + _r + "," + _r + " 0 0 1 " + _r + "," + _r
                                + "v" + (h - 2 * _r)
                                + "a" + _r + "," + _r + " 0 0 1 " + (_r*-1) + "," + _r
                                + "h" + (_r - w);
                            return path;
                        });
                    return result;
                }

                /**
                 * =====================================================================
                 * Init the node object which bind nodes to svg element
                 * Drawing process is divided in 3 part  Enter , Update , Remove
                 * - nodeEnter ( create the node append it to element to ui )
                 * - nodeUpdate ( update the node portions according to reflect data, If link is already present then no need to re-render them just update it. )
                 * - nodeRemove ( if ndoe is no longer exist then remove it from ui )
                 *
                 * Note - If you change any style attribute at nodeEnter make sure that it will not override by nodeUpdate
                 *        because after nodeEnter, nodeUpdate will update all the link attributes like color (for rect container fill).
                 * ======================================================================
                 */

                var node = chartGroup.selectAll("g.node")
                    .data(nodes, function(d) { return d.id || (d.id = ++i); });

                var nodeEnter = node.enter().append("svg:g")
                    .attr("class", "node")
                    .attr("transform", function(d) {
                        return "translate(" + source.x + "," + source.y + ")";
                    })
                    .style("opacity", 1e-6);

                /**
                 * Enter any new nodes at the parent's previous position.
                 * append rectangle to node
                 *
                 * */
                nodeEnter.each(function(d) {
                    /**
                     * return for root node we don't want to show main container.
                     * */
                    if(d == root){
                        return;
                    }
                    /**
                     * Append rectangle for each node
                     * */
                    if (d.children
                            || d._children
                            || d.objecttype == "Response"
                            || d.objecttype == "Request"
                            || d.objecttype == "ReferenceEmbed"
                            || ((d.objecttype == "DataType" || d.objecttype == "ReferenceLink")  && (!d.children && !d._children))) {

                        d3.select(this).append("svg:rect")
                            .attr("anchorId", d.anchorId)
                            .attr("class", DiagramSpecifics.getContainerBackgroundClass)
                            .attr("height", function(d) { return d.height; })
                            .attr("width", function(d) { return d.width; })
                            .attr("rx", 2)
                            .attr("ry", 2);

                            d3.select(this).select('rect.method').on("click", clickSelection);
                            d3.select(this).select('rect.resource').on("click", clickSelection);
                            d3.select(this).select('rect.dataType').on("click", clickSelection);

                        if(d.objecttype == "ReferenceEmbed"){
                            d3.select(this).select('rect').style('fill',function(){
                                   return referenceEmbedColors(d.embedLevel);
                            });
                        }

                        /**
                         *  Add request/response Port on method container
                         * */
                        if(d.objecttype == "Method"){
                            var d3this = d3.select(this);
                            relocateRequestResponsePortCenteredAt(d,
                                function() {
                                    return d3this
                                        .append("path")
                                        .attr("class", "requestPort");
                                },
                                function (f) {
                                    return f.objecttype == "Request";
                                });
                            relocateRequestResponsePortCenteredAt(d,
                                function() {
                                    return d3this
                                        .append("path")
                                        .attr("class", "responsePort");
                                },
                                function (f) {
                                    return f.objecttype == "Response";
                                });
                        }
                    }

                });

                /**
                 * Append header separator, header text , header image of container
                 * */
                nodeEnter.each(function(d) {
                    if(d == root){
                        return;
                    }
                    if (d.children || d._children) {
                        // draw a horizonal line for containers to separate the header and contents
                        if(d.objecttype != "Response" && d.objecttype != "Request" ){
                            //FIXME: revisit, I never seen this line anyway
                            d3.select(this).append("line")
                                .attr("class", "separator")
                                .style("stroke-opacity",function(d){
                                    return d._children ? "0" : "0.5";
                                })
                                .attr("x1","0")
                                .attr("y1",function(d){
                                    if(d.objecttype == "ObjectResource"  || d.objecttype == "CollectionResource")return 30;
                                    return 20;
                                })
                                .attr("x2", function(d){return d.width;})
                                .attr("y2",function(d){
                                    if(d.objecttype == "ObjectResource"  || d.objecttype == "CollectionResource")return 30;
                                    return 20;
                                });

                            d.viewmap.applyToDom(this);

                            var expanderBlock = d3.select(this).select(".expander-group");
                            expanderBlock.attr("pointer-events", "all");

                            expanderBlock.append("rect")
                            	.attr("class", "expander-clicks-capture")
                            	.style("visibility", "hidden")
                                .attr("x", 0)
                                .attr("y", 0)
                                .attr("width", 12)//FIXME: hardcoded value should match the one from viewmap
                                .attr("height", 16)
                            ;

                            expanderBlock.append("path")
                                .attr("class", "expander")
                                .attr("d", "M 0 0 L 6 6 L 0 6 z")
                                .attr("transform", function(d) {
                                    return d._children ? "translate(6,14)rotate(225)" : "translate(5,8)rotate(315)";
                                });
						}

                    } else {
                    	// non-containers
                        d.viewmap.applyToDom(this);
                    }
                });

                /**
                 * Transition nodes to their new position.
                 * */
                nodeEnter.transition()
                    .duration(transitionDuration)
                    .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; })
                    .style("opacity", 1);

                var nodeUpdate = node.transition()
                    .duration(transitionDuration);

                nodeUpdate.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; })
                    .style("opacity", 1);

                /**
                 * On update node ( collapse/expand ) change header separator and image
                 * */
                nodeUpdate.each(function(d) {
                    if (d.children || d._children) {
                        d3.select(this).select(".expander-group").on("click", click);
                        d3.select(this).select(".expander").transition()
                            .duration(transitionDuration)
                            .attr("transform", function(d) {
                                //FIXME: translate value depends on insets because we don't have a group
                                return d._children ? "translate(6,14)rotate(225)" : "translate(5,8)rotate(315)";
                            });

                        d3.select(this).select(".separator").transition()
                            .duration(transitionDuration)
                            .style("stroke-opacity",function(d){
                                return d._children ? "0" : "0.5";
                            })
                            .attr("x1","0")
                            .attr("y1",function(d){
                                if(d.objecttype == "ReferenceLink" && d.linkRelation && d.linkRelation.length > 0 )return 30;
                                if(d.objecttype == "ObjectResource" || d.objecttype == "CollectionResource")return 30;
                                return 20;
                            })
                            .attr("x2", function(d){return d.width;})
                            .attr("y2",function(d){
                                if(d.objecttype == "ReferenceLink" && d.linkRelation && d.linkRelation.length > 0 )return 30;
                                if(d.objecttype == "ObjectResource" || d.objecttype == "CollectionResource")return 30;
                                return 20;
                            });

                        var requestPort = d3.select(this).selectAll(".requestPort");
                        requestPort.transition()
                            .duration(transitionDuration)
                            .style("fill-opacity",function(d){
                                return d._children ? 0 : 1;
                            })
                            .style("stroke-width",function(d){
                                return d._children ? "0" : "1.5";
                            })
                            .style("stroke-opacity",function(d){
                                return d._children ? "0" : "0.5";
                            });

                        relocateRequestResponsePortCenteredAt(d,
                            function() {return requestPort; },
                            function(f) { return f.objecttype == "Request"; });

                        var responsePort = d3.select(this).selectAll(".responsePort");
                        responsePort.transition()
                            .duration(transitionDuration)
                            .style("fill-opacity",function(d){
                                return d._children ? 0 : 1;
                            })
                            .style("stroke-width",function(d){
                                return d._children ? "0" : "1.5";
                            })
                            .style("stroke-opacity",function(d){
                                return d._children ? "0" : "0.5";
                            });

                        relocateRequestResponsePortCenteredAt(d,
                            function() {return responsePort; },
                            function(f) { return f.objecttype == "Response"; });
                    }

                });

                nodeUpdate.select(".background")
                    .attr("height", function(d) { return d.height; })
                    .attr("width", function(d) { return d.width; });

                // Transition exiting nodes to the parent's new position.
                node.exit().transition()
                    .duration(transitionDuration)
                    .attr("transform", function(d) { return "translate(" + source.x + "," + source.y + ")"; })
                    .style("opacity", 1e-6)
                    .remove();

                /*
                *  Update Extra links layer when resize will take delay so node take
                *  it's position properly
                * */
                if(isResize){
                    var _this = this;
                    this.updateExtraLinks(nodes);
                    setTimeout(function(){
                        _this.updateExtraLinks(nodes);
                    },transitionDuration);
                }else{
                    this.updateExtraLinks(nodes);
                }

            };

            /**
             * first resize call with all data element
             * */
            resize(root,true); /** First attemp to create chart */
            chart.update(flow.updateNodesFromSettings(root),true);  /** Update chart according to visibility control */
            /**
             * Last resize after everything setup
             * */
            resize(rootObject,false);


        });
    }

    chart.width = function(value) {
        if (!arguments.length) return width;
        width = parseInt(value);
        return this;
    };

    chart.height = function(value) {
        if (!arguments.length) return height;
        height = parseInt(value);
        return this;
    };

    chart.margin = function(_) {
        if (!arguments.length) return margin;
        margin.top    = typeof _.top    != 'undefined' ? _.top    : margin.top;
        margin.right  = typeof _.right  != 'undefined' ? _.right  : margin.right;
        margin.bottom = typeof _.bottom != 'undefined' ? _.bottom : margin.bottom;
        margin.left   = typeof _.left   != 'undefined' ? _.left   : margin.left;
        return chart;
    };

    chart.updateSetting = function(){
        chart.resizeChart(flow.updateNodesFromSettings(root),false);
    };

    return chart;
};