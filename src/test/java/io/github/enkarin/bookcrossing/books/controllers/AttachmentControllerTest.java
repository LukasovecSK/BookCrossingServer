package io.github.enkarin.bookcrossing.books.controllers;

import io.github.enkarin.bookcrossing.books.dto.AttachmentMultipartDto;
import io.github.enkarin.bookcrossing.books.service.AttachmentService;
import io.github.enkarin.bookcrossing.exception.AttachmentNotFoundException;
import io.github.enkarin.bookcrossing.exception.BadRequestException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.Principal;

@ExtendWith(MockitoExtension.class)
class AttachmentControllerTest {

    @InjectMocks
    private AttachmentController attachmentController;

    @Mock
    private AttachmentService attachmentService;

    @Mock
    private Principal principal;

    private static final String USER_NAME = "user1";

    @BeforeEach
    void setUp(){
        Mockito.when(this.principal.getName()).thenReturn(USER_NAME);
    }

    @Test
    void testSaveAttachment201() throws IOException {
        final File file = ResourceUtils.getFile("classpath:files/image.jpg");
        final MultipartFile multipartFile = new MockMultipartFile(file.getName(), file.getName(), "image/jpg", Files.readAllBytes(file.toPath()));
        final AttachmentMultipartDto attachmentMultipartDto = AttachmentMultipartDto.fromFile(123, multipartFile);

        final ResponseEntity<Void> response = this.attachmentController.saveAttachment(attachmentMultipartDto , this.principal);

        Mockito.verify(this.attachmentService).saveAttachment(attachmentMultipartDto, USER_NAME);

        Assertions.assertEquals(201, response.getStatusCodeValue());
    }

    @Test
    void testSaveAttachment404() throws IOException {
        final File file = ResourceUtils.getFile("classpath:files/black.bmp");
        final MultipartFile multipartFile = new MockMultipartFile(file.getName(), file.getName(), "image/bmp", Files.readAllBytes(file.toPath()));
        final AttachmentMultipartDto attachmentMultipartDto = AttachmentMultipartDto.fromFile(1234, multipartFile);

        Mockito.when(this.attachmentService.saveAttachment(attachmentMultipartDto, USER_NAME)).thenThrow(AttachmentNotFoundException.class);

        Assertions.assertThrows(AttachmentNotFoundException.class, () ->  this.attachmentController.saveAttachment(attachmentMultipartDto , this.principal));
    }

    @Test
    void testSaveAttachment415() throws IOException {
        final AttachmentMultipartDto attachmentMultipartDto = Mockito.mock(AttachmentMultipartDto.class);

        Mockito.when(this.attachmentService.saveAttachment(attachmentMultipartDto, USER_NAME)).thenThrow(BadRequestException.class);

        Assertions.assertThrows(BadRequestException.class, () ->  this.attachmentController.saveAttachment(attachmentMultipartDto , this.principal));
    }

    @Test
    void testDeleteAttachment201() {
        final int bookId = 123;

        final ResponseEntity<Void> response = this.attachmentController.deleteAttachment(bookId , this.principal);

        Mockito.verify(this.attachmentService).deleteAttachment(bookId, USER_NAME);

        Assertions.assertEquals(200, response.getStatusCodeValue());
    }
}
