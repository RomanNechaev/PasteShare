package ru.nechaev.pasteshare.util;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

public class UniqueUrlGenerator {

    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

    public static String generate() {
        UUID uuid = UUID.randomUUID();
        byte[] bytes = uuidToBytes(uuid);
        return BASE64_URL_ENCODER.encodeToString(bytes);
    }

    private static byte[] uuidToBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }
}
