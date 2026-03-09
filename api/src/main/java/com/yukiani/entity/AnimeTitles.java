package com.yukiani.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class AnimeTitles {
    String titleNative;
    String titleRomaji;
    String titleEn;
    String titleCn;

    public void merge(AnimeTitles b) {
        titleNative = titleNative != null ? titleNative : b.titleNative;
        titleRomaji = titleRomaji != null ? titleRomaji : b.titleRomaji;
        titleEn     = titleEn     != null ? titleEn     : b.titleEn;
        titleCn     = titleCn     != null ? titleCn     : b.titleCn;
    }
}
