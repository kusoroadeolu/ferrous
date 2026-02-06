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

class ErrTest {

    @Nested
    public class BasicOperations {
        @Test
        public void shouldReturnFalse_onIsOk() {
            Result<String, Integer> result = Result.err(42);
            assertFalse(result.isOk());
        }

        @Test
        public void shouldReturnTrue_onIsErr() {
            Result<String, Integer> result = Result.err(42);
            assertTrue(result.isErr());
        }

        @Test
        public void shouldThrowResultException_whenUnwrap() {
            var ex = assertThrows(ResultException.class,
                () -> Result.<String, Integer>err(42).unwrap());
            assertEquals("unWrap() called on type 'err'", ex.getMessage());
        }

        @Test
        public void shouldReturnError_whenUnwrapErr() {
            Integer error = Result.<String, Integer>err(42).unwrapErr();
            assertEquals(42, error);
        }

        @Test
        public void shouldReturnFallback_whenUnwrapOr() {
            String value = Result.<String, Integer>err(42).unwrapOr("fallback");
            assertEquals("fallback", value);
        }

        @Test
        public void shouldReturnSuppliedValue_whenUnwrapOrElse() {
            String value = Result.<String, Integer>err(42)
                .unwrapOrElse(() -> "fallback");
            assertEquals("fallback", value);
        }

        @Test
        public void shouldThrowResultException_whenExpect() {
            var ex = assertThrows(ResultException.class,
                () -> Result.<String, Integer>err(42).expect("is error"));
            assertEquals("is error", ex.getMessage());
        }

        @Test
        public void shouldReturnError_whenExpectErr() {
            Integer error = Result.<String, Integer>err(42)
                .expectErr("should not throw");
            assertEquals(42, error);
        }
    }

    @Nested
    public class MappingOperations {
        @Test
        public void shouldNotMapValue_whenMapping() {
            Result<Integer, String> result = Result.<String, String>err("error")
                .map(String::length);
            assertInstanceOf(Err.class, result);
            assertEquals("error", result.unwrapErr());
        }

        @Test
        public void shouldMapError_whenMapErr() {
            Result<String, Integer> result = Result.<String, String>err("error")
                .mapErr(String::length);
            assertInstanceOf(Err.class, result);
            assertEquals(5, result.unwrapErr());
        }

        @Test
        public void shouldNotFlatMapValue_whenFlatMapping() {
            Result<Integer, String> result = Result.<String, String>err("error")
                .flatMap(s -> Result.ok(s.length()));
            assertInstanceOf(Err.class, result);
            assertEquals("error", result.unwrapErr());
        }
    }

    @Nested
    public class Combinators {
        @Test
        public void shouldReturnOther_onAnd() {
            Result<Integer, String> other = Result.ok(42);
            Result<Integer, String> result = Result.<String, String>err("error")
                .and(other);
            assertEquals(other, result);
        }

        @Test
        public void shouldReturnOtherErr_onAndWithErr() {
            Result<Integer, String> other = Result.err("other error");
            Result<Integer, String> result = Result.<String, String>err("error")
                .and(other);
            assertEquals(other, result);
        }

        @Test
        public void shouldNotApplyFunction_onAndThen() {
            Result<Integer, String> result = Result.<String, String>err("error")
                .andThen(s -> Result.ok(s.length()));
            assertInstanceOf(Err.class, result);
            assertEquals("error", result.unwrapErr());
        }

        @Test
        public void shouldReturnOther_onOr() {
            Result<String, Integer> other = Result.ok("value");
            Result<String, Integer> result = Result.<String, Integer>err(42)
                .or(other);
            assertEquals(other, result);
        }

        @Test
        public void shouldReturnOtherErr_onOrWithErr() {
            Result<String, Integer> other = Result.err(100);
            Result<String, Integer> result = Result.<String, Integer>err(42)
                .or(other);
            assertEquals(other, result);
        }

        @Test
        public void shouldReturnSuppliedResult_onOrElse() {
            Result<String, Integer> result = Result.<String, Integer>err(42)
                .orElse(() -> Result.ok("recovered"));
            assertInstanceOf(Ok.class, result);
            assertEquals("recovered", result.unwrap());
        }

        @Test
        public void shouldReturnSuppliedErr_onOrElseWithErr() {
            Result<String, Integer> result = Result.<String, Integer>err(42)
                .orElse(() -> Result.err(100));
            assertInstanceOf(Err.class, result);
            assertEquals(100, result.unwrapErr());
        }
    }

    @Nested
    public class OptionConversions {
        @Test
        public void shouldReturnNone_onOk() {
            Option<String> option = Result.<String, Integer>err(42).ok();
            assertInstanceOf(None.class, option);
        }

        @Test
        public void shouldReturnSome_onErr() {
            Option<Integer> option = Result.<String, Integer>err(42).err();
            assertInstanceOf(Some.class, option);
            assertEquals(42, option.unwrap());
        }
    }

    @Nested
    public class ContainsOperations {
        @Test
        public void shouldReturnFalse_onContains() {
            assertFalse(Result.<String, Integer>err(42).contains("test"));
        }

        @Test
        public void shouldReturnTrue_onContainsErrWithEqualError() {
            assertTrue(Result.<String, Integer>err(42).containsErr(42));
        }

        @Test
        public void shouldReturnFalse_onContainsErrWithDifferentError() {
            assertFalse(Result.<String, Integer>err(42).containsErr(100));
        }
    }

    @Nested
    public class InspectOperations {
        @Test
        public void shouldNotCallConsumer_onInspect() {
            List<String> ls = new ArrayList<>();
            Result<String, Integer> result = Result.<String, Integer>err(42)
                .inspect(ls::add);
            assertInstanceOf(Err.class, result);
            assertEquals(0, ls.size());
        }

        @Test
        public void shouldCallConsumer_onInspectErr() {
            List<Integer> ls = new ArrayList<>();
            Result<String, Integer> result = Result.<String, Integer>err(42)
                .inspectErr(ls::add);
            assertInstanceOf(Err.class, result);
            assertEquals(1, ls.size());
            assertEquals(42, ls.get(0));
        }
    }

    @Nested
    public class ZippingOperations {
        @Test
        public void shouldReturnErr_onZipWithOk() {
            Result<Pair<String, Integer>, String> zipped = 
                Result.<String, String>err("error").zip(Result.ok(5));
            assertInstanceOf(Err.class, zipped);
            assertEquals("error", zipped.unwrapErr());
        }

        @Test
        public void shouldReturnErr_onZipWithErr() {
            Result<Pair<String, Integer>, String> zipped = 
                Result.<String, String>err("error1").zip(Result.err("error2"));
            assertInstanceOf(Err.class, zipped);
            assertEquals("error1", zipped.unwrapErr());
        }

        @Test
        public void shouldReturnErr_onZipWithWithOk() {
            Result<String, Integer> zipped = Result.<String, Integer>err(42)
                .zipWith(Result.ok(5), (s, i) -> s + i);
            assertInstanceOf(Err.class, zipped);
            assertEquals(42, zipped.unwrapErr());
        }

        @Test
        public void shouldReturnErr_onZipWithWithErr() {
            Result<String, Integer> zipped = Result.<String, Integer>err(42)
                .zipWith(Result.err(100), (s, i) -> s + i);
            assertInstanceOf(Err.class, zipped);
            assertEquals(42, zipped.unwrapErr());
        }
    }

    @Nested
    public class TransposeOperations {
        @Test
        public void shouldReturnSomeErr_onTranspose() {
            var result = Result.<Option<String>, Integer>err(42).transpose();
            assertInstanceOf(Some.class, result);
            assertInstanceOf(Err.class, result.unwrap());
            assertEquals(42, result.unwrap().unwrapErr());
        }
    }

    @Nested
    public class ConditionalOperations {
        @Test
        public void shouldNotCallConsumer_onIfOk() {
            List<String> ls = new ArrayList<>();
            Result.<String, Integer>err(42).ifOk(ls::add);
            assertEquals(0, ls.size());
        }

        @Test
        public void shouldCallConsumer_onIfErr() {
            List<Integer> ls = new ArrayList<>();
            Result.<String, Integer>err(42).ifErr(ls::add);
            assertEquals(1, ls.size());
            assertEquals(42, ls.get(0));
        }

        @Test
        public void shouldCallOnErr_onIfOkOrElse() {
            List<String> okList = new ArrayList<>();
            List<Integer> errList = new ArrayList<>();
            Result.<String, Integer>err(42)
                .ifOkOrElse(okList::add, errList::add);
            assertEquals(0, okList.size());
            assertEquals(1, errList.size());
            assertEquals(42, errList.get(0));
        }
    }

    @Nested
    public class StreamOperations {
        @Test
        public void shouldReturnEmptyStream() {
            Stream<String> stream = Result.<String, Integer>err(42).stream();
            assertEquals(0, stream.count());
        }
    }

    @Nested
    public class StaticFactoryMethods {
        @Test
        public void shouldCatchException_withCatchingSupplier() {
            Result<String, Exception> result = Result.catching(() -> {
                throw new RuntimeException("error");
            });
            assertInstanceOf(Err.class, result);
            assertInstanceOf(RuntimeException.class, result.unwrapErr());
            assertEquals("error", result.unwrapErr().getMessage());
        }

        @Test
        public void shouldCatchException_withCatchingSupplierAndMapper() {
            Result<String, String> result = Result.catching(
                () -> {
                    throw new RuntimeException("error");
                },
                Exception::getMessage
            );
            assertInstanceOf(Err.class, result);
            assertEquals("error", result.unwrapErr());
        }

        @Test
        public void shouldCatchException_withCatchingFunction() {
            Result<Integer, Exception> result = Result.catching(
                (String s) -> {
                    throw new RuntimeException("error");
                },
                "test"
            );
            assertInstanceOf(Err.class, result);
            assertInstanceOf(RuntimeException.class, result.unwrapErr());
            assertEquals("error", result.unwrapErr().getMessage());
        }

        @Test
        public void shouldCatchException_withCatchingFunctionAndMapper() {
            Result<Integer, String> result = Result.catching(
                (String s) -> {
                    throw new RuntimeException("error");
                },
                "test",
                Exception::getMessage
            );
            assertInstanceOf(Err.class, result);
            assertEquals("error", result.unwrapErr());
        }

        @Test
        public void shouldMapExceptionWithCustomMapper() {
            Result<String, Integer> result = Result.catching(
                () -> {
                    throw new RuntimeException("test");
                },
                e -> e.getMessage().length()
            );
            assertInstanceOf(Err.class, result);
            assertEquals(4, result.unwrapErr()); // "test".length() = 4
        }
    }
}