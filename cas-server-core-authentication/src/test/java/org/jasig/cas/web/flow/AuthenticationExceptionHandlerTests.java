package org.jasig.cas.web.flow;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.ContextualAuthenticationPolicy;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.UnsatisfiedAuthenticationPolicyException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.springframework.binding.message.DefaultMessageResolver;
import org.springframework.binding.message.MessageContext;

import javax.security.auth.login.AccountNotFoundException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@RunWith(JUnit4.class)
public class AuthenticationExceptionHandlerTests {
 
    @Test
    public void handleAccountNotFoundExceptionByDefefault() {
        final AuthenticationExceptionHandler handler = new AuthenticationExceptionHandler();
        final MessageContext ctx = mock(MessageContext.class);
        
        final Map<String, Class<? extends Exception>> map = new HashMap<>();
        map.put("notFound", AccountNotFoundException.class);
        final String id = handler.handle(new AuthenticationException(map), ctx);
        assertEquals(id, AccountNotFoundException.class.getSimpleName());
    }

    @Test
    public void handleUnknownExceptionByDefefault() {
        final AuthenticationExceptionHandler handler = new AuthenticationExceptionHandler();
        final MessageContext ctx = mock(MessageContext.class);
        
        final Map<String, Class<? extends Exception>> map = new HashMap<>();
        map.put("unknown", GeneralSecurityException.class);
        final String id = handler.handle(new AuthenticationException(map), ctx);
        assertEquals(id, "UNKNOWN");
    }

    @Test
    public void handleUnknownTicketExceptionByDefault() {
        final AuthenticationExceptionHandler handler = new AuthenticationExceptionHandler();
        final MessageContext ctx = mock(MessageContext.class);

        final String id = handler.handle(new InvalidTicketException("TGT"), ctx);
        assertEquals(id, "UNKNOWN");
        verifyZeroInteractions(ctx);
    }

    @Test
    public void handleUnsatisfiedAuthenticationPolicyExceptionByDefault() {
        final AuthenticationExceptionHandler handler = new AuthenticationExceptionHandler();
        final MessageContext ctx = mock(MessageContext.class);

        final ContextualAuthenticationPolicy<?> policy = new ContextualAuthenticationPolicy<Object>() {
            @Override
            public Optional<String> getCode() {
                return Optional.of("CUSTOM_CODE");
            }

            @Override
            public Object getContext() {
                return null;
            }

            @Override
            public boolean isSatisfiedBy(final Authentication authentication) {
                return false;
            }
        };
        final String id = handler.handle(new UnsatisfiedAuthenticationPolicyException(policy), ctx);
        assertEquals(id, "UnsatisfiedAuthenticationPolicyException");
        ArgumentCaptor<DefaultMessageResolver> message = ArgumentCaptor.forClass(DefaultMessageResolver.class);
        verify(ctx, times(1)).addMessage(message.capture());
        assertArrayEquals(new String[]{"CUSTOM_CODE"}, message.getValue().getCodes());
    }
}
