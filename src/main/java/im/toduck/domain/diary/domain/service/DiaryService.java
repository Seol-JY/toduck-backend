package im.toduck.domain.diary.domain.service;

import java.util.List;

import org.springframework.stereotype.Service;

import im.toduck.domain.diary.common.mapper.DiaryImageFileMapper;
import im.toduck.domain.diary.common.mapper.DiaryMapper;
import im.toduck.domain.diary.persistence.entity.Diary;
import im.toduck.domain.diary.persistence.entity.DiaryImage;
import im.toduck.domain.diary.persistence.repository.DiaryImageRepository;
import im.toduck.domain.diary.persistence.repository.DiaryRepository;
import im.toduck.domain.diary.presentation.dto.request.DiaryCreateRequest;
import im.toduck.domain.user.persistence.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiaryService {
	private final DiaryRepository diaryRepository;
	private final DiaryImageRepository diaryImageRepository;

	@Transactional
	public Diary createDiary(
		final User user,
		final DiaryCreateRequest request
	) {
		Diary diary = DiaryMapper.toDiary(
			user,
			request.date(),
			request.emotion(),
			request.title(),
			request.memo()
		);
		return diaryRepository.save(diary);
	}

	@Transactional
	public void addDiaryImageFiles(final List<String> imageUrls, final Diary diary) {
		List<DiaryImage> diaryImageFiles = imageUrls.stream()
			.map(url -> DiaryImageFileMapper.toDiaryImageFile(diary, url))
			.toList();
		diaryImageRepository.saveAll(diaryImageFiles);
	}
}
