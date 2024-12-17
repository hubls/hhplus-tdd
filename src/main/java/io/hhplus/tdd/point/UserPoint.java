package io.hhplus.tdd.point;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {
    private static final long MAX_POINT = 1_000_000L;
    private static final long MIN_POINT = 0L;

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    public UserPoint charge(long amount) {
        if (amount <= MIN_POINT) {
            throw new IllegalArgumentException("충전할 포인트가 0보다 작습니다.");
        }

        long updatePoint = this.point + amount;

        if (updatePoint >= MAX_POINT) {
            throw new IllegalArgumentException(String.format("충전가능한 최대 포인트 %d을 초과했습니다. 충전 가능 포인트: %d", MAX_POINT, MAX_POINT - this.point));
        }

        return new UserPoint(id, updatePoint, System.currentTimeMillis());
    }
}
