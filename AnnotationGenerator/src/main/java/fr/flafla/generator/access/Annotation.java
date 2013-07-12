package fr.flafla.generator.access;

import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

public class Annotation<A> {
	private final AnnotationMirror mirror;

	public Annotation(AnnotationMirror mirror) {
		this.mirror = mirror;
	}
	
	public String get(String param) {
		// Get the value for valueName
		for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> v : mirror.getElementValues().entrySet()) {
			if (v.getKey().getSimpleName().toString().equals(param)) {
				Environment.getMessager().printMessage(Kind.NOTE, "Param "+param+" of type "+v.getValue().getValue().getClass());
				return (String) v.getValue().getValue();
			}
		}
		return null;
	}
	
	public Boolean getBoolean(String param) {
		// Get the value for valueName
		for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> v : mirror.getElementValues().entrySet()) {
			if (v.getKey().getSimpleName().toString().equals(param)) {
				Environment.getMessager().printMessage(Kind.NOTE, "Param "+param+" of type "+v.getValue().getValue().getClass());
				return (Boolean) v.getValue().getValue();
			}
		}
		return null;
	}
	
	public String[] getStrings(String param) {
		// Get the value for valueName
		for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> v : mirror.getElementValues().entrySet()) {
			if (v.getKey().getSimpleName().toString().equals(param)) {
				Environment.getMessager().printMessage(Kind.NOTE, "Param "+param+" of type "+v.getValue().getValue().getClass());
				@SuppressWarnings("unchecked")
				final List<AnnotationValue> values = (List<AnnotationValue>) v.getValue().getValue();
				final String[] result = new String[values.size()];
				for (int i = 0; i < values.size(); i++) {
					AnnotationValue value = values.get(i);
					result[i] = (String) value.getValue();
				}
				return result;
			}
		}
		return null;
	}
	
	public Type getType(String param) {
		// Get the value for valueName
		for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> v : mirror.getElementValues().entrySet()) {
			if (v.getKey().getSimpleName().toString().equals(param)) {
				Environment.getMessager().printMessage(Kind.NOTE, "Param "+param+" of type "+v.getValue().getValue().getClass());
				return new Type((TypeMirror) v.getValue().getValue());
			}
		}
		return null;
	}
	
	public Type[] getTypes(String param) {
		// Get the value for valueName
		for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> v : mirror.getElementValues().entrySet()) {
			if (v.getKey().getSimpleName().toString().equals(param)) {
				Environment.getMessager().printMessage(Kind.NOTE, "Param "+param+" of type "+v.getValue().getValue().getClass());
				final TypeMirror[] types = (TypeMirror[]) v.getValue().getValue();
				final Type[] result = new Type[types.length];
				for (int i = 0; i < types.length; i++) {
					final TypeMirror type = types[i];
					result[i] = new Type(type);
				}
				return result;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <H> Annotation<H>[] getAnnotations(String param) {
		// Get the value for valueName
		for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> v : mirror.getElementValues().entrySet()) {
			if (v.getKey().getSimpleName().toString().equals(param)) {
				Environment.getMessager().printMessage(Kind.NOTE, "Param "+param+" of type "+v.getValue().getValue().getClass());
				final List<AnnotationMirror> types = (List<AnnotationMirror>) v.getValue().getValue();
				final Annotation<H>[] result = new Annotation[types.size()];
				for (int i = 0; i < types.size(); i++) {
					final AnnotationMirror type = types.get(i);
					result[i] = new Annotation<H>(type);
				}
				return result;
			}
		}
		return null;
	}
}