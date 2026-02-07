package io.github.kusoroadeolu.ferrous.option;

import io.github.kusoroadeolu.ferrous.result.Err;
import io.github.kusoroadeolu.ferrous.result.Ok;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class SomeTest {

    @Nested
    public class BasicOperations {
        @Test
        public void shouldReturnTrue_onIsSome() {
            Option<String> opt = Option.some("value");
            assertTrue(opt.isSome());
        }

        @Test
        public void shouldReturnFalse_onIsNone() {
            Option<String> opt = Option.some("value");
            assertFalse(opt.isNone());
        }

        @Test
        public void shouldReturnValue_whenUnwrapped() {
            String value = Option.some("test").unwrap();
            assertEquals("test", value);
        }

        @Test
        public void shouldReturnValue_whenUnwrappedOr() {
            String value = Option.some("test").unwrapOr("fallback");
            assertEquals("test", value);
        }

        @Test
        public void shouldReturnValue_whenUnwrappedOrElse() {
            String value = Option.some("test").unwrapOrElse(() -> "fallback");
            assertEquals("test", value);
        }

        @Test
        public void shouldReturnValue_whenExpect() {
            String value = Option.some("test").expect("should not throw");
            assertEquals("test", value);
        }

        @Test
        public void shouldReturnValue_onToNullable() {
            String value = Option.some("test").toNullable();
            assertEquals("test", value);
        }
    }

    @Nested
    public class MappingAndFiltering {
        @Test
        public void shouldMapValue_whenMapping() {
            Option<Integer> val = Option.some("hello").map(String::length);
            assertInstanceOf(Some.class, val);
            assertEquals(5, val.unwrap());
        }

        @Test
        public void shouldFlatMapValue_whenFlatMapping() {
            Option<Integer> val = Option.some("hello")
                .flatMap(s -> Option.some(s.length()));
            assertInstanceOf(Some.class, val);
            assertEquals(5, val.unwrap());
        }

        @Test
        public void shouldReturnNone_whenFlatMappingReturnsNone() {
            Option<Integer> val = Option.some("hello")
                .flatMap(s -> Option.none());
            assertInstanceOf(None.class, val);
        }

        @Test
        public void shouldReturnSome_whenFilterPasses() {
            Option<String> val = Option.some("hello").filter(s -> s.length() > 3);
            assertInstanceOf(Some.class, val);
            assertEquals("hello", val.unwrap());
        }

        @Test
        public void shouldReturnNone_whenFilterFails() {
            Option<String> val = Option.some("hi").filter(s -> s.length() > 3);
            assertInstanceOf(None.class, val);
        }

        @Test
        public void shouldReturnOther_onAnd() {
            Option<Integer> other = Option.some(42);
            Option<Integer> val = Option.some("test").and(other);
            assertEquals(other, val);
        }

        @Test
        public void shouldReturnNone_onAndWithNone() {
            Option<Integer> other = Option.none();
            Option<Integer> val = Option.some("test").and(other);
            assertInstanceOf(None.class, val);
        }

        @Test
        public void shouldApplyFunction_onAndThen() {
            Option<Integer> val = Option.some("hello")
                .andThen(s -> Option.some(s.length()));
            assertInstanceOf(Some.class, val);
            assertEquals(5, val.unwrap());
        }

        @Test
        public void shouldReturnNone_onAndThenReturningNone() {
            Option<Integer> val = Option.some("hello")
                .andThen(s -> Option.none());
            assertInstanceOf(None.class, val);
        }

        @Test
        public void shouldReturnOkWithSome_onTranspose() {
            var innerResult = new Ok<String, Integer>("value");
            var result = Option.some(innerResult).transpose();
            assertInstanceOf(Ok.class, result);
            assertInstanceOf(Some.class, result.unwrap());
            assertEquals("value", result.unwrap().unwrap());
        }

        @Test
        public void shouldReturnErrWithError_onTransposeWithErr() {
            var innerResult = new Err<String, Integer>(42);
            var result = Option.some(innerResult).transpose();
            assertInstanceOf(Err.class, result);
            assertEquals(42, result.unwrapErr());
        }
    }

    @Nested
    public class Conditionals {
        @Test
        public void shouldReturnThis_onOr() {
            Option<String> some = Option.some("first");
            Option<String> other = Option.some("second");
            Option<String> val = some.or(other);
            assertEquals(some, val);
        }

        @Test
        public void shouldReturnThis_onOrElse() {
            Option<String> some = Option.some("first");
            Option<String> val = some.orElse(() -> Option.some("second"));
            assertEquals(some, val);
        }

        @Test
        public void shouldReturnTrue_onContainsWithEqualValue() {
            assertTrue(Option.some("test").contains("test"));
        }

        @Test
        public void shouldReturnFalse_onContainsWithDifferentValue() {
            assertFalse(Option.some("test").contains("other"));
        }

        @Test
        public void shouldReturnOk_onOkOr() {
            var res = Option.some("value").okOr("error");
            assertInstanceOf(Ok.class, res);
            assertEquals("value", res.unwrap());
        }

        @Test
        public void shouldReturnOk_onOkOrElse() {
            var res = Option.some("value").okOrElse(() -> "error");
            assertInstanceOf(Ok.class, res);
            assertEquals("value", res.unwrap());
        }

        @Test
        public void shouldReturnThis_onXorWithNone() {
            Option<String> some = Option.some("value");
            Option<String> val = some.xor(Option.none());
            assertEquals(some, val);
        }

        @Test
        public void shouldReturnThis_onXorWithSome() {
            Option<String> some = Option.some("first");
            Option<String> other = Option.some("second");
            Option<String> val = some.xor(other);
            assertEquals(some, val);
        }

        @Test
        public void shouldConsumeValue_onIfSome() {
            List<String> ls = new ArrayList<>();
            Option.some("test").ifSome(ls::add);
            assertEquals(1, ls.size());
            assertEquals("test", ls.get(0));
        }

        @Test
        public void shouldNotRun_onIfNone() {
            List<Integer> ls = new ArrayList<>();
            Option.some("test").ifNone(() -> ls.add(1));
            assertEquals(0, ls.size());
        }

        @Test
        public void shouldCallConsumer_onInspect() {
            List<String> ls = new ArrayList<>();
            Option<String> opt = Option.some("test").inspect(ls::add);
            assertInstanceOf(Some.class, opt);
            assertEquals(1, ls.size());
            assertEquals("test", ls.get(0));
        }
    }

    @Nested
    public class ZippingOperations {
        @Test
        public void shouldZipTwoSomes() {
            Option<Pair<String, Integer>> zipped = 
                Option.some("hello").zip(Option.some(5));
            assertInstanceOf(Some.class, zipped);
            Pair<String, Integer> pair = zipped.unwrap();
            assertEquals("hello", pair.a());
            assertEquals(5, pair.b());
        }

        @Test
        public void shouldReturnNone_onZipWithNone() {
            Option<Pair<String, Integer>> zipped = 
                Option.some("hello").zip(Option.none());
            assertInstanceOf(None.class, zipped);
        }

        @Test
        public void shouldZipWithFunction() {
            Option<String> zipped = Option.some("hello")
                .zipWith(Option.some(5), (s, i) -> s + i);
            assertInstanceOf(Some.class, zipped);
            assertEquals("hello5", zipped.unwrap());
        }

    }

    @Nested
    public class ConversionOperations {
        @Test
        public void shouldConvertToStream() {
            Stream<String> stream = Option.some("test").stream();
            assertEquals(1, stream.count());
        }

        @Test
        public void shouldConvertToStreamWithValue() {
            Stream<String> stream = Option.some("test").stream();
            assertEquals("test", stream.findFirst().orElse(null));
        }

        @Test
        public void shouldConvertToOptional() {
            Optional<String> optional = Option.some("test").toOptional();
            assertTrue(optional.isPresent());
            assertEquals("test", optional.get());
        }
    }
}