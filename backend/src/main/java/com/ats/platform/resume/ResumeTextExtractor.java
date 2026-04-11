package com.ats.platform.resume;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Component
public class ResumeTextExtractor {

	private static final int MAX_CHARS = 2_000_000;

	private final AutoDetectParser parser = new AutoDetectParser();

	public String extract(byte[] bytes, String filenameHint) throws IOException, TikaException, SAXException {
		Metadata metadata = new Metadata();
		if (filenameHint != null) {
			metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, filenameHint);
		}
		BodyContentHandler handler = new BodyContentHandler(MAX_CHARS);
		ParseContext ctx = new ParseContext();
		try (InputStream in = new ByteArrayInputStream(bytes)) {
			parser.parse(in, handler, metadata, ctx);
		}
		return handler.toString().trim();
	}
}
