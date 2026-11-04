package com.yukiani.server.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 平台及平台内条目 ID 组成的值对象，整体标识一个外部平台条目。
 *
 * <p>不提供单字段修改方法，避免平台与条目 ID 被分别修改。
 * 抓取阶段允许 platformId 为空，由调用方在持久化前过滤。</p>
 */
@Getter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class PlatformKey {
    @Enumerated(EnumType.STRING)
    private Platform platform;

    private String platformId;
}
