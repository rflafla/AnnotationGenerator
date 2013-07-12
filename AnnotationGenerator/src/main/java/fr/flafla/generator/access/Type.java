package fr.flafla.generator.access;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class Type {
	private final TypeMirror typeMirror;
	private final TypeElement type;
	private final String sourcePackage;
	private final String qualifiedName;
	private final String name;

	public Type(TypeMirror type) {
		this.typeMirror = type;
		this.type = (TypeElement) Environment.get().getTypeUtils().asElement(type);
		qualifiedName = this.type.getQualifiedName().toString();
		final int lastPoint = qualifiedName.lastIndexOf('.');
		sourcePackage = qualifiedName.substring(0, lastPoint);
		name = qualifiedName.substring(lastPoint + 1);
	}

	public Type(Class<?> clazz) {
		this(Environment.get().getElementUtils().getTypeElement(clazz.getName()).asType());
	}

	public Type(Type type) {
		this(type.typeMirror);
	}

	public TypeElement getElement() {
		return type;
	}

	public String getQualifiedName() {
		return qualifiedName;
	}

	public String getCompleteName() {
		try {
			final DeclaredType simplify = (DeclaredType) TypeSimplifier.simplify(typeMirror, false, null);
			final List<? extends TypeMirror> args = simplify.getTypeArguments();
			if (args.size() > 0) {
				final StringBuilder sb = new StringBuilder();
				sb.append(qualifiedName).append("<");
				for (TypeMirror arg : args) {
					final Element argElt = Environment.asElement(arg);
					sb.append(argElt);
				}
				sb.append(">");
				return sb.toString();
			}
			return qualifiedName;
		} catch (RuntimeException e) {
			e.printStackTrace();
			throw e;
		}
	}

	public String getName() {
		return name;
	}

	public String getPackage() {
		return sourcePackage;
	}

	public Iterable<? extends Method> getMethods() {
		final HashSet<String> names = new HashSet<String>();
		final List<Method> result = new ArrayList<Method>();
		TypeElement superClass = type;
		while (superClass != null && !superClass.getQualifiedName().toString().equals("java.lang.Object")) {
			final List<? extends Element> typeElements = superClass.getEnclosedElements();
			for (Element elt : typeElements) {
				if (elt instanceof ExecutableElement) {
					if (names.add(elt.getSimpleName().toString()))
						result.add(new Method((ExecutableElement) elt));
				}
			}
			superClass = (TypeElement) Environment.get().getTypeUtils().asElement(superClass.getSuperclass());
		}

		return result;
	}

	public Iterable<? extends Method> getGetter() {
		return Iterables.filter(getMethods(), new Predicate<Method>() {
			@Override
			public boolean apply(Method input) {
				return input.getName().startsWith("get");
			}
		});
	}
	
	public boolean has(Class<? extends Annotation> annotation) {
		return type.getAnnotation(annotation) != null;
	}

	public <A extends Annotation> fr.flafla.generator.access.Annotation<A> get(Class<A> annotation) {
		final List<? extends AnnotationMirror> annotationMirrors = type.getAnnotationMirrors();
		for (AnnotationMirror mirror : annotationMirrors) {
			if (mirror.getAnnotationType().asElement().toString().equals(annotation.getName())) {
				return new fr.flafla.generator.access.Annotation<A>(mirror);
			}
		}
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Type)
			return typeMirror.equals(((Type) obj).typeMirror);
		return false;
	}
}