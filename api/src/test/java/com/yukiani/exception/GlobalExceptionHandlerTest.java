package com.yukiani.exception;

import com.yukiani.common.Response;
import com.yukiani.entity.Platform;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Anime", 123L);

        ResponseEntity<Response<Void>> response = handler.handleBusinessException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("NOT_FOUND", response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("Anime"));
        assertTrue(response.getBody().getMessage().contains("123"));
    }

    @Test
    void handleResourceAlreadyExistsException() {
        ResourceAlreadyExistsException ex = new ResourceAlreadyExistsException("Mapping", "MAL-12345");

        ResponseEntity<Response<Void>> response = handler.handleBusinessException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("ALREADY_EXISTS", response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("Mapping"));
    }

    @Test
    void handleUnsupportedPlatformException() {
        UnsupportedPlatformException ex = new UnsupportedPlatformException(Platform.Bangumi);

        ResponseEntity<Response<Void>> response = handler.handleBusinessException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("UNSUPPORTED_PLATFORM", response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("Bangumi"));
    }

    @Test
    void handleFetchFailedException() {
        FetchFailedException ex = new FetchFailedException(Platform.MyAnimeList, "12345");

        ResponseEntity<Response<Void>> response = handler.handleBusinessException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("FETCH_FAILED", response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("MyAnimeList"));
        assertTrue(response.getBody().getMessage().contains("12345"));
    }

    @Test
    void handleValidationException() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("request", "title", "不能为空");
        FieldError fieldError2 = new FieldError("request", "startDate", "格式不正确");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        ResponseEntity<Response<Void>> response = handler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("VALIDATION_ERROR", response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("title"));
        assertTrue(response.getBody().getMessage().contains("startDate"));
    }

    @Test
    void handleValidationException_NoFieldErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        ResponseEntity<Response<Void>> response = handler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("VALIDATION_ERROR", response.getBody().getCode());
        assertEquals("参数校验失败", response.getBody().getMessage());
    }

    @Test
    void handleOptimisticLockException() {
        ObjectOptimisticLockingFailureException ex = new ObjectOptimisticLockingFailureException("Anime", 1L);

        ResponseEntity<Response<Void>> response = handler.handleOptimisticLockException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("CONFLICT", response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("刷新"));
    }

    @Test
    void handleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("无效的参数值");

        ResponseEntity<Response<Void>> response = handler.handleIllegalArgumentException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("INVALID_ARGUMENT", response.getBody().getCode());
        assertEquals("无效的参数值", response.getBody().getMessage());
    }

    @Test
    void handleGenericException() {
        Exception ex = new RuntimeException("未知错误");

        ResponseEntity<Response<Void>> response = handler.handleException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("INTERNAL_ERROR", response.getBody().getCode());
        assertEquals("服务器内部错误", response.getBody().getMessage());
    }
}
