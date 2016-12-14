$(document).ready(function() {

	function refresh() {
		$.ajax({
			type: "GET",
	    	url: "/ui/summary/body",
	    	
	    	success: function(result){
	    		$("body").html(result);
//	    		document.open();
//	    		document.write(result);
//	    		document.close();
		        //$("#div1").html(result);
	    	}
	    });
	}
	
	setInterval ( function() {
		refresh();
	}, 5000 );
	
});