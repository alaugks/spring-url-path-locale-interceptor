package io.github.alaugks.spring.requesturilocaleinterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.springframework.util.Assert;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.util.UriComponentsBuilder;

public class RequestURILocaleInterceptor implements HandlerInterceptor {

    private static final String PATH_DELIMITER = "/";
    private final Locale defaultLocale;
    private final List<Locale> supportedLocales;
    private final String defaultHomePath;

    public RequestURILocaleInterceptor(Builder builder) {
        this.defaultLocale = builder.defaultLocale;
        this.supportedLocales = builder.supportedLocales;
        this.defaultHomePath = builder.defaultRequestURI;
    }

    /**
     * @deprecated
     */
    @Deprecated(since = "0.3.0")
    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Locale defaultLocale) {
        return new Builder(defaultLocale);
    }

    public static final class Builder {

        private Locale defaultLocale;
        private List<Locale> supportedLocales;
        private String defaultRequestURI;

        public Builder() {
        }

        public Builder(Locale defaultLocale) {
            this.defaultLocale = defaultLocale;
        }

        /**
         * @deprecated
         */
        @Deprecated(since = "0.3.0")
        public Builder defaultLocale(Locale defaultLocale) {
            this.defaultLocale = defaultLocale;
            return this;
        }

        public Builder supportedLocales(List<Locale> supportedLocales) {
            this.supportedLocales = supportedLocales;
            return this;
        }

        public Builder defaultRequestURI(String defaultRequestURI) {
            this.defaultRequestURI = defaultRequestURI;
            return this;
        }

        public RequestURILocaleInterceptor build() {
            Assert.notNull(defaultLocale, "Default locale is null");
            Assert.isTrue(!defaultLocale.toString().trim().isEmpty(), "Default locale is empty");

            if (this.supportedLocales == null) {
                this.supportedLocales = new ArrayList<>();
            }

            if (this.defaultRequestURI == null) {
                this.defaultRequestURI = PATH_DELIMITER + this.defaultLocale;
            }

            return new RequestURILocaleInterceptor(this);
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            String[] explodedRequestUri = request.getRequestURI().substring(1).split(PATH_DELIMITER);

            LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);

            if (localeResolver == null) {
                throw new IllegalStateException("LocaleResolver not found");
            }

            Locale localeUri = Locale.forLanguageTag(explodedRequestUri[0]);

            boolean isLocaleSupported = this.supportedLocales
                .stream()
                .anyMatch(l -> l.toString().equals(localeUri.toString()));

            if (isLocaleSupported) {
                localeResolver.setLocale(request, response, localeUri);
                return true;
            }

            URL url = this.createUri(request, this.joinUri(explodedRequestUri)).toURL();

            // Send redirect only with path + query.
            // No domain handling domain/ip vs. proxies and forwarded.
            response.sendRedirect(
                url.getPath() + (url.getQuery() != null ? url.getQuery() : "")
            );

            return false;

        } catch (Exception e) {
            throw new RequestURILocaleInterceptorException(e);
        }
    }

    private String joinUri(String... uri) {
        String joinedUri = String.join(
            PATH_DELIMITER,
            Arrays.copyOfRange(uri, 1, uri.length)
        );

        String path = !joinedUri.isEmpty() ? PATH_DELIMITER + joinedUri : "";
        return !path.isEmpty()
            ? PATH_DELIMITER + this.formatLocale(this.defaultLocale) + path
            : String.format(this.defaultHomePath, this.formatLocale(this.defaultLocale));
    }

    private String formatLocale(Locale locale) {
        return locale.toString().toLowerCase().replace("_", "-");
    }

    public URI createUri(final HttpServletRequest req, String path) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
            .newInstance()
            .scheme(req.getScheme())
            .host(req.getRemoteHost())
            .path(path)
            .query(req.getQueryString());

        if (!List.of(80, 443).contains(req.getRemotePort())) {
            uriBuilder.port(req.getRemotePort());
        }

        return uriBuilder.build().toUri();
    }
}
