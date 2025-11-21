<?php
// CRITICAL: Set content type header FIRST to ensure JSON integrity.
header('Content-Type: application/json');

$host = "localhost";
$user = "root";       // default XAMPP user
$pass = "";           // default XAMPP password is empty
$dbname = "assignment-03";

// Create database connection
$conn = new mysqli($host, $user, $pass, $dbname);

// Check connection
if ($conn->connect_error) {
    // If connection fails, send JSON error and immediately stop execution
    die(json_encode(["status" => "error", "message" => "DB connection failed: " . $conn->connect_error]));
}

// Optional: Set character set for proper data handling
$conn->set_charset("utf8mb4");

// Note: No output should appear after this closing tag
?>