package com.yukiani.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class MappingInfo {
    @Embedded
    AnimeTitles title;
    String coverImage;
    LocalDate startDate;
}
