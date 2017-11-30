package org.deviceconnect.codegen;


import io.swagger.models.Info;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class MultipleSwaggerConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultipleSwaggerConverter.class);

    private static final String SEPARATOR = DConnectPath.SEPARATOR;

    Map<String, Swagger> convert(final List<Swagger> swaggerList)
            throws IllegalPathFormatException, DuplicatedPathException {
        Map<String, Swagger> result = new HashMap<>();
        NameDuplicationCounter counter = new NameDuplicationCounter();
        for (Swagger swagger : swaggerList) {
            convert(swagger, result, counter);
        }
        return result;
    }

    private void convert(final Swagger swagger,
                         final Map<String, Swagger> result,
                         final NameDuplicationCounter counter)
            throws IllegalPathFormatException, DuplicatedPathException {
        String basePath = swagger.getBasePath();
        if (basePath == null || basePath.equals("")) {
            basePath = SEPARATOR;
        }

        Map<String, Path> paths = swagger.getPaths();
        for (Map.Entry<String, Path> entry : paths.entrySet()) {
            String pathName = entry.getKey();
            DConnectPath path = DConnectPath.parsePath(basePath, pathName);
            counter.count(path.toCanonicalPathName());
            LOGGER.info("Base Path: " + path.getBathPath() + ", Sub Path: " + path.getSubPath() + ", Profile: " + path.getProfileName());

            String key = path.getProfileName();
            Swagger cache = result.get(key);
            if (cache == null) {
                cache = createProfileSpec(swagger);
                cache.setBasePath(path.getBathPath());
                result.put(key, cache);
            }
            String subPathName = path.getSubPath();
            Map<String, Path> pathSpecs = cache.getPaths();
            Path pathSpec = pathSpecs.get(subPathName);
            if (pathSpec == null) {
                pathSpecs.put(subPathName, entry.getValue());
            }
            cache.setPaths(pathSpecs);
        }

        // 重複チェック
        List<NameDuplication> duplications = counter.getDuplications();
        if (duplications.size() > 0) {
            throw new DuplicatedPathException(duplications);
        }
    }

    private static Swagger createProfileSpec(final Swagger swagger) {
        Swagger profile = new Swagger();
        profile.setSwagger(swagger.getSwagger());
        Info info = new Info();
        info.setTitle(swagger.getInfo().getTitle());
        info.setVersion(swagger.getInfo().getVersion());
        profile.setInfo(info);
        profile.setConsumes(swagger.getConsumes());
        profile.setExternalDocs(swagger.getExternalDocs());
        profile.setHost(swagger.getHost());
        profile.setParameters(swagger.getParameters());
        profile.setResponses(swagger.getResponses());
        profile.setProduces(swagger.getProduces());
        profile.setSecurity(swagger.getSecurity());
        profile.setSecurityDefinitions(swagger.getSecurityDefinitions());
        profile.setSchemes(swagger.getSchemes());
        profile.setTags(swagger.getTags());
        Map<String, Object> extensions = swagger.getVendorExtensions();
        if (extensions != null) {
            for (Map.Entry<String, Object> entry : extensions.entrySet()) {
                profile.setVendorExtension(entry.getKey(), entry.getValue());
            }
        }
        profile.setDefinitions(swagger.getDefinitions());
        profile.setPaths(new HashMap<String, Path>());
        return profile;
    }

    private static class NameDuplicationCounter {

        private final Map<String, NameDuplication> duplications = new HashMap<>();

        private NameDuplicationCounter() {}

        void count(final String name) {
            NameDuplication dup = duplications.get(name);
            if (dup == null) {
                dup = new NameDuplication(name);
                duplications.put(name, dup);
            }
            dup.countUp();
        }

        List<NameDuplication> getDuplications() {
            List<NameDuplication> result = new ArrayList<>();
            for (Map.Entry<String, NameDuplication> entry : duplications.entrySet()) {
                NameDuplication dup = entry.getValue();
                if (dup.isDuplicated()) {
                    result.add(dup);
                }
            }
            return result;
        }
    }
}