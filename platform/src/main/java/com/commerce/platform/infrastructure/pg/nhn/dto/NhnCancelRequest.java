package com.commerce.platform.infrastructure.pg.nhn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * NHN 결제 취소 요청
 */
@Builder
public record NhnCancelRequest (
        @JsonProperty("site_cd")
        String site_cd,

        @JsonProperty("tno")
        String tno,

        @JsonProperty("kcp_cert_info")
        String kcp_cert_info,

        @JsonProperty("kcp_sign_data")
        String kcp_sign_data,

        @JsonProperty("mod_type")
        String mod_type
) {}
