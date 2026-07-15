package com.dobak.backend.service;

import com.dobak.backend.dto.ProgressResponse;
import com.dobak.backend.entity.Progress;
import com.dobak.backend.entity.User;
import com.dobak.backend.repository.ProgressRepository;
import com.dobak.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class ProgressService {

    private final ProgressRepository progressRepository;
    private final UserRepository userRepository;

    public ProgressService(ProgressRepository progressRepository, UserRepository userRepository) {
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
    }

    public ProgressResponse getProgress(Long childId) {
        Progress progress = findOrCreate(childId);
        return toResponse(progress);
    }

    public ProgressResponse addGrape(Long childId) {
        Progress progress = findOrCreate(childId);
        progress.addGrape();
        progressRepository.save(progress);
        return toResponse(progress);
    }

    private Progress findOrCreate(Long childId) {
        return progressRepository.findByChildId(childId)
                .orElseGet(() -> {
                    User child = userRepository.findById(childId)
                            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이 계정: " + childId));
                    return progressRepository.save(new Progress(child));
                });
    }

    private ProgressResponse toResponse(Progress progress) {
        return new ProgressResponse(
                progress.getGrapeCount(),
                progress.getCurrentBunchCount(),
                progress.getTotalBunchesCompleted(),
                Progress.GRAPES_PER_BUNCH
        );
    }
}
