package com.daimler.dna.notifications.common.event.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.daimler.dna.notifications.dto.NotificationVO;

import io.lettuce.core.codec.RedisCodec;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NotificationRedisCodec implements RedisCodec<String, NotificationVO>{

	private final Charset charset = StandardCharsets.UTF_8;
	
	@Override
	public String decodeKey(ByteBuffer bytes) {
		return charset.decode(bytes).toString();
	}

	@Override
	public NotificationVO decodeValue(ByteBuffer bytes) {
		try {
			byte[] array = new byte[bytes.remaining()];
			bytes.get(array);
			ObjectInputStream inStream = new ObjectInputStream(new ByteArrayInputStream(array));
			return (NotificationVO) inStream.readObject();
		}catch(Exception e) {
			log.error("Failed while decoding bytes to NotificationVO object");
			return null;
		}
	}

	@Override
	public ByteBuffer encodeKey(String key) {
		return charset.encode(key);
	}

	@Override
	public ByteBuffer encodeValue(NotificationVO value) {
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			ObjectOutputStream outStream = new ObjectOutputStream(bytes);
			outStream.writeObject(value);
			return ByteBuffer.wrap(bytes.toByteArray());
		}catch(Exception e) {
			log.error("Failed while encoding NotificationVO object to bytebuffer");
			return null;
		}
	}

}
