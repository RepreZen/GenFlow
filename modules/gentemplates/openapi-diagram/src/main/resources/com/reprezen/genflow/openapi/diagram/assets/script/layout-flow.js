/**
 * FIXME: this comment is probably outdated (old layouting code had been refactored under #2274)
 *
 * flow layout object
 * variables
 * hierarchy - d3 hierarchy method will help to find data hierarchy
 * nodeWidth - default layout width ( if missing from parameters )
 * nodeHeight - default layout height ( if missing from parameters )
 * containerHeight - default node height ( if missing from parameters )
 * containerWidth - default node width ( if missing from parameters )
 * width - default flow height ( if missing from parameters )
 * height - default flow height ( if missing from parameters )
 * padding - default padding ( if missing from parameters )
 * margin - default padding ( if missing from parameters )
 * */
d3.custom.layout.flow = function() {

    var hierarchy = d3.layout.hierarchy().sort(null).value(null),
        nodeWidth = 300,
        nodeHeight = 100,
        containerHeight = 20,
        containerWidth = 300,
        linkPadding = 6,
        width = 900,
        height = 0,
        padding = {top:20, left:10, bottom:10, right:10},
        margin = {top:10, left:10, bottom:10, right:10};

    function flow(d, i) {
        /**
         * Basic hierarchy going to find out for key "children".
         * we override d3.js's hierarchy method because in out data we don't have children key .
         * In out override method we decide by our self which are the children of current node by their type.
         * */
        var nodes = hierarchy
                .children(DiagramSpecifics.computeChildren)
                .call(this, d, i),

            root = nodes[0];

        function assertOldLayoutCodeOnlyForResourceAPI(node) {
            if (node.objecttype != 'ResourceAPI') {
                throw "Old layout code should only work for diagram canvas: " + node.objecttype;
            }
        }

        /**
         * First walk will create node container for each node in data
         * */
        function firstWalk(node) {
            //FIXME: now there are no need to be recursive here in firstWalk
            var layoutManager =
                node.objecttype == "ResourceAPI" ? null : LayoutModule.ViewMapGate._getLayoutManager(node);
            if (layoutManager != null) {
                layoutManager.pack(node);
                return;
            }

            assertOldLayoutCodeOnlyForResourceAPI(node);

            // old layout code starts here

            var children = node.children;
            if (children && children.length > 0) {
                var n = children.length,
                    i = -1,
                    child;

                while (++i < n) {
                    child = children[i];
                    firstWalk(child);
                }
                gridLayout(node, children,1 );

            } else {

                /**
                 * If there is no child then set default height and width of attribute or node
                 * */
                node.width = node._children ? containerWidth - ( 2 * (padding.left + padding.right)) : nodeWidth;
                node.height = node._children ? containerHeight : nodeHeight;

                var _debugVisibleString = DiagramSpecifics.getVisibleNodeString(node);
                var textWidth = ViewmapUtils.createSVGtext(_debugVisibleString).width;
                // console.log("firstWalk: " + _debugVisibleString + " width: " + textWidth);
                if(textWidth > node.width ){
                    if(node._children){
                        node.width = textWidth + ( 1 * ( padding.left + padding.right + 10 ));
                    }else if(node.objecttype == "Response" || node.objecttype == "Request"){
                        // Response and Request have visual children (labels) which are not 'children'
                        node.width = textWidth + ( 1 * ( padding.left + padding.right));
                    } else {
                        node.width = textWidth;
                    }
                }

                if(node._references && node._references > 1){
                    var _expectedHeight = node._references * linkPadding;
                    if(node.height < _expectedHeight){
                        node.height = _expectedHeight + margin.top;
                    }
                }

                if(node._isTarget){
                    if(node._expectedWidth){
                        if(node.width < node._expectedWidth){
                            node.width = node._expectedWidth + padding.right;
                        }
                    }

                    if(node._expectedHeight){
                        if(node.height < node._expectedHeight){
                            node.height = node._expectedHeight + margin.top;
                        }
                    }
                }

            }
        }

        /**
         *  Second walk is to transform parent-local positions (x, y) to absolute positions on canvas
         *  For every child node we recursively adjusting for its semantic-parent positions
         *  and (when needed) for content-pane non-semantic location
         * */
        function secondWalk(node) {
            var children = node.children;
            if (children && children.length > 0) {
                var i = -1,
                    n = children.length,
                    child;
                while (++i < n) {
                    child = children[i];
                    child.x += node.x;
                    child.y += node.y;

                    if (node.viewmap) {
                        var contentPaneAdjustment = node.viewmap.getContentPaneAdjustment(child);
                        if (contentPaneAdjustment) {
                            child.x += contentPaneAdjustment.getX();
                            child.y += contentPaneAdjustment.getY();
                        }
                    }


                    secondWalk(child);
                }
            }
        }






        /**
         * Create node container according to it's children's
         * */
        function gridLayout(node, children, depth) {

            assertOldLayoutCodeOnlyForResourceAPI(node);

            var paddingValue = node.parent ? padding.left + padding.right : margin.left + margin.right;
            var availableWidth = containerWidth - (depth * (padding.left + padding.right)),
                currentX = padding.left,
                currentY = padding.top,
                tallestChildHeight = 0;

            var maxWidth = availableWidth;
            var calculatedWidth;

            var te = ViewmapUtils.createSVGtext(DiagramSpecifics.getVisibleNodeString(node));

            calculatedWidth = te.width + (depth * (padding.left + padding.right)) + 20;// add extra for icon and arrow

            if(node.objecttype == "ObjectResource" || node.objecttype == "CollectionResource"){
                if(node._isTarget){
                    if(node._expectedWidth){
                        if(calculatedWidth < node._expectedWidth){
                            calculatedWidth = node._expectedWidth + ( depth * (padding.left + padding.right)) + 30;
                        }
                    }
                }
            }

            if(node.objecttype == "ObjectResource" || node.objecttype == "CollectionResource"){
                currentY = 50;
            }

            //if( node.objecttype == "ReferenceLink" && node.linkRelation && node.linkRelation.length > 0 ) {
            //    currentY = currentY + 10;
            //}

            if(calculatedWidth > maxWidth){
                maxWidth = calculatedWidth - (depth * (padding.left + padding.right));
            }

            if(node.objecttype == "ResourceAPI"){
                availableWidth = width - (depth * (padding.left + padding.right));
            }else{

                for(_c in children){
                    var _ch = children[_c];
                    if(_ch.width > maxWidth){
                        maxWidth = _ch.width;// + (depth * (padding.left + padding.right));
                    }
                }

                // logForNode('>>>>> Max Child Width ', node, ' ' + maxWidth);

                /**
                 * Go to the leaf child and set width ( ZEN-494,504 )
                 * */
                function setWidth(d,_width) {
                    // logForNode('>>>>> setWidth ', d, ', requestedWidth: ' + _width + ', grid-node: ' + nodeToString(node));
                    var _nChildren = d.children;
                    if (_nChildren && _nChildren.length > 0) {
                        var i = -1,
                            n = _nChildren.length,
                            child;
                        while (++i < n) {
                            child = _nChildren[i];
                            var _nWidth = _width - (depth * (padding.left + padding.right));
                            // if(d.objecttype == "Method") _nWidth = _nWidth - ( depth * ( padding.right ));
                            setWidth(child,_nWidth);
                        }
                    }

                    // if(d!= node){
                         // logForNode('<<<<< setWidth ', d, ': ' + d.width + ' ---> ' + _width);
                         d.width = _width;
                    // }
                }
                setWidth(node,maxWidth + (padding.left + padding.right));
            }

            if(node.objecttype == "Response"|| node.objecttype == "Request"){
                if(!node.statusCode) currentY = 0;
                else currentY = margin.bottom + margin.top;
            }
            /**
             *
             * Looking for top padding
             * While creating links if links going up side in minus
             * add more top padding
             * I will work only for first row where links may go in minus
             *
             * */
            if(node._topPadding){
                var _padding = node._topPadding;
                if(_padding <= 0){
                    if(-1*( _padding ) > margin.top){
                        currentY = -1*( _padding );
                    }else{
                        currentY = -1*( _padding ) + margin.top;
                    }
                }
            }

            var URIParamCount = 1;
            //if(node.objecttype == "ObjectResource" || node.objecttype == "CollectionResource"){
            //    var URIParams = flow.getURIParameterList(node);
            //    if(URIParams) URIParamCount += URIParams.length;
            //}

            if(node.objecttype == "ObjectResource" || node.objecttype == "CollectionResource" || node.objecttype == "DataType"){
                var _node = node.objecttype == "DataType" ? node.parent : node;
                var _numberOfReferences = flow.getReferences(_node).length;
                node._references = _numberOfReferences;
            }
            var _topPadding = null;

            var lastPadding = 0;
            children.forEach(function(child) {
                if ((currentX + child.width + padding.right) >= availableWidth) {
                    currentX = padding.right;
                    currentY += tallestChildHeight;
                    tallestChildHeight = 0;
                    // Set child of ObjectResource according to URI parameter binding current top padding is 30 so check for 4 ( 4 * 20 ) 10px is for header URL
                    // and padding left is 10 so check for 2 parameter ( each link take padding of 5 px )
                    if( (node.objecttype == "ObjectResource" || node.objecttype == "CollectionResource") && URIParamCount > 2 ) currentX += 5 * ( URIParamCount - 2);
                    if( (node.objecttype == "ObjectResource" || node.objecttype == "CollectionResource") && URIParamCount > 4 ) currentY += 5 * ( URIParamCount - 4);
                    // set child of req/res to left side
                    if(node.objecttype == "Response"|| node.objecttype == "Request" ) currentX = 0;
                    if(node.objecttype == "ResourceAPI") currentY += margin.top;
                    lastPadding = 0;
                }

                child.x = currentX;
                child.y = currentY;
                /**
                 * Calculating last left padding if node have n links outgoing
                 * then will need more padding on left to neighbor node
                 * So depends on number of links add last padding so next node
                 * will make sure that how many padding it needed on left side
                 * */
                if(child.objecttype == "ObjectResource" || child.objecttype == "CollectionResource"){
                    if(lastPadding > 0){
                        child.x += lastPadding;
                    }
                    var _numberOfReferences = child._references;
                    if(_numberOfReferences > 2){
                        lastPadding = ( (_numberOfReferences-1) * linkPadding);
                    }
                }

                currentX += child.width + padding.right;
                if(node == root){
                    currentX += ( padding.left + padding.right + 10);
                }
                tallestChildHeight = Math.max(tallestChildHeight, child.height + padding.bottom );
            });

            node.width = maxWidth + ( depth * (padding.left + padding.right));
            node.height = currentY + tallestChildHeight;

            if(node.objecttype == "ObjectResource" || node.objecttype == "CollectionResource" ){
                node.height = node.height + 10;

                if(node._isTarget){
                    if(node._expectedWidth){
                        if(node.width < node._expectedWidth){
                            node.width = node._expectedWidth + padding.right;
                        }
                    }

                    if(node._expectedHeight){
                        if(node.height < node._expectedHeight){
                            node.height = node._expectedHeight + margin.top;
                        }
                    }
                }

            }

//            if( node.objecttype == "ReferenceLink" && node.linkRelation && node.linkRelation.length > 0 ) {
//                node.height = node.height + 10;
//            }

            if( (node.objecttype == "ObjectResource" || node.objecttype == "CollectionResource") && URIParamCount > 2 ) node.width += 5 * ( URIParamCount - 2);

            node.x = node.parent ? padding.left : margin.left;
            node.y = node.parent ? padding.top  : margin.top;
        }

        firstWalk(root);
        secondWalk(root);

        //FIXME: this code below added to fix the ZEN-2376
        //the old layout code called this from gridLayout (!) as a routine unrelated code for size computation
        //var _numberOfReferences = flow.getReferences(_node).length;
        //however it looks like to display colors correctly we need #embedLevel property to be set for ReferenceEmbed's
        //and this is what getReferences() do as a sub-product
        //FIXME: getReferences() logic is complicated and I don't see how to extract the logic for embedLevel from it
        //FIXME: but this should be done someday
        (root.children || []).forEach(function (d) {
            //FIXME: copy pasted from old layout code, questionable itself
            if (d.objecttype == "ObjectResource" || d.objecttype == "CollectionResource" || d.objecttype == "DataType") {
                var references = flow.getReferences(d.objecttype == "DataType" ? d.parent : d);
                d._references = references.length;
            }
        });
        //FIXME: end of ZEN-2376

        height = root.height;

        return nodes;
    }

    flow.padding = function(_) {
        if (!arguments.length) return padding;
        padding.top    = typeof _.top    != 'undefined' ? _.top    : padding.top;
        padding.right  = typeof _.right  != 'undefined' ? _.right  : padding.right;
        padding.bottom = typeof _.bottom != 'undefined' ? _.bottom : padding.bottom;
        padding.left   = typeof _.left   != 'undefined' ? _.left   : padding.left;
        return this;
    };

    flow.margin = function(_) {
        if (!arguments.length) return margin;
        flow.top    = typeof _.top    != 'undefined' ? _.top    : flow.top;
        flow.right  = typeof _.right  != 'undefined' ? _.right  : flow.right;
        flow.bottom = typeof _.bottom != 'undefined' ? _.bottom : flow.bottom;
        flow.left   = typeof _.left   != 'undefined' ? _.left   : flow.left;
        return this;
    };

    flow.width = function(value) {
        if (!arguments.length) return width;
        width = parseInt(value);
        return this;
    };

    flow.height = function(value) {
        if (!arguments.length) return height;
        height = parseInt(value);
        return this;
    };

    flow.nodeWidth = function(value) {
        if (!arguments.length) return nodeWidth;
        nodeWidth = parseInt(value);
        return this;
    };

    flow.nodeHeight = function(value) {
        if (!arguments.length) return nodeHeight;
        nodeHeight = parseInt(value);
        return this;
    };

    flow.containerWidth = function(value){
        if (!arguments.length) return containerWidth;
        containerWidth = parseInt(value);
        return this;
    };

    flow.containerHeight = function(value) {
        if (!arguments.length) return containerHeight;
        containerHeight = parseInt(value);
        return this;
    };

    flow.linkPadding = function(value) {
        if (!arguments.length) return linkPadding;
        linkPadding = parseInt(value);
        return this;
    };

    flow.updateNodesFromSettings = function(rootNode){
        var _gSetting = JSON.parse(getGlobalSetting());
        function isCollapsed(d){
            var value = null;
            var type = d.objecttype;

            switch (type){
                case "ObjectResource":case "CollectionResource":
                    value = _gSetting.R ? false : true;
                    break;
                case "DataType":
                    value = _gSetting.DT ? false : true;
                    break;
                case "ReferenceLink":
                    value = _gSetting.RL ? false : true;
                    break;
                case "Method":
                    value = _gSetting.MTD ? false : true;
                    break;
                case "Response":
                case "Request":
                    // we don't have .MSG configuration, so Response / Request are NEVER collapsed
                    value = false;
                    break;
                default :
                    value = null;
            }
            return value;
        }

        function collapse(d) {
            if (d.children) {
                d.children.forEach(collapse);
            }else{
                if(d._children) d._children.forEach(collapse);
            }
            if(isCollapsed(d)){
                if(d.children){
                    d._children = d.children;
                    d.children = null;
                }
            }else{
                if(d._children){
                    d.children = d._children;
                    d._children = null;
                }
            }
        }
        rootNode.children.forEach(collapse);
        return rootNode;
    };

    flow.getReferences = function(node,nodes){
        var extraLinkData = [];
        var _resource = node;
        var __target = null;
        var __resourceChild = [];
        var isCollapsed = false;

        if(_resource.children){
            __resourceChild = _resource.children;
        }else{
            if(_resource._children){
                __resourceChild = _resource._children;
                __target = _resource;
                isCollapsed = true;
            }
        }

        var _dataTypes = __resourceChild.filter(function(d){
            return d.objecttype == "DataType";
        });

        /**
         * Reference links
         */
        var _references = [];

        if(_dataTypes.length > 0){
            var __child = [];
            var _dataType = _dataTypes[0];

            if(_dataType.children){
                __child = _dataType.children;
            }else{
                if(_dataType._children){
                    __child = _dataType._children;
                    if(!__target) __target = _dataType;
                    isCollapsed = true;
                }
            }

            /**
             * Recursively add reference links to the _references list;
             * Calculate embedLevel property for reference embeds.
             */
            function _recursive(_ChildNode){
                if(_ChildNode.parent.objecttype == "ReferenceEmbed"){
                    _ChildNode.embedLevel = _ChildNode.parent.embedLevel + 1;
                }else{
                    _ChildNode.embedLevel = 0;
                }
                if(_ChildNode.objecttype == "ReferenceLink"){
                    _references.push(_ChildNode);
                }
                if(_ChildNode.objecttype == "ReferenceEmbed"){
                    var _embChild = [];
                    if(_ChildNode.children){
                         _embChild = _ChildNode.children;
                    }else{
                        if(_ChildNode._children){
                            _embChild = _ChildNode._children;
                            if(!__target) __target = _ChildNode;
                            isCollapsed = true;
                        }

                    }
                    if(_embChild.length > 0 ){
                        for(_eachChild in _embChild){
                            _recursive(_embChild[_eachChild]);
                        }
                    }
                }
            }


            for(_eachChild in __child){
                if(__child[_eachChild].objecttype == "ReferenceEmbed" || __child[_eachChild].objecttype == "ReferenceLink"){
                    _recursive(__child[_eachChild]);
                }

            }

//            var _references = __child.filter(function(d){
//                return d.objecttype == "ReferenceLink";
//            });
        } else if (_resource.children && _resource.children[0].objecttype == "ReferenceLink") {
            _references.push(_resource.children[0]);
        }

        if(_references.length > 0){
            var _pad = 0;
            if(nodes){
                for( _referenceIndex in _references){
                    var _reference = _references[_referenceIndex];

                    var _resultRes = nodes.filter(function(v){
                        return v.id == _reference.referencedResourceId;
                    });

                    if(_resultRes.length > 0){
                        var _linkObject = {};
                        _linkObject.id = _reference.id+"_"+_resultRes[0].id;
                        _linkObject.source = __target ? __target : _reference;
                        _linkObject.target =  _resultRes[0];
                        _linkObject.source_resource= _resource;
                        _linkObject.type = "referenceLink";
                        _linkObject.cardinality = _reference.cardinality;
                        _linkObject.extra = {};
                        _linkObject.extra.count = _referenceIndex;
                        _linkObject.extra.pad = _pad;
                        _linkObject.isCollapsed = isCollapsed;
                        _linkObject.total =  _references.length;
                        extraLinkData.push(_linkObject);
                        _pad += 5;
                    } else { // create unconnected external reference link
                        var _linkObject = {};
                        _linkObject.id = _reference.id+"_ext";
                        _linkObject.source = _reference;
                        _linkObject.target = _reference;
                        _linkObject.isCollapsed = isCollapsed;
                        _linkObject.total =  _references.length;
                        _linkObject.extra = {};
                        _linkObject.extra.count = _referenceIndex;
                        _linkObject.extra.pad = _pad;
                        _linkObject.type = "externalReferenceLink";
                        extraLinkData.push(_linkObject);
                    }
                }
            }else{
                extraLinkData = _references;
            }
        }
        return extraLinkData;
    };

    return flow;
};