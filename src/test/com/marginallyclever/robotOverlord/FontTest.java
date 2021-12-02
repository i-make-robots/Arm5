package com.marginallyclever.robotOverlord;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import java.awt.*;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisabledIfEnvironmentVariable(named = "CI", matches = "true")
public class FontTest {

    @Test
    public void testCompatibleFonts() {
        String s = "\u23EF";
        Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        System.out.println("Total fonts: \t" + fonts.length);
        assertTrue(Arrays.stream(fonts).filter(font -> font.canDisplayUpTo(s) < 0).count() > 0);
    }

}
