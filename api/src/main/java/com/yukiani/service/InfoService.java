package com.yukiani.service;

import com.yukiani.entity.*;
import org.springframework.stereotype.Service;

@Service
public class InfoService {
    public void aggregateInfo(Anime anime) {
        cleanInfo(anime);
        for (Mapping mapping : anime.getMappings()) {
            applyMappingInfo(anime, mapping);
        }
        setCoverImageFromBangumi(anime);
    }

    public void applyMappingInfo(Anime anime, Mapping mapping) {
        MappingInfo mappingInfo = mapping.getMappingInfo();
        if (mappingInfo == null) {
            return;
        }

        if (anime.getCoverImage() == null) {
            anime.setCoverImage(mappingInfo.getCoverImage());
        }
        if (anime.getStartDate() == null) {
            anime.setStartDate(mappingInfo.getStartDate());
        }

        if (anime.getTitle() == null) {
            anime.setTitle(new AnimeTitles());
        }
        anime.getTitle().merge(mappingInfo.getTitle());
    }

    public void cleanInfo(Anime anime) {
        anime.setCoverImage(null);
        anime.setStartDate(null);
        anime.setTitle(new AnimeTitles());
    }

    public void setCoverImageFromBangumi(Anime anime) {
        Mapping bangumi = anime.getMappingByPlatform(Platform.Bangumi);
        if (bangumi != null && bangumi.getMappingInfo() != null) {
            anime.setCoverImage(bangumi.getMappingInfo().getCoverImage());
        }
    }
}
