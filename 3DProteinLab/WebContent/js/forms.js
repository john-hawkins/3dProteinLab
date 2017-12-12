
$(function(){

	/*
	 * Toggle function to control the behavior of the STATUS BAR
	 */

    jQuery.fn.fadeToggle = function(speed, easing, callback) {
	    return this.animate({opacity: 'toggle'}, speed, easing, callback);
    };
    
    $("#status_toggler").click(function() {
	    $("#status").fadeToggle("slow");
    });
    
    // Set it to invisible to begin with
    $("#status").fadeToggle("slow");
	
	// Contact Form ANIMATION

	$('a.contact').toggle(function(){
		
		//remove the wrapper first if it exists
		$('#contact-wrap').remove();
		
		//add class to highlight link
		$(this).addClass("current");
		
		//create the wrapper
		//$('body').prepend('<div id="contact-wrap"></div>');
		$('#content').prepend('<div id="contact-wrap"></div>');
		
		//load contact form into the wrapper
		$.ajax({
		  url: "contact-form.jsp",
		  cache: false,
		  success: function(html){
		    $("#contact-wrap").append(html);
			//we need to validate the form
			//$('#contact').validate();
		  }
		});
		
		//slide the wrapper into view
		$('#contact-wrap').hide().slideDown('fast');
		
	}, function(){
		$(this).removeClass("current");
		$('#contact-wrap').slideUp();
	});
	
	// Search Form ANIMATION
	// NOT USED ANYMORE 
	$('a.search').toggle(function(){
		
		//remove the wrapper first if it exists
		$('#search-wrap').remove();
		
		//add class to highlight link
		$(this).addClass("current");
		
		//create the wrapper
		$('#content').prepend('<div id="search-wrap"></div>');
		
		//load contact form into the wrapper
		$.ajax({
		  url: "search-form.jsp",
		  cache: false,
		  success: function(html){
		    $("#search-wrap").append(html);
			//we need to validate the form
			//$('#contact').validate();
		  }
		});
		
		//slide the wrapper into view
		$('#search-wrap').hide().slideDown('fast');
		
	}, function(){
		$(this).removeClass("current");
		$('#search-wrap').slideUp();
	});
	
	
	// HELP PAGE Form ANIMATION

	$('a.help').toggle(function(){
		
		//remove the wrapper first if it exists
		$('#help-wrap').remove();
		
		//add class to highlight link
		$(this).addClass("current");
		
		//create the wrapper
		$('#content').prepend('<div id="help-wrap"></div>');
		
		//load contact form into the wrapper
		$.ajax({
		  url: "help-page.jsp",
		  cache: false,
		  success: function(html){
		    $("#help-wrap").append(html);
		  }
		});
		
		//slide the wrapper into view
		$('#help-wrap').hide().slideDown('fast');
		
	}, function(){
		$(this).removeClass("current");
		$('#help-wrap').slideUp();
	});
	
	
	// Filter Form ANIMATION

	$('a.regexfilter').toggle(function(){
		
		//remove the wrapper first if it exists
		$('#regexfilter-wrap').remove();
		
		//add class to highlight link
		$(this).addClass("current");
		
		//create the wrapper
		$('#content').prepend('<div id="regexfilter-wrap"></div>');
		
		//load contact form into the wrapper
		$.ajax({
		  url: "regex-filter-form.jsp",
		  cache: false,
		  success: function(html){
		    $("#regexfilter-wrap").append(html);
			//we need to validate the form
			//$('#contact').validate();
		    document.filterForm.regex.value = document.masterForm.regex.value;
		  }
		});
		
		//slide the wrapper into view
		$('#regexfilter-wrap').hide().slideDown('fast');
		
	}, function(){
		$(this).removeClass("current");
		$('#regexfilter-wrap').slideUp();
	});
	
	
	// SCOWLP Filter Form ANIMATION

	$('a.scowlpfilter').toggle(function(){
		
		//remove the wrapper first if it exists
		$('#scowlpfilter-wrap').remove();
		
		//add class to highlight link
		$(this).addClass("current");
		
		//create the wrapper
		$('#content').prepend('<div id="scowlpfilter-wrap"></div>');
		
		//load contact form into the wrapper
		$.ajax({
		  url: "scowlp-filter-form.jsp",
		  cache: false,
		  success: function(html){
		    $("#scowlpfilter-wrap").append(html);
			//we need to validate the form
			//$('#contact').validate();
		  }
		});
		
		//slide the wrapper into view
		$('#scowlpfilter-wrap').hide().slideDown('fast');
		
	}, function(){
		$(this).removeClass("current");
		$('#scowlpfilter-wrap').slideUp();
	});
	
});

