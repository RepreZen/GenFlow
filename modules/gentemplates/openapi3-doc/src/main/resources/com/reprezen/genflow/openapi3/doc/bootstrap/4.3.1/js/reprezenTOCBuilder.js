// builds <ul> structure representing links nested at various levels
// elements in toc-entry class appear in the structure. Other attributes are used to the entry:
// * data-toc-level - the nesting level of this entry
// * data-toc-href - the href attribute of the generaetd link
// * data-toc-text - the text of the generated link
// Links are placed into the structure in the order they appear in the document.
// Nesting levels are not skipped. If you jump from level 0 to level 2, an intermediate
// <ul> at level 1 will be interposed.
// The structure is decorated with classes so as to work as a bootstrap TOC.
function buildToc() {
	(function($) {
		// pluck details from a toc-entry class element
		function getLevel(entry) {
			return Number($(entry).attr("data-toc-level"));
		}
		function getHref(entry) {
			return $(entry).attr("data-toc-href") || "#"+$(entry).attr("id");
		}
		function getText(entry) {
			return $(entry).attr("data-toc-text");
		}
		// create a fully formed list item with link for a toc-entry class element
		function getItem(entry) {
			return $("<li></li>").addClass("nav-item").append($("<a></a>").addClass("nav-link").attr("href", getHref(entry)).text(getText(entry)));
		}
		// create a list of entries from the doc-wide list, starting with position 'start' and
		// collecting a run of entries that are at level greater than 'level'
		function getSubEntries(entries, start, level) {
			for (i = start; i < entries.length; i++) {
				if (getLevel(entries[i]) <= level) {
					return entries.slice(start, i);
				}
			}
			return entries.slice(start);
		}
		function build(level, entries) {
			var html = $("<ul></ul>").addClass("nav").addClass(level == 0 ? "bs-sidenav flex-column" : null);
			for (var i = 0; i < entries.length;) {
				var entry = $(entries).get(i);
				var item;
				if (getLevel(entry) == level) {
					item = getItem(entry);
					i += 1;
				} else {
					item = $("<li></li>").addClass("nav-item");
				}
				var subEntries = getSubEntries(entries, i, level);
				if (subEntries.length > 0) {
					item.append(build(level+1, subEntries));
					i += subEntries.length
				}
				html.append(item);
			}
			return html;	
		}
		var toc = build(0, $(".toc-entry"));
		$("#toc").empty().append(toc);
	}(jQuery));
}
jQuery(document).ready(function() {
	buildToc();	
});