-- 멀티상품 원자 차감(전부 충분할 때만 전부 차감, all-or-nothing).
-- KEYS = [ stock:{p1}, ..., stock:{pn} ]
-- ARGV = [ qty1, ..., qtyn ]
-- 반환: 1=SUCCESS, -1=NOT_LOADED, -(100+i)=INSUFFICIENT(i번째 상품, 1-based)
--
-- 멱등성 미보장: order.created 재전달 시 같은 주문이 중복 차감될 수 있다(보수적 under-count,
-- 오버셀 아님). 정확히-한-번은 추후 outbox + 컨슈머별 처리오프셋으로 일괄 보강한다.
local n = #KEYS

for i = 1, n do
    local s = redis.call('GET', KEYS[i])
    if not s then
        return -1
    end
    if tonumber(s) < tonumber(ARGV[i]) then
        return -(100 + i)
    end
end

for i = 1, n do
    redis.call('DECRBY', KEYS[i], tonumber(ARGV[i]))
end

return 1
