package io.github.enkarin.bookcrossing.books.controllers;

import io.github.enkarin.bookcrossing.base.BookCrossingBaseTest;
import io.github.enkarin.bookcrossing.books.dto.BookFiltersRequest;
import io.github.enkarin.bookcrossing.books.dto.BookModelDto;
import io.github.enkarin.bookcrossing.books.service.BookService;
import io.github.enkarin.bookcrossing.support.TestDataProvider;
import io.github.enkarin.bookcrossing.user.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookControllerTest extends BookCrossingBaseTest {

    @Autowired
    private BookService bookService;

    @Test
    void booksShouldnWork() {
        final var user = TestDataProvider.buildUsers().stream()
                .map(this::createAndSaveUser)
                .findAny()
                .orElseThrow();
        enabledUser(user.getUserId());

        final var booksId = createAndSaveBooks(user.getLogin());
        final var response = webClient.get()
                .uri("/books/all")
                .exchange()
                .expectStatus().isEqualTo(200)
                .expectBodyList(BookModelDto.class)
                .returnResult().getResponseBody();
        assertThat(response)
                .hasSize(3)
                .isEqualTo(TestDataProvider.buildBookModels(booksId.get(0), booksId.get(1), booksId.get(2)));
    }

    @Test
    void booksShouldnWorkWithEmptyTableBook() {
        final var response = webClient.get()
                .uri("/books/all")
                .exchange()
                .expectStatus().isEqualTo(200)
                .expectBodyList(BookModelDto.class)
                .returnResult().getResponseBody();
        assertThat(response)
                .isEmpty();
    }

    @Test
    void bookInfoShouldnWork() {
        final var user = TestDataProvider.buildUsers().stream()
                .map(this::createAndSaveUser)
                .findAny()
                .orElseThrow();
        enabledUser(user.getUserId());
        final var bookId = bookService.saveBook(TestDataProvider.buildDorian(), user.getLogin()).getBookId();
        final var response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .pathSegment("books", "info")
                        .queryParam("bookId", String.valueOf(bookId))
                        .build())
                .exchange()
                .expectStatus().isEqualTo(200)
                .expectBody(BookModelDto.class)
                .returnResult().getResponseBody();
        assertThat(response)
                .isEqualTo(TestDataProvider.buildDorian(bookId));
    }

    @Test
    void bookInfoShouldnFailBecauseBookNotFound() {
        webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .pathSegment("books", "info")
                        .queryParam("bookId", String.valueOf(Integer.MAX_VALUE))
                        .build())
                .exchange()
                .expectStatus().isEqualTo(404)
                .expectBody()
                .jsonPath("$.book")
                .isEqualTo("Книга не найдена");
    }

    @Test
    void searchByTitle() {
        final var user = TestDataProvider.buildUsers().stream()
                .map(this::createAndSaveUser)
                .findAny()
                .orElseThrow();
        enabledUser(user.getUserId());
        final var firstBookId = bookService.saveBook(TestDataProvider.buildDandelion(), user.getLogin()).getBookId();
        final var secondBookId = bookService.saveBook(TestDataProvider.buildDandelion(), user.getLogin()).getBookId();
        final var response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .pathSegment("books", "searchByTitle")
                        .queryParam("title", "Dandelion")
                        .build())
                .exchange()
                .expectStatus().isEqualTo(200)
                .expectBodyList(BookModelDto.class)
                .returnResult().getResponseBody();
        assertThat(response)
                .hasSize(2)
                .isEqualTo(List.of(TestDataProvider.buildDandelion(firstBookId),
                        TestDataProvider.buildDandelion(secondBookId)));
    }

    @Test
    void searchByTitleShouldnWorkWithoutBooks() {
        final var response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .pathSegment("books", "searchByTitle")
                        .queryParam("title", "TestName") //books not contains in db
                        .build())
                .exchange()
                .expectStatus().isEqualTo(200)
                .expectBodyList(BookModelDto.class)
                .returnResult().getResponseBody();
        assertThat(response)
                .isEmpty();
    }

    @Test
    void searchWithFiltersShouldnWork() {
        final var user = TestDataProvider.buildUsers().stream()
                .map(this::createAndSaveUser)
                .map(UserDto::getLogin)
                .findAny()
                .orElseThrow();

        final var booksId = createAndSaveBooks(user);

        final var response = webClient
                .method(HttpMethod.GET)
                .uri(uriBuilder -> uriBuilder
                        .pathSegment("books", "searchWithFilters")
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(BookFiltersRequest.create("Novosibirsk", "Wolves",
                        "author", "story", "publishing_house", 2000))
                .exchange()
                .expectStatus().isEqualTo(200)
                .expectBodyList(BookModelDto.class)
                .returnResult().getResponseBody();

        assertThat(response)
                .hasSize(1)
                .containsOnly(TestDataProvider.buildWolves(booksId.get(2)));
    }
}
