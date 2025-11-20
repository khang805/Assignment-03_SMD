<?php
include 'db.php';

header('Content-Type: application/json');

if (!isset($_GET['user_id'])) {
    echo json_encode(["status" => "error", "message" => "Missing user_id"]);
    exit;
}

$user_id = intval($_GET['user_id']);

$query = "SELECT 
    u.id,
    u.username,
    u.first_name,
    u.last_name,
    up.profile_photo_path,
    f.created_at
FROM follows f
INNER JOIN users u ON f.follower_id = u.id
LEFT JOIN user_profiles up ON u.id = up.user_id
WHERE f.following_id = ?
ORDER BY f.created_at DESC";

$stmt = $conn->prepare($query);
$stmt->bind_param("i", $user_id);
$stmt->execute();
$result = $stmt->get_result();

$followers = [];
while ($row = $result->fetch_assoc()) {
    $followers[] = [
        "id" => intval($row['id']),
        "username" => $row['username'],
        "first_name" => $row['first_name'],
        "last_name" => $row['last_name'],
        "profile_photo_url" => $row['profile_photo_path'] 
            ? "http://192.168.0.113/assignment-03/" . $row['profile_photo_path'] 
            : null
    ];
}

echo json_encode(["status" => "success", "followers" => $followers]);
$stmt->close();
?>
