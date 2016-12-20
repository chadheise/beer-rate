$(document).ready(function() {

	function refresh() {
		$.ajax({
			type: "GET",
	    	url: "/ui/draught/body",
	    	
	    	success: function(result){
	    		$("body").html(result);
	    	    reloadStyle();
	    	}
	    });
	}
	
	function reloadStyle(){
		var stylesheet = $('#draughtStyle').attr('href');
		$('#draughtStyle').attr('href',stylesheet);
	}
	
	refresh();
	
	setInterval ( function() {
		refresh();
	}, 5000 );
	
});