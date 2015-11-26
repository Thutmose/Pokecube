package com.mcf.davidee.nbteditpqb.gui;

import com.mcf.davidee.nbteditpqb.NBTStringHelper;

import net.minecraft.util.ChatAllowedCharacters;

public class CharacterFilter {
	public static String filerAllowedCharacters(String str, boolean section) {
        StringBuilder sb = new StringBuilder();
        char[] arr = str.toCharArray();
		for (char c : arr) {
			if (ChatAllowedCharacters.isAllowedCharacter(c) || (section && (c == NBTStringHelper.SECTION_SIGN || c == '\n')))
				sb.append(c);
		}

        return sb.toString();
    }
}
