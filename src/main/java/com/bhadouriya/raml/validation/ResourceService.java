package com.bhadouriya.raml.validation;

import com.bhadouriya.raml.efficacies.Efficacy;
import org.apache.commons.lang.StringUtils;
import org.raml.v2.api.RamlModelBuilder;
import org.raml.v2.api.RamlModelResult;
import org.raml.v2.api.model.v10.api.Api;
import org.raml.v2.api.model.v10.methods.Method;
import org.raml.v2.api.model.v10.resources.Resource;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.bhadouriya.raml.validation.Constant.FAILED_TO_LOAD_RAML_FROM_FILESYS;
import static com.bhadouriya.raml.validation.Constant.URI_PARAM_PLACEHOLDER;
import static java.util.Optional.of;
import static org.springframework.util.CollectionUtils.isEmpty;

public class ResourceService {

    private static final Logger LOGGER = Logger.getLogger(ResourceService.class.getName());
    private final boolean isRamlOnClassPath;
    private final String ramlLocation;
    private final List<Resource> RESOURCES = new ArrayList<>();

    public ResourceService(boolean isRamlOnClassPath, String ramlLocation) {
        this.isRamlOnClassPath = isRamlOnClassPath;
        this.ramlLocation = ramlLocation;
        init();
    }

    private static String createPatternFromResource(String resourcePath) {
        //Define Start n End points
        String newResourcePath = "^" + resourcePath + "$";
        //Escape All forward Slashes
        newResourcePath = newResourcePath.replaceAll("/", "\\\\/");
        //replace URI Param placeholdr with allpowed patter
        newResourcePath = newResourcePath.replaceAll(URI_PARAM_PLACEHOLDER, "\\\\S+");
        return newResourcePath;
    }

    private static String stripTrailingForwardSlash(final String resourcePath) {
        return resourcePath.replaceAll("/$", "");
    }

    public static void unZipFiles(final ZipInputStream zipInputStream, Path unzipPath) throws IOException {

        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(unzipPath.toAbsolutePath().toString()))) {
            byte[] bytes = new byte[1024];
            int read = 0;
            while ((read = zipInputStream.read(bytes)) != -1) {
                bos.write(bytes, 0, read);
            }
        }
    }

    public Optional<Resource> findResource(String resourcePath) {
        Optional<Resource> findResource = Optional.empty();

        if (!StringUtils.isBlank(resourcePath)) {
            final String sanitisedResourcePath = stripTrailingForwardSlash(resourcePath);
            findResource = this.findHardMatch(this.RESOURCES, findResource, sanitisedResourcePath);
            findResource = this.findCustomMatch(resourcePath, this.RESOURCES, findResource, sanitisedResourcePath);
        }

        if (!findResource.isPresent()) {
            LOGGER.log(Level.INFO, String.format("Failed to find Rule for for path '%s", resourcePath));

        }
        return findResource;
    }

    private Optional<Resource> findCustomMatch(final String resourcePath, final List<Resource> resources, Optional<Resource> findResource, final String sanitisedResourcePath) {

        if (!findResource.isPresent()) {
            for (final Resource resource : resources
            ) {
                if (this.findExactMatch(resourcePath, resource.resourcePath())) {
                    LOGGER.log(Level.INFO, String.format("Custom Match found for resource path '%s'", sanitisedResourcePath));
                    findResource = of(resource);
                    break;
                }

            }
        }
        return findResource;
    }

    private Optional<Resource> findHardMatch(final List<Resource> resources, Optional<Resource> findResource, final String sanitisedResourcePath) {
        for (final Resource resource : resources
        ) {
            if (sanitisedResourcePath.equalsIgnoreCase(resource.resourcePath())) {
                LOGGER.log(Level.INFO, String.format("Hard Match found for resource path '%s'", sanitisedResourcePath));
                findResource = of(resource);
                break;
            }
        }
        return findResource;
    }

    private boolean findExactMatch(final String uri, final String pattern) {
        final int uCount = StringUtils.countMatches(uri, "/");
        final int pCount = StringUtils.countMatches(pattern, "/");
        if (uCount == pCount) {
            final String[] s1 = uri.split("/");
            final String[] s2 = pattern.split("/");
            if (s1.length == s2.length) {
                for (int i = 0; i < s1.length; i++) {
                    if (s1[i].equals(s2[i]) || (s2[i].contains("{") && s2[i].contains("}"))) {
                        if ((i > 0 && s1[i].isEmpty())) {
                            return false;
                        }
                    } else {
                        return false;
                    }

                }
                return true;
            }
        }

        return false;
    }

    public Optional<Method> findMethod(final String methodType, final String resourcePath) {
        Optional<Method> findMethod = Optional.empty();
        if (!StringUtils.isBlank(methodType)) {
            Optional<Resource> findResource = this.findResource(resourcePath);

            if (findResource.isPresent()) {
                for (final Method method : findResource.get().methods()
                ) {
                    if (StringUtils.isBlank(methodType) && methodType.equalsIgnoreCase(method.method())) {
                        LOGGER.log(Level.INFO, String.format("Hard Match found for method type '%s' at path '%s", methodType, resourcePath));
                        findMethod = of(method);
                        break;
                    }
                }
            }

        }

        return findMethod;
    }

    private void init() {
        try {
            File ramlFile;
            if (this.isRamlOnClassPath) {
                ramlFile = this.loadFileFromClasspath();
            } else if (this.ramlLocation.toLowerCase().endsWith(".raml")) {
                ramlFile = new File(this.ramlLocation);
                if (!ramlFile.exists()) {
                    throw new IllegalArgumentException("Invalid path");
                }
            } else {
                ramlFile = this.unzipAndLoadRaml();
            }

            //RamlModelResult ramlModelResult = new RamlModelBuilder().buildApi(getInputStreamRamlFromClasspath(),ramlLocation);
            RamlModelResult ramlModelResult = new RamlModelBuilder().buildApi(ramlFile);
            if (ramlModelResult.hasErrors()) {

                throw RamlInitializationException.create(ramlModelResult.getValidationResults());
            } else {
                if (ramlModelResult.isVersion10()) {
                    Api api = ramlModelResult.getApiV10();
                    this.extractChildResources(api.resources());
                } else {
                    throw new IllegalArgumentException("Not Supported");
                }
            }
        } catch (Exception e) {
            throw RamlInitializationException.create(e);
        }
    }

    private void extractChildResources(List<Resource> resources) {
        if (!isEmpty(resources)) {
            Iterator<Resource> itr = resources.iterator();
            while (itr.hasNext()) {
                Resource resource = itr.next();
                this.RESOURCES.add(resource);
                LOGGER.log(Level.INFO, String.format("RAML Policy for resource path %s loaded", resource.resourcePath()));
                this.extractChildResources(resource.resources());
            }
        }
    }

    private void getBaseRaml(File ramlBaseLocation, AtomicReference<File> fileAtomicReference) {
        File[] files = ramlBaseLocation.listFiles();

        if (files.length == 1 && files[0].isDirectory()) {
            this.getBaseRaml(new File(ramlBaseLocation.getAbsoluteFile() + File.separator + files[0].getName()), fileAtomicReference);
        } else {
            File[] files1 = ramlBaseLocation.listFiles((pathname -> {
                return pathname.getName().toLowerCase().endsWith(".raml");
            }));
            if (files1.length != 1) {
                throw RamlInitializationException.create(FAILED_TO_LOAD_RAML_FROM_FILESYS);
            }
            fileAtomicReference.set(files1[0].getAbsoluteFile());
        }
    }

    private InputStreamReader getInputStreamRamlFromClasspath() {
        InputStream is = this.getClass().getResourceAsStream(ramlLocation);
        return new InputStreamReader(is);
    }

    private File loadFileFromClasspath() {
        File file = new File(this.ramlLocation);
        if (file.exists()) {
            return file;
        }
        ClassPathResource classPathResource = new ClassPathResource(this.ramlLocation);
        if (classPathResource.exists()) {
            try {
                file = classPathResource.getFile();
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to load RAML");
            }
            return file;
        }
        URL url = Efficacy.class.getClassLoader().getResource("Failed to Load Dir from Classpath");
        if (url != null) {
            file = new File(url.getFile());
            if (file.exists()) {
                return file;
            }
        }
        return file;
    }

    private void unzip(String zipFilePath, String unZipLocation) throws IOException {
        if (!Files.exists(Paths.get(unZipLocation))) {
            Files.createDirectories(Paths.get(unZipLocation));
        }

        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry = zipInputStream.getNextEntry();
            while (entry != null) {
                Path filePath = Paths.get(unZipLocation, entry.getName());
                if (!entry.isDirectory()) {
                    if (!(new File(filePath.toFile().getParent())).exists()) {
                        Files.createDirectories(filePath.getParent());
                    }
                    unZipFiles(zipInputStream, filePath);
                } else {
                    Files.createDirectories(filePath);
                }
                zipInputStream.closeEntry();
                entry = zipInputStream.getNextEntry();
            }
        }

    }

    private File unzipAndLoadRaml() throws IOException {
        File tempDir = com.google.common.io.Files.createTempDir();
        this.unzip(this.ramlLocation, tempDir.getAbsolutePath());
        AtomicReference<File> fileAtomicReference = new AtomicReference<>();
        this.getBaseRaml(tempDir.getAbsoluteFile(), fileAtomicReference);
        return fileAtomicReference.get();
    }

}
