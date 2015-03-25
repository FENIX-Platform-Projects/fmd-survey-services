package org.fao.fenix.fmd.tools.utils;

import sun.net.www.protocol.file.FileURLConnection;

import javax.enterprise.context.ApplicationScoped;
import java.io.*;
import java.lang.reflect.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

@ApplicationScoped
public class ClassUtils {

    /**
     * Private helper method
     *
     * @param directory
     *            The directory to start with
     * @param pckgname
     *            The package name to search for. Will be needed for getting the
     *            Class object.
     * @param classes
     *            if a file isn't loaded but still is in the directory
     * @throws ClassNotFoundException
     */
    private static void checkDirectory(File directory, String pckgname,
                                       ArrayList<Class<?>> classes) throws ClassNotFoundException {
        File tmpDirectory;

        if (directory.exists() && directory.isDirectory()) {
            final String[] files = directory.list();

            for (final String file : files) {
                if (file.endsWith(".class")) {
                    try {
                        classes.add(Class.forName(pckgname + '.'
                                + file.substring(0, file.length() - 6)));
                    } catch (final NoClassDefFoundError e) {
                        // do nothing. this class hasn't been found by the
                        // loader, and we don't care.
                    }
                } else if ((tmpDirectory = new File(directory, file))
                        .isDirectory()) {
                    checkDirectory(tmpDirectory, pckgname + "." + file, classes);
                }
            }
        }
    }

    /**
     * Private helper method.
     *
     * @param connection
     *            the connection to the jar
     * @param pckgname
     *            the package name to search for
     * @param classes
     *            the current ArrayList of all classes. This method will simply
     *            add new classes.
     * @throws ClassNotFoundException
     *             if a file isn't loaded but still is in the jar file
     * @throws IOException
     *             if it can't correctly read from the jar file.
     */
    private static void checkJarFile(JarURLConnection connection,
                                     String pckgname, ArrayList<Class<?>> classes)
            throws ClassNotFoundException, IOException {
        final JarFile jarFile = connection.getJarFile();
        final Enumeration<JarEntry> entries = jarFile.entries();
        String name;

        for (JarEntry jarEntry = null; entries.hasMoreElements()
                && ((jarEntry = entries.nextElement()) != null);) {
            name = jarEntry.getName();

            if (name.contains(".class")) {
                name = name.substring(0, name.length() - 6).replace('/', '.');

                if (name.contains(pckgname)) {
                    classes.add(Class.forName(name));
                }
            }
        }
    }

    /**
     * Attempts to list all the classes in the specified package as determined
     * by the context class loader
     *
     * @param pckgname
     *            the package name to search
     * @return a list of classes that exist within that package
     * @throws ClassNotFoundException
     *             if something went wrong
     */
    public static ArrayList<Class<?>> getClasses(String pckgname)
            throws ClassNotFoundException {
        final ArrayList<Class<?>> classes = new ArrayList<Class<?>>();

        try {
            final ClassLoader cld = Thread.currentThread()
                    .getContextClassLoader();

            if (cld == null)
                throw new ClassNotFoundException("Can't get class loader.");

            final Enumeration<URL> resources = cld.getResources(pckgname
                    .replace('.', '/'));
            URLConnection connection;

            for (URL url = null; resources.hasMoreElements()
                    && ((url = resources.nextElement()) != null);) {
                try {
                    connection = url.openConnection();

                    if (connection instanceof JarURLConnection) {
                        checkJarFile((JarURLConnection) connection, pckgname,
                                classes);
                    } else if (connection instanceof FileURLConnection) {
                        try {
                            checkDirectory(
                                    new File(URLDecoder.decode(url.getPath(),
                                            "UTF-8")), pckgname, classes);
                        } catch (final UnsupportedEncodingException ex) {
                            throw new ClassNotFoundException(
                                    pckgname
                                            + " does not appear to be a valid package (Unsupported encoding)",
                                    ex);
                        }
                    } else
                        throw new ClassNotFoundException(pckgname + " ("
                                + url.getPath()
                                + ") does not appear to be a valid package");
                } catch (final IOException ioex) {
                    throw new ClassNotFoundException(
                            "IOException was thrown when trying to get all resources for "
                                    + pckgname, ioex);
                }
            }
        } catch (final NullPointerException ex) {
            throw new ClassNotFoundException(
                    pckgname
                            + " does not appear to be a valid package (Null pointer exception)",
                    ex);
        } catch (final IOException ioex) {
            throw new ClassNotFoundException(
                    "IOException was thrown when trying to get all resources for "
                            + pckgname, ioex);
        }

        return classes;
    }
/*
    public Set<Class<?>> getClasses(String packageName) throws Exception {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        return getClasses(loader, packageName);
    }

    public Set<Class<?>> getClasses(ClassLoader loader, String packageName) throws IOException, ClassNotFoundException {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = loader.getResources(path);
        if (resources != null) {
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                String filePath = url.getFile();
                // WINDOWS HACK
                if(filePath.indexOf("%20") > 0)
                    filePath = filePath.replaceAll("%20", " ");
                if (filePath != null) {
                    if ((filePath.indexOf("!") > 0) & (filePath.indexOf(".jar") > 0)) {
                        String jarPath = filePath.substring(0, filePath.indexOf("!"))
                                .substring(filePath.indexOf(":") + 1);
                        // WINDOWS HACK
                        if (jarPath.indexOf(":") >= 0) jarPath = jarPath.substring(1);
                        classes.addAll(getFromJARFile(jarPath, path));
                    } else {
                        classes.addAll(
                                getFromDirectory(new File(filePath), packageName));
                    }
                }
            }
        }
        return classes;
    }

    private Set<Class<?>> getFromDirectory(File directory, String packageName) throws ClassNotFoundException {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        if (directory.exists()) {
            for (String file : directory.list()) {
                if (file.endsWith(".class")) {
                    String name = packageName + '.' + stripFilenameExtension(file);
                    Class<?> clazz = Class.forName(name);
                    classes.add(clazz);
                }
            }
        }
        return classes;
    }

    private Set<Class<?>> getFromJARFile(String jar, String packageName) throws IOException, ClassNotFoundException {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        JarInputStream jarFile = new JarInputStream(new FileInputStream(jar));
        JarEntry jarEntry;
        do {
            jarEntry = jarFile.getNextJarEntry();
            if (jarEntry != null) {
                String className = jarEntry.getName();
                if (className.endsWith(".class")) {
                    className = stripFilenameExtension(className);
                    if (className.startsWith(packageName))
                        classes.add(Class.forName(className.replace('/', '.')));
                }
            }
        } while (jarEntry != null);
        return classes;
    }

    private String stripFilenameExtension(String className) {
        return className.substring(0,className.lastIndexOf('.'));
    }


*/
    //Pojo conversion
    public <T> T convert (Object source, Class<T> destinationClass) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        return convert(
                source, source.getClass(), null, source.getClass().getPackage().getName(),
                destinationClass, null, destinationClass.getPackage().getName()
        );
    }

    private <T> T convert (Object source, Class<?> sourceClass, Type sourceType, String sourcePackage, Class<T> destinationClass, Type[] destinationType, String destinationPackage) throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        if (source==null)
            return null;

        if (destinationClass.isAssignableFrom(sourceClass))
            return (T)source;

        T destination = destinationClass.newInstance();
        if (Collection.class.isAssignableFrom(sourceClass))
                for (Object s : (Collection)source)
                    ((Collection)destination).add(convert(  s, (Class)((ParameterizedType)sourceType).getActualTypeArguments()[0], null, sourcePackage,
                                                            (Class)((ParameterizedType)destinationType[0]).getActualTypeArguments()[0], null, destinationPackage    ));

        Class<?>[] destinationParameterClass = null;
        Method sourceMethod =null;
        for (Method m : destinationClass.getMethods())
            if (m.getReturnType().equals(Void.TYPE) && Modifier.isPublic(m.getModifiers()) && m.getName().startsWith("set") && (destinationParameterClass=m.getParameterTypes()).length==1)
                if (( sourceMethod = sourceClass.getMethod(destinationParameterClass[0].equals(Boolean.class) ? "is" + m.getName().substring(3) : 'g' + m.getName().substring(1)) )!=null)
                    m.invoke(destination, convert(sourceMethod.invoke(source), sourceMethod.getReturnType(), sourceMethod.getGenericReturnType(), sourcePackage, destinationParameterClass[0], m.getGenericParameterTypes(), destinationPackage) );

        return destination;
    }


    public byte[] serialize(Object obj) throws IOException {
        if (obj==null)
            return null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }
    public Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        if (data==null || data.length==0)
            return null;
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }


}
