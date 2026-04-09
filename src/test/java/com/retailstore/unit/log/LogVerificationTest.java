package com.retailstore.unit.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class LogVerificationTest {

    private Logger cartLogger;
    private Logger productLogger;
    private Logger rootLogger;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp(){
        cartLogger = (Logger) LoggerFactory.getLogger("com.retailstore.cart");
        productLogger = (Logger) LoggerFactory.getLogger("com.retailstore.product");
        rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

        cartLogger.setLevel(Level.DEBUG);
        productLogger.setLevel(Level.DEBUG);
        rootLogger.setLevel(Level.INFO);

        listAppender = new ListAppender<>();
        listAppender.setContext(rootLogger.getLoggerContext());
        listAppender.start();
        rootLogger.addAppender(listAppender);
    }

    @AfterEach
    void cleanUp(){
        rootLogger.detachAppender(listAppender);
    }

    @Test
    void testLogLevelsFromApplicationProperties(){
        assertThat(cartLogger.getLevel()).isEqualTo(Level.DEBUG);
        assertThat(productLogger.getLevel()).isEqualTo(Level.DEBUG);
        assertThat(rootLogger.getLevel()).isEqualTo(Level.INFO);
    }

    @Test
    void testProductDebugLogsCaptured(){
        productLogger.debug("debug-msg");
        productLogger.info("info-msg");

        assertThat(listAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains("debug-msg", "info-msg");
    }

    @Test
    void testCartDebugLogsCaptured() {
        cartLogger.debug("cart-debug");
        cartLogger.error("cart-error");

        assertThat(listAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains("cart-debug", "cart-error");
    }

    @Test
    void testRootWarnErrorLogsCaptured() {
        rootLogger.warn("root-warn");
        rootLogger.error("root-error");

        assertThat(listAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains("root-warn", "root-error");
    }

    @Test
    void testLoggerLevelsFilterLowerThanSet(){
        rootLogger.debug("should-not-appear");
        rootLogger.info("should-appear");

        assertThat(listAppender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .doesNotContain("should-not-appear")
                .contains("should-appear");
    }
}
