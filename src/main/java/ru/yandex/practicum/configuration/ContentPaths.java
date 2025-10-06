package ru.yandex.practicum.configuration;

import jakarta.servlet.ServletContext;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.Objects;

@Component
@Slf4j
public class ContentPaths {
    private static final String IMAGE_DIR = "WEB-INF/classes/uploads/";

    @Setter(onMethod_ = @Autowired)
    private ServletContext servletContext;

    private String imageStrPathCached;

    public String getImagePathStr() {
        if (Objects.isNull(imageStrPathCached)) {
            imageStrPathCached = Paths.get(servletContext.getRealPath("/"), IMAGE_DIR).toString();
        }

        return imageStrPathCached;
    }
}
