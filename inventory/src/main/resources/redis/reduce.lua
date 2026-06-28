-- 단건 원자 차감(관리자 보정용). 재고가 충분할 때만 차감.
-- KEYS = [ stock:{productId} ]
-- ARGV = [ qty ]
-- 반환: 1=SUCCESS, 0=INSUFFICIENT, -1=NOT_LOADED
local s = redis.call('GET', KEYS[1])
if not s then
    return -1
end
if tonumber(s) < tonumber(ARGV[1]) then
    return 0
end
redis.call('DECRBY', KEYS[1], tonumber(ARGV[1]))
return 1
