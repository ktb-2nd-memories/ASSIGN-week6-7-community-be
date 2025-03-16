package ktb.community.be.domain.comment.api;

import io.swagger.v3.oas.annotations.Operation;
import ktb.community.be.domain.comment.application.PostCommentService;
import ktb.community.be.domain.comment.dto.CommentRequestDto;
import ktb.community.be.domain.comment.dto.CommentResponseDto;
import ktb.community.be.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class PostCommentController {

    private final PostCommentService postCommentService;

    @Operation(summary = "댓글 작성", description = "특정 게시글에 댓글을 작성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponseDto>> createComment(
            @PathVariable Long postId,
            @RequestBody CommentRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.success(postCommentService.createComment(postId, requestDto)));
    }

    @Operation(summary = "대댓글 작성", description = "특정 댓글에 대한 대댓글을 작성합니다.")
    @PostMapping("/{commentId}/replies")
    public ResponseEntity<ApiResponse<CommentResponseDto>> createReply(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody CommentRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.success(postCommentService.createReply(postId, commentId, requestDto)));
    }

    @Operation(summary = "댓글 수정", description = "특정 댓글을 수정합니다.")
    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponseDto>> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.success(postCommentService.updateComment(commentId, requestDto)));
    }

    @Operation(summary = "댓글 삭제", description = "특정 댓글을 삭제합니다. (Soft Delete)")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long commentId) {
        postCommentService.deleteComment(commentId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "댓글 목록 조회", description = "특정 게시글의 댓글 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentResponseDto>>> getCommentsByPostId(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.success(postCommentService.getCommentsByPostId(postId)));
    }
}
