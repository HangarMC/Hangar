package io.papermc.hangar.security.annotations.ratelimit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.checkerframework.common.value.qual.IntRange;

/**
 * Annotation for controllers or individual endpoint methods.
 * This should only be used to prevent short-term endpoint spam, sending the user to an error page.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    int MAX_REFILL_DELAY = 60 * 5;

    /**
     * Returns how many tokens can be accumulated in total.
     * When refilling tokens, they will use this value as their maximum refill value.
     *
     * @return how many tokens can be accumulated in total
     */
    @IntRange(from = 1)
    int overdraft() default 100;

    /**
     * Returns how many tokens should be refilled per refill, as specified in {@link #refillSeconds()}.
     *
     * @return how many tokens should be refilled per refill
     */
    @IntRange(from = 1)
    int refillTokens() default 20;

    /**
     * Returns after how many seconds {@link #refillTokens()} should be refilled.
     * Has to be between 1 second and 5 minutes, with rate limits specifically made
     * for short-term spam prevention.
     *
     * @return after how many seconds tokens should be refilled
     */
    @IntRange(from = 1, to = MAX_REFILL_DELAY)
    int refillSeconds() default 5;

    /**
     * Returns whether tokens should be gradually refilled within an interval.
     * If set to false, tokens will only be refilled once per period with the full {@link #refillTokens()} amount.
     *
     * @return whether tokens should be gradually refilled
     */
    boolean greedy() default false;

    /**
     * Returns the path to rate limit that differs from the actual request path.
     * Useful for limiting every method inside a controller by the super path.
     * <p>
     * If none is specified, the path of the method's or class's request mapping will be used.
     *
     * @return path to rate limit that differs from the final path
     */
    String path() default "";
}
