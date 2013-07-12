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

public class FileGenerator {
	private final Engine engine;
	private final String extension;
	private final String template;

	public FileGenerator(Class<?> clazz, String templateFile, String extension) {
		this.extension = extension;
		String template = getTemplate(clazz, templateFile);
		engine = Engine.createCompilingEngine();
		engine.getTemplate(template);
		this.template = template;
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
		} catch (IOException ex) {
			throw new RuntimeException("The template class.template is not readable");
		}
	}
	
	public void generate(Map<String, Object> model, Type type) throws IOException {
		Filer filer = Environment.get().getFiler();
		Environment.getMessager().printMessage(Kind.NOTE, "create template implementation for " + type.getQualifiedName());
		FileObject o = filer.createSourceFile(type.getPackage() + "." + type.getName() + extension, type.getElement());
		Writer w = o.openWriter();

		try {
			final String content = engine.transform(template, model);
			w.append(content);
		} catch (RuntimeException e) {
			e.printStackTrace(new PrintWriter(w));
			throw e;
		} finally {
			w.flush();
			w.close();
		}

		Environment.getMessager().printMessage(Kind.NOTE, "File finished");
	}
}