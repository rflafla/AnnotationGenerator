package fr.flafla.generator;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import fr.flafla.generator.access.Environment;

/**
 * This is an abstract generator to help source code generation
 * 
 * @author rflanet
 *
 */
public abstract class Generator extends AbstractProcessor {
	
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		Environment.set(processingEnv, processingEnv.getMessager());
	}
	
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		for (TypeElement annotation : annotations) {
			final Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotation);
			process(elements);
		}
		
		return false;
	}
	
	protected abstract void process(Set<? extends Element> elements);

	/**
	 * Get the content of a template
	 * @param file The file name
	 * @return The content
	 */
	protected String getTemplate(String file) {
		final URL resource = getClass().getResource(file);
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
	
	/**
	 * Generate the code with a template and a map of vars.
	 * The template has to mark variables with #{varname}.
	 * @param template The template
	 * @param datas Map of variables
	 * @return The code generated
	 */
	protected String generate(String template, Map<String, String> datas) {
		String result = template;
		for (Map.Entry<String, String> e : datas.entrySet()) {
			try {
				result = result.replace("#{"+e.getKey()+"}", e.getValue());
			} catch (IllegalArgumentException ex) {
				throw new RuntimeException("Error for key "+e.getKey(), ex);
			}
		}
		return result;
	}
	

	/**
	 * Get the type of an element
	 * @param processingEnv Environment provided by java processor mechanism
	 * @param element The element
	 * @return The type
	 */
	protected String getType(ProcessingEnvironment processingEnv, Element element) {
		Element typeFieldElement = processingEnv.getTypeUtils().asElement(element.asType());
		
		String typeField;
		if (typeFieldElement == null) 
			typeField = element.asType().toString();
		else
			typeField = typeFieldElement.toString();
		return typeField;
	}
	
	/**
	 * Construct the getter name
	 * @param fieldName The field
	 * @return the getter name
	 */
	protected String getGetter(String fieldName) {
		return "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
	}
	
	@SuppressWarnings("unchecked")
	protected <H> H getAnnotationValue(Element element, Class<?> annotationClass, String valueName) {
		// Get all annotations for this element
		final List<? extends AnnotationMirror> annotations = element.getAnnotationMirrors();
		for (AnnotationMirror annotation : annotations) {
			// Check the type of the annotation
			if (annotation.getAnnotationType().asElement().toString().equals(annotationClass.getName())) {
				// Get the value for valueName
				for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> v : annotation.getElementValues().entrySet()) {
					if (v.getKey().getSimpleName().toString().equals(valueName)) {
						return (H) v.getValue().getValue();
					}
				}
			}
		}
		
		return null;
	}
	
	protected List<String> getAnnotationValueStringList(Element element, Class<?> annotationClass, String valueName) {
		final Object value = getAnnotationValue(element, annotationClass, valueName);
		List<String> result = null;
		if (value instanceof List<?>) {
			result = new ArrayList<String>();
			for (Object string : (List<?>)value) {
				final String s = string.toString();
				// Remove " in first and last position
				result.add(s.substring(1, s.length()-1));
			}
		} else if (value instanceof String) {
			result = new ArrayList<String>();
			result.add((String) value);
		}
		return result;
	}
		
	protected String join(List<String> strings, String split) {
		StringBuilder result = new StringBuilder();
		for (String string : strings) {
			if (result.length() > 0)
				result.append(split);
			result.append(string);
		}
		return result.toString();
	}
}