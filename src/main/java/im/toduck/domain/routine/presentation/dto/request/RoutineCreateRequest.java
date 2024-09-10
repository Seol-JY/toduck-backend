package im.toduck.domain.routine.presentation.dto.request;

import static im.toduck.global.regex.PlanRegex.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;

import im.toduck.domain.person.PlanCategory;
import im.toduck.global.util.DayOfWeekListDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Schema(description = "루틴 생성 요청 DTO")
public record RoutineCreateRequest(
	@NotBlank(message = "제목은 비어있을 수 없습니다.")
	@Size(max = 20, message = "제목은 40자를 초과할 수 없습니다.") // TODO: 확정 필요
	@Schema(description = "루틴 제목", example = "아침 운동")
	String title,

	@Schema(description = "루틴 카테고리 (카테고리 없으면 null)", example = "EXERCISE")
	PlanCategory category, // TODO: 확정 필요

	@Schema(description = "루틴 색상 (색상 없으면 null)", example = "#FF5733")
	@Pattern(regexp = HEX_COLOR_CODE_REGEX, message = "색상은 유효한 Hex code 여야 합니다.")
	String color,

	@JsonDeserialize(using = LocalTimeDeserializer.class)
	@JsonFormat(pattern = "HH:mm")
	@Schema(description = "루틴 시간 (종일 루틴이면 null)", example = "07:00")
	LocalTime time,

	@NotNull(message = "공개 여부는 null 일 수 없습니다.")
	@Schema(description = "공개 여부", example = "true")
	Boolean isPublic,

	@JsonDeserialize(using = DayOfWeekListDeserializer.class)
	@NotNull(message = "반복 요일은 null 일 수 없습니다.")
	@NotEmpty(message = "반	복 요일은 최소 하나 이상 선택되어야 합니다.")
	@Schema(description = "반복 요일", example = "[\"MONDAY\",\"TUESDAY\"]")
	List<DayOfWeek> daysOfWeek,

	@PositiveOrZero(message = "분은 양수여야 합니다.")
	@Schema(description = "알림 시간 (분 단위, null 이면 알림을 보내지 않음)", example = "30")
	Integer reminderMinutes,

	@Schema(description = "메모", example = "30분 동안 조깅하기")
	@Size(max = 40, message = "메모는 40자를 넘을 수 없습니다.")
	String memo
) {
}