///// CONFIGURATION VARIABLES:

var name				= "#rightsidebar";
var menu_top_limit 		= 43;
var menu_top_margin 	= 43;
var menu_shift_duration = 500;
var menuYloc = null;
///////////////////////////////////

$(window).scroll(function() 
{ 
	// Calculate the top offset, adding a limit
	offset = menuYloc + $(document).scrollTop() + menu_top_margin;
	
	// Limit the offset to 241 pixels...
	// This keeps the menu out of our header area:
	if(offset < menu_top_limit)
		offset = menu_top_limit;
	
	// Give it the PX for pixels:
	offset += "px";
	
	// Animate:
	$(name).animate({top:offset},{duration:menu_shift_duration,queue:false});
});
