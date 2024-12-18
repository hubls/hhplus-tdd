package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final Map<Long, Lock> userLocks = new ConcurrentHashMap<>();

    public UserPoint chargeUserPoint(long userId, long amount) {
        // 사용자별 Lock 가져오기 (없으면 생성)
        Lock lock = userLocks.computeIfAbsent(userId, id -> new ReentrantLock(true));

        // 다른 쓰레드가 접근 못하도록 제어
        log.info("Attempting to acquire lock for user ID: {}", userId);
        lock.lock();
        log.info("Lock acquired for user ID: {}", userId);
        try {
            // 실제 비즈니스 로직
            UserPoint userPoint = userPointRepository.findById(userId);
            UserPoint updatedUserPoint = userPoint.charge(amount);
            pointHistoryRepository.save(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());
            return userPointRepository.saveOrUpdate(userId, updatedUserPoint.point());
        } catch (IllegalArgumentException e) {
            log.error("Error charging user points for user {}: {}", userId, e.getMessage());
            throw e; // 예외를 다시 던진다.
        } finally {
            lock.unlock();
            log.info("Lock released for user ID: {}", userId);
        }
    }

    public UserPoint useUserPoint(long userId, long amount) {
        // 사용자별 Lock 가져오기 (없으면 생성)
        Lock lock = userLocks.computeIfAbsent(userId, id -> new ReentrantLock(true));

        // 다른 쓰레드가 접근 못하도록 제어
        log.info("Attempting to acquire lock for user ID: {}", userId);
        lock.lock();
        log.info("Lock acquired for user ID: {}", userId);
        try {
            // 실제 비즈니스 로직
            UserPoint userPoint = userPointRepository.findById(userId);
            UserPoint updatedUserpoint = userPoint.use(amount);
            pointHistoryRepository.save(userId, amount, TransactionType.USE, System.currentTimeMillis());
            return userPointRepository.saveOrUpdate(userId, updatedUserpoint.point());
        } catch (IllegalArgumentException e) {
            log.error("Error using user points for user {}: {}", userId, e.getMessage());
            throw e; // 예외를 다시 던진다.
        } finally {
            lock.unlock();
            log.info("Lock released for user ID: {}", userId);
        }
    }
}
