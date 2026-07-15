package com.dobak.backend.service;

import com.dobak.backend.dto.EggRewardInfo;
import com.dobak.backend.dto.HatchRewardSummary;
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
    private final HatchRewardService hatchRewardService;

    public ProgressService(ProgressRepository progressRepository, UserRepository userRepository,
                            HatchRewardService hatchRewardService) {
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
        this.hatchRewardService = hatchRewardService;
    }

    public ProgressResponse getProgress(Long childId) {
        return toResponse(findOrCreate(childId));
    }

    /**
     * 학습 진행 시 자동 호출 (알 +1). PracticeService/SimilarSentenceService에서 정확도 기준
     * 넘거나 문항을 완료하면 호출함. 이번 호출로 부화까지 됐으면 리워드를 바로 발급해서 같이 돌려준다
     * ("6·리워드"/"7·부화" 화면을 이 응답 하나로 그릴 수 있게).
     */
    public EggRewardInfo addEgg(Long childId) {
        Progress progress = findOrCreate(childId);
        boolean justHatched = progress.addEgg();
        progressRepository.save(progress);

        HatchRewardSummary issuedReward = null;
        if (justHatched) {
            issuedReward = hatchRewardService.issue(
                    progress.getChild(), progress.getSelectedRewardCode(), progress.getSelectedByGuardian()
            );
        }

        return new EggRewardInfo(1, progress.getCurrentHatchProgress(), Progress.EGGS_PER_HATCH, justHatched, issuedReward);
    }

    /** 보호자가 칭찬 도장을 보낼 때 호출 (칭찬도장 +1). ReportService.sendStamp에서 호출함. */
    public ProgressResponse addStamp(Long childId) {
        Progress progress = findOrCreate(childId);
        progress.addStamp();
        progressRepository.save(progress);
        return toResponse(progress);
    }

    /** 발음점검 1회 끝날 때마다 호출 — 정확도를 곧바로 저장하지 않고 누적 평균에 반영. PracticeService에서 호출함. */
    public void recordAccuracy(Long childId, double accuracy) {
        Progress progress = findOrCreate(childId);
        progress.recordAccuracy(accuracy);
        progressRepository.save(progress);
    }

    /** 보호자가 다음 부화 때 받을 리워드를 미리 골라둘 때 호출 */
    public ProgressResponse selectReward(Long childId, String rewardCode, Long guardianId) {
        Progress progress = findOrCreate(childId);
        User guardian = guardianId != null
                ? userRepository.findById(guardianId)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 보호자 계정: " + guardianId))
                : null;
        progress.selectReward(rewardCode, guardian);
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
                progress.getEggCount(),
                progress.getCurrentHatchProgress(),
                progress.getTotalHatchesCompleted(),
                Progress.EGGS_PER_HATCH,
                progress.getStampCount(),
                progress.getAverageAccuracy(),
                progress.getSelectedRewardCode()
        );
    }
}
