package com.bhadouriya.raml.efficacies;

import com.bhadouriya.raml.artifacts.WrapperApi;
import org.apache.commons.io.IOUtils;
import org.raml.v2.api.RamlModelBuilder;
import org.raml.v2.api.RamlModelResult;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.nio.file.Files.createDirectories;
import static java.util.Arrays.asList;
import static org.apache.commons.io.FilenameUtils.isExtension;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.uncapitalize;
import static org.apache.commons.lang.WordUtils.capitalize;
import static org.springframework.util.FileSystemUtils.deleteRecursively;

public class Efficacy {

    public static String abbreviator(final String packageName) {
        final String abbrev = alphaNumericWithSpace(packageName);
        final String[] words = abbrev.split(" ");
        final StringBuilder initials = new StringBuilder();
        if (words.length > 1) {
            final Pattern p = Pattern.compile("\\b[a-zA-Z]");
            final Matcher m = p.matcher(abbrev);
            while (m.find()) {
                initials.append(m.group());
            }
        } else {
            initials.append(words[0]);
        }
        return initials.toString().toLowerCase();
    }

    public static String alphaNumeric(final String packageName) {
        return packageName.replaceAll("[^a-zA-Z0-9]", "").trim();
    }

    public static String alphaNumeric(final String packageName, final String replace) {
        return packageName.replaceAll("[^a-zA-Z0-9]", replace).trim();
    }

    public static String alphaNumericWithSpace(final String packageName) {
        return packageName.replaceAll("[^a-zA-Z0-9 ]", "").trim();
    }

    public static String capatalizeAndAppend(final String packageName) {
        return alphaNumeric(capitalize(alphaNumeric(packageName, " ")));
    }

    public static void deleteDir(final File dir) {
        deleteRecursively(dir);
    }

    public static String format(final long time) {
        return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date(time));
    }

    public static String getConstantName(final Class<?> clz, final String value, final boolean isEnum) {
        String[] constantName = {""};
        asList(clz.getDeclaredFields()).forEach(fd -> {
            final int mod = fd.getModifiers();
            try {
                if (Modifier.isPublic(mod) && Modifier.isStatic(mod) && Modifier.isFinal(mod)
                        && (value.equalsIgnoreCase(fd.get(null).toString()) || (isEnum && value.equalsIgnoreCase(fd.getName())))) {
                    constantName[0] = fd.getName();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return constantName[0];
    }

    public static URL getCurrentUrl(final HttpServletRequest request) {
        try {
            final URL url = new URL(request.getRequestURL().toString());
            final String host = url.getHost();
            final String userInfo = url.getUserInfo();
            final String scheme = url.getProtocol();
            final int port = url.getPort();
            final String path = (String) request.getAttribute("javax.servlet.forward.request_uri");
            final String query = (String) request.getAttribute("javax.servlet.forward.query_string");
            final URI uri = new URI(scheme, userInfo, host, port, path, query, null);

            return new URL(uri.toString());
        } catch (MalformedURLException | URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getIndentString(final int indent) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            sb.append("|  ");
        }
        return sb.toString();
    }

    public static String getStubUrl(final URL baseUrl, final String seperatorToUnix) {
        String stubUrl = seperatorToUnix;
        try {
            stubUrl = new URL(baseUrl, seperatorToUnix).toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return stubUrl;
    }

    public static void listDirectories(final String direcName, final Map<String, File> directories) {
        final File directory = new File(direcName);
        final File[] fList = directory.listFiles();
        for (final File file : fList) {
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

    public static File loadFileFromClasspath(final String path) throws IOException {
        File file = new File(path);
        if (file.exists()) {
            return file;
        }
        final ClassPathResource classPathResource = new ClassPathResource(path);
        if (classPathResource.exists()) {
            file = classPathResource.getFile();
            return file;
        }
        final URL url = Efficacy.class.getClassLoader().getResource("Failed to Load Dir from Classpath");
        if (url != null) {
            file = new File(url.getFile());
            if (file.exists()) {
                return file;
            }
        }
        return file;
    }

    public static String printDirectoryTree(final File dosire) {
        if (!dosire.isDirectory()) {
            throw new IllegalArgumentException("Not Directory");
        }
        final int indent = 0;
        final StringBuilder sb = new StringBuilder();
        printDirectoryTree(dosire, indent, sb);
        return sb.toString();
    }

    public static void printDirectoryTree(final File dosire, final int indent, final StringBuilder sb) {
        if (!dosire.isDirectory()) {
            throw new IllegalArgumentException("Not Directory");
        }
        sb.append(getIndentString(indent));
        sb.append("+--");
        sb.append(dosire.getName());
        sb.append("/");
        sb.append("\n");
        for (final File file : dosire.listFiles()) {
            if (file.isDirectory()) {
                printDirectoryTree(file, indent + 1, sb);
            } else {
                printFile(file, indent + 1, sb);
            }
        }
    }

    private static void printFile(File file, int indent, StringBuilder sb) {
        sb.append(getIndentString(indent));
        sb.append("+--");
        sb.append(file.getName());
        sb.append("\n");
    }

    public static void printToErr(final String str) {
        System.err.println(str);
        System.err.flush();
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void printToOut(final String str) {
        System.out.println(str);
        System.out.flush();
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static WrapperApi readRaml(final String file) {
        WrapperApi wrapperApi = null;
        final RamlModelResult ramlModelResult = new RamlModelBuilder().buildApi(file);
        if (ramlModelResult.hasErrors()) {
            final List<String> errs = new ArrayList<>();
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

    public static String schemaName(final String beanName) {
        return uncapitalize(beanName.replaceAll("([A-Z])", "-$1").replaceFirst("-", ""));
    }

    private static void unZipEachFile(final FileSystem fileSystem, final String[] apiRamlFile, final ZipFile file
            , final Enumeration<? extends ZipEntry> entries, final String uncompressedDir) throws IOException {
        final ZipEntry entry = entries.nextElement();

        //If Dir then create new Dir in uncompresse Folder
        if (entry.isDirectory()) {
            createDirectories(fileSystem.getPath(uncompressedDir + File.separator + entry.getName()));
        }
        //Else create the file
        else {
            final InputStream is = file.getInputStream(entry);
            final BufferedInputStream bis = new BufferedInputStream(is);
            final String unCompressedFileName = uncompressedDir + File.separator + entry.getName();
            final Path unCompressedFilePath = fileSystem.getPath(unCompressedFileName);
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
            try (FileOutputStream fileOutputStream = new FileOutputStream(unCompressedFileName)) {
                while (bis.available() > 0) {
                    fileOutputStream.write(bis.read());
                }
            }
        }
    }

    public static String[] unzipRaml(final MultipartFile mulFile, final String orininalName, final Path tempUserDirectory, final FileSystem fileSystem) throws IOException {
        final String[] fileNames = {null, null, null};

        final File zipFile = File.createTempFile(orininalName, null, tempUserDirectory.toFile());
        IOUtils.copy(mulFile.getInputStream(), new FileOutputStream(zipFile));

        //Open File
        try (ZipFile file = new ZipFile(zipFile)) {
            //get the entries
            final Enumeration<? extends ZipEntry> entries = file.entries();

            final Path tempRamlDir = Files.createTempDirectory(tempUserDirectory, orininalName + "_");

            //we will unzip files in this folder
            final String uncompressedDirectory = tempRamlDir.toString();

            //Iterate over

            while (entries.hasMoreElements()) {
                unZipEachFile(fileSystem, fileNames, file, entries, uncompressedDirectory);
                fileNames[2] = uncompressedDirectory;
            }
            zipFile.delete();
            return fileNames;

        }
    }
}