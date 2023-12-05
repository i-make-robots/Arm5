package com.marginallyclever.robotoverlord.renderpanel;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.marginallyclever.convenience.helpers.FileHelper;
import com.marginallyclever.robotoverlord.parameters.TextureParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class TextureFactory {
    private static final Logger logger = LoggerFactory.getLogger(TextureFactory.class);
    public static final HashMap<String, Texture> texturePool = new HashMap<>();

    // supported file formats
    private static final List<FileFilter> filters = List.of(
            new FileNameExtensionFilter("PNG", "png"),
            new FileNameExtensionFilter("BMP", "bmp"),
            new FileNameExtensionFilter("JPEG", "jpeg","jpg"),
            new FileNameExtensionFilter("TGA", "tga")
    );

    public static List<FileFilter> getFilters() {
        return filters;
    }

    private static Texture loadTextureFromFile(String filename) {
        Texture t = null;

        try {
            t = TextureIO.newTexture(FileHelper.open(filename), false, filename.substring(filename.lastIndexOf('.') + 1));
        } catch (IOException e) {
            //e.printStackTrace();
            logger.error("Failed to load {}", filename, e);
        }

        return t;
    }

    public static void unloadAll(GL3 gl) {
        for (Texture t : texturePool.values()) {
            t.destroy(gl);
        }
    }

    public static void loadAll() {
        Set<String> keys = texturePool.keySet();
        texturePool.clear();
        for (String key : keys) {
            Texture t = TextureFactory.createTexture(key);
            texturePool.put(key, t);
        }
    }

    public static Texture createTexture(String filename) {
        Texture texture = texturePool.get(filename);
        if (texture == null) {
            texture = loadTextureFromFile(filename);
            if (texture != null)
                texturePool.put(filename, texture);
        }
        return texture;
    }
}
