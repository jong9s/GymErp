package com.example.gymerp.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.gymerp.config.FileStorageProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * 프로필 이미지 저장/삭제
 * - 저장 파일명: {prefix}_{timestamp}.{확장자} (원본 파일명은 사용하지 않음)
 * - 이미지 확장자만 허용
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

	private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp", "avif");

	private final FileStorageProperties fileStorageProperties;

	public String storeImage(MultipartFile file, String prefix) {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("파일이 비어있습니다.");
		}
		String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
		if (ext == null || !ALLOWED_EXTENSIONS.contains(ext.toLowerCase(Locale.ROOT))) {
			throw new IllegalArgumentException("이미지 파일(" + String.join(", ", ALLOWED_EXTENSIONS) + ")만 업로드할 수 있습니다.");
		}

		String fileName = prefix + "_" + System.currentTimeMillis() + "." + ext.toLowerCase(Locale.ROOT);
		Path target = fileStorageProperties.prepareUploadDir().resolve(fileName);
		try {
			file.transferTo(target.toFile());
		} catch (IOException e) {
			throw new UncheckedIOException("파일 저장 실패: " + fileName, e);
		}
		return fileName;
	}

	// 기존 파일 삭제 (실패해도 요청 자체는 계속 진행)
	public void deleteQuietly(String fileName) {
		if (!StringUtils.hasText(fileName)) return;
		Path uploadDir = fileStorageProperties.getUploadDir();
		Path target = uploadDir.resolve(fileName).normalize();
		if (!target.startsWith(uploadDir)) return;
		try {
			Files.deleteIfExists(target);
		} catch (IOException e) {
			log.warn("파일 삭제 실패: {}", target, e);
		}
	}
}
