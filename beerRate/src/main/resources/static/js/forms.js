// $(document).ready(function() {

// 	$("#newRatingForm").submit(function() { // intercepts the submit event
// 		// alert("hey!");
// 		$.ajax({ // make an AJAX request
// 			type : "POST",
// 			url : "/forms/ratings", // it's the URL of your component B
// 			data : $("#newRatingForm").serialize(), // serializes the form's
// 												// elements
// 			success : function(data) {
// 				// show the data you got from B in result div
// 				$("#results").show();
// 			}
// 		});
// 		e.preventDefault(); // avoid to execute the actual submit of the form
// 	});

// });