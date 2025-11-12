package com.example.gymerp.config;

import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class FileStorageProperties {

	private static final Logger log = LoggerFactory.getLogger(FileStorageProperties.class);

	private final Path uploadDir;

	public FileStorageProperties(@Value("${file.location:}") String configuredPath) {
		this.uploadDir = resolvePath(configuredPath);
		log.info("Resolved file upload directory: {}", this.uploadDir);
	}

	private Path resolvePath(String configuredPath) {
		String targetPath = configuredPath;
		if (!StringUtils.hasText(targetPath)) {
			String os = System.getProperty("os.name").toLowerCase();
			targetPath = os.contains("win")
					? "C:/playground/final_project/GymErp/profile/"
					: "profile";
		}
		return Paths.get(targetPath).toAbsolutePath().normalize();
	}

	public Path getUploadDir() {
		return uploadDir;
	}

	public Path prepareUploadDir() {
		try {
			Files.createDirectories(uploadDir);
			return uploadDir;
		} catch (java.io.IOException e) {
			throw new UncheckedIOException("업로드 디렉터리를 생성할 수 없습니다: " + uploadDir, e);
		}
	}

	public Path resolve(String fileName) {
		return uploadDir.resolve(fileName);
	}

	public String getResourceLocation() {
		return uploadDir.toUri().toString();
	}
}
