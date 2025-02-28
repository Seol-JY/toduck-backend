package im.toduck.domain.social.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record CommentLikeCreateResponse(
	@Schema(description = "생성된 댓글 좋아요 ID", example = "1")
	Long commentLikeId
) {
}
