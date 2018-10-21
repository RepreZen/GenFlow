/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.common.xtend

import com.reprezen.genflow.common.HtmlInjections

class XLiveViewHelpers {
	
	def static getHtmlInjections(String anchor, String urlPrefix) {
		new HtmlInjections( //
			HtmlInjections::headBottom(generateErrorHandlerForLiveView, urlPrefix.generateStylesForLiveView), //
			HtmlInjections::bodyBottom(anchor.generateJavaScriptForLiveView) //
		)
	}

	def static String generateErrorHandlerForLiveView() {
		'''
			<script>
				window.onerror = function(errorMsg, url, lineNumber) {
					reprezen_onError(errorMsg, url, lineNumber);
					return true;
				};
			</script>
		'''
	}

	def static String generateJavaScriptForLiveView(String anchor_name) {
		'''
			«IF anchor_name != ''»
				<script>
					(function($) {
						$(document).ready(function() { 
							try {
								$('html, body').scrollTop($('«anchor_name»').offset().top)
							} catch (e) {}
						})
					})(jQuery);
					</script>
            «ENDIF»
			<script>
				function scrollToAnchor(anchorName, pathDelim, duration) {
					try {
						// anchorName can contain JQuery-unfriendly symbols, such as "/","{", or "}"
						var loc = document.getElementById(anchorName);
						if (loc) {
							$('html, body').animate({ 'scrollTop': loc.offsetTop}, duration)
							return anchorName;
						} else if (pathDelim && anchorName.lastIndexOf(pathDelim) > 1) {
							var parentAnchor = anchorName.slice(0, anchorName.lastIndexOf(pathDelim));
							scrollToAnchor(parentAnchor, pathDelim, duration);
						}
					} catch (e) {
						alert('Error ' + e);
					}
				}

				function showMessage(kind, msg, onClose) {
					var style = "font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;"
						+ "font-size: 14px;"
						+ "line-height: 1.42857143;"
						+ "color: #333;"
						+ "background-color: #fff;";
					(function($) {
						try {
							var div = $('#errors-placeholder').html(
								'<div class="reprezen" style="'+style+'"><div id="doc-error" class="alert alert-'+kind+'" role="alert">' + 
								'<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</button>' +
								msg +
								'</div></div>');
							if (onClose) {
								div.find('.alert').each(function(_, elt) {$(elt).on('closed.bs.alert', onClose);});
							}
							div.show();
						} catch (e) {}
					}(jQuery));
				}

				function showError(msg, onClose) {
					showMessage('danger', msg, onClose);
				}

				function showInfo(msg, onClose) {
					showMessage('info', msg, onClose);
				}

				function hideError() {
					(function($) {
						try {
							$('#errors-placeholder').hide();
						} catch (e) {}
					}(jQuery));
				}

				(function($) {
					if ($.bbq) {
						$.bbq.pushState = function(){};
					}
				}(jQuery));
			</script>
			<div id="errors-placeholder"></div>
		'''
	}

	def static String generateStylesForLiveView(String image_url_prefix) {
		'''
			<style>
				div.navbar { display: none; }
				div.page-header { display: none; }
				body { padding-top: 4px !important; min-height: auto !important; }
				.col-md-3 { display: none; }

				div#doc-error {
					position: fixed;
					left: 50px;
					top: 10px;
					right: 50px;
					z-index: 1060;
				}
			</style>
'''
	}
}
