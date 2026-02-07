package io.github.kusoroadeolu.ferrous.option;

import io.github.kusoroadeolu.ferrous.result.Err;
import io.github.kusoroadeolu.ferrous.result.Ok;
import io.github.kusoroadeolu.ferrous.result.Result;
import io.github.kusoroadeolu.ferrous.result.ResultException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class OkTest {

    @Nested
    public class BasicOperations {
        @Test
        public void shouldReturnTrue_onIsOk() {
            Result<String, Integer> result = Result.ok("value");
            assertTrue(result.isOk());
        }

        @Test
        public void shouldReturnFalse_onIsErr() {
            Result<String, Integer> result = Result.ok("value");
            assertFalse(result.isErr());
        }

        @Test
        public void shouldReturnValue_whenUnwrapped() {
            String value = Result.<String, Integer>ok("test").unwrap();
            assertEquals("test", value);
        }

        @Test
        public void shouldThrowResultException_whenUnwrapErr() {
            var ex = assertThrows(ResultException.class,
                () -> Result.<String, Integer>ok("test").unwrapErr());
            assertEquals("unwrapErr() called on type 'ok'", ex.getMessage());
        }

        @Test
        public void shouldReturnValue_whenUnwrapOr() {
            String value = Result.<String, Integer>ok("test").unwrapOr("fallback");
            assertEquals("test", value);
        }

        @Test
        public void shouldReturnValue_whenUnwrapOrElse() {
            String value = Result.<String, Integer>ok("test")
                .unwrapOrElse(() -> "fallback");
            assertEquals("test", value);
        }

        @Test
        public void shouldReturnValue_whenExpect() {
            String value = Result.<String, Integer>ok("test")
                .expect("should not throw");
            assertEquals("test", value);
        }

        @Test
        public void shouldThrowResultException_whenExpectErr() {
            var ex = assertThrows(ResultException.class,
                () -> Result.<String, Integer>ok("test").expectErr("is ok"));
            assertEquals("is ok", ex.getMessage());
        }
    }

    @Nested
    public class MappingOperations {
        @Test
        public void shouldMapValue_whenMapping() {
            Result<Integer, String> result = Result.<String, String>ok("hello")
                .map(String::length);
            assertInstanceOf(Ok.class, result);
            assertEquals(5, result.unwrap());
        }

        @Test
        public void shouldNotMapError_whenMapErr() {
            Result<String, Integer> result = Result.<String, String>ok("test")
                .mapErr(String::length);
            assertInstanceOf(Ok.class, result);
            assertEquals("test", result.unwrap());
        }

        @Test
        public void shouldFlatMapValue_whenFlatMapping() {
            Result<Integer, String> result = Result.<String, String>ok("hello")
                .flatMap(s -> Result.ok(s.length()));
            assertInstanceOf(Ok.class, result);
            assertEquals(5, result.unwrap());
        }

        @Test
        public void shouldReturnErr_whenFlatMappingReturnsErr() {
            Result<Integer, String> result = Result.<String, String>ok("hello")
                .flatMap(s -> Result.err("error"));
            assertInstanceOf(Err.class, result);
            assertEquals("error", result.unwrapErr());
        }
    }

    @Nested
    public class Combinators {
        @Test
        public void shouldReturnOther_onAnd() {
            Result<Integer, String> other = Result.ok(42);
            Result<Integer, String> result = Result.<String, String>ok("test")
                .and(other);
            assertEquals(other, result);
        }

        @Test
        public void shouldReturnErr_onAndWithErr() {
            Result<Integer, String> other = Result.err("error");
            Result<Integer, String> result = Result.<String, String>ok("test")
                .and(other);
            assertInstanceOf(Err.class, result);
            assertEquals("error", result.unwrapErr());
        }

        @Test
        public void shouldApplyFunction_onAndThen() {
            Result<Integer, String> result = Result.<String, String>ok("hello")
                .andThen(s -> Result.ok(s.length()));
            assertInstanceOf(Ok.class, result);
            assertEquals(5, result.unwrap());
        }

        @Test
        public void shouldReturnErr_onAndThenReturningErr() {
            Result<Integer, String> result = Result.<String, String>ok("hello")
                .andThen(s -> Result.err("error"));
            assertInstanceOf(Err.class, result);
            assertEquals("error", result.unwrapErr());
        }

        @Test
        public void shouldReturnThis_onOr() {
            Result<String, Integer> ok = Result.ok("first");
            Result<String, Integer> other = Result.ok("second");
            Result<String, Integer> result = ok.or(other);
            assertEquals(ok, result);
        }

        @Test
        public void shouldReturnThis_onOrWithErr() {
            Result<String, Integer> ok = Result.ok("value");
            Result<String, Integer> other = Result.err(42);
            Result<String, Integer> result = ok.or(other);
            assertEquals(ok, result);
        }

        @Test
        public void shouldReturnThis_onOrElse() {
            Result<String, Integer> ok = Result.ok("first");
            Result<String, Integer> result = ok.orElse(() -> Result.ok("second"));
            assertEquals(ok, result);
        }
    }

    @Nested
    public class OptionConversions {
        @Test
        public void shouldReturnSome_onOk() {
            Option<String> option = Result.<String, Integer>ok("value").ok();
            assertInstanceOf(Some.class, option);
            assertEquals("value", option.unwrap());
        }

        @Test
        public void shouldReturnNone_onErr() {
            Option<Integer> option = Result.<String, Integer>ok("value").err();
            assertInstanceOf(None.class, option);
        }
    }

    @Nested
    public class ContainsOperations {
        @Test
        public void shouldReturnTrue_onContainsWithEqualValue() {
            assertTrue(Result.<String, Integer>ok("test").contains("test"));
        }

        @Test
        public void shouldReturnFalse_onContainsWithDifferentValue() {
            assertFalse(Result.<String, Integer>ok("test").contains("other"));
        }

        @Test
        public void shouldReturnFalse_onContainsErr() {
            assertFalse(Result.<String, Integer>ok("test").containsErr(42));
        }
    }

    @Nested
    public class InspectOperations {
        @Test
        public void shouldCallConsumer_onInspect() {
            List<String> ls = new ArrayList<>();
            Result<String, Integer> result = Result.<String, Integer>ok("test")
                .inspect(ls::add);
            assertInstanceOf(Ok.class, result);
            assertEquals(1, ls.size());
            assertEquals("test", ls.get(0));
        }

        @Test
        public void shouldNotCallConsumer_onInspectErr() {
            List<Integer> ls = new ArrayList<>();
            Result<String, Integer> result = Result.<String, Integer>ok("test")
                .inspectErr(ls::add);
            assertInstanceOf(Ok.class, result);
            assertEquals(0, ls.size());
        }
    }

    @Nested
    public class ZippingOperations {
        @Test
        public void shouldZipTwoOks() {
            Result<Pair<String, Integer>, String> zipped = 
                Result.<String, String>ok("hello").zip(Result.ok(5));
            assertInstanceOf(Ok.class, zipped);
            Pair<String, Integer> pair = zipped.unwrap();
            assertEquals("hello", pair.a());
            assertEquals(5, pair.b());
        }

        @Test
        public void shouldZipWithFunction() {
            Result<String, Integer> zipped = Result.<String, Integer>ok("hello")
                .zipWith(Result.ok(5), (s, i) -> s + i);
            assertInstanceOf(Ok.class, zipped);
            assertEquals("hello5", zipped.unwrap());
        }

        @Test
        public void shouldReturnErr_onZipWithErr() {
            Result<String, String> zipped = Result.<String, String>ok("hello")
                .zipWith(Result.err("error"), (s, i) -> s + i);
            assertInstanceOf(Err.class, zipped);
            assertEquals("error", zipped.unwrapErr());
        }
    }

    @Nested
    public class TransposeOperations {
        @Test
        public void shouldReturnSomeOk_onTransposeWithSome() {
            var innerOption = Option.some("value");
            var result = Result.<Option<String>, Integer>ok(innerOption).transpose();
            assertInstanceOf(Some.class, result);
            assertInstanceOf(Ok.class, result.unwrap());
            assertEquals("value", result.unwrap().unwrap());
        }

        @Test
        public void shouldReturnNone_onTransposeWithNone() {
            var innerOption = Option.<String>none();
            var result = Result.<Option<String>, Integer>ok(innerOption).transpose();
            assertInstanceOf(None.class, result);
        }
    }

    @Nested
    public class ConditionalOperations {
        @Test
        public void shouldCallConsumer_onIfOk() {
            List<String> ls = new ArrayList<>();
            Result.<String, Integer>ok("test").ifOk(ls::add);
            assertEquals(1, ls.size());
            assertEquals("test", ls.get(0));
        }

        @Test
        public void shouldNotCallConsumer_onIfErr() {
            List<Integer> ls = new ArrayList<>();
            Result.<String, Integer>ok("test").ifErr(ls::add);
            assertEquals(0, ls.size());
        }

        @Test
        public void shouldCallOnOk_onIfOkOrElse() {
            List<String> okList = new ArrayList<>();
            List<Integer> errList = new ArrayList<>();
            Result.<String, Integer>ok("test")
                .ifOkOrElse(okList::add, errList::add);
            assertEquals(1, okList.size());
            assertEquals("test", okList.get(0));
            assertEquals(0, errList.size());
        }
    }

    @Nested
    public class StreamOperations {
        @Test
        public void shouldConvertToStream() {
            Stream<String> stream = Result.<String, Integer>ok("test").stream();
            assertEquals(1, stream.count());
        }

        @Test
        public void shouldConvertToStreamWithValue() {
            Stream<String> stream = Result.<String, Integer>ok("test").stream();
            assertEquals("test", stream.findFirst().orElse(null));
        }
    }

    @Nested
    public class StaticFactoryMethods {
        @Test
        public void shouldCatchException_withCatchingSupplier() {
            Result<String, Exception> result = Result.catching(() -> "success");
            assertInstanceOf(Ok.class, result);
            assertEquals("success", result.unwrap());
        }

        @Test
        public void shouldCatchException_withCatchingSupplierAndMapper() {
            Result<String, String> result = Result.catching(
                () -> "success",
                Exception::getMessage
            );
            assertInstanceOf(Ok.class, result);
            assertEquals("success", result.unwrap());
        }

        @Test
        public void shouldCatchException_withCatchingFunction() {
            Result<Integer, Exception> result = Result.catching(
                String::length,
                "test"
            );
            assertInstanceOf(Ok.class, result);
            assertEquals(4, result.unwrap());
        }

        @Test
        public void shouldCatchException_withCatchingFunctionAndMapper() {
            Result<Integer, String> result = Result.catching(
                String::length,
                "test",
                Exception::getMessage
            );
            assertInstanceOf(Ok.class, result);
            assertEquals(4, result.unwrap());
        }
    }
}