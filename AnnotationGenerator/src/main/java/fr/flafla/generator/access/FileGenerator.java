package fr.flafla.generator.access;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import javax.annotation.processing.Filer;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import fr.flafla.generator.GeneratedAnnotation;

public class FileGenerator {
	private final VelocityEngine engine;
	private final String extension;
	private final String template;

	public FileGenerator(Class<?> clazz, String templateFile, String extension) {
		this.extension = extension;
		final String template = getTemplate(clazz, templateFile);
		engine = new VelocityEngine();
		final Properties properties = new Properties();
		properties.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.SystemLogChute");
		properties.setProperty("resource.manager.class", "org.apache.velocity.runtime.resource.ResourceManagerImpl");
		try {
			final Thread thread = Thread.currentThread();
//			ClassLoader loader = thread.getContextClassLoader();
			thread.setContextClassLoader(this.getClass().getClassLoader());
			engine.init(properties);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
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
	 * 
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
				sb.append((char) b);
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
		final Writer writer = o.openWriter();

		try {
			final VelocityContext context = new VelocityContext();
			context.put("typeName", typeName);
			for (final Map.Entry<String, Object> e : model.entrySet())
				context.put(e.getKey(), e.getValue());
			engine.evaluate(context, writer, "<tmp>", template);
		} catch (final RuntimeException e) {
			e.printStackTrace(new PrintWriter(writer));
			throw e;
		} finally {
			writer.flush();
			writer.close();
		}

		Environment.getMessager().printMessage(Kind.NOTE, "File finished");
	}
}
