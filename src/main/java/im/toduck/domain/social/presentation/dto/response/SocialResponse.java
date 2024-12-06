package im.toduck.domain.social.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import im.toduck.domain.routine.presentation.dto.response.RoutineDetailResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record SocialResponse(
	@Schema(description = "소셜 게시글 ID", example = "1")
	Long socialId,

	@Schema(description = "작성자 정보")
	OwnerDto owner,

	@Schema(description = "공유할 루틴 정보")
	RoutineDetailResponse routine,

	@Schema(description = "게시글 내용", example = "어제 잠들기 전 새로운 루틴을 추가했다👀")
	String content,

	@Schema(description = "이미지가 포함되어 있는지 여부", example = "true")
	boolean hasImages,

	@Schema(description = "게시글 이미지 목록")
	List<SocialImageDto> images,

	@Schema(description = "좋아요 정보")
	SocialLikeDto socialLikeInfo,

	@Schema(description = "댓글 수", example = "1")
	int commentCount,

	@Schema(description = "게시글 작성 시간", type = "string", pattern = "yyyy-MM-dd HH:mm", example = "2024-09-11 10:30")
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
	LocalDateTime createdAt
) {
}
