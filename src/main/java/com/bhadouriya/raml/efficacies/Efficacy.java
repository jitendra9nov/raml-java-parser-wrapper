package com.bhadouriya.raml.efficacies;

import com.bhadouriya.raml.artifacts.WrapperApi;
import com.bhadouriya.raml.efficacies.demo.Response;
import com.bhadouriya.raml.efficacies.demo.SubResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.raml.v2.api.RamlModelBuilder;
import org.raml.v2.api.RamlModelResult;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.nio.file.Files.createDirectories;
import static java.util.Arrays.asList;
import static java.util.Base64.getUrlDecoder;
import static java.util.Base64.getUrlEncoder;
import static org.apache.commons.io.FilenameUtils.isExtension;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.uncapitalize;
import static org.apache.commons.lang.WordUtils.capitalize;
import static org.springframework.util.FileSystemUtils.deleteRecursively;

public class Efficacy {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String abbreviator(String packageName) {
        String abbrev = alphaNumericWithSpace(packageName);
        String[] words = abbrev.split(" ");
        StringBuilder initials = new StringBuilder();
        if (words.length > 1) {
            Pattern p = Pattern.compile("\\b[a-zA-Z]");
            Matcher m = p.matcher(abbrev);
            while (m.find()) {
                initials.append(m.group());
            }
        } else {
            initials.append(words[0]);
        }
        return initials.toString().toLowerCase();
    }

    public static String alphaNumeric(String packageName) {
        return packageName.replaceAll("[^a-zA-Z0-9]", "").trim();
    }

    public static String alphaNumeric(String packageName, String replace) {
        return packageName.replaceAll("[^a-zA-Z0-9]", replace).trim();
    }

    public static String alphaNumericWithSpace(String packageName) {
        return packageName.replaceAll("[^a-zA-Z0-9 ]", "").trim();
    }

    public static String capatalizeAndAppend(String packageName) {
        return alphaNumeric(capitalize(alphaNumeric(packageName, " ")));
    }

    public static void deleteDir(File dir) {
        deleteRecursively(dir);
    }

    public static String format(long time) {
        return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date(time));
    }

    public static String getConstantName(Class<?> clz, String value, boolean isEnum) {
        final String[] constantName = {""};
        asList(clz.getDeclaredFields()).forEach(fd -> {
            int mod = fd.getModifiers();
            try {
                if (Modifier.isPublic(mod) && Modifier.isStatic(mod) && Modifier.isFinal(mod)
                        && (value.equalsIgnoreCase(fd.get(null).toString()) || (isEnum && value.equalsIgnoreCase(fd.getName())))) {
                    constantName[0] = fd.getName();
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        });

        return constantName[0];
    }

    public static URL getCurrentUrl(HttpServletRequest request) {
        try {
            URL url = new URL(request.getRequestURL().toString());
            String host = url.getHost();
            String userInfo = url.getUserInfo();
            String scheme = url.getProtocol();
            int port = url.getPort();
            String path = (String) request.getAttribute("javax.servlet.forward.request_uri");
            String query = (String) request.getAttribute("javax.servlet.forward.query_string");
            URI uri = new URI(scheme, userInfo, host, port, path, query, null);

            return new URL(uri.toString());
        } catch (final MalformedURLException | URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getIndentString(int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            sb.append("|  ");
        }
        return sb.toString();
    }

    public static String getStubUrl(URL baseUrl, String seperatorToUnix) {
        String stubUrl = seperatorToUnix;
        try {
            stubUrl = new URL(baseUrl, seperatorToUnix).toString();
        } catch (final MalformedURLException e) {
            e.printStackTrace();
        }
        return stubUrl;
    }

    public static void listDirectories(String direcName, Map<String, File> directories) {
        File directory = new File(direcName);
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                continue;
            } else if (file.isDirectory()) {
                {
                    directories.put(file.getName(), file);
                    listDirectories(file.getAbsolutePath(), directories);
                }
            }
        }
    }

    public static File loadFileFromClasspath(String path) throws IOException {
        File file = new File(path);
        if (file.exists()) {
            return file;
        }
        ClassPathResource classPathResource = new ClassPathResource(path);
        if (classPathResource.exists()) {
            file = classPathResource.getFile();
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

    public static String printDirectoryTree(File dosire) {
        if (!dosire.isDirectory()) {
            throw new IllegalArgumentException("Not Directory");
        }
        final int indent = 0;
        StringBuilder sb = new StringBuilder();
        printDirectoryTree(dosire, indent, sb);
        return sb.toString();
    }

    public static void printDirectoryTree(File dosire, int indent, StringBuilder sb) {
        if (!dosire.isDirectory()) {
            throw new IllegalArgumentException("Not Directory");
        }
        sb.append(getIndentString(indent));
        sb.append("+--");
        sb.append(dosire.getName());
        sb.append("/");
        sb.append("\n");
        for (File file : dosire.listFiles()) {
            if (file.isDirectory()) {
                printDirectoryTree(file, indent + 1, sb);
            } else {
                printFile(file, indent + 1, sb);
            }
        }
    }

    private static void printFile(final File file, final int indent, final StringBuilder sb) {
        sb.append(getIndentString(indent));
        sb.append("+--");
        sb.append(file.getName());
        sb.append("\n");
    }

    public static void printToErr(String str) {
        System.err.println(str);
        System.err.flush();
        try {
            Thread.sleep(10);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void printToOut(String str) {
        System.out.println(str);
        System.out.flush();
        try {
            Thread.sleep(10);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static WrapperApi readRaml(String file) {
        WrapperApi wrapperApi = null;
        RamlModelResult ramlModelResult = new RamlModelBuilder().buildApi(file);
        if (ramlModelResult.hasErrors()) {
            List<String> errs = new ArrayList<>();
            ramlModelResult.getValidationResults().forEach(validationResult -> {
                errs.add(validationResult.getPath() + validationResult.getMessage());
            });
        } else {
            if (ramlModelResult.isVersion10()) {
                wrapperApi = new WrapperApi(ramlModelResult.getApiV10());
            } else {
                throw new IllegalArgumentException("Not Supported");
            }
        }
        return wrapperApi;

    }

    public static String schemaName(String beanName) {
        return uncapitalize(beanName.replaceAll("([A-Z])", "-$1").replaceFirst("-", ""));
    }

    private static void unZipEachFile(FileSystem fileSystem, String[] apiRamlFile, ZipFile file
            , Enumeration<? extends ZipEntry> entries, String uncompressedDir) throws IOException {
        ZipEntry entry = entries.nextElement();

        //If Dir then create new Dir in uncompresse Folder
        if (entry.isDirectory()) {
            createDirectories(fileSystem.getPath(uncompressedDir + File.separator + entry.getName()));
        }
        //Else create the file
        else {
            InputStream is = file.getInputStream(entry);
            BufferedInputStream bis = new BufferedInputStream(is);
            String unCompressedFileName = uncompressedDir + File.separator + entry.getName();
            Path unCompressedFilePath = fileSystem.getPath(unCompressedFileName);
            if (isExtension(entry.getName(), asList("raml", "RANL"))) {
                if (isBlank(apiRamlFile[0]) && (null == new File(entry.getName()).getParent())) {
                    apiRamlFile[0] = unCompressedFileName;
                } else if (isBlank(apiRamlFile[1])) {
                    apiRamlFile[1] = unCompressedFileName;
                }
            }
            if (!unCompressedFilePath.toFile().exists()) {
                createDirectories(unCompressedFilePath.getParent());
            }
            Files.createFile(unCompressedFilePath);
            try (final FileOutputStream fileOutputStream = new FileOutputStream(unCompressedFileName)) {
                while (bis.available() > 0) {
                    fileOutputStream.write(bis.read());
                }
            }
        }
    }

    public static String[] unzipRaml(MultipartFile mulFile, String orininalName, Path tempUserDirectory, FileSystem fileSystem) throws IOException {
        String[] fileNames = {null, null, null};

        File zipFile = File.createTempFile(orininalName, null, tempUserDirectory.toFile());
        IOUtils.copy(mulFile.getInputStream(), new FileOutputStream(zipFile));

        //Open File
        try (final ZipFile file = new ZipFile(zipFile)) {
            //get the entries
            Enumeration<? extends ZipEntry> entries = file.entries();

            Path tempRamlDir = Files.createTempDirectory(tempUserDirectory, orininalName + "_");

            //we will unzip files in this folder
            String uncompressedDirectory = tempRamlDir.toString();

            //Iterate over

            while (entries.hasMoreElements()) {
                unZipEachFile(fileSystem, fileNames, file, entries, uncompressedDirectory);
                fileNames[2] = uncompressedDirectory;
            }
            zipFile.delete();
            return fileNames;

        }
    }


    public static String decrypt(final String encrypt) {
        final byte[] decodeBytes = getUrlDecoder().decode(encrypt);
        return new String(decodeBytes);
    }

    public static <T> T decrypt(final String encrypt, final Class<T> type) throws IOException {
        final byte[] decodeBytes = getUrlDecoder().decode(encrypt);
        return parseJSON(new String(decodeBytes), type);
    }


    public static String encrypt(final Object object) throws IOException {
        final String jsonString = toJSON(object);
        final byte[] text = jsonString.getBytes();
        return getUrlEncoder().withoutPadding().encodeToString(text);
    }


    public static String encrypt(final String plainString) throws IOException {
        final byte[] text = plainString.getBytes();
        return getUrlEncoder().withoutPadding().encodeToString(text);
    }

    private static <T> T parseJSON(final String jsonString, final Class<T> type) throws IOException {
        return mapper.readValue(jsonString, type);
    }

    private static String toJSON(final Object object) throws IOException {
        String result = "";
        if (null != object) {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            mapper.writeValue(out, object);
            result = out.toString("UTF-8");
        }
        return result;
    }

    private static void printConstantValues() throws IOException, IllegalAccessException {
        Path path = Paths.get("./Error.txt");
        Field[] fields = SubResponse.class.getDeclaredFields();
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path)) {
            for (Field f : fields
            ) {
                if (Modifier.isStatic(f.getModifiers()) && (f.getType() == SubResponse.class || f.getType() == Response.class)) {
                    SubResponse subResponse = (SubResponse) f.get(null);

                    bufferedWriter.write(subResponse.getCode() + "|" + subResponse.getDescription()
                            + "|" + printFieldValue(subResponse.getAttributes()) + "\n");
                }
            }
        }
    }

    //Access private field by using field name
    private static String printFieldValue(List<?> attributes) {
        String finalList = "";
        if (!CollectionUtils.isEmpty(attributes)) {
            finalList = attributes.stream().map(obj -> {
                String value = null;
                Field field = null;
                try {
                    field = obj.getClass().getDeclaredField("description");
                } catch (Exception e) {
                    try {
                        field = obj.getClass().getSuperclass().getDeclaredField("description");
                    } catch (Exception e1) {
                        value = "NO_SUCH_FIELD$$$";
                    }
                }
                if (null != field && Modifier.isPrivate(field.getModifiers())) {
                    field.setAccessible(true);
                    try {
                        value = (String) field.get(obj);
                    } catch (Exception e1) {
                        value = "NO_SUCH_FIELD$$$";
                    }
                }
                return value;
            }).collect(Collectors.joining(","));
        }
        return finalList;
    }

    public static String createClasName(String prefix, String baseClass) {
        return MessageFormat.format("{0}{1}", capitalize(prefix), capitalize(baseClass));
    }
}