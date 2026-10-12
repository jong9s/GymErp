package com.example.gymerp.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.gymerp.config.FileStorageProperties;

/**
 * 업로드 이미지 응답 테스트 - 경로 조작(../) 차단 확인
 */
class ImageControllerTest {

    @TempDir
    Path root;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        Path uploadDir = Files.createDirectories(root.resolve("upload"));
        Files.write(uploadDir.resolve("a.png"), new byte[] { 1, 2, 3 });
        Files.writeString(root.resolve("secret.txt"), "secret");

        FileStorageProperties props = new FileStorageProperties(uploadDir.toString());
        mockMvc = MockMvcBuilders.standaloneSetup(new ImageController(props)).build();
    }

    @Test
    @DisplayName("업로드 디렉터리 안의 파일은 내용을 응답한다")
    void servesUploadedFile() throws Exception {
        mockMvc.perform(get("/upload/a.png"))
            .andExpect(status().isOk())
            .andExpect(content().bytes(new byte[] { 1, 2, 3 }));
    }

    @Test
    @DisplayName("없는 파일은 404")
    void missingFile() throws Exception {
        mockMvc.perform(get("/upload/none.png"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("../ 로 업로드 디렉터리 밖의 파일을 요청하면 404")
    void blocksPathTraversal() throws Exception {
        mockMvc.perform(get("/upload/{name}", "../secret.txt"))
            .andExpect(status().isNotFound());
    }
}
