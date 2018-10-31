/**
    Ths file is used to control the global selection
    of the dropdown settings] which acts as a base
    for the visualisation
 **/

/*
    This function is used for setting up global parameters if previous cookie exists it returns the
    saved values else sets a default value and creates a new cookie.
    Parameters : none
 */
function getGlobalSetting(){
    var val = getCookie('globalSetting');
    if( val.length > 0 ){
        return val;
    }else{
        var __globalSetting = '{"R":1,"DT":1,"RL":1,"MTD":1}';
        createCookie('globalSetting',__globalSetting,30);
        return __globalSetting;
    }
}

/*
    This is a wrapper method to access createCookie method
    Parameters : _globalSetting
 */
function setGlobalSetting(_globalSetting){
    createCookie('globalSetting',_globalSetting,30);
}

/**
 * Create drop-down for visibility control with the help of JQuery and bootstrap
 * */

/**
 *  Prevent clicks on content
 * */
$('.dropdown-menu').on('click.dropdown-checkbox.data-api', function(e) {
    e.stopPropagation();
});

/**
 * Create checkbox widget
 * */
$('.box').checkbox();


/**
 * Get previous saved settings
 * */
var _gSetting = JSON.parse(getGlobalSetting());
$('.bootstrap-checkbox').each(function(el,u){
    var g = $(u).checkbox();
    var val = _gSetting[''+g.attr('id')+''];
    if(val == 1){
        g.chbxChecked(true);
    }else{
        g.chbxChecked(false);
    }

    var _this = $(u);
    var yy = this;
    var parent = $(_this.parents()[0]);
    var t = parent.children().filter(function(u){
        return yy != parent.children()[u];
    });
    if(t.length > 0 ){
        var ch = $(t[0]);
        var checkbox = $(yy).checkbox();
        if(!checkbox.chbxChecked()){
            ch.addClass('disablenode');
        }else{
            ch.removeClass('disablenode');
        }
    }
});

/**
 * Bind click event on checkbox
 * */
$('.bootstrap-checkbox').click(function(element){
    var yy = this;
    var _this = $(this);
    var parent = $(_this.parents()[0]);
    var t = parent.children().filter(function(u){
        return yy != parent.children()[u];
    });

    if(t.length > 0 ){
        var ch = $(t[0]);
        var checkbox = $(yy).checkbox();
        if(!checkbox.chbxChecked()){
            ch.addClass('disablenode');
        }else{
            ch.removeClass('disablenode');
        }
    }
    getSettingObject();
});

/**
 * Create object for current checked state
 * */
function getSettingObject(){
    var s = {};
    $('.bootstrap-checkbox').each(function(el,u){
        var g = $(u).checkbox();
        s[g.attr('id')] = (g.chbxChecked() ? 1 : 0 );
    });
    setGlobalSetting(JSON.stringify(s));
    flowTree.updateSetting();
}