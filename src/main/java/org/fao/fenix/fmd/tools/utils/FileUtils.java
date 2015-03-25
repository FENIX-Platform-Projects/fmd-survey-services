package org.fao.fenix.fmd.tools.utils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@ApplicationScoped
public class FileUtils {
    public static final Charset UTF8 = Charset.forName("UTF-8");

    public void delete(File toDelete) {
        if (toDelete.exists()) {
            if (toDelete.isDirectory())
                for (File f:toDelete.listFiles())
                    delete(f);
            toDelete.delete();
        }
    }

    public String readTextFile(String file) throws IOException { return readTextFile(new FileInputStream(file), UTF8); }
    public String readTextFile(File file) throws IOException { return readTextFile(new FileInputStream(file), UTF8); }
    public String readTextFile(String file, Charset charset) throws IOException { return readTextFile(new FileInputStream(file), charset); }
    public String readTextFile(File file, Charset charset) throws IOException {return readTextFile(new FileInputStream(file),charset); }
    public String readTextFile(InputStream in) throws IOException { return readTextFile(in, UTF8); }
    public String readTextFile(InputStream in, Charset charset) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in,charset));
        StringBuilder buffer = new StringBuilder();
        for (String line = reader.readLine(); line!=null; line = reader.readLine())
            buffer.append('\n').append(line);
        return buffer.length()>0 ? buffer.substring(1) : null;
    }

    public void writeTextFile(File file, String text) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(file,false), 1024);
        out.write(text);
        out.flush();
        out.close();
    }

    public void appendToBinaryFile (File file, byte[] data) throws IOException {
        if (!file.exists())
            file.createNewFile();

        OutputStream out = new FileOutputStream(file,true);
        out.write(data);
        out.close();
    }
    public void appendToBinaryFile (File file, InputStream data) throws IOException {
        if (!file.exists())
            file.createNewFile();

        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file, true), 1024*10);
        byte[] buffer = new byte[1024*10];
        while (data.available()>0)
            out.write(buffer,0,data.read(buffer));
        out.close();
    }

    public byte[] readBinaryFile(File file) throws IOException {
        if (file.exists() && file.isFile()) {
            byte[] content = new byte[(int)file.length()];
            new FileInputStream(file).read(content);
            return content;
        } else
            return null;
    }


}
