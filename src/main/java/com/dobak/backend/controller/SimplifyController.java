package com.dobak.backend.controller;

import com.dobak.backend.dto.SimplifyResponse;
import com.dobak.backend.service.SimplifyService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class SimplifyController {

    private final SimplifyService simplifyService;

    public SimplifyController(SimplifyService simplifyService) {
        this.simplifyService = simplifyService;
    }

    /**
     * A모드: 사진/PDF 이미지를 업로드하면 쉬운 문장 + 낭독 음성 + 하이라이트 타임스탬프를 반환.
     */
    @PostMapping(value = "/simplify", consumes = "multipart/form-data")
    public SimplifyResponse simplify(@RequestParam("file") MultipartFile file) {
        return simplifyService.process(file);
    }
}
