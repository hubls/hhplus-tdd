package io.hhplus.tdd.point.repository;

import io.hhplus.tdd.point.UserPoint;

public interface UserPointRepository {
    UserPoint findById(long id);
    UserPoint saveOrUpdate(long id, long amount);
}
