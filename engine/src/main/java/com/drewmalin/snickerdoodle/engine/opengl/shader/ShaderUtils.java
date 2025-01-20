package com.drewmalin.snickerdoodle.engine.opengl.shader;

import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import static org.lwjgl.BufferUtils.createByteBuffer;
import static org.lwjgl.system.MemoryUtil.memSlice;

public class ShaderUtils {

    public static ByteBuffer resourceToByteBuffer(final String resource, final int bufferCapacity)
        throws IOException {

        ByteBuffer buffer;

        try (
            final InputStream source = ShaderUtils.class.getResourceAsStream(resource);
            final ReadableByteChannel rbc = Channels.newChannel(source)
        ) {
            buffer = createByteBuffer(bufferCapacity);

            while (true) {
                int bytes = rbc.read(buffer);
                if (bytes == -1) {
                    break;
                }
                if (buffer.remaining() == 0) {
                    buffer = resizeBuffer(buffer, buffer.capacity() * 3 / 2); // 50%
                }
            }
        }

        buffer.flip();
        return memSlice(buffer);
    }

    private static ByteBuffer resizeBuffer(final ByteBuffer buffer, final int newCapacity) {
        final var newBuffer = BufferUtils.createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }
}
