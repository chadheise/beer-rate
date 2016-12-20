$(document).ready(function() {
	
	// Code to execute on page load
	$(".form").hide();
	
	$("#addUserButton").click(function () {
		$(".form").hide();
		$("#addUserForm").show();
	});
	
	$("#addTeamButton").click(function () {
		$(".form").hide();
		$("#addTeamForm").show();
	});
	
	$("#changeTeamButton").click(function () {
		$(".form").hide();
		$("#changeTeamForm").show();
	});
	
	$("#teamStatsButton").click(function () {
		$(".form").hide();
		$("#teamStatsForm").show();
	});
	
});