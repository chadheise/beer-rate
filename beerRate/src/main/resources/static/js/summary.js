$(document).ready(function() {

	function refresh() {
		$.ajax({
			type: "GET",
	    	url: "/ui/summary/body",
	    	
	    	success: function(result){
	    		$("body").html(result);
	    	    reloadStyle();
	    	}
	    });
	}
	
	function reloadStyle(){
		var stylesheet = $('#summaryStyle').attr('href');
		$('#summaryStyle').attr('href',stylesheet);
	}
	
	setInterval ( function() {
		refresh();
	}, 5000 );
	
});