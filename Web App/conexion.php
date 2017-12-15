<?php
	$servername = "18.217.115.39";
	$username = "root";
	$password = "root";
	$dbname = "distribuidos";

	// Create connection
	$conn = new mysqli($servername, $username, $password, $dbname);
	// Check connection
	if ($conn->connect_error) {
	    die("Falló la Conexión: " . $conn->connect_error);
	}
?>