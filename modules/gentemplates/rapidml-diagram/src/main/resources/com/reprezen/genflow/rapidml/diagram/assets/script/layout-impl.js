var LayoutModule = {

    newColumnLayout: function (vertical) {
        return new LayoutModule.ColumnLayout(LayoutModule.ViewMapGate, vertical);
    },

    /*
     * This is a configurable part of ColumnLayout manager containing all its communication with its environment
     * (thus the name "gate"). E.g, the gate decides how to extract
     * - the sizes from 'nodes',
     * - what are the child layouts,
     * - insets, etc.
     * <p/>
     * At the end of layout refactoring for ZEN-2724 there is only single "gate" implementation left, as expected.
     * Still, separation between layout algorithm and its environment is a good thing,
     * - it allows to reuse ColumnLayout in different environment
     * - it allows to implement different layout algorithms (BorlderLayout?) operating with the same environment
     */
    ViewMapGate: {
        $debugName: 'ViewMapGate',

        _replaceSemanticWithFigure: function (semanticOrFigure) {
            if (semanticOrFigure.objecttype) {
                //FIXME: revisit: here is the place where semantic is replaced with figure
                if (!semanticOrFigure.viewmap) {
                    throw "Semantic element with viewmap expected: " + semanticOrFigure.objecttype;
                }
                return semanticOrFigure.viewmap;
            }
            return semanticOrFigure;
        },

        _assertFigure: function (figure) {
            figure = this._replaceSemanticWithFigure(figure);
            if (figure instanceof Viewmaps._Internal.BaseFigure) {
                return figure;
            }
            throw "Figure expected : " + figure.constructor.name;
        },

        _assertContainerFigure: function (parent) {
            parent = this._replaceSemanticWithFigure(parent);
            if (parent instanceof Viewmaps._Internal.BaseContainerFigure) {
                return parent;
            }
            throw "Unexpected parent: " + parent.constructor.name;
        },

        _getChildren: function (parent) {
            return this._assertContainerFigure(parent).getChildrenFigures();
        },

        _setBounds: function (figure, bounds) {
            var target = this._assertFigure(figure);
            //console.log("_setBounds: " + target.datum.objecttype + ":" + target.datum.id
            //    + ", figureId: " + (target.debugKind || "") + "$" + target.debugId
            //    + (target.isSemanticRoot() ? "(Root)" : "")
            //    + " -> " + bounds.toDebugString());
            return target.setBounds(bounds);
        },

        _getClientArea: function (parent) {
            return this._assertContainerFigure(parent).getClientArea();
        },

        _getChildPreferredSize: function (child) {
            child = this._replaceSemanticWithFigure(child);
            if (!child.getPreferredSize) {
                throw "Unexpected child: " + child.constructor.name;
            }
            return child.getPreferredSize();
        },

        _getChildMinimumSize: function (child) {
            return this._getChildPreferredSize(child);
        },

        _getInsets: function (parent) {
            return this._assertContainerFigure(parent).getInsets();
        },

        _getLayoutManager: function (figure) {
            return this._assertFigure(figure).getLayoutManager();
        }
    },

    /**
     * @param gate see LayoutModule.ViewMapGate below
     * @param vertical true for vertical of false for horizontal layout
     * @constructor
     */
    ColumnLayout: function (gate, vertical) {
        this.gate = gate;
        this.vertical = vertical;
        this.transposer = new Draw2D.Transposer(!vertical);
        this.spacing = 0;
        this.minorAlignment = Draw2D.Alignment.FILL;

        this.setSpacing = function (s) {
            this.spacing = s;
        };

        this.setMinorAlignment = function (align) {
            this.minorAlignment = align;
        };

        this.transpose = function (t) {
            return this.transposer.transpose(t);
        };

        this.getChildren = function (datum) {
            return this.gate._getChildren(datum);
        };

        this.layout = function (datum) {
            var children = this.getChildren(datum) || [];
            var numChildren = children.length;
            if (numChildren == 0) {
                return;
            }

            var rawClientArea = this.gate._getClientArea(datum);
            var clientArea = this.transpose(rawClientArea);
            var x = clientArea.getX();
            var y = clientArea.getY();
            var availableHeight = clientArea.getHeight();

            var prefSizes = new Array(numChildren);
            var minSizes = new Array(numChildren);

            // Calculate the width and height hints. If it's a vertical ToolBarLayout,
            // then ignore the height hint (set it to -1); otherwise, ignore the
            // width hint. These hints will be passed to the children of the parent
            // figure when getting their preferred size.
            var wHint = -1;
            var hHint = -1;
            if (this.vertical) {
                wHint = rawClientArea.getWidth();
            } else {
                hHint = rawClientArea.getHeight();
            }

            /*
             * Calculate sum of preferred heights of all children(totalHeight).
             * Calculate sum of minimum heights of all children(minHeight). Cache
             * Preferred Sizes and Minimum Sizes of all children.
             *
             * totalHeight is the sum of the preferred heights of all children
             * totalMinHeight is the sum of the minimum heights of all children
             * prefMinSumHeight is the sum of the difference between all children's
             * preferred heights and minimum heights. (This is used as a ratio to
             * calculate how much each child will shrink).
             */
            var totalHeight = 0;
            var totalMinHeight = 0;

            for (var i = 0; i < numChildren; i++) {
                var child = children[i];

                prefSizes[i] = this.transpose( //
                    this.gate._getChildPreferredSize(child, wHint, hHint));
                minSizes[i] = this.transpose( //
                    this.gate._getChildMinimumSize(child, wHint, hHint));

                totalHeight += prefSizes[i].getHeight();
                totalMinHeight += minSizes[i].getHeight();
            }

            totalHeight += (numChildren - 1) * this.spacing;
            totalMinHeight += (numChildren - 1) * this.spacing;

            // TODO: [MG] original D2D code tries to shrink the children if needed
            // TODO: we probably will NOT need this
            var prefMinSumHeight = totalHeight - totalMinHeight;
            /*
             * The total amount that the children must be shrunk is the sum of the
             * preferred Heights of the children minus Max(the available area and
             * the sum of the minimum heights of the children).
             *
             * amntShrinkHeight is the combined amount that the children must shrink
             * amntShrinkCurrentHeight is the amount each child will shrink
             * respectively
             */
            var amntShrinkHeight = totalHeight - Math.max(availableHeight, totalMinHeight);
            if (amntShrinkHeight < 0) {
                amntShrinkHeight = 0;
            }

            for (var i = 0; i < numChildren; i++) {
                var amntShrinkCurrentHeight = 0; //TODO : shrinking here
                var prefHeight = prefSizes[i].getHeight();
                var minHeight = minSizes[i].getHeight();
                var prefWidth = prefSizes[i].getWidth();
                var minWidth = minSizes[i].getWidth();

                var newWidth = prefWidth;
                var newHeight = prefHeight;
                if (this.minorAlignment == Draw2D.Alignment.FILL) {
                    newWidth = clientArea.getWidth();
                }

                if (prefMinSumHeight != 0) {
                    //TODO : shrinking here
                    amntShrinkCurrentHeight = //
                        (prefHeight - minHeight) * amntShrinkHeight / (prefMinSumHeight);

                    newHeight -= amntShrinkCurrentHeight;
                    amntShrinkHeight -= amntShrinkCurrentHeight;
                    prefMinSumHeight -= (prefHeight - minHeight);
                }

                var adjust = clientArea.getWidth() - newWidth;
                switch (this.minorAlignment) {
                    case Draw2D.Alignment.TOP_LEFT:
                        adjust = 0;
                        break;
                    case Draw2D.Alignment.CENTER:
                        adjust /= 2;
                        break;
                    case Draw2D.Alignment.BOTTOM_RIGHT:
                    case Draw2D.Alignment.FILL:
                        break;
                }

                var newBounds = new Draw2D.Rectangle(x + adjust, y, newWidth, newHeight);
                this.gate._setBounds(children[i], this.transpose(newBounds));

                // FIXME: Revisit: Draw2D relies on the separate validate() calls, we have to layout children explicitly
                // FIXME: probably move this to _setBounds
                var childLayout = this.gate._getLayoutManager(children[i]);
                if (childLayout != null) {
                    //FIXME: only if changed
                    childLayout.layout(children[i]);
                }

                y += newBounds.getHeight() + this.spacing;
            }
        };

        this.getPreferredSize = function (datum, wHint, hHint) {
            var insets = this.gate._getInsets(datum);
            if (this.vertical) {
                hHint = -1;
                if (wHint >= 0) {
                    wHint = Math.max(0, wHint - insets.getWidth());
                }
            } else {
                wHint = -1;
                if (hHint >= 0) {
                    hHint = Math.max(0, hHint - insets.getHeight());
                }
            }

            var children = this.getChildren(datum) || [];
            var prefSize = this._calculateChildrenSize(children, wHint, hHint, true);
            // Do a second pass, if necessary
            if (wHint >= 0 && prefSize.getWidth() > wHint) {
                prefSize = this._calculateChildrenSize(children, prefSize.getWidth(), hHint, true);
            } else if (hHint >= 0 && prefSize.getHeight() > hHint) {
                //FIXME: original D2D code has (children, wHint, prefSize.getWidth(), true) which does not make sense
                prefSize = this._calculateChildrenSize(children, wHint, prefSize.getHeight(), true);
            }

            prefSize.expand(0, Math.max(0, children.length - 1) * this.spacing);
            return this.transpose(prefSize).expand(insets.getWidth(), insets.getHeight());
        };

        this.pack = function (datum) {
            var prefSize = this.getPreferredSize(datum, -1, -1);

            //FIXME: layout currently works in LOCAL coordinates
            //@see also FIXME in _setBounds
            var xBefore = datum.x;
            var yBefore = datum.y;

            this.gate._setBounds(datum,
                new Draw2D.Rectangle(0, 0, prefSize.getWidth(), prefSize.getHeight()));
            this.layout(datum);

            if (xBefore) {
                datum.x = xBefore;
            }
            if (yBefore) {
                datum.y = yBefore;
            }
        };

        this._calculateChildrenSize = function (children, wHint, hHint, prefNotMin) {
            var width = 0;
            var height = 0;
            for (var i = 0; i < children.length; i++) {
                var rawChildSize = prefNotMin ?
                    this.gate._getChildPreferredSize(children[i], wHint, hHint)
                    : this.gate._getChildMinimumSize(children[i], wHint, hHint);
                var childSize = this.transpose(rawChildSize);
                height += childSize.getHeight();
                width = Math.max(width, childSize.getWidth());
            }
            return new Draw2D.Dimension(width, height);
        };

    }

};