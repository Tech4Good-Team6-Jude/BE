package com.dobak.backend.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * 해커톤 단계: 로컬 디스크 저장. 배포 시 S3 등으로 교체하려면 이 클래스만 바꾸면 됨.
 */
@Service
public class FileStorageService {

    private final Path basePath;

    public FileStorageService(@Value("${file.storage.base-path}") String basePathValue) {
        this.basePath = Path.of(basePathValue);
        try {
            Files.createDirectories(basePath);
        } catch (IOException e) {
            throw new IllegalStateException("파일 저장소 디렉토리 생성 실패: " + basePath, e);
        }
    }

    /** 업로드 파일을 저장하고, 접근 가능한 상대 경로(URL)를 반환한다. */
    public String save(MultipartFile file, String subDir) {
        try {
            Path dir = basePath.resolve(subDir);
            Files.createDirectories(dir);

            String extension = "";
            String original = file.getOriginalFilename();
            if (original != null && original.contains(".")) {
                extension = original.substring(original.lastIndexOf('.'));
            }
            String fileName = UUID.randomUUID() + extension;
            Path target = dir.resolve(fileName);
            file.transferTo(target);

            return "/files/" + subDir + "/" + fileName;
        } catch (IOException e) {
            throw new IllegalStateException("파일 저장 실패", e);
        }
    }
}
