package com.iccues.metaanimebackend.controller;

import com.iccues.metaanimebackend.common.Response;
import com.iccues.metaanimebackend.dto.admin.AdminAnimeDTO;
import com.iccues.metaanimebackend.dto.admin.AnimeCreateRequest;
import com.iccues.metaanimebackend.dto.admin.AnimeUpdateRequest;
import com.iccues.metaanimebackend.entity.Anime;
import com.iccues.metaanimebackend.mapper.AdminAnimeMapper;
import com.iccues.metaanimebackend.repo.MappingRepository;
import com.iccues.metaanimebackend.repo.AnimeRepository;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/admin")
public class AdminAnimeController {

    @Resource
    AnimeRepository animeRepository;

    @Resource
    AdminAnimeMapper adminAnimeMapper;

    @ResponseBody
    @GetMapping("/get_anime_list")
    public Response<List<AdminAnimeDTO>> getAnimeList() {
        List<Anime> animeList = animeRepository.findAll();
        List<AdminAnimeDTO> animeDtoList = adminAnimeMapper.toAnimeDtoList(animeList);
        return Response.ok(animeDtoList);
    }

    @ResponseBody
    @PostMapping("/create_anime")
    public Response<AdminAnimeDTO> createAnime(@Valid @RequestBody AnimeCreateRequest request) {
        Anime anime = new Anime();
        anime.setTitle(request.title());
        anime.setCoverImage(request.coverImage());
        anime.setStartDate(request.startDate());

        Anime savedAnime = animeRepository.save(anime);
        AdminAnimeDTO animeDto = adminAnimeMapper.toAnimeDto(savedAnime);
        return Response.ok(animeDto);
    }

    @ResponseBody
    @PutMapping("/update_anime")
    public Response<AdminAnimeDTO> updateAnime(@Valid @RequestBody AnimeUpdateRequest request) {
        Anime anime = animeRepository.findById(request.animeId())
                .orElseThrow(() -> new RuntimeException("Anime not found with id: " + request.animeId()));

        if (request.title() != null) {
            anime.setTitle(request.title());
        }
        if (request.coverImage() != null) {
            anime.setCoverImage(request.coverImage());
        }
        if (request.startDate() != null) {
            anime.setStartDate(request.startDate());
        }

        Anime updatedAnime = animeRepository.save(anime);
        AdminAnimeDTO animeDto = adminAnimeMapper.toAnimeDto(updatedAnime);
        return Response.ok(animeDto);
    }

    @ResponseBody
    @DeleteMapping("/delete_anime/{animeId}")
    public Response<Void> deleteAnime(@PathVariable Long animeId) {
        if (!animeRepository.existsById(animeId)) {
            throw new RuntimeException("Anime not found with id: " + animeId);
        }
        animeRepository.deleteById(animeId);
        return Response.ok(null);
    }
}
