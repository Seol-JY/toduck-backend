package im.toduck.domain.social.domain.usecase;

import static im.toduck.fixtures.social.SocialFixtures.*;
import static im.toduck.fixtures.user.UserFixtures.*;
import static im.toduck.global.exception.ExceptionCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import im.toduck.ServiceTest;
import im.toduck.domain.social.persistence.entity.Social;
import im.toduck.domain.social.presentation.dto.response.SocialProfileResponse;
import im.toduck.domain.social.presentation.dto.response.SocialResponse;
import im.toduck.domain.user.persistence.entity.User;
import im.toduck.global.exception.CommonException;
import im.toduck.global.presentation.dto.response.CursorPaginationResponse;

public class SocialProfileUseCaseTest extends ServiceTest {

	@Autowired
	private SocialProfileUseCase socialProfileUseCase;

	private User PROFILE_USER;
	private User AUTH_USER;
	private User OHTER_USER;

	@BeforeEach
	void setUp() {
		PROFILE_USER = testFixtureBuilder.buildUser(GENERAL_USER());
		AUTH_USER = testFixtureBuilder.buildUser(GENERAL_USER());
		OHTER_USER = testFixtureBuilder.buildUser(GENERAL_USER());
	}

	@Nested
	@DisplayName("SocialProfile 조회시")
	class GetUserProfileTests {

		@Test
		void 프로필_조회를_할_수_있다() {
			// given
			int followingCount = 3;
			int followerCount = 2;
			int postCount = 4;

			// profileUser가 팔로우한 수 생성 (followingCount)
			for (int i = 0; i < followingCount; i++) {
				User followed = testFixtureBuilder.buildUser(GENERAL_USER());
				testFixtureBuilder.buildFollow(PROFILE_USER, followed);
			}

			// profileUser를 팔로우하는 수 생성 (followerCount)
			for (int i = 0; i < followerCount; i++) {
				User follower = testFixtureBuilder.buildUser(GENERAL_USER());
				testFixtureBuilder.buildFollow(follower, PROFILE_USER);
			}

			// profileUser가 작성한 게시글 생성 (postCount)
			for (int i = 0; i < postCount; i++) {
				testFixtureBuilder.buildSocial(
					im.toduck.fixtures.social.SocialFixtures.SINGLE_SOCIAL(PROFILE_USER, false));
			}

			// when
			SocialProfileResponse response = socialProfileUseCase.getUserProfile(
				PROFILE_USER.getId(),
				AUTH_USER.getId()
			);

			// then
			assertThat(response).isNotNull();
			assertThat(response.nickname()).isEqualTo(PROFILE_USER.getNickname());
			assertThat(response.followingCount()).isEqualTo(followingCount);
			assertThat(response.followerCount()).isEqualTo(followerCount);
			assertThat(response.postCount()).isEqualTo(postCount);
			assertThat(response.isMe()).isFalse();
		}

		@Test
		void 자신의_프로필_조회시_isMe가_true이다() {
			// when
			SocialProfileResponse response = socialProfileUseCase.getUserProfile(
				PROFILE_USER.getId(),
				PROFILE_USER.getId()
			);

			// then
			assertThat(response.isMe()).isTrue();
		}

		@Test
		void 존재하지_않는_사용자_프로필_조회에_실패한다() {
			// given
			Long nonExistentUserId = -1L;

			// when & then
			assertThatThrownBy(() -> socialProfileUseCase.getUserProfile(nonExistentUserId, AUTH_USER.getId()))
				.isInstanceOf(CommonException.class)
				.hasMessageContaining(NOT_FOUND_USER.getMessage());
		}
	}

	@Nested
	@DisplayName("특정 유저의 Social 게시글 목록 조회시")
	class GetUserSocialsTests {

		@Test
		void 유저가_작성한_게시글_목록을_페이지네이션으로_조회한다() {
			// given
			int totalPosts = 15;
			int limit = 10;

			List<Social> profileUserSocials = testFixtureBuilder.buildSocials(
				MULTIPLE_SOCIALS(PROFILE_USER, totalPosts));

			testFixtureBuilder.buildSocial(SINGLE_SOCIAL(OHTER_USER, false));

			// when
			CursorPaginationResponse<SocialResponse> response = socialProfileUseCase.getUserSocials(
				PROFILE_USER.getId(),
				AUTH_USER.getId(), null, limit);

			// then
			Social firstExpectedSocial = profileUserSocials.get(totalPosts - 1);
			Social lastExpectedSocialOnPage = profileUserSocials.get(totalPosts - limit);

			assertSoftly(softly -> {
				softly.assertThat(response.hasMore()).isTrue();
				softly.assertThat(response.results()).hasSize(limit);
				softly.assertThat(response.nextCursor()).isEqualTo(lastExpectedSocialOnPage.getId());

				response.results().forEach(socialResponse ->
					softly.assertThat(socialResponse.owner().ownerId()).isEqualTo(PROFILE_USER.getId())
				);

				softly.assertThat(response.results().get(0).socialId()).isEqualTo(firstExpectedSocial.getId());
			});
		}

		@Test
		void 커서를_사용하여_다음_페이지를_조회한다() {
			// given
			int totalPosts = 15;
			int limit = 10;
			List<Social> profileUserSocials = testFixtureBuilder.buildSocials(
				MULTIPLE_SOCIALS(PROFILE_USER, totalPosts)
			);
			Long cursor = profileUserSocials.get(totalPosts - limit).getId();

			// when
			CursorPaginationResponse<SocialResponse> nextPage = socialProfileUseCase.getUserSocials(
				PROFILE_USER.getId(),
				AUTH_USER.getId(),
				cursor,
				limit
			);

			// then
			int remainingPosts = totalPosts - limit;
			Social oldestSocial = profileUserSocials.get(0);

			assertSoftly(softly -> {
				softly.assertThat(nextPage.hasMore()).isFalse();
				softly.assertThat(nextPage.results()).hasSize(remainingPosts);
				softly.assertThat(nextPage.nextCursor()).isNull();

				softly.assertThat(nextPage.results().get(remainingPosts - 1).socialId())
					.isEqualTo(oldestSocial.getId());
			});
		}

		@Test
		void 게시글이_없는_경우_빈_목록을_반환한다() {
			// when
			CursorPaginationResponse<SocialResponse> response = socialProfileUseCase.getUserSocials(
				PROFILE_USER.getId(),
				AUTH_USER.getId(),
				null,
				10
			);

			// then
			assertSoftly(softly -> {
				softly.assertThat(response.hasMore()).isFalse();
				softly.assertThat(response.results()).isEmpty();
				softly.assertThat(response.nextCursor()).isNull();
			});
		}

		@Test
		void 존재하지_않는_사용자의_게시글_조회_시_예외가_발생한다() {
			// given
			Long nonExistentUserId = -1L;

			// when & then
			assertThatThrownBy(
				() -> socialProfileUseCase.getUserSocials(nonExistentUserId, AUTH_USER.getId(), null, 10))
				.isInstanceOf(CommonException.class)
				.hasMessageContaining(NOT_FOUND_USER.getMessage());
		}

		// TODO: 차단 로직 구현 시 차단된 사용자의 게시글 조회 테스트 추가
	}
}
