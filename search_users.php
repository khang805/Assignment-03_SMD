<?php
include 'db.php';

header('Content-Type: application/json');

if (!isset($_GET['query'])) {
    echo json_encode(["status" => "error", "message" => "Missing search query"]);
    exit;
}

$query = trim($_GET['query']);
$current_user_id = isset($_GET['current_user_id']) ? intval($_GET['current_user_id']) : 0;

if (empty($query)) {
    echo json_encode(["status" => "success", "users" => []]);
    exit;
}

$search_query = "%$query%";
$sql = "SELECT 
    u.id,
    u.username,
    u.first_name,
    u.last_name,
    up.profile_photo_path
FROM users u
LEFT JOIN user_profiles up ON u.id = up.user_id
WHERE u.username LIKE ? OR u.first_name LIKE ? OR u.last_name LIKE ?
ORDER BY u.username ASC
LIMIT 50";

$stmt = $conn->prepare($sql);
$stmt->bind_param("sss", $search_query, $search_query, $search_query);
$stmt->execute();
$result = $stmt->get_result();

$users = [];
while ($row = $result->fetch_assoc()) {
    // Skip current user
    if ($row['id'] == $current_user_id) {
        continue;
    }

    $users[] = [
        "id" => intval($row['id']),
        "username" => $row['username'],
        "first_name" => $row['first_name'],
        "last_name" => $row['last_name'],
        "profile_photo_url" => $row['profile_photo_path'] 
            ? "http://192.168.0.113/assignment-03/" . $row['profile_photo_path'] 
            : null
    ];
}

echo json_encode(["status" => "success", "users" => $users]);
$stmt->close();
?>
