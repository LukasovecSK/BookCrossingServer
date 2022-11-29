package io.github.enkarin.bookcrossing;

import io.github.enkarin.bookcrossing.base.BookCrossingBaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

class BookCrossingServerApplicationTest extends BookCrossingBaseTest {

    @Test
    void contextLoads(@Autowired final ApplicationContext context) {
        assertThat(context).isNotNull();
    }
}
