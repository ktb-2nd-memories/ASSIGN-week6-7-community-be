package ktb.community.be.domain.post.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import ktb.community.be.domain.post.application.PostService;
import ktb.community.be.domain.post.dto.*;
import ktb.community.be.global.response.ApiResponse;
import ktb.community.be.global.response.ApiResponseConstants;
import ktb.community.be.global.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final SecurityUtil securityUtil;
    private final ObjectMapper objectMapper;

    @Operation(summary = "게시글 작성", description = "게시글을 작성합니다.")
    @ApiResponseConstants.CommonResponses
    @PostMapping
    public ResponseEntity<ApiResponse<PostCreateResponseDto>> createPost(
            @Valid @RequestBody PostCreateRequestDto requestDto) {
        Long memberId = securityUtil.getCurrentMemberId();
        PostCreateResponseDto responseDto = postService.createPost(memberId, requestDto, null, null);
        return ResponseEntity.ok(ApiResponse.success("게시글이 작성되었습니다.", responseDto));
    }

    @Operation(summary = "게시글 상세 조회", description = "게시글 ID를 기반으로 상세 정보를 조회합니다.")
    @ApiResponseConstants.PostDetailResponses
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponseDto>> getPostDetail(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.success("게시글을 조회했습니다.", postService.getPostDetail(postId)));
    }

    /**
     * 게시글 수정 (multipart 포함 → PUT 사용 어려우므로 POST 사용)
     */
    @Operation(summary = "게시글 수정", description = "게시글을 수정합니다.")
    @PostMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PostDetailResponseDto>> updatePost(
            @PathVariable Long postId,
            @RequestPart("updateData") @Valid PostUpdateWithImageRequestDto updateDto
    ) {
        Long memberId = securityUtil.getCurrentMemberId();
        PostDetailResponseDto updated = postService.updatePost(postId, memberId, updateDto);
        return ResponseEntity.ok(ApiResponse.success("게시글이 수정되었습니다.", updated));
    }

    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다. (Soft Delete)")
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long postId) {
        Long memberId = securityUtil.getCurrentMemberId();
        postService.deletePost(postId, memberId);
        return ResponseEntity.ok(ApiResponse.success("게시글이 삭제되었습니다."));
    }

    @Operation(summary = "게시글 전체 조회", description = "커서 기반 페이지네이션을 사용하여 게시글을 최신순으로 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PostListResponseDto>>> getAllPosts(
            @RequestParam(required = false) LocalDateTime cursor,
            @RequestParam(defaultValue = "10") int size) {
        List<PostListResponseDto> posts = postService.getAllPosts(cursor, PageRequest.of(0, size));
        return ResponseEntity.ok(ApiResponse.success("게시글 목록을 조회했습니다.", posts));
    }
}
