package fr.flafla.generator.access;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public class Method {
	protected final ExecutableElement elt;
	protected final ExecutableType type;

	public Method(ExecutableElement elt) {
		this.type = (ExecutableType) elt.asType();
		this.elt = elt;
	}

	public Method(Method method) {
		this.type = method.type;
		this.elt = method.elt;
	}

	public String getName() {
		return elt.getSimpleName().toString();
	}

	public String getDocument() {
		try {
			return Environment.get().getElementUtils().getDocComment(elt);
		} catch (RuntimeException e) {
			e.printStackTrace();
			throw e;
		}
	}

	public Iterable<? extends Parameter> getParameters() {
		final List<? extends TypeMirror> parameters = type.getParameterTypes();
		final List<? extends VariableElement> eltParameters = elt.getParameters();
		final List<Parameter> result = new ArrayList<Parameter>();
		for (int i = 0; i < eltParameters.size(); i++) {
			VariableElement elt = eltParameters.get(i);
			final TypeMirror mirror = parameters.get(i);
			result.add(new Parameter((DeclaredType) mirror, elt.getSimpleName().toString()));
		}
		return result;
	}

	public String getBoxedReturnType() {
		final TypeMirror type = elt.getReturnType();
		if (type.getKind() == TypeKind.VOID)
			return "Void";

		return new Type(type).getCompleteName();
	}

	public String getReturnType() {
		final TypeMirror type = elt.getReturnType();
		switch (type.getKind()) {
		case VOID:
			return "void";
		case INT:
			return "int";
		case DOUBLE:
			return "double";
		case BYTE:
			return "byte";
		case BOOLEAN:
			return "boolean";
		case FLOAT:
			return "float";
		case LONG:
			return "long";
		case SHORT:
			return "short";
		case CHAR:
			return "char";
		default:
			return new Type(type).getCompleteName();
		}

	}

	public String getField() {
		final String name = getName();
		if (name.startsWith("get") || name.startsWith("set")) {
			return Character.toLowerCase(name.charAt(3)) + name.substring(4);
		}
		return name;
	}

	public String getConst() {
		try {
			final String field = getField();
			return field.replaceAll("([A-Z])", "_$1").toUpperCase();
		} catch (RuntimeException e) {
			e.printStackTrace();
			throw e;
		}
	}

	public boolean has(Class<? extends Annotation> annotation) {
		return elt.getAnnotation(annotation) != null;
	}

	public <A extends Annotation> fr.flafla.generator.access.Annotation<A> get(Class<A> annotation) {
		final List<? extends AnnotationMirror> annotationMirrors = elt.getAnnotationMirrors();
		for (AnnotationMirror mirror : annotationMirrors) {
			if (mirror.getAnnotationType().asElement().toString().equals(annotation.getName())) {
				return new fr.flafla.generator.access.Annotation<A>(mirror);
			}
		}
		return null;
	}
}