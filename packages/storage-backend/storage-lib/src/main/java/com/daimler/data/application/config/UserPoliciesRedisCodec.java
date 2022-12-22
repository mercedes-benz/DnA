package com.daimler.data.application.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import io.lettuce.core.codec.RedisCodec;
import io.minio.admin.UserInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserPoliciesRedisCodec implements RedisCodec<String, Map<String, UserInfo>>{

	private final Charset charset = StandardCharsets.UTF_8;
	
	@Override
	public String decodeKey(ByteBuffer bytes) {
		return charset.decode(bytes).toString();
	}

	@Override
	public Map<String, UserInfo> decodeValue(ByteBuffer bytes) {
		try {
			byte[] array = new byte[bytes.remaining()];
			bytes.get(array);
			ObjectInputStream inStream = new ObjectInputStream(new ByteArrayInputStream(array));
			return (Map<String, UserInfo>) inStream.readObject();
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
	public ByteBuffer encodeValue(Map<String, UserInfo> value) {
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
