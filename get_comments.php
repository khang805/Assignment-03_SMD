<?php
include 'db.php';

header('Content-Type: application/json');

if (!isset($_GET['post_id'])) {
    echo json_encode(["status" => "error", "message" => "Missing post_id"]);
    exit;
}

$post_id = intval($_GET['post_id']);

$query = "SELECT 
    c.id,
    c.user_id,
    c.comment,
    u.username,
    u.first_name,
    u.last_name
FROM post_comments c
INNER JOIN users u ON c.user_id = u.id
WHERE c.post_id = ?
ORDER BY c.id ASC";

$stmt = $conn->prepare($query);
$stmt->bind_param("i", $post_id);
$stmt->execute();
$result = $stmt->get_result();

$comments = [];
while ($row = $result->fetch_assoc()) {
    $comments[] = [
        "id" => intval($row['id']),
        "user_id" => intval($row['user_id']),
        "username" => $row['username'],
        "first_name" => $row['first_name'],
        "last_name" => $row['last_name'],
        "comment" => $row['comment']
    ];
}

echo json_encode(["status" => "success", "comments" => $comments]);
$stmt->close();
?>


