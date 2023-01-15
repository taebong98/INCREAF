package com.main19.server.comment.like.service;

import com.main19.server.auth.jwt.JwtTokenizer;
import com.main19.server.comment.entity.Comment;
import com.main19.server.comment.like.entity.CommentLike;
import com.main19.server.comment.like.repository.CommentLikeRepository;
import com.main19.server.comment.service.CommentService;
import com.main19.server.exception.BusinessLogicException;
import com.main19.server.exception.ExceptionCode;
import com.main19.server.member.service.MemberService;

import com.main19.server.sse.entity.Sse.SseType;
import com.main19.server.sse.service.SseService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentLikeService {

    private final CommentLikeRepository commentLikeRepository;
    private final MemberService memberService;
    private final CommentService commentService;
    private final SseService sseService;
    private final JwtTokenizer jwtTokenizer;

    public CommentLike createLike(CommentLike commentLike, long commentId, long memberId, String token) {

        long tokenId = jwtTokenizer.getMemberId(token);

        if (memberId != tokenId) {
            throw new BusinessLogicException(ExceptionCode.FORBIDDEN);
        }

        CommentLike findCommentLike = commentLikeRepository.findByMember_MemberIdAndComment_CommentId(memberId,commentId);

        if(findCommentLike != null) {
            throw new BusinessLogicException(ExceptionCode.COMMENT_LIKE_EXISTS);
        }

        commentLike.setMember(memberService.findMember(memberId));

        Comment comment = commentService.findComment(commentId);

        comment.setLikeCount(comment.getLikeCount()+1);

        commentLike.setComment(comment);

        if(comment.getMember().getMemberId() != tokenId) {
            sseService.sendPosting(comment.getMember(), SseType.commentLike, memberService.findMember(memberId),comment.getPosting());
        }

        return commentLikeRepository.save(commentLike);
    }

    public void deleteLike(long commentLikeId, String token) {

        long tokenId = jwtTokenizer.getMemberId(token);

        if (findVerifiedCommentLike(commentLikeId).getMember().getMemberId() != tokenId) {
            throw new BusinessLogicException(ExceptionCode.FORBIDDEN);
        }

        CommentLike commentLike = findVerifiedCommentLike(commentLikeId);

        commentLike.getComment().setLikeCount(commentLike.getComment().getLikeCount()-1);

        commentLikeRepository.delete(commentLike);

    }

    private CommentLike findVerifiedCommentLike(long commentLikeId) {
        Optional<CommentLike> optionalCommentLike = commentLikeRepository.findById(commentLikeId);
        CommentLike findCommentLike =
            optionalCommentLike.orElseThrow(() -> new BusinessLogicException(ExceptionCode.COMMENT_LIKE_NOT_FOUND));
        return findCommentLike;
    }
}
