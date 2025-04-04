package ktb.community.be.domain.comment.application;

import ktb.community.be.domain.comment.dao.PostCommentRepository;
import ktb.community.be.domain.comment.domain.PostComment;
import ktb.community.be.domain.comment.dto.CommentRequestDto;
import ktb.community.be.domain.comment.dto.CommentResponseDto;
import ktb.community.be.domain.post.dao.PostRepository;
import ktb.community.be.domain.post.domain.Post;
import ktb.community.be.domain.member.dao.MemberRepository;
import ktb.community.be.domain.member.domain.Member;
import ktb.community.be.global.exception.CustomException;
import ktb.community.be.global.exception.ErrorCode;
import ktb.community.be.global.util.CommentHierarchyBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PostCommentService {

    private final PostCommentRepository postCommentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    /**
     * 댓글 작성
     */
    @Transactional
    public CommentResponseDto createComment(Long postId, Long memberId, CommentRequestDto requestDto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        PostComment comment = PostComment.builder()
                .post(post)
                .member(member)
                .content(requestDto.getContent())
                .build();

        postCommentRepository.save(comment);
        updateCommentCount(post);

        return CommentResponseDto.from(comment);
    }

    /**
     * 대댓글 작성
     */
    @Transactional
    public CommentResponseDto createReply(Long postId, Long parentCommentId, Long memberId, CommentRequestDto requestDto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        PostComment parentComment = postCommentRepository.findById(parentCommentId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST, "부모 댓글을 찾을 수 없습니다."));

        if (parentComment.getIsDeleted()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "삭제된 댓글에는 답글을 달 수 없습니다.");
        }

        PostComment reply = PostComment.builder()
                .post(post)
                .member(member)
                .content(requestDto.getContent())
                .parentComment(parentComment)
                .build();

        postCommentRepository.save(reply);
        updateCommentCount(post);

        return CommentResponseDto.from(reply);
    }

    /**
     * 댓글 수정
     */
    @Transactional
    public CommentResponseDto updateComment(Long commentId, Long memberId, CommentRequestDto requestDto) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST, "댓글을 찾을 수 없습니다."));

        if (comment.getIsDeleted()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "삭제된 댓글은 수정할 수 없습니다.");
        }

        if (!comment.getMember().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED, "댓글 수정 권한이 없습니다.");
        }

        comment.updateContent(requestDto.getContent());

        return CommentResponseDto.from(comment);
    }

    /**
     * 댓글 삭제 (Soft Delete)
     */
    @Transactional
    public void deleteComment(Long commentId, Long memberId) {
        PostComment comment = postCommentRepository.findByIdIncludingDeleted(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST, "댓글을 찾을 수 없습니다."));

        if (comment.getIsDeleted()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "이미 삭제된 댓글입니다.");
        }

        if (!comment.getMember().getId().equals(memberId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED, "댓글 삭제 권한이 없습니다.");
        }

        comment.softDelete();
        postCommentRepository.save(comment);

        updateCommentCount(comment.getPost());
    }

    /**
     * 댓글 수 업데이트 메서드
     */
    private void updateCommentCount(Post post) {
        int commentCount = postCommentRepository.countByPostId(post.getId()); // 삭제되지 않은 댓글 수 조회
        post.updateCommentCount(commentCount);
        postRepository.save(post);
    }

    /**
     * 게시글에 달린 댓글 목록 조회
     */
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentsByPostId(Long postId) {
        postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        List<PostComment> comments = postCommentRepository.findAllByPostId(postId);

        return CommentHierarchyBuilder.buildCommentHierarchy(comments);
    }
}
