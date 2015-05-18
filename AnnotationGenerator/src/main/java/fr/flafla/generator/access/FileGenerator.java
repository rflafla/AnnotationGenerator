package fr.flafla.generator.access;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;

import com.floreysoft.jmte.Engine;
import com.google.common.collect.Maps;

import fr.flafla.generator.GeneratedAnnotation;

public class FileGenerator {
	private final Engine engine;
	private final String extension;
	private final String template;

	public FileGenerator(Class<?> clazz, String templateFile, String extension) {
		this.extension = extension;
		final String template = getTemplate(clazz, templateFile);
		engine = Engine.createCompilingEngine();
		engine.getTemplate(template);
		this.template = template;
	}
	
	public static FileGenerator create(Class<?> clazz, String templateFile, Class<? extends java.lang.annotation.Annotation> annotation) {
		return create(clazz, templateFile, annotation.getAnnotation(GeneratedAnnotation.class).value());
	}
	
	public static FileGenerator create(Class<?> clazz, String templateFile, String extension) {
		return new FileGenerator(clazz, templateFile, extension);
	}
	
	/**
	 * Get the content of a template
	 * @param file The file name
	 * @return The content
	 */
	protected String getTemplate(Class<?> clazz, String file) {
		final URL resource = clazz.getResource(file);
		try {
			final StringBuilder sb = new StringBuilder();
			final InputStream os = resource.openStream();
			int b;
			while ((b = os.read()) >= 0) {
				sb.append((char)b);
			}
			return sb.toString();
		} catch (final IOException ex) {
			throw new RuntimeException("The template class.template is not readable");
		}
	}
	
	public void generate(Map<String, Object> model, Type type) throws IOException {
		final Filer filer = Environment.get().getFiler();
		Environment.getMessager().printMessage(Kind.NOTE, "create template implementation for " + type.getQualifiedName());
		final String typeName = type.getName() + extension;
		final FileObject o = filer.createSourceFile(type.getPackage() + "." + typeName, type.getElement());
		final Writer w = o.openWriter();

		try {
			final Map<String, Object> datas = Maps.newHashMap(model);
			datas.put("typeName", typeName);
			final String content = engine.transform(template, datas);
			w.append(content);
		} catch (final RuntimeException e) {
			e.printStackTrace(new PrintWriter(w));
			throw e;
		} finally {
			w.flush();
			w.close();
		}

		Environment.getMessager().printMessage(Kind.NOTE, "File finished");
	}
}
