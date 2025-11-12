package com.example.gymerp.service;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gymerp.dto.PtLogDto;
import com.example.gymerp.dto.PtRegistrationDto;
import com.example.gymerp.dto.ScheduleDto;
import com.example.gymerp.repository.LogDao;
import com.example.gymerp.repository.ScheduleDao;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleDao scheduleDao;                 // 일정테이블 접근
    private final PtRegistrationService ptRegistrationService; // PT 등록 테이블 접근
    private final LogDao logDao;                           // PT 로그 테이블 접근
    private final SqlSession session;                      // 공용 SqlSession

    // 어드민 검색
    @Override
    public Map<String, Object> searchForAdmin(Map<String, Object> p) {
        String keyword = String.valueOf(p.getOrDefault("keyword", "")).trim();
        String codeBid = String.valueOf(p.getOrDefault("codeBid", "")).trim();
        int offset = (int) p.getOrDefault("offset", 0);
        int limit = (int) p.getOrDefault("limit", 20);

        Map<String, Object> param = new HashMap<>();
        param.put("keyword", keyword);
        param.put("codeBid", codeBid);
        param.put("offset", offset);
        param.put("limit", limit);

        List<ScheduleDto> list = scheduleDao.selectByFiltersForAdmin(param);
        int total = scheduleDao.countByFiltersForAdmin(param);

        Map<String, Object> res = new HashMap<>();
        res.put("list", list);
        res.put("total", total);
        res.put("page", (offset / Math.max(limit, 1)) + 1);
        res.put("size", limit);
        return res;
    }

    private static Integer optInt(String s) {
        try { return s == null ? null : Integer.valueOf(s); }
        catch (Exception e) { return null; }
    }
    private static int optInt(String s, int d) {
        try { return Integer.parseInt(s); }
        catch (Exception e) { return d; }
    }
    private static String str(String s) { return (s == null || s.isBlank()) ? null : s; }
    private static Timestamp ts(String s) {
        if (s == null || s.isBlank()) return null;
        return Timestamp.valueOf(s.replace("T", " ") + (s.length() == 16 ? ":00" : ""));
    }

    // 기본 조회들
    @Override public List<ScheduleDto> getAllSchedules() { return scheduleDao.selectAll(); }
    @Override public ScheduleDto getScheduleById(int shNum) { return scheduleDao.selectByShNum(shNum); }
    @Override public List<ScheduleDto> getSchedulesByEmpNum(int empNum) { return scheduleDao.selectByEmpNum(empNum); }
    @Override public List<ScheduleDto> getSchedulesByDateRange(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        return scheduleDao.selectByDateRange(startDate, endDate);
    }

    // --------------------------- 생성 ---------------------------
    @Override
    @Transactional
    public int createSchedule(ScheduleDto schedule) {
        if (schedule == null) throw new IllegalArgumentException("요청 데이터가 없습니다.");

        // (A) 휴가: 기간 중복 선검사
        if ("VACATION".equalsIgnoreCase(schedule.getCodeBid())) {
            int dup = scheduleDao.countVacationOverlap(
                schedule.getEmpNum(),
                schedule.getStartTime(),
                schedule.getEndTime()
            );
            if (dup > 0) throw new IllegalStateException("해당 기간에 이미 휴가가 등록되어 있습니다.");
        }

        // (B) 공통: 트레이너 시간 겹침 방지 - DB에서 강제
        int inserted = scheduleDao.insertIfNoOverlap(schedule);
        if (inserted == 0) {
            throw new IllegalStateException("해당 트레이너의 같은 시간대에 이미 일정이 있습니다.");
        }
        System.out.println("[일정 등록 완료] shNum=" + schedule.getShNum() + ", codeBid=" + schedule.getCodeBid());

        // (C) PT가 아니면 여기서 끝
        if (!"SCHEDULE-PT".equalsIgnoreCase(schedule.getCodeBid())) {
            return inserted; // 보통 1
        }

        // (D) PT 필수값 검사
        if (schedule.getMemNum() == null) {
            throw new IllegalStateException("PT 예약에는 회원(memNum)이 필요합니다.");
        }

        // (E) 사전 상태 체크(로그용): 회원권/잔여 PT
        Long mem = schedule.getMemNum().longValue();
        Integer voucherValid = session.selectOne("LogMapper.checkVoucherValid", mem);
        Integer remainingPt  = session.selectOne("LogMapper.selectRemainingPtCount", mem);
        System.out.printf("[사전체크] memNum=%d, voucherValid=%d, remainingPt=%d%n",
                mem,
                (voucherValid == null ? 0 : voucherValid),
                (remainingPt == null ? 0 : remainingPt));

        // (F) REGISTRATION 조건부 INSERT (회원권 유효, 잔여 PT>0, 중복 등록 방지)
        PtRegistrationDto reg = PtRegistrationDto.builder()
            .empNum((long) schedule.getEmpNum())
            .memNum(mem)
            .shNum((long) schedule.getShNum())
            .regNote(schedule.getMemo())
            .build();

        int rows = session.insert("PtRegistrationMapper.insertRegistrationIfAllowed", reg);
        if (rows == 0) {
            System.out.printf("[차단사유] memNum=%d, voucherValid=%d, remainingPt=%d, shNum=%d%n",
                    mem,
                    (voucherValid == null ? 0 : voucherValid),
                    (remainingPt == null ? 0 : remainingPt),
                    schedule.getShNum());
            throw new IllegalStateException("유효한 회원권이 없거나 잔여 PT 회차가 없거나 이미 등록된 일정입니다.");
        }
        System.out.println("[PT 예약 등록 완료] regNum=" + reg.getRegNum());

        // (G) PT_LOG 소비 -1 (동시성까지 DB에서 보장)
        PtLogDto consume = PtLogDto.builder()
            .memNum(mem)
            .empNum((long) schedule.getEmpNum())
            .regId(reg.getRegNum().longValue())
            .status("소비")
            .countChange(-1L)
            .build();

        int logRows = session.insert("LogMapper.insertPtConsumeLog", consume);
        if (logRows == 0) {
            throw new IllegalStateException("남은 PT 회차가 없어 예약할 수 없습니다.");
        }

        return inserted;
    }

    // --------------------------- 수정 ---------------------------
    @Transactional
    @Override
    public int updateSchedule(ScheduleDto schedule) {
        if (schedule == null) throw new IllegalArgumentException("요청 데이터가 없습니다.");

        // Long → int 안전 변환 (오버플로 시 즉시 예외로 데이터 정합성 보장)
        if (schedule.getShNum() == null) {
            throw new IllegalArgumentException("shNum이 없습니다.");
        }
        final int shNum = Math.toIntExact(schedule.getShNum());  

        // 기존 스케줄 조회 (int 사용)
        ScheduleDto oldSchedule = scheduleDao.selectByShNum(shNum);

        boolean wasPt = oldSchedule != null && "SCHEDULE-PT".equalsIgnoreCase(oldSchedule.getCodeBid());
        boolean nowPt = "SCHEDULE-PT".equalsIgnoreCase(schedule.getCodeBid());

        // VACATION: 자기 자신 제외 중복검사 (int 사용)
        if ("VACATION".equalsIgnoreCase(schedule.getCodeBid())) {
            int dup = scheduleDao.countVacationOverlapExcludingSelf(
                schedule.getEmpNum(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                shNum
            );
            if (dup > 0) throw new IllegalStateException("해당 기간에 이미 휴가가 등록되어 있습니다.");
        }

        // PT 일정은 회원 필수
        if (nowPt && schedule.getMemNum() == null) {
            throw new IllegalStateException("PT 일정은 회원(memNum)이 필요합니다.");
        }

        // 시간 겹침 금지 업데이트 (schedule 객체는 그대로 사용)
        int updated = scheduleDao.updateIfNoOverlap(schedule);
        if (updated == 0) {
            throw new IllegalStateException("해당 트레이너의 같은 시간대에 이미 다른 일정이 있어 수정할 수 없습니다.");
        }

        // PT → PT 이면서 회원 변경 시에만 추가 처리
        if (wasPt && nowPt
                && oldSchedule.getMemNum() != null
                && !Objects.equals(oldSchedule.getMemNum(), schedule.getMemNum())) {

            // ✅ regNum 조회에도 int 사용
            Integer regNum = ptRegistrationService.findRegNumByShNum(shNum);
            if (regNum == null) {
                throw new IllegalStateException("등록된 PT 예약 정보가 없습니다.");
            }

            // 기존 회원 예약취소(+1)
            PtLogDto cancelLog = PtLogDto.builder()
                    .memNum(oldSchedule.getMemNum().longValue())
                    .empNum((long) schedule.getEmpNum())
                    .regId(regNum.longValue())
                    .status("예약취소")
                    .countChange(1L)
                    .build();
            logDao.insertPtCancelLog(cancelLog);

            // REGISTRATION의 memNum을 새 회원으로 교체
            ptRegistrationService.updateRegistrationMemNum(regNum, schedule.getMemNum());

            // 새 회원 권리/잔여 PT 확인
            Long newMem = schedule.getMemNum().longValue();
            Integer voucherValid = session.selectOne("LogMapper.checkVoucherValid", newMem);
            Integer remainingPt  = session.selectOne("LogMapper.selectRemainingPtCount", newMem);
            if (voucherValid == null || voucherValid <= 0
                    || remainingPt == null || remainingPt <= 0) {
                throw new IllegalStateException("유효한 회원권이 없거나 남은 PT 회차가 없어 회원 변경을 수행할 수 없습니다.");
            }

            // 새 회원 소비(-1)
            PtLogDto consumeLog = PtLogDto.builder()
                    .memNum(newMem)
                    .empNum((long) schedule.getEmpNum())
                    .regId(regNum.longValue())
                    .status("소비")
                    .countChange(-1L)
                    .build();
            logDao.insertPtConsumeLog(consumeLog);
        }

        System.out.println("[일정 수정 완료] shNum=" + shNum);
        return updated;
    }


    // --------------------------- 삭제 ---------------------------
    @Override
    @Transactional
    public int deleteSchedule(int shNum) {
        // 삭제 대상 확인
        ScheduleDto target = scheduleDao.selectByShNum(shNum);
        if (target == null) throw new IllegalArgumentException("존재하지 않는 일정입니다.");

        System.out.println("[일정 삭제 요청] shNum=" + shNum + ", type=" + target.getCodeBid());

        // PT 일정인 경우만 추가 로직 수행
        if ("SCHEDULE-PT".equalsIgnoreCase(target.getCodeBid())) {
            Integer regNum = ptRegistrationService.findRegNumByShNum(shNum);

            if (regNum != null) {
                // 회원이 존재할 때만 PT_LOG에 예약취소(+1) 기록
                if (target.getMemNum() != null) {
                    PtLogDto cancelLog = PtLogDto.builder()
                        .memNum(target.getMemNum().longValue())
                        .empNum((long) target.getEmpNum())
                        .regId(regNum.longValue())
                        .status("예약취소")
                        .countChange(1L)
                        .build();
                    logDao.insertPtCancelLog(cancelLog);
                    System.out.println("[PT 예약취소 로그 등록 완료] regNum=" + regNum);
                } else {
                    System.out.println("[회원 없는 일정 → PT 로그 생략]");
                }

                // REGISTRATION 삭제
                ptRegistrationService.deletePtRegistration(regNum);
                System.out.println("[PT 등록 데이터 삭제 완료]");
            } else {
                System.out.println("[PT 등록번호 없음 → 로그 등록 생략]");
            }
        }

        // SCHEDULE 삭제
        int deleted = scheduleDao.delete(shNum);
        System.out.println("[일정 삭제 완료] deleted=" + deleted);
        return deleted;
    }
}
