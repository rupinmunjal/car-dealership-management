package ca.sheridancollege.munjalru.config;

import ca.sheridancollege.munjalru.exception.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String API_PREFIX = "/api/v1/";
    private static final String AUTH_PREFIX = "/api/v1/auth/";
    private static final long STALE_AFTER_NANOS = Duration.ofMinutes(10).toNanos();

    private final ObjectMapper objectMapper;
    private final Map<String, ClientBucket> generalBuckets = new ConcurrentHashMap<>();
    private final Map<String, ClientBucket> authBuckets = new ConcurrentHashMap<>();
    private final AtomicLong requestCounter = new AtomicLong();

    @Value("${app.rate-limit.enabled:true}")
    private boolean enabled;

    @Value("${app.rate-limit.general-per-minute:100}")
    private long generalLimit;

    @Value("${app.rate-limit.auth-per-minute:10}")
    private long authLimit;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !enabled || !request.getRequestURI().startsWith(API_PREFIX);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        boolean authRequest = request.getRequestURI().startsWith(AUTH_PREFIX);
        long limit = authRequest ? authLimit : generalLimit;
        Map<String, ClientBucket> buckets = authRequest ? authBuckets : generalBuckets;
        String clientKey = request.getRemoteAddr();
        long now = System.nanoTime();

        ClientBucket clientBucket = buckets.computeIfAbsent(clientKey,
                ignored -> new ClientBucket(createBucket(limit), new AtomicLong(now)));
        clientBucket.lastAccessNanos().set(now);

        if (!clientBucket.bucket().tryConsume(1)) {
            writeTooManyRequests(response, limit);
            return;
        }

        if (requestCounter.incrementAndGet() % 1_000 == 0) {
            removeStaleEntries(now);
        }
        filterChain.doFilter(request, response);
    }

    private Bucket createBucket(long limit) {
        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(limit)
                .refillIntervally(limit, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(bandwidth).build();
    }

    private void writeTooManyRequests(HttpServletResponse response, long limit) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader(HttpHeaders.RETRY_AFTER, "60");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), ApiError.builder()
                .status(HttpStatus.TOO_MANY_REQUESTS.value())
                .error("Too Many Requests")
                .message("Rate limit exceeded. Maximum " + limit + " requests per minute.")
                .timestamp(Instant.now())
                .build());
    }

    private void removeStaleEntries(long now) {
        generalBuckets.values().removeIf(bucket -> now - bucket.lastAccessNanos().get() > STALE_AFTER_NANOS);
        authBuckets.values().removeIf(bucket -> now - bucket.lastAccessNanos().get() > STALE_AFTER_NANOS);
    }

    private record ClientBucket(Bucket bucket, AtomicLong lastAccessNanos) {}
}
