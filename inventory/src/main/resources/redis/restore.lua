-- 멀티상품 원자 복원 + 복원 1회.
-- KEYS = [ stock:{p1}, ..., stock:{pn}, restored:{orderId} ]
-- ARGV = [ ttl, qty1, ..., qtyn ]
-- 반환: 1=RESTORED, 0=SKIPPED(이미 복원)
--
-- deduct가 더 이상 차감 마커를 남기지 않으므로 '차감된 적 있는지' 팬텀 가드는 제거됐다.
-- 대신 saga가 차감 성공(inventory.reserved) 경로에서만 복원을 트리거한다는 전제에 의존한다.
-- restore-once(rmark)는 재전달 시 중복 복원(오버셀 방향)을 막는 잠정 가드로, 추후 outbox/처리오프셋으로 이관한다.
local n = #KEYS - 1
local rmark = KEYS[n + 1]
local ttl = tonumber(ARGV[1])

-- 이미 복원된 주문은 다시 복원하지 않는다
if redis.call('EXISTS', rmark) == 1 then
    return 0
end

for i = 1, n do
    redis.call('INCRBY', KEYS[i], tonumber(ARGV[i + 1]))
end

redis.call('SET', rmark, '1')
redis.call('EXPIRE', rmark, ttl)
return 1
