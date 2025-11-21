<?php
include 'db.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    if (!isset($_POST['user_id']) || !isset($_POST['post_id'])) {
        echo json_encode(["status" => "error", "message" => "Missing required fields"]);
        exit;
    }

    $user_id = intval($_POST['user_id']);
    $post_id = intval($_POST['post_id']);

    // Check if like already exists
    $check = $conn->prepare("SELECT id FROM post_likes WHERE user_id = ? AND post_id = ?");
    $check->bind_param("ii", $user_id, $post_id);
    $check->execute();
    $result = $check->get_result();

    if ($result->num_rows > 0) {
        // Unlike - remove the like
        $delete = $conn->prepare("DELETE FROM post_likes WHERE user_id = ? AND post_id = ?");
        $delete->bind_param("ii", $user_id, $post_id);
        if ($delete->execute()) {
            // Get updated like count
            $count_stmt = $conn->prepare("SELECT COUNT(*) as count FROM post_likes WHERE post_id = ?");
            $count_stmt->bind_param("i", $post_id);
            $count_stmt->execute();
            $count_result = $count_stmt->get_result();
            $count_row = $count_result->fetch_assoc();
            
            echo json_encode([
                "status" => "success",
                "action" => "unliked",
                "like_count" => intval($count_row['count'])
            ]);
            $count_stmt->close();
        } else {
            echo json_encode(["status" => "error", "message" => "Failed to unlike"]);
        }
        $delete->close();
    } else {
        // Like - add the like
        $insert = $conn->prepare("INSERT INTO post_likes (user_id, post_id) VALUES (?, ?)");
        $insert->bind_param("ii", $user_id, $post_id);
        if ($insert->execute()) {
            // Get updated like count
            $count_stmt = $conn->prepare("SELECT COUNT(*) as count FROM post_likes WHERE post_id = ?");
            $count_stmt->bind_param("i", $post_id);
            $count_stmt->execute();
            $count_result = $count_stmt->get_result();
            $count_row = $count_result->fetch_assoc();
            
            echo json_encode([
                "status" => "success",
                "action" => "liked",
                "like_count" => intval($count_row['count'])
            ]);
            $count_stmt->close();
        } else {
            echo json_encode(["status" => "error", "message" => "Failed to like"]);
        }
        $insert->close();
    }
    $check->close();
} else {
    echo json_encode(["status" => "error", "message" => "Invalid request method"]);
}
?>


