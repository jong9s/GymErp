package com.example.gymerp.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.gymerp.config.FileStorageProperties;

import io.swagger.v3.oas.annotations.Hidden;

/*
 * 업로드된 이미지 응답 컨트롤러
 * GET /upload/{saveFileName} → 업로드 디렉터리의 파일을 그대로 응답
 */
@Hidden // Swagger에서 이 컨트롤러 제외
@Controller
public class ImageController {

	private final FileStorageProperties fileStorageProperties;

	public ImageController(FileStorageProperties fileStorageProperties) {
		this.fileStorageProperties = fileStorageProperties;
	}

	@GetMapping("/upload/{saveFileName}")
	public ResponseEntity<Resource> image(@PathVariable("saveFileName") String name) throws IOException {
		Path uploadDir = fileStorageProperties.getUploadDir();
		Path file = uploadDir.resolve(name).normalize();

		// "../" 등으로 업로드 디렉터리 밖의 파일을 요청하는 경우(경로 조작) 차단
		if (!file.startsWith(uploadDir) || !Files.isRegularFile(file)) {
			return ResponseEntity.notFound().build();
		}

		String mimeType = Files.probeContentType(file);
		MediaType mediaType = mimeType != null
				? MediaType.parseMediaType(mimeType)
				: MediaType.APPLICATION_OCTET_STREAM;

		return ResponseEntity.ok()
				.contentType(mediaType)
				.contentLength(Files.size(file))
				.body(new FileSystemResource(file));
	}

}
