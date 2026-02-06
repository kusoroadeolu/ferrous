package io.github.kusoroadeolu.ferrous.option;

import io.github.kusoroadeolu.ferrous.result.Err;
import io.github.kusoroadeolu.ferrous.result.Ok;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NoneTest {

    @Nested
    public class BasicOperations{
        @Test
        public void shouldReturnFalse_onIsSome(){
            Option<String> opt = Option.none();
            assertFalse(opt.isSome());

        }

        @Test
        public void shouldReturnFalse_onIsNone(){
            Option<String> opt = Option.none();
            assertTrue(opt.isNone());
        }

        @Test
        public void shouldThrowOptionEx_whenUnwrapped(){
            var ex = assertThrows(OptionException.class, () -> Option.none().unwrap());
            assertEquals("unwrap() called on 'none' type", ex.getMessage());
        }

        @Test
        public void shouldReturnFallbackValue_whenUnwrappedOr(){
            String value = Option.<String>none().unwrapOr("fallback");
            assertEquals("fallback", value);
        }

        @Test
        public void shouldReturnFallbackSuppliedValue_whenUnwrappedOrElse(){
            String value = Option.<String>none().unwrapOrElse(() -> "fallback");
            assertEquals("fallback", value);
        }

        @Test
        public void shouldThrowOptionException_whenExpect(){
            var ex = assertThrows(OptionException.class, () -> Option.<String>none().expect("is none"));
            assertEquals("is none", ex.getMessage());
        }

        @Test
        public void shouldReturnNull_onToNullable(){
            assertNull(Option.none().toNullable());
        }
    }

    @Nested
    public class MappingAndFiltering{
        @Test
        public void shouldReturnNone_whenMapping(){
            Option<Integer> val = Option.<String>none().map(String::length);
            assertInstanceOf(None.class, val);
        }

        @Test
        public void shouldReturnNone_whenFlatMapping(){
            Option<String> opt = Option.some("String");
            Option<String> val = Option.none().flatMap(_ -> opt);
            assertInstanceOf(None.class, val);
        }

        @Test
        public void shouldReturnNone_whenFiltering(){
            Option<String> val = new None<String>().filter(String::isBlank);
            assertInstanceOf(None.class, val);
        }

        @Test
        public void shouldReturnNone_onAnd(){
            Option<String> some = new Some<>("some_value");
            Option<String> val = new None<String>().and(some);
            assertEquals(some, val);
        }

        @Test
        public void shouldReturnNone_onAndThen(){
            Option<String> some = new Some<>("some_value");
            Option<String> val = new None<String>().andThen(_ -> some);
            assertInstanceOf(None.class, val);
        }

        @Test
        public void shouldReturnOk_onTranspose(){
            var result = Option.none().transpose();
            assertInstanceOf(Ok.class, result);
            assertInstanceOf(None.class, result.unwrap());
        }
    }

    @Nested
    public class Conditionals{
        @Test
        public void shouldReturnOtherOption_onOr(){
            Option<String> some = new Some<>("some_value");
            Option<String> val = new None<String>().or(some);
            assertEquals(some, val);
        }

        @Test
        public void shouldReturnOtherOption_onOrElse(){
            Option<String> some = new Some<>("some_value");
            Option<String> val = new None<String>().orElse(() -> some);
            assertEquals(some, val);
        }

        @Test
        public void shouldReturnFalse_onContains(){
            assertFalse(Option.none().contains(new Object()));
        }

        @Test
        public void shouldReturnErr_onOkOr(){
            var res = Option.<String>none().okOr("None");
            assertInstanceOf(Err.class, res);
            assertEquals("None", res.err().unwrap());
        }

        @Test
        public void shouldReturnErr_onOkOrElse(){
            var res = Option.<String>none().okOrElse(() -> "None");
            assertInstanceOf(Err.class, res);
            assertEquals("None", res.err().unwrap());
        }

        @Test
        public void shouldReturnOther_onXor(){
            Option<String> some = new Some<>("some_value");
            Option<String> val = new None<String>().xor(some);
            assertEquals(some, val);
        }

        @Test
        public void shouldNotConsumeValue_onIfSome(){
            List<Integer> ls = new ArrayList<>();
            Option.none().ifSome(_ -> ls.add(1));
            assertEquals(0, ls.size());
        }

        @Test
        public void shouldRun_onIfNone(){
            List<Integer> ls = new ArrayList<>();
            Option.none().ifNone(() -> ls.add(1));
            assertEquals(1, ls.size());
        }
    }
}