// COPIED FROM https://github.com/vsch/flexmark-java/blob/master/flexmark-ext-resizable-image/src/main/java/com/vladsch/flexmark/ext/resizable/image/internal/ResizableImageInlineParserExtension.java TO FIX AN ISSUE, SEE // FIX
// REMOVE ME AFTER THIS GOT MERGED: https://github.com/vsch/flexmark-java/pull/503
// AND https://github.com/vsch/flexmark-java/issues/542 IS RESOLVED

/**
 * Copyright (c) 2015-2016, Atlassian Pty Ltd
 * All rights reserved.
 * <p>
 * Copyright (c) 2016-2018, Vladimir Schneider,
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p>
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.vladsch.flexmark.ext.resizable.image.internal;

import com.vladsch.flexmark.ext.resizable.image.ResizableImage;
import com.vladsch.flexmark.parser.InlineParser;
import com.vladsch.flexmark.parser.InlineParserExtension;
import com.vladsch.flexmark.parser.InlineParserExtensionFactory;
import com.vladsch.flexmark.parser.LightInlineParser;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import java.util.Set;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ResizableImageInlineParserExtension implements InlineParserExtension {
    public static final Pattern IMAGE_PATTERN = Pattern.compile("!\\[([^\\s\\]]*)]\\(([^\\s\\]]+)\\s*=*(\\d*)x*(\\d*)\\)", // Hangar - fix image in link
        Pattern.CASE_INSENSITIVE);

    public ResizableImageInlineParserExtension(final LightInlineParser inlineParser) {
    }

    @Override
    public void finalizeDocument(final @NotNull InlineParser inlineParser) {
    }

    @Override
    public void finalizeBlock(final @NotNull InlineParser inlineParser) {
    }

    @Override
    public boolean parse(final @NotNull LightInlineParser inlineParser) {
        final int index = inlineParser.getIndex();
        // FIX
        final BasedSequence input = inlineParser.getInput();
        if (index + 1 >= input.length()) {
            return false;
        }
        final char c = input.charAt(index + 1);
        // END FIX
        if (c == '[') {
            final BasedSequence[] matches = inlineParser.matchWithGroups(IMAGE_PATTERN);
            if (matches != null) {
                inlineParser.flushTextNode();

                final BasedSequence text = matches[1];
                final BasedSequence source = matches[2];
                final BasedSequence width = matches[3];
                final BasedSequence height = matches[4];

                final ResizableImage image = new ResizableImage(text, source, width, height);
                inlineParser.getBlock().appendChild(image);
                return true;
            }
        }
        return false;
    }

    public static class Factory implements InlineParserExtensionFactory {
        @Override
        public @Nullable Set<Class<?>> getAfterDependents() {
            return null;
        }

        @Override
        public @NotNull CharSequence getCharacters() {
            return "!";
        }

        @Override
        public @Nullable Set<Class<?>> getBeforeDependents() {
            return null;
        }

        @Override
        public @NotNull InlineParserExtension apply(final @NotNull LightInlineParser lightInlineParser) {
            return new ResizableImageInlineParserExtension(lightInlineParser);
        }

        @Override
        public boolean affectsGlobalScope() {
            return false;
        }
    }
}
