# RequestURI Locale Interceptor for Spring

Handling Locale (i18n) as first part of RequestURI. 

Example RequestURI:
```
/{locale}/example/path?param=value
```

An [example](https://spring-boot-xliff-example.alaugks.dev/) in action can be viewed here.

## Dependency
```xml
<dependency>
    <groupId>io.github.alaugks</groupId>
    <artifactId>spring-requesturi-locale-interceptor</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Configuration

### Options

<table>
<thead>
    <tr>
        <th style="text-align: left; vertical-align: top">Options</th>
        <th style="text-align: left; vertical-align: top">Description</th>
        <th style="text-align: left; vertical-align: top">Required</th>
    </tr>
</thead>
<tbody>
    <tr>
        <td style="text-align: left; vertical-align: top">setSupportedLocales(Locale locale)</td>
        <td style="text-align: left; vertical-align: top">Default Locale and Locale fallback.</td>
        <td style="text-align: left; vertical-align: top">Yes</td>
    </tr>
    <tr>
        <td style="text-align: left; vertical-align: top">setDefaultLocale(List<Locale> locales)</td>
        <td style="text-align: left; vertical-align: top">List all locales that are supported.</td>
        <td style="text-align: left; vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="text-align: left; vertical-align: top">setDefaultHomePath(String path)</td>
        <td style="text-align: left; vertical-align: top">If a RequestURI is not exists (empty), a redirect to the path is performed.</td>
        <td style="text-align: left; vertical-align: top">
            No (The default request URI /{defaultLocale} is generated.)
        </td>
    </tr>
</tbody>
</table>

### Spring Configuration

```java
import io.github.alaugks.spring.requesturilocaleinterceptor.RequestURILocaleInterceptor;
import io.github.alaugks.spring.requesturilocaleinterceptor.RequestURILocaleResolver;
import java.util.List;
import java.util.Locale;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfigurerConfig implements WebMvcConfigurer {

    private final Locale defaultLocale = Locale.forLanguageTag("en");

    private final List<Locale> supportedLocales = List.of(
        Locale.forLanguageTag("en"),
        Locale.forLanguageTag("de"),
        Locale.forLanguageTag("en-US")
    );

    @Bean
    public LocaleResolver localeResolver() {
        RequestURILocaleResolver resolver = new RequestURILocaleResolver();
        resolver.setDefaultLocale(this.defaultLocale);
        return resolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        RequestURILocaleInterceptor interceptor = RequestURILocaleInterceptor
            .builder()
            .defaultLocale(this.defaultLocale)
            .supportedLocales(this.supportedLocales)
            .defaultRequestURI(
                String.format(
                    "/%s/home",
                    this.defaultLocale.toString()
                )
            )
            .build();

        registry.addInterceptor(urlInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns("/static/**", "/error");
    }
}
```
